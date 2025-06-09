package com.taxiapp.driver_auth.dto.http

data class EmailSendRequestTO(
    val recipient: String,
    val subject: String,
    val body: String,
)