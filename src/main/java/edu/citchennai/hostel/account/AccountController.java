package edu.citchennai.hostel.account;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AccountController {
    private final AccountService accountService;
    private final String collegeEmailDomain;

    public AccountController(AccountService accountService,
                             @Value("${app.college-email-domain:citchennai.net}") String collegeEmailDomain) {
        this.accountService = accountService;
        this.collegeEmailDomain = collegeEmailDomain.startsWith("@")
                ? collegeEmailDomain.substring(1)
                : collegeEmailDomain;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("form", new RegistrationForm());
        model.addAttribute("collegeEmailDomain", collegeEmailDomain);
        model.addAttribute("hasValidationErrors", false);
        return "register";
    }

    @PostMapping("/register")
    public String createAccount(@Valid @ModelAttribute("form") RegistrationForm form,
                                BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("collegeEmailDomain", collegeEmailDomain);
            model.addAttribute("hasValidationErrors", true);
            return "register";
        }
        try {
            accountService.register(form);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("registrationError", exception.getMessage());
            model.addAttribute("collegeEmailDomain", collegeEmailDomain);
            return "register";
        }
        return "redirect:/login?registered";
    }
}
