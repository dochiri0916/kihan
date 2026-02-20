package com.example.kihan.application.deadline.dto;

public record UpdateDeadlineCommand(
        Long userId,
        Long deadlineId,
        String title,
        String description
) {
}