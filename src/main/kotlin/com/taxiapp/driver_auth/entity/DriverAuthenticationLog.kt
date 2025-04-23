package com.taxiapp.driver_auth.entity

import com.taxiapp.driver_auth.entity.enums.AuthenticationEvent
import com.taxiapp.driver_auth.entity.enums.VerificationStatus
import jakarta.persistence.*
import java.util.Date

@Entity(name = "driver_authentication_log")
class DriverAuthenticationLog(
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "driver_personal_info_id", nullable = false)
    var driverPersonalInfo: DriverPersonalInfo,

    @Column
    var timestamp: Date,

    @Column
    var eventType: AuthenticationEvent,

    @Column
    var statusBefore: VerificationStatus,

    @Column
    var statusAfter: VerificationStatus,

    @Column(length = 1024, nullable = true)
    var comment: String? = null,
)