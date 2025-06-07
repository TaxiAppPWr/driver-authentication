package com.taxiapp.driver_auth.repository

import com.taxiapp.driver_auth.entity.DriverPersonalInfo
import com.taxiapp.driver_auth.entity.enums.VerificationStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DriverPersonalInfoRepository : JpaRepository<DriverPersonalInfo, Long> {
    fun findByUsername(username: String): DriverPersonalInfo?
    fun findAllByVerificationStatus(verificationStatus: VerificationStatus): List<DriverPersonalInfo>
    fun existsByUsername(username: String): Boolean
    fun findFirstByVerificationStatus(verificationStatus: VerificationStatus): DriverPersonalInfo?
}