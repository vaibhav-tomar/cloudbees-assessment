package com.cloudbees.assessment.api.response;

import com.cloudbees.assessment.entity.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReceiptResponse {
    private Long id;
    private String from;
    private String to;
    private User user;
    private BigDecimal price;

    public ReceiptResponse(String from, String to, User user, BigDecimal price) {
        this.from = from;
        this.to = to;
        this.user = user;
        this.price = price;
    }
}
