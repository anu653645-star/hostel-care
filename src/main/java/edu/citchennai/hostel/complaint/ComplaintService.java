package edu.citchennai.hostel.complaint;

import edu.citchennai.hostel.account.Role;
import edu.citchennai.hostel.account.UserAccount;
import edu.citchennai.hostel.account.UserAccountRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.Locale;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ComplaintService {
    private static final long MAX_PHOTO_BYTES = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_PHOTO_TYPES = Set.of(
            MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, "image/webp");

    private final ComplaintRepository complaints;
    private final UserAccountRepository accounts;

    public ComplaintService(ComplaintRepository complaints, UserAccountRepository accounts) {
        this.complaints = complaints;
        this.accounts = accounts;
    }

    @Transactional(readOnly = true)
    public List<Complaint> visibleTo(UserAccount user) {
        if (user.getRole() == Role.STUDENT) {
            return complaints.findByStudentIdOrderByCreatedAtDesc(user.getId());
        }
        if (user.getRole() == Role.HOSTEL_AUNTY) {
            return complaints.findByStatusInOrderByCreatedAtDesc(List.of(
                    ComplaintStatus.WAITING_FOR_HOSTEL_AUNTY,
                    ComplaintStatus.VISIT_SCHEDULED,
                    ComplaintStatus.SOLVED));
        }
        return complaints.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public Complaint submit(UserAccount student, String categoryValue, String description,
                            MultipartFile photo) {
        if (student.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException("Only students can submit complaints.");
        }
        ComplaintCategory category = parseCategory(categoryValue);
        if (description == null || description.isBlank() || description.length() > 2000) {
            throw new IllegalArgumentException("Describe the problem in 1 to 2000 characters.");
        }
        if (photo == null || photo.isEmpty()) {
            throw new IllegalArgumentException("Upload a photo of the problem.");
        }
        if (photo.getSize() > MAX_PHOTO_BYTES) {
            throw new IllegalArgumentException("The photo must be 5 MB or smaller.");
        }
        String contentType = photo.getContentType();
        if (contentType == null || !ALLOWED_PHOTO_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Upload a JPG, PNG, or WEBP image.");
        }
        try {
            byte[] photoBytes = photo.getBytes();
            if (!hasValidImageSignature(photoBytes, contentType.toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException("The uploaded file does not match its image type.");
            }
            return complaints.save(new Complaint(
                    student, category, description.trim(), photoBytes, contentType.toLowerCase(Locale.ROOT)));
        } catch (java.io.IOException exception) {
            throw new IllegalArgumentException("The uploaded photo could not be read.", exception);
        }
    }

    private boolean hasValidImageSignature(byte[] bytes, String contentType) {
        if (contentType.equals(MediaType.IMAGE_JPEG_VALUE)) {
            return bytes.length >= 3 && (bytes[0] & 0xff) == 0xff
                    && (bytes[1] & 0xff) == 0xd8 && (bytes[2] & 0xff) == 0xff;
        }
        if (contentType.equals(MediaType.IMAGE_PNG_VALUE)) {
            return bytes.length >= 8 && (bytes[0] & 0xff) == 0x89
                    && bytes[1] == 0x50 && bytes[2] == 0x4e && bytes[3] == 0x47
                    && bytes[4] == 0x0d && bytes[5] == 0x0a && bytes[6] == 0x1a && bytes[7] == 0x0a;
        }
        return contentType.equals("image/webp") && bytes.length >= 12
                && bytes[0] == 0x52 && bytes[1] == 0x49 && bytes[2] == 0x46 && bytes[3] == 0x46
                && bytes[8] == 0x57 && bytes[9] == 0x45 && bytes[10] == 0x42 && bytes[11] == 0x50;
    }

    @Transactional
    public void acceptByWarden(Long complaintId, UserAccount warden) {
        requireRole(warden, Role.WARDEN);
        Complaint complaint = find(complaintId);
        if (complaint.getStatus() != ComplaintStatus.WAITING_FOR_WARDEN) {
            throw new IllegalStateException("This complaint is no longer waiting for a warden.");
        }
        complaint.acceptByWarden();
    }

    @Transactional
    public void scheduleVisit(Long complaintId, UserAccount aunty, LocalDate visitDate) {
        requireRole(aunty, Role.HOSTEL_AUNTY);
        Complaint complaint = find(complaintId);
        if (complaint.getStatus() != ComplaintStatus.WAITING_FOR_HOSTEL_AUNTY) {
            throw new IllegalStateException("The warden must accept this complaint before a visit is scheduled.");
        }
        if (visitDate == null || visitDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Choose today or a future date for the visit.");
        }
        complaint.scheduleVisit(visitDate);
    }

    @Transactional
    public void markSolved(Long complaintId, UserAccount student) {
        requireRole(student, Role.STUDENT);
        Complaint complaint = find(complaintId);
        if (!complaint.getStudent().getId().equals(student.getId())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You can only confirm your own complaints.");
        }
        if (complaint.getStatus() != ComplaintStatus.VISIT_SCHEDULED) {
            throw new IllegalStateException("The problem can be confirmed as solved after a visit is scheduled.");
        }
        complaint.markSolved();
    }

    @Transactional(readOnly = true)
    public Complaint findForPhoto(Long complaintId, UserAccount viewer) {
        Complaint complaint = find(complaintId);
        if (viewer.getRole() == Role.STUDENT && !complaint.getStudent().getId().equals(viewer.getId())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You can only view photos for your own complaints.");
        }
        return complaint;
    }

    private Complaint find(Long id) {
        return complaints.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found."));
    }

    private ComplaintCategory parseCategory(String value) {
        try {
            return ComplaintCategory.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new IllegalArgumentException("Choose a valid complaint category.");
        }
    }

    private void requireRole(UserAccount user, Role requiredRole) {
        if (user.getRole() != requiredRole) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "This action is not available for your account.");
        }
    }
}
