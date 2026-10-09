package com.smartcomplaint.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcomplaint.model.Complaint;

public interface ComplaintRepository
        extends JpaRepository<Complaint, Long> {

    Optional<Complaint> findByComplaintId(
            String complaintId
    );

    List<Complaint> findByUserIdOrderByCreatedAtDesc(
            String userId
    );
}