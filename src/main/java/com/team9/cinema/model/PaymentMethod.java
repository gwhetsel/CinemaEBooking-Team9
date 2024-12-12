package com.team9.cinema.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cardType;
    private String cardNumber;
    private String expirationDate;
    private String billingAddress;

    @Transient
    private String plainTextCardNumber;

    @Transient
    private String maskedCardNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public void setUser(User user) {
        this.user = user;
        if (user != null && !user.getPaymentMethods().contains(this)) {
            user.getPaymentMethods().add(this);
        }
    }

    public String getMaskedCardNumber() {
        if (this.cardNumber != null && this.cardNumber.length() >= 4) {
            return "**** **** **** " + this.cardNumber.substring(this.cardNumber.length() - 4);
        }
        return null;
    }    
}