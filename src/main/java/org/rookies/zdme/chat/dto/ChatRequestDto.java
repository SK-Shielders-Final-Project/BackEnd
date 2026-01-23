package org.rookies.zdme.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChatRequestDto(
        @NotNull Long userId,
        @NotBlank String message
) {}
