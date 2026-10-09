package com.smartcomplaint.controller;

import com.smartcomplaint.model.Complaint;
import com.smartcomplaint.model.ComplaintStatus;
import com.smartcomplaint.repository.ComplaintRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {
    private final ComplaintRepository repo;

    public ComplaintController(ComplaintRepository repo) {
        this.repo = repo;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> create(
            @RequestParam String type,
            @RequestParam String description,
            @RequestParam String contact,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) String address,
            @RequestPart(required = false) MultipartFile proof) {

        if (description.isBlank() || contact.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Description and contact are required"));
        }

        try {
            Complaint c = new Complaint();
            c.setComplaintId("CMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            c.setType(type);
            c.setDescription(description);
            c.setContact(contact);
            c.setLatitude(latitude);
            c.setLongitude(longitude);
            c.setAddress(address);
            c.setStatus(ComplaintStatus.PENDING);

            if (proof != null && !proof.isEmpty()) {
                if (proof.getSize() > 10 * 1024 * 1024) {
                    return ResponseEntity.badRequest().body(Map.of("message", "Proof image must be 10MB or smaller"));
                }
                String contentType = proof.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    return ResponseEntity.badRequest().body(Map.of("message", "Only image proof files are allowed"));
                }
                c.setProofFileName(proof.getOriginalFilename());
                c.setProofContentType(contentType);
                c.setProofImage(proof.getBytes());
            }

            return ResponseEntity.ok(repo.save(c));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Could not save complaint"));
        }
    }

    @GetMapping
    public List<Complaint> all() {
        return repo.findAllByOrderByCreatedAtDesc();
    }

    @GetMapping("/{complaintId}")
    public ResponseEntity<?> get(@PathVariable String complaintId) {
        return repo.findByComplaintId(complaintId)
                .<ResponseEntity<?>>map(c -> {
                    c.setProofImage(null);
                    return ResponseEntity.ok(c);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{complaintId}/proof")
    public ResponseEntity<?> proof(@PathVariable String complaintId) {
        return repo.findByComplaintId(complaintId)
                .map(c -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(
                                c.getProofContentType() == null ? "image/jpeg" : c.getProofContentType()))
                        .body(c.getProofImage()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{complaintId}")
    public ResponseEntity<?> update(@PathVariable String complaintId,
                                    @RequestBody Map<String, String> body) {
        Optional<Complaint> optional = repo.findByComplaintId(complaintId);
        if (optional.isEmpty()) return ResponseEntity.notFound().build();

        Complaint c = optional.get();

        if (body.containsKey("status")) {
            try {
                c.setStatus(ComplaintStatus.valueOf(body.get("status")));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid status"));
            }
        }

        if (body.containsKey("assignedStaff")) {
            c.setAssignedStaff(body.get("assignedStaff"));
        }

        return ResponseEntity.ok(repo.save(c));
    }
}
