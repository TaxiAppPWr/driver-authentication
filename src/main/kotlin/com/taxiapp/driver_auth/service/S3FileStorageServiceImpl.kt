package com.taxiapp.driver_auth.service

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.sdk.kotlin.services.s3.presigners.presignGetObject
import aws.smithy.kotlin.runtime.content.asByteStream
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.time.Duration.Companion.hours

@Profile("!local")
@Service
class S3FileStorageServiceImpl : FileStorageService {
    @Value("\${aws.bucket.name}")
    private val bucketName: String? = null
    @Value("\${aws.region}")
    private val awsRegion: String? = null


    override fun storeFile(file: MultipartFile): String {
        val fileName = UUID.randomUUID().toString() + "_" + file.originalFilename
        val convFile = File(fileName)
        val fos = FileOutputStream(convFile)
        fos.write(file.bytes)
        fos.close()

        runBlocking {
            uploadToS3(fileName, convFile)
        }
        return fileName
    }

    override fun getFileUrl(filePath: String): String {
        val unsignedRequest =
            GetObjectRequest {
                bucket = bucketName
                key = filePath
            }
        return runBlocking {
            S3Client { region = awsRegion }.use { s3 ->
                val presignedRequest = s3.presignGetObject(unsignedRequest, 24.hours)
                return@runBlocking presignedRequest.url.toString()
            }
        }
    }

    private suspend fun uploadToS3(name: String, file: File) {
        val request =
            PutObjectRequest {
                bucket = bucketName
                key = name
                body = file.asByteStream()
            }

        S3Client { region = awsRegion }.use { s3 ->
            s3.putObject(request)
        }
    }
}