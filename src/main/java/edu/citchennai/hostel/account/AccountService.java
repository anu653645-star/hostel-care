package edu.citchennai.hostel.account;

import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {
    private final UserAccountRepository accounts;
    private final PasswordEncoder passwordEncoder;
    private final String collegeEmailDomain;

    public AccountService(UserAccountRepository accounts, PasswordEncoder passwordEncoder,
                          @Value("${app.college-email-domain:citchennai.net}") String collegeEmailDomain) {
        this.accounts = accounts;
        this.passwordEncoder = passwordEncoder;
        this.collegeEmailDomain = collegeEmailDomain.startsWith("@")
                ? collegeEmailDomain.substring(1).toLowerCase(Locale.ROOT)
                : collegeEmailDomain.toLowerCase(Locale.ROOT);
    }

    @Transactional
    public void register(RegistrationForm form) {
        String email = form.getEmail().trim().toLowerCase(Locale.ROOT);
        if (!email.endsWith("@" + collegeEmailDomain)) {
            throw new IllegalArgumentException("Use your @" + collegeEmailDomain + " college email address.");
        }
        if (accounts.existsByEmail(email)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        Role role;
        try {
            role = Role.valueOf(form.getRole());
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new IllegalArgumentException("Choose a valid account type.");
        }

        String hostel = role == Role.STUDENT ? requiredText(form.getHostel(), "Hostel") : null;
        String roomNumber = role == Role.STUDENT ? requiredText(form.getRoomNumber(), "Room number") : null;
        accounts.save(new UserAccount(
                form.getFullName().trim(),
                email,
                passwordEncoder.encode(form.getPassword()),
                role,
                hostel,
                roomNumber));
    }

    private String requiredText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required for student accounts.");
        }
        return value.trim();
    }
}
