package com.team9.cinema.dtos;

import com.team9.cinema.model.PaymentMethod;
import lombok.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegistrationDto {
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // base
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    @Setter
    @Getter
    private boolean promotions;
    private String currentPassword;
    private String newPassword;

    // payment
    private List<PaymentMethod> paymentMethods;

}
