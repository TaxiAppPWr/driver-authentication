package com.taxiapp.driver_auth.entity.enums

enum class VerificationStatus {
    WAITING_FOR_SUBMIT,
    PENDING_AUTO_VERIFICATION,
    PENDING_MANUAL_VERIFICATION,
    APPROVED,
    REJECTED,
    CANCELLED
}