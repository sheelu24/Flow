package com.launchpad.flow.service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.springframework.scheduling.support.CronExpression;

public final class CronSupport {
    private CronSupport() {
    }

    public static Instant next(String cronExpression, Instant from) {
        String normalized = normalize(cronExpression);
        CronExpression expression = CronExpression.parse(normalized);
        ZonedDateTime next = expression.next(ZonedDateTime.ofInstant(from, ZoneOffset.UTC));
        if (next == null) {
            throw AppException.badRequest("Cron expression has no next execution time, error");
        }
        return next.toInstant();
    }

    private static String normalize(String cronExpression) {
        String trimmed = cronExpression == null ? "" : cronExpression.trim();
        if (trimmed.split("\\s+").length == 5) {
            return "0 " + trimmed;
        }
        return trimmed;
    }
}

