package edu.citchennai.hostel.complaint;

import edu.citchennai.hostel.account.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "complaints")
public class Complaint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private UserAccount student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComplaintCategory category;

    @Column(nullable = false, length = 2000)
    private String description;

    @Lob
    @Column(nullable = false)
    private byte[] photo;

    @Column(nullable = false)
    private String photoContentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComplaintStatus status;

    private LocalDate visitDate;
    private Instant createdAt;
    private Instant wardenAcceptedAt;

    protected Complaint() {
    }

    public Complaint(UserAccount student, ComplaintCategory category, String description,
                     byte[] photo, String photoContentType) {
        this.student = student;
        this.category = category;
        this.description = description;
        this.photo = photo;
        this.photoContentType = photoContentType;
        this.status = ComplaintStatus.WAITING_FOR_WARDEN;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public UserAccount getStudent() {
        return student;
    }

    public ComplaintCategory getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public String getPhotoContentType() {
        return photoContentType;
    }

    public ComplaintStatus getStatus() {
        return status;
    }

    public LocalDate getVisitDate() {
        return visitDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isWardenAccepted() {
        return wardenAcceptedAt != null;
    }

    public void acceptByWarden() {
        this.wardenAcceptedAt = Instant.now();
        this.status = ComplaintStatus.WAITING_FOR_HOSTEL_AUNTY;
    }

    public void scheduleVisit(LocalDate date) {
        this.visitDate = date;
        this.status = ComplaintStatus.VISIT_SCHEDULED;
    }

    public void markSolved() {
        this.status = ComplaintStatus.SOLVED;
    }
}
