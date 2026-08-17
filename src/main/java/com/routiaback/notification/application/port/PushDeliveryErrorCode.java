package com.routiaback.notification.application.port;

public enum PushDeliveryErrorCode {
    INVALID_TARGET,
    UNREGISTERED,
    TEMPORARY_FAILURE,
    AUTH_FAILURE,
    UNKNOWN
}
