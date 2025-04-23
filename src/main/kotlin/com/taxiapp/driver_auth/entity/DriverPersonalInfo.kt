package com.taxiapp.driver_auth.entity

import com.taxiapp.driver_auth.entity.enums.VerificationStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity(name = "driver_personal_info")
@Table(indexes = [
    Index(columnList = "username")
])
class DriverPersonalInfo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column
    var username: String,

    @Column
    var name: String,

    @Column
    var surname: String,

    @Column
    var email: String,

    @Column
    var verificationStatus: VerificationStatus = VerificationStatus.WAITING_FOR_SUBMIT,

    @OneToOne
    @JoinColumn(name = "driver_authentication_info_id", nullable = true)
    var driverAuthenticationInfo: DriverAuthenticationInfo? = null,
)