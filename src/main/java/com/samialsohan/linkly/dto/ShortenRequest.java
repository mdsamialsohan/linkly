package com.samialsohan.linkly.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ShortenRequest(
        @NotBlank(message = "longUrl is required")
        @Size(max = 2048, message = "longUrl must be at most 2048 characters")
        @Pattern(
                regexp = "^https?://.+",
                message = "longUrl must start with http:// or https://"
        )
        String longUrl
) {
}