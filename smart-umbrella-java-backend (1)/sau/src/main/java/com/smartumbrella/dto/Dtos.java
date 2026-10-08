package com.smartumbrella.dto;

import jakarta.validation.constraints.*;

/** Request bodies with validation rules. */
public final class Dtos {
    private Dtos() {}
    private static final String PHONE = "^\\+?\\d{10,15}$";

    public record RegisterRequest(
        @NotBlank @Size(min = 2, max = 100) String fullName,
        @Pattern(regexp = PHONE) String mobile,
        @Email @NotBlank String email,
        @Size(min = 8, max = 72) String password,
        @NotBlank String emergencyContactName,
        @Pattern(regexp = PHONE) String emergencyContactNumber,
        @Pattern(regexp = "^[A-Za-z0-9-]{4,32}$") String deviceId,
        @Pattern(regexp = "^$|" + PHONE) String simNumber) {}

    public record LoginRequest(@NotBlank String identifier, @NotBlank String password) {}

    public record LocationRequest(@DecimalMin("-90") @DecimalMax("90") double latitude,
                                  @DecimalMin("-180") @DecimalMax("180") double longitude, Double accuracy) {}

    public record SosRequest(Double latitude, Double longitude, @Size(max = 200) String note) {}

    public record HapticRequest(@Pattern(regexp = "obstacle|left|right|stop|danger|emergency") String type,
                                Double latitude, Double longitude) {}

    public record HeartbeatRequest(@Min(0) @Max(100) Integer battery, String gsm, String gps, String firmware) {}

    public record DeviceRegisterRequest(@Pattern(regexp = "^[A-Za-z0-9-]{4,32}$") String deviceId,
                                        @Pattern(regexp = "^$|" + PHONE) String simNumber) {}

    public record ContactRequest(@NotBlank @Size(max = 100) String name, @Size(max = 50) String relationship,
                                 @Pattern(regexp = PHONE) String phone, @Min(1) @Max(10) Integer priority) {}
}
