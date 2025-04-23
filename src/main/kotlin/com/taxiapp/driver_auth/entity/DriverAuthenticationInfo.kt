package com.taxiapp.driver_auth.entity

import jakarta.persistence.*
import java.util.*

@Entity(name = "driver_authentication_info")
class DriverAuthenticationInfo(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column
    var submittedAt: Date,

    @Column
    var driverLicenceNumber: String,

    @Column
    var registrationDocumentNumber: String,

    @Column
    var plateNumber: String,

    @Column
    var driverLicenseFrontPhotoPath: String,

    @Column
    var driverLicenseBackPhotoPath: String,

    @Column
    var pesel: String,

    @Column
    var street: String,
    @Column
    val buildingNumber: String,
    @Column
    val apartmentNumber: String?,
    @Column
    val postCode: String,
    @Column
    val city: String,
    @Column
    val country: String
)