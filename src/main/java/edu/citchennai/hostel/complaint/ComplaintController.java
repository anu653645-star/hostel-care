package edu.citchennai.hostel.complaint;

import edu.citchennai.hostel.account.Role;
import edu.citchennai.hostel.account.UserAccount;
import java.time.LocalDate;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ComplaintController {
    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @GetMapping("/")
    public String dashboard(@AuthenticationPrincipal UserAccount user, Model model) {
        model.addAttribute("user", user);
        model.addAttribute("complaints", complaintService.visibleTo(user));
        model.addAttribute("categories", ComplaintCategory.values());
        model.addAttribute("today", LocalDate.now());
        return "dashboard";
    }

    @PostMapping("/complaints")
    public String submit(@AuthenticationPrincipal UserAccount user,
                         @RequestParam String category,
                         @RequestParam String description,
                         @RequestParam MultipartFile photo,
                         Model model) {
        try {
            Complaint complaint = complaintService.submit(user, category, description, photo);
            return "redirect:/?submitted=" + complaint.getId();
        } catch (IllegalArgumentException exception) {
            model.addAttribute("user", user);
            model.addAttribute("complaints", complaintService.visibleTo(user));
            model.addAttribute("categories", ComplaintCategory.values());
            model.addAttribute("today", LocalDate.now());
            model.addAttribute("formError", exception.getMessage());
            return "dashboard";
        }
    }

    @PostMapping("/complaints/{id}/accept")
    public String accept(@PathVariable Long id, @AuthenticationPrincipal UserAccount user) {
        complaintService.acceptByWarden(id, user);
        return "redirect:/";
    }

    @PostMapping("/complaints/{id}/schedule")
    public String schedule(@PathVariable Long id, @AuthenticationPrincipal UserAccount user,
                           @RequestParam LocalDate visitDate) {
        complaintService.scheduleVisit(id, user, visitDate);
        return "redirect:/";
    }

    @PostMapping("/complaints/{id}/solved")
    public String solved(@PathVariable Long id, @AuthenticationPrincipal UserAccount user) {
        complaintService.markSolved(id, user);
        return "redirect:/";
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public String workflowError() {
        return "redirect:/?workflowError";
    }

    @GetMapping("/complaints/{id}/photo")
    public ResponseEntity<byte[]> photo(@PathVariable Long id,
                                        @AuthenticationPrincipal UserAccount user) {
        Complaint complaint = complaintService.findForPhoto(id, user);
        String safeContentType = switch (complaint.getPhotoContentType()) {
            case "image/jpeg", "image/png", "image/webp" -> complaint.getPhotoContentType();
            default -> throw new AccessDeniedException("Unsupported image type.");
        };
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header("X-Content-Type-Options", "nosniff")
                .contentType(org.springframework.http.MediaType.parseMediaType(safeContentType))
                .body(complaint.getPhoto());
    }
}
