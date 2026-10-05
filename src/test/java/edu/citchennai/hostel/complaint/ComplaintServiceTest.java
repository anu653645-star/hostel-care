package edu.citchennai.hostel.complaint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.citchennai.hostel.account.Role;
import edu.citchennai.hostel.account.UserAccount;
import edu.citchennai.hostel.account.UserAccountRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ComplaintServiceTest {
    @Mock
    private ComplaintRepository complaints;
    @Mock
    private UserAccountRepository accounts;

    private ComplaintService service;
    private UserAccount student;
    private Complaint complaint;

    @BeforeEach
    void setUp() {
        service = new ComplaintService(complaints, accounts);
        student = new UserAccount("Student", "student@citchennai.net", "hash",
                Role.STUDENT, "A Block", "204");
        ReflectionTestUtils.setField(student, "id", 10L);
        complaint = new Complaint(student, ComplaintCategory.FAN, "Fan is not working",
                new byte[]{1}, "image/jpeg");
        ReflectionTestUtils.setField(complaint, "id", 20L);
    }

    @Test
    void complaintMovesThroughWardenAuntyAndStudentConfirmation() {
        when(complaints.findById(20L)).thenReturn(Optional.of(complaint));
        UserAccount warden = account(Role.WARDEN);
        UserAccount aunty = account(Role.HOSTEL_AUNTY);

        service.acceptByWarden(20L, warden);
        assertEquals(ComplaintStatus.WAITING_FOR_HOSTEL_AUNTY, complaint.getStatus());

        service.scheduleVisit(20L, aunty, LocalDate.now().plusDays(1));
        assertEquals(ComplaintStatus.VISIT_SCHEDULED, complaint.getStatus());

        service.markSolved(20L, student);
        assertEquals(ComplaintStatus.SOLVED, complaint.getStatus());
    }

    @Test
    void hostelAuntyOnlyReceivesComplaintsAcceptedByWarden() {
        UserAccount aunty = account(Role.HOSTEL_AUNTY);
        List<Complaint> visible = List.of(complaint);
        when(complaints.findByStatusInOrderByCreatedAtDesc(List.of(
                ComplaintStatus.WAITING_FOR_HOSTEL_AUNTY,
                ComplaintStatus.VISIT_SCHEDULED,
                ComplaintStatus.SOLVED))).thenReturn(visible);

        assertEquals(visible, service.visibleTo(aunty));

        verify(complaints).findByStatusInOrderByCreatedAtDesc(List.of(
                ComplaintStatus.WAITING_FOR_HOSTEL_AUNTY,
                ComplaintStatus.VISIT_SCHEDULED,
                ComplaintStatus.SOLVED));
    }

    @Test
    void studentCannotConfirmAnotherStudentsComplaint() {
        when(complaints.findById(20L)).thenReturn(Optional.of(complaint));
        UserAccount otherStudent = new UserAccount("Another Student", "other@citchennai.net",
                "hash", Role.STUDENT, "B Block", "105");
        ReflectionTestUtils.setField(otherStudent, "id", 11L);
        complaint.acceptByWarden();
        complaint.scheduleVisit(LocalDate.now().plusDays(1));

        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> service.markSolved(20L, otherStudent));
    }

    private UserAccount account(Role role) {
        return new UserAccount(role.name(), role.name().toLowerCase() + "@citchennai.net",
                "hash", role, null, null);
    }
}
