package com.cloudbees.assessment.api.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SeatUpdateResponse {

    private Long userId;
    private Integer newSeat;
}
