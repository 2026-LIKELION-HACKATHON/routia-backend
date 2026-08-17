package com.routiaback.auth.application.command;

public record EmailVerifyCommand(String email, String code) {
}
