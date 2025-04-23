package com.taxiapp.driver_auth.repository

import com.taxiapp.driver_auth.entity.DriverAuthenticationLog
import org.springframework.data.jpa.repository.JpaRepository

interface DriverAuthenticationLogRepository : JpaRepository<DriverAuthenticationLog, Long> {

}