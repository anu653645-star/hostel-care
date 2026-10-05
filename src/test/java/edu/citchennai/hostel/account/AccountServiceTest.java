package edu.citchennai.hostel.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private UserAccountRepository accounts;
    @Mock
    private PasswordEncoder passwordEncoder;

    private AccountService service;

    @BeforeEach
    void setUp() {
        service = new AccountService(accounts, passwordEncoder, "@citchennai.net");
    }

    @Test
    void rejectsEmailOutsideCollegeDomain() {
        RegistrationForm form = form("student@example.com");

        assertThrows(IllegalArgumentException.class, () -> service.register(form));

        verify(accounts, never()).save(any());
    }

    @Test
    void normalizesCollegeEmailAndHashesPassword() {
        RegistrationForm form = form(" Student@CITChennai.net ");
        when(passwordEncoder.encode("password123")).thenReturn("encoded");

        service.register(form);

        ArgumentCaptor<UserAccount> account = ArgumentCaptor.forClass(UserAccount.class);
        verify(accounts).save(account.capture());
        assertEquals("student@citchennai.net", account.getValue().getEmail());
        assertEquals("encoded", account.getValue().getPassword());
        assertEquals(Role.STUDENT, account.getValue().getRole());
    }

    private RegistrationForm form(String email) {
        RegistrationForm form = new RegistrationForm();
        form.setFullName("Student Name");
        form.setEmail(email);
        form.setPassword("password123");
        form.setRole("STUDENT");
        form.setHostel("A Block");
        form.setRoomNumber("204");
        return form;
    }
}
