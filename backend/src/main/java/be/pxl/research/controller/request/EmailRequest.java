package be.pxl.research.controller.request;

import jakarta.validation.constraints.NotNull;

public record EmailRequest(@NotNull String to, @NotNull String subject, @NotNull String body) {
}
