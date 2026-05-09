package com.launchpad.flow.domain;

public enum NodeType {
    WEBHOOK_TRIGGER,
    SCHEDULE_TRIGGER,
    MANUAL_TRIGGER,
    HTTP_REQUEST,
    CONDITION,
    DELAY,
    GMAIL_SEND_EMAIL,
    GITHUB_CREATE_ISSUE
}

