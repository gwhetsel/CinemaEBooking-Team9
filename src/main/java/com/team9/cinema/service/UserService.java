package com.team9.cinema.service;

import com.team9.cinema.dtos.UserRegistrationDto;
import com.team9.cinema.model.PaymentMethod;
import com.team9.cinema.model.User;
import com.team9.cinema.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EncryptionService encryptionService;

    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#_\\-])[A-Za-z\\d@$!%*?&#_\\-]{8,}$";

    // Register a user
    public User registerNewUserAccount(UserRegistrationDto userDto) {
        User user = new User();
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setEnabled(false);
        user.setRole("USER");
        user.setPromotions(userDto.isPromotions());

        // encrypt and assign payment methods
        if (userDto.getPaymentMethods() != null) {
            userDto.getPaymentMethods().stream()
                    .filter(this::isPaymentMethodValid)
                    .forEach(dtoPaymentMethod -> {
                        PaymentMethod encryptedPaymentMethod = createEncryptedPaymentMethod(dtoPaymentMethod);
                        encryptedPaymentMethod.setUser(user); // Link payment to user
                        user.getPaymentMethods().add(encryptedPaymentMethod);
                    });
        }

        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        userRepository.save(user);

        return user;
    }

    // Send reset password
    public boolean sendResetPasswordToken(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String resetToken = UUID.randomUUID().toString();
            user.setResetPasswordToken(resetToken);
            userRepository.save(user);

            String subject = "Reset Your Password";
            String message = "Your reset password token is: " + resetToken;

            emailService.sendEmail(user.getEmail(), subject, message);

            return true;
        }
        return false;
    }

    // Verify reset password token
    public boolean verifyResetPasswordToken(String token) {
        return userRepository.findByResetPasswordToken(token).isPresent();
    }

    // Only reset if valid
    public boolean resetPassword(String token, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Passwords do not match");
        }
    
        if (!newPassword.matches(PASSWORD_REGEX)) {
            throw new IllegalArgumentException("Password does not meet complexity requirements");
        }
    
        Optional<User> userOptional = userRepository.findByResetPasswordToken(token);
    
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setResetPasswordToken(null); // used, clear token
            userRepository.save(user);
    
            return true;
        }
    
        throw new IllegalArgumentException("Invalid token");
    }    

    // Update the user profile
    public User updateUserProfile(UserRegistrationDto userDto) {
        User currentUser = getCurrentUser();

        currentUser.setFirstName(userDto.getFirstName());
        currentUser.setLastName(userDto.getLastName());
        currentUser.setPhoneNumber(userDto.getPhoneNumber());

        if (userDto.getNewPassword() != null && !userDto.getNewPassword().isEmpty()) {
            currentUser.setPassword(passwordEncoder.encode(userDto.getNewPassword()));
        }

        if (userDto.getPaymentMethods() != null) {
            currentUser.getPaymentMethods().clear();
            userDto.getPaymentMethods().stream()
                    .filter(this::isPaymentMethodValid)
                    .forEach(dtoPaymentMethod -> {
                        PaymentMethod encryptedPaymentMethod = createEncryptedPaymentMethod(dtoPaymentMethod);
                        encryptedPaymentMethod.setUser(currentUser); // link the payment to current user
                        currentUser.getPaymentMethods().add(encryptedPaymentMethod);
                    });
        }

        return userRepository.save(currentUser);
    }

    // Display the user
    public User getCurrentUserForDisplay() {
        User currentUser = getCurrentUser();

        currentUser.getPaymentMethods().forEach(paymentMethod -> {
            try {
                if (paymentMethod.getCardNumber() != null && !paymentMethod.getCardNumber().isEmpty()) {
                    String encryptedWithLastFour = paymentMethod.getCardNumber();
                    String encryptedPart = encryptedWithLastFour.substring(0, encryptedWithLastFour.length() - 4);
                    String lastFourDigits = encryptedWithLastFour.substring(encryptedWithLastFour.length() - 4);

                    String decryptedFirstTwelve = encryptionService.decrypt(encryptedPart);
                    String maskedCardNumber = "**** **** **** " + lastFourDigits;

                    paymentMethod.setMaskedCardNumber(maskedCardNumber);
                    paymentMethod.setPlainTextCardNumber(decryptedFirstTwelve + lastFourDigits);
                }
            } catch (Exception e) {
                System.err.println("Error decrypting card number: " + e.getMessage());
                paymentMethod.setMaskedCardNumber("Invalid Card");
            }
        });

        return currentUser;
    }

    // Encrypt and create payment
    public PaymentMethod createEncryptedPaymentMethod(PaymentMethod dtoPaymentMethod) {
        PaymentMethod paymentMethod = new PaymentMethod();
        paymentMethod.setCardType(dtoPaymentMethod.getCardType());
        if (dtoPaymentMethod.getCardNumber() != null && dtoPaymentMethod.getCardNumber().length() == 16) {
            String firstTwelve = dtoPaymentMethod.getCardNumber().substring(0, 12);
            String lastFour = dtoPaymentMethod.getCardNumber().substring(12);
            String encryptedFirstTwelve = encryptionService.encrypt(firstTwelve);

            paymentMethod.setCardNumber(encryptedFirstTwelve + lastFour);
            paymentMethod.setPlainTextCardNumber(lastFour);
        }
        paymentMethod.setExpirationDate(dtoPaymentMethod.getExpirationDate());
        paymentMethod.setBillingAddress(dtoPaymentMethod.getBillingAddress());
        return paymentMethod;
    }

    // Validate the payment method
    public boolean isPaymentMethodValid(PaymentMethod paymentMethod) {
        return paymentMethod.getCardType() != null && !paymentMethod.getCardType().isEmpty() &&
                paymentMethod.getCardNumber() != null && paymentMethod.getCardNumber().length() == 16 &&
                paymentMethod.getExpirationDate() != null && paymentMethod.getExpirationDate().matches("^(0[1-9]|1[0-2])\\/\\d{2}$") &&
                paymentMethod.getBillingAddress() != null && !paymentMethod.getBillingAddress().isEmpty();
    }

    // get a user
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    // Check if the email exists
    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    // Save the user
    public void save(User user) {
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getSubscribedUsers() {
        return userRepository.findByPromotions(true);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public void deleteById(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            userRepository.deleteById(id);
        } else {
            throw new RuntimeException("User not found.");
        }
    }

    // Update the user
    public User updateUser(Long id, User updatedUser) {
        // Retrieve the user from the repository
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // update the details
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
        existingUser.setRole(updatedUser.getRole()); // update the role
        existingUser.setPromotions(updatedUser.getPromotions());

        // save and return
        return userRepository.save(existingUser);
    }

    // get a payment method by id
    public PaymentMethod getPaymentMethodById(Long paymentMethodId) {
        User currentUser = getCurrentUser();
        return currentUser.getPaymentMethods()
            .stream()
            .filter(pm -> pm.getId().equals(paymentMethodId))
            .findFirst()
            .orElse(null);
    }    
}