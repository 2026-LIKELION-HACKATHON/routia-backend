package com.routiaback.personalization.application.command;

public record ProfileImageUpload(byte[] content, String contentType) {

    public long size() {
        return content == null ? 0 : content.length;
    }
}
