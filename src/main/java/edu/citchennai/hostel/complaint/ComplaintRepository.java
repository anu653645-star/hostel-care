package edu.citchennai.hostel.complaint;

import java.util.List;
import java.util.Collection;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    @EntityGraph(attributePaths = "student")
    List<Complaint> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "student")
    List<Complaint> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    @EntityGraph(attributePaths = "student")
    List<Complaint> findByStatusInOrderByCreatedAtDesc(Collection<ComplaintStatus> statuses);
}
