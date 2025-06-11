package com.taxiapp.driver_auth.service

import aws.sdk.kotlin.services.cognitoidentityprovider.CognitoIdentityProviderClient
import aws.sdk.kotlin.services.cognitoidentityprovider.model.AdminAddUserToGroupRequest
import aws.sdk.kotlin.services.sns.SnsClient
import aws.sdk.kotlin.services.sns.model.PublishRequest
import com.taxiapp.driver_auth.dto.AddressTO
import com.taxiapp.driver_auth.dto.event.DriverAuthenticationApprovedEvent
import com.taxiapp.driver_auth.dto.event.DriverAuthenticationFailedEvent
import com.taxiapp.driver_auth.dto.event.DriverCreatedEvent
import com.taxiapp.driver_auth.dto.event.DriverFormSubmittedEvent
import com.taxiapp.driver_auth.dto.http.EmailSendRequestTO
import com.taxiapp.driver_auth.dto.request.DriverAuthenticationRequestTO
import com.taxiapp.driver_auth.dto.response.*
import com.taxiapp.driver_auth.entity.DriverAuthenticationInfo
import com.taxiapp.driver_auth.entity.DriverAuthenticationLog
import com.taxiapp.driver_auth.entity.DriverPersonalInfo
import com.taxiapp.driver_auth.entity.enums.AuthenticationEvent
import com.taxiapp.driver_auth.entity.enums.VerificationStatus
import com.taxiapp.driver_auth.repository.DriverAuthenticationInfoRepository
import com.taxiapp.driver_auth.repository.DriverAuthenticationLogRepository
import com.taxiapp.driver_auth.repository.DriverPersonalInfoRepository
import jakarta.transaction.Transactional
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.util.UriComponentsBuilder
import java.time.Instant
import java.util.*

@Service
open class DriverAuthenticationServiceImpl(
    private val driverPersonalInfoRepository: DriverPersonalInfoRepository,
    private val driverAuthenticationInfoRepository: DriverAuthenticationInfoRepository,
    private val driverAuthenticationLogRepository: DriverAuthenticationLogRepository,
    private val storageService: FileStorageService,
    private val exchange: TopicExchange,
    private val template: RabbitTemplate,
    private val restTemplate: RestTemplate,
    private val cognitoClient: CognitoIdentityProviderClient,
    private val snsClient: SnsClient
) : DriverAuthenticationService {
    private val logger = LoggerFactory.getLogger(DriverAuthenticationServiceImpl::class.java)

    @Value("\${rabbit.topic.driver-auth.submitted}")
    private val driverAuthSubmittedTopic: String? = null

    @Value("\${rabbit.topic.driver-auth.failed}")
    private val driverAuthFailedTopic: String? = null

    @Value("\${rabbit.topic.driver-auth.approved}")
    private val driverAuthApprovedTopic: String? = null

    @Value("\${aws.cognito.user-pool-id}")
    private val cognitoUserPoolId: String? = null

    @Value("\${aws.cognito.group-name}")
    private val cognitoGroupName: String? = null

    @Value("\${service.address.notification}")
    private val notificationServiceAddress: String? = null

    @Value("\${aws.sns.employeetopic.arn}")
    private val employeeNotificationArn: String? = null

    override fun createDriverInfo(userEvent: DriverCreatedEvent) {
        if (driverPersonalInfoRepository.existsByUsername(userEvent.username))
            return

        val personalInfo = DriverPersonalInfo(
            username = userEvent.username,
            name = userEvent.firstname,
            surname = userEvent.lastname,
            email = userEvent.email,
            verificationStatus = VerificationStatus.WAITING_FOR_SUBMIT
        )

        driverPersonalInfoRepository.save(personalInfo)
    }

    override fun getAuthenticationStatus(username: String): ResultInterface {
        val driverPersonalInfo = driverPersonalInfoRepository.findByUsername(username)
        if (driverPersonalInfo != null) {
            return AuthenticationStatusTO(
                status = driverPersonalInfo.verificationStatus.name
            )
        }
        return ResultTO(
            httpStatus = HttpStatus.NOT_FOUND
        )
    }

    @Transactional
    override fun submitDriverAuthentication(
        username: String,
        authenticationRequestTO: DriverAuthenticationRequestTO,
        licenseFrontPhoto: MultipartFile,
        licenseBackPhoto: MultipartFile
    ): ResultInterface {
        val driverPersonalInfo = driverPersonalInfoRepository.findByUsername(username)
            ?: return ResultTO(httpStatus = HttpStatus.NOT_FOUND)

        if (driverPersonalInfo.verificationStatus == VerificationStatus.PENDING_AUTO_VERIFICATION ||
            driverPersonalInfo.verificationStatus == VerificationStatus.PENDING_MANUAL_VERIFICATION ||
            driverPersonalInfo.verificationStatus == VerificationStatus.APPROVED
        ) {
            return ResultTO(
                httpStatus = HttpStatus.BAD_REQUEST,
                messages = listOf("Driver authentication is already in progress")
            )
        }

        val frontLicencePhotoPath = storageService.storeFile(licenseFrontPhoto)
        val backLicencePhotoPath = storageService.storeFile(licenseBackPhoto)

        val authenticationInfo = DriverAuthenticationInfo(
            submittedAt = Date.from(Instant.now()),
            driverLicenceNumber = authenticationRequestTO.driverLicenceNumber,
            registrationDocumentNumber = authenticationRequestTO.registrationDocumentNumber,
            plateNumber = authenticationRequestTO.plateNumber,
            driverLicenseFrontPhotoPath = frontLicencePhotoPath,
            driverLicenseBackPhotoPath = backLicencePhotoPath,
            pesel = authenticationRequestTO.pesel,
            street = authenticationRequestTO.address.street,
            buildingNumber = authenticationRequestTO.address.buildingNumber,
            apartmentNumber = authenticationRequestTO.address.apartmentNumber,
            postCode = authenticationRequestTO.address.postCode,
            city = authenticationRequestTO.address.city,
            country = authenticationRequestTO.address.country
        )

        driverAuthenticationInfoRepository.save(authenticationInfo)


        val authenticationLog = DriverAuthenticationLog(
            driverPersonalInfo = driverPersonalInfo,
            timestamp = Date.from(Instant.now()),
            eventType = AuthenticationEvent.DATA_SUBMITTED,
            statusBefore = driverPersonalInfo.verificationStatus,
            statusAfter = VerificationStatus.PENDING_AUTO_VERIFICATION
        )


        driverPersonalInfo.driverAuthenticationInfo = authenticationInfo
        driverPersonalInfo.verificationStatus = VerificationStatus.PENDING_AUTO_VERIFICATION

        driverPersonalInfoRepository.save(driverPersonalInfo)


        driverAuthenticationLogRepository.save(authenticationLog)


        val event = DriverFormSubmittedEvent(
            userFormSubmittedEventId = authenticationInfo.id,
            username = driverPersonalInfo.username
        )

        template.convertAndSend(exchange.name, "$driverAuthSubmittedTopic", event)


        return ResultTO(
            httpStatus = HttpStatus.CREATED,
            messages = listOf("Driver authentication submitted successfully")
        )
    }

    @Transactional
    override fun performAutoVerification(username: String) {
        logger.info("Performing auto-verification for driver: $username")
        val driverPersonalInfo = driverPersonalInfoRepository.findByUsername(username)
            ?: return

        if (driverPersonalInfo.verificationStatus != VerificationStatus.PENDING_AUTO_VERIFICATION)
            return

        // Using mocked verification - 90% success rate
        val success = (0..9).random() < 9

        val authenticationLog = DriverAuthenticationLog(
            driverPersonalInfo = driverPersonalInfo,
            timestamp = Date.from(Instant.now()),
            eventType = if (success) AuthenticationEvent.AUTO_VERIFICATION_PASSED else AuthenticationEvent.AUTO_VERIFICATION_REJECTED,
            statusBefore = driverPersonalInfo.verificationStatus,
            statusAfter = if (success) VerificationStatus.PENDING_MANUAL_VERIFICATION else VerificationStatus.REJECTED
        )

        driverPersonalInfo.verificationStatus = if (success) VerificationStatus.PENDING_MANUAL_VERIFICATION else VerificationStatus.REJECTED

        driverPersonalInfoRepository.save(driverPersonalInfo)

        driverAuthenticationLogRepository.save(authenticationLog)

        if (!success) {
            template.convertAndSend(
                exchange.name,
                "$driverAuthFailedTopic",
                DriverAuthenticationFailedEvent(
                    userAuthenticationFailedEventId = driverPersonalInfo.id!!,
                    username = driverPersonalInfo.username,
                    autoVerification = true
                )
            )
            sendEmail(
                recipient = driverPersonalInfo.email,
                subject = "Driver Authentication Failed",
                body = "Dear ${driverPersonalInfo.name},\n\n" +
                        "Your driver authentication request has failed during automatic verification. " +
                        "Please resubmit your application.\n\n" +
                        "Best regards,\nTaxiApp Team"
            )
        } else {
            val publishRequest = PublishRequest {
                topicArn = employeeNotificationArn
                message = "Driver $username has been auto-verified and is pending manual verification."
            }
            runBlocking {
                launch { snsClient.publish(publishRequest) }
            }
        }

    }

    override fun getFirstPendingVerification(): ResultInterface {
        val driverPersonalInfo = driverPersonalInfoRepository.findFirstByVerificationStatus(
            VerificationStatus.PENDING_MANUAL_VERIFICATION
        ) ?: return ResultTO(httpStatus = HttpStatus.NOT_FOUND)

        if (driverPersonalInfo.driverAuthenticationInfo == null) {
            return ResultTO(
                httpStatus = HttpStatus.BAD_REQUEST,
                messages = listOf("Driver authentication info not found")
            )
        }

        return getPendingVerificationDetails(driverPersonalInfo)
    }

    @Transactional
    override fun cancelDriverAuthentication(
        username: String,
    ): ResultInterface {
        val driverPersonalInfo = driverPersonalInfoRepository.findByUsername(username)
            ?: return ResultTO(httpStatus = HttpStatus.NOT_FOUND)

        if (driverPersonalInfo.verificationStatus != VerificationStatus.PENDING_AUTO_VERIFICATION &&
            driverPersonalInfo.verificationStatus != VerificationStatus.PENDING_MANUAL_VERIFICATION
        ) {
            return ResultTO(
                httpStatus = HttpStatus.BAD_REQUEST,
                messages = listOf("Driver authentication is not in progress")
            )
        }

        val authenticationLog = DriverAuthenticationLog(
            driverPersonalInfo = driverPersonalInfo,
            timestamp = Date.from(Instant.now()),
            eventType = AuthenticationEvent.VERIFICATION_CANCELLED,
            statusBefore = driverPersonalInfo.verificationStatus,
            statusAfter = VerificationStatus.CANCELLED
        )

        driverAuthenticationInfoRepository.delete(driverPersonalInfo.driverAuthenticationInfo!!)


        driverPersonalInfo.verificationStatus = VerificationStatus.CANCELLED
        driverPersonalInfo.driverAuthenticationInfo = null
        driverPersonalInfoRepository.save(driverPersonalInfo)

        driverAuthenticationLogRepository.save(authenticationLog)


        return ResultTO(
            httpStatus = HttpStatus.NO_CONTENT,
            messages = listOf("Driver authentication cancelled successfully")
        )
    }

    override fun getPendingVerifications(): ResultInterface {
        val pendingVerifications = driverPersonalInfoRepository.findAllByVerificationStatus(
            VerificationStatus.PENDING_MANUAL_VERIFICATION
        )

        val pendingVerificationsList = pendingVerifications.map {
            PendingVerificationMinimalTO(
                id = it.id!!,
                name = it.name,
                surname = it.surname
            )
        }

        return PendingVerificationsListTO(
            verifications = pendingVerificationsList
        )
    }

    override fun getPendingVerificationById(id: Long): ResultInterface {
        val driverPersonalInfo = driverPersonalInfoRepository.findById(id)
            .orElse(null) ?: return ResultTO(httpStatus = HttpStatus.NOT_FOUND)

        if (driverPersonalInfo.verificationStatus != VerificationStatus.PENDING_MANUAL_VERIFICATION) {
            return ResultTO(
                httpStatus = HttpStatus.BAD_REQUEST,
                messages = listOf("Driver authentication not subject to manual verification")
            )
        }
        return getPendingVerificationDetails(driverPersonalInfo)
    }

    private fun getPendingVerificationDetails(
        driverPersonalInfo: DriverPersonalInfo
    ): PendingVerificationTO {
        val authenticationInfo = driverPersonalInfo.driverAuthenticationInfo
            ?: throw IllegalArgumentException("Driver authentication info not found")

        return PendingVerificationTO(
            id = driverPersonalInfo.id!!,
            name = driverPersonalInfo.name,
            surname = driverPersonalInfo.surname,
            driverLicenceNumber = authenticationInfo.driverLicenceNumber,
            registrationDocumentNumber = authenticationInfo.registrationDocumentNumber,
            plateNumber = authenticationInfo.plateNumber,
            driverLicenseFrontPhotoUrl = storageService.getFileUrl(authenticationInfo.driverLicenseFrontPhotoPath),
            driverLicenseBackPhotoUrl = storageService.getFileUrl(authenticationInfo.driverLicenseBackPhotoPath)
        )
    }

    @Transactional
    override fun approveVerification(id: Long): ResultInterface {
        val driverPersonalInfo = driverPersonalInfoRepository.findById(id)
            .orElse(null) ?: return ResultTO(httpStatus = HttpStatus.NOT_FOUND)

        if (driverPersonalInfo.verificationStatus != VerificationStatus.PENDING_MANUAL_VERIFICATION) {
            return ResultTO(
                httpStatus = HttpStatus.BAD_REQUEST,
                messages = listOf("Driver authentication not subject to manual verification")
            )
        }

        val authenticationInfo = driverPersonalInfo.driverAuthenticationInfo
            ?: return ResultTO(
                httpStatus = HttpStatus.BAD_REQUEST,
                messages = listOf("Driver authentication info not found")
            )

        val authenticationLog = DriverAuthenticationLog(
            driverPersonalInfo = driverPersonalInfo,
            timestamp = Date.from(Instant.now()),
            eventType = AuthenticationEvent.MANUAL_VERIFICATION_PASSED,
            statusBefore = driverPersonalInfo.verificationStatus,
            statusAfter = VerificationStatus.APPROVED
        )

        val addUserToGroupRequest = AdminAddUserToGroupRequest {
            groupName = cognitoGroupName
            username = driverPersonalInfo.username
            userPoolId = cognitoUserPoolId
        }

        runBlocking {
            launch { cognitoClient.adminAddUserToGroup(addUserToGroupRequest) }
        }

        driverPersonalInfo.verificationStatus = VerificationStatus.APPROVED
        driverPersonalInfoRepository.save(driverPersonalInfo)

        driverAuthenticationLogRepository.save(authenticationLog)


        val event = DriverAuthenticationApprovedEvent(
            driverAuthenticationApprovedId = authenticationLog.id,
            username = driverPersonalInfo.username,
            firstName = driverPersonalInfo.name,
            lastName = driverPersonalInfo.surname,
            driverLicenceNumber = authenticationInfo.driverLicenceNumber,
            registrationDocumentNumber = authenticationInfo.registrationDocumentNumber,
            plateNumber = authenticationInfo.plateNumber,
            pesel = authenticationInfo.pesel,
            address = AddressTO(
                street = authenticationInfo.street,
                buildingNumber = authenticationInfo.buildingNumber,
                apartmentNumber = authenticationInfo.apartmentNumber,
                postCode = authenticationInfo.postCode,
                city = authenticationInfo.city,
                country = authenticationInfo.country
            )
        )

        template.convertAndSend(
            exchange.name,
            "$driverAuthApprovedTopic",
            event
        )

        sendEmail(
            recipient = driverPersonalInfo.email,
            subject = "Driver Authentication Approved",
            body = "Dear ${driverPersonalInfo.name},\n\n" +
                    "Your driver authentication request has been approved. You can now start accepting rides.\n\n" +
                    "Best regards,\nTaxiApp Team"
        )


        return ResultTO(
            httpStatus = HttpStatus.NO_CONTENT,
            messages = listOf("Driver authentication approved successfully")
        )
    }

    @Transactional
    override fun rejectVerification(id: Long): ResultInterface {
        val driverPersonalInfo = driverPersonalInfoRepository.findById(id)
            .orElse(null) ?: return ResultTO(httpStatus = HttpStatus.NOT_FOUND)

        if (driverPersonalInfo.verificationStatus != VerificationStatus.PENDING_MANUAL_VERIFICATION) {
            return ResultTO(
                httpStatus = HttpStatus.BAD_REQUEST,
                messages = listOf("Driver authentication not subject to manual verification")
            )
        }

        val authenticationLog = DriverAuthenticationLog(
            driverPersonalInfo = driverPersonalInfo,
            timestamp = Date.from(Instant.now()),
            eventType = AuthenticationEvent.MANUAL_VERIFICATION_REJECTED,
            statusBefore = driverPersonalInfo.verificationStatus,
            statusAfter = VerificationStatus.REJECTED
        )

        driverPersonalInfo.verificationStatus = VerificationStatus.REJECTED

        driverPersonalInfoRepository.save(driverPersonalInfo)

        driverAuthenticationLogRepository.save(authenticationLog)

        sendEmail(
            recipient = driverPersonalInfo.email,
            subject = "Driver Authentication Rejected",
            body = "Dear ${driverPersonalInfo.name},\n\n" +
                    "Your driver authentication request has been rejected. You can resubmit it using form.\n\n" +
                    "Best regards,\nTaxiApp Team"
        )

        return ResultTO(
            httpStatus = HttpStatus.NO_CONTENT,
            messages = listOf("Driver authentication rejected successfully")
        )
    }

    private fun sendEmail(recipient: String, subject: String, body: String) {
        val notificationUri = UriComponentsBuilder
            .fromUriString("$notificationServiceAddress/api/notification/email")
            .build()
            .toUri()

        val emailRequest = EmailSendRequestTO(
            recipient = recipient,
            subject = subject,
            body = body
        )

        val response = restTemplate.postForEntity(notificationUri, emailRequest, Object::class.java)
        check(!response.statusCode.is2xxSuccessful) {
            throw IllegalStateException("Failed to send email: ${response.body}")
        }
    }
}