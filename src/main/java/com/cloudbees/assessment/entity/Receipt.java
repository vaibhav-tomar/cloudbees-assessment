package com.cloudbees.assessment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Receipt {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String fromStation;
    private String toStation;

    // Bidirectional one to one mapping between receipt and user
    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    private BigDecimal price;

    public Receipt(String fromStation, String toStation, User user, BigDecimal price) {
        this.fromStation = fromStation;
        this.toStation = toStation;
        this.user = user;
        this.price = price;
    }
}
