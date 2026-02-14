package com.example.kihan.application.deadline.command;

public record UpdateDeadlineCommand(
        Long userId,
        Long deadlineId,
        String title,
        String description
) {
}
