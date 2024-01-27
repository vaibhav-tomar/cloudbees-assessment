package com.cloudbees.assessment.api.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SeatUpdateRequest {
    @NotNull
    private Long userId;
    @NotNull
    private Integer newSeat;
}
