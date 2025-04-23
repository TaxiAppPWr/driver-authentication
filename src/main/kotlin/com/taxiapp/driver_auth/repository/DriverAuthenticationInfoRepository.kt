package com.taxiapp.driver_auth.repository

import com.taxiapp.driver_auth.entity.DriverAuthenticationInfo
import org.springframework.data.jpa.repository.JpaRepository

interface DriverAuthenticationInfoRepository : JpaRepository<DriverAuthenticationInfo, Long> {
}