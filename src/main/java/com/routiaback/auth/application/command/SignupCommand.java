package com.routiaback.auth.application.command;

public record SignupCommand(String email, String password, String name) {
}
