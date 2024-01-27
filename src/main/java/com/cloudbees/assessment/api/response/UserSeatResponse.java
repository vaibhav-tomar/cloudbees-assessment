package com.cloudbees.assessment.api.response;

import com.cloudbees.assessment.enums.Section;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserSeatResponse {
    private Long userId;
    private String email;
    private Integer seatNumber;
    private Section section;
}
