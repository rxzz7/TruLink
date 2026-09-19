package com.tru_link.Trulink.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateRequest (
    @NotBlank(message = "URL must not be blank")
    @Size(max = 1024, message = "URl must be at most 104 characters")
    @Pattern(regexp = "^https?://.*", message = "URL must start with http:// or https://")
    String originalUrl){}