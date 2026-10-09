package com.smartcomplaint.controller;

import com.smartcomplaint.model.Complaint;
import com.smartcomplaint.model.ComplaintStatus;
import com.smartcomplaint.repository.ComplaintRepository;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/complaints")
@CrossOrigin(origins = "*")
public class ComplaintController {

    private final ComplaintRepository complaintRepository;

    public ComplaintController(
            ComplaintRepository complaintRepository
    ) {
        this.complaintRepository = complaintRepository;
    }

    // =========================================================
    // CREATE COMPLAINT
    // =========================================================

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> createComplaint(

            @RequestParam String userId,

            @RequestParam String type,

            @RequestParam String description,

            @RequestParam(required = false)
            String contact,

            @RequestParam(required = false)
            String latitude,

            @RequestParam(required = false)
            String longitude,

            @RequestParam(required = false)
            String address,

            @RequestPart(required = false)
            MultipartFile proof
    ) {

        try {

            // -------------------------------------------------
            // Validate required fields
            // -------------------------------------------------

            if (userId == null || userId.isBlank()) {
                return ResponseEntity
                        .badRequest()
                        .body(
                                Map.of(
                                        "message",
                                        "User ID is required."
                                )
                        );
            }

            if (type == null || type.isBlank()) {
                return ResponseEntity
                        .badRequest()
                        .body(
                                Map.of(
                                        "message",
                                        "Complaint type is required."
                                )
                        );
            }

            if (description == null || description.isBlank()) {
                return ResponseEntity
                        .badRequest()
                        .body(
                                Map.of(
                                        "message",
                                        "Complaint description is required."
                                )
                        );
            }

            // -------------------------------------------------
            // Create complaint object
            // -------------------------------------------------

            Complaint complaint = new Complaint();

            // -------------------------------------------------
            // Generate unique complaint ID
            // Example: CMP-2E1BE4BB
            // -------------------------------------------------

            String complaintId =
                    "CMP-" +
                    UUID.randomUUID()
                            .toString()
                            .substring(0, 8)
                            .toUpperCase();

            complaint.setComplaintId(complaintId);

            // -------------------------------------------------
            // Basic complaint information
            // -------------------------------------------------

            complaint.setUserId(userId);

            complaint.setType(type);

            complaint.setDescription(description);

            complaint.setContact(contact);

            // -------------------------------------------------
            // Location
            // -------------------------------------------------

            if (latitude != null && !latitude.isBlank()) {

                try {

                    complaint.setLatitude(
                            Double.parseDouble(latitude)
                    );

                } catch (NumberFormatException e) {

                    return ResponseEntity
                            .badRequest()
                            .body(
                                    Map.of(
                                            "message",
                                            "Invalid latitude value."
                                    )
                            );
                }
            }

            if (longitude != null && !longitude.isBlank()) {

                try {

                    complaint.setLongitude(
                            Double.parseDouble(longitude)
                    );

                } catch (NumberFormatException e) {

                    return ResponseEntity
                            .badRequest()
                            .body(
                                    Map.of(
                                            "message",
                                            "Invalid longitude value."
                                    )
                            );
                }
            }

            complaint.setAddress(address);

            // -------------------------------------------------
            // Default status
            // -------------------------------------------------

            complaint.setStatus(
                    ComplaintStatus.PENDING
            );

            // =================================================
            // PROOF PHOTO
            // =================================================

            if (proof != null && !proof.isEmpty()) {

                // Maximum 10 MB

                if (proof.getSize() >
                        10L * 1024L * 1024L) {

                    return ResponseEntity
                            .badRequest()
                            .body(
                                    Map.of(
                                            "message",
                                            "Proof image must be smaller than 10 MB."
                                    )
                            );
                }

                // Only allow images

                String contentType =
                        proof.getContentType();

                if (
                        contentType == null ||
                        !contentType.startsWith("image/")
                ) {

                    return ResponseEntity
                            .badRequest()
                            .body(
                                    Map.of(
                                            "message",
                                            "Only image files are allowed as proof."
                                    )
                            );
                }

                complaint.setProofFileName(
                        proof.getOriginalFilename()
                );

                complaint.setProofContentType(
                        contentType
                );

                complaint.setProofImage(
                        proof.getBytes()
                );
            }

            // =================================================
            // SAVE COMPLAINT
            // =================================================

            Complaint savedComplaint =
                    complaintRepository.save(
                            complaint
                    );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(savedComplaint);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            Map.of(
                                    "message",
                                    "Unable to create complaint.",
                                    "error",
                                    e.getMessage() != null
                                            ? e.getMessage()
                                            : "Unknown error"
                            )
                    );
        }
    }

    // =========================================================
    // GET ALL COMPLAINTS
    // ADMIN
    // =========================================================

    @GetMapping
    public ResponseEntity<List<Complaint>>
    getAllComplaints() {

        List<Complaint> complaints =
                complaintRepository.findAll();

        return ResponseEntity.ok(
                complaints
        );
    }

    // =========================================================
    // GET USER COMPLAINTS
    //
    // Example:
    // GET /api/complaints/user/USER-123
    // =========================================================

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Complaint>>
    getUserComplaints(
            @PathVariable String userId
    ) {

        if (userId == null || userId.isBlank()) {

            return ResponseEntity
                    .badRequest()
                    .build();
        }

        List<Complaint> complaints =
                complaintRepository
                        .findByUserIdOrderByCreatedAtDesc(
                                userId
                        );

        return ResponseEntity.ok(
                complaints
        );
    }

    // =========================================================
    // GET SINGLE COMPLAINT
    //
    // Example:
    // GET /api/complaints/CMP-12345678
    // =========================================================

    @GetMapping("/{complaintId}")
    public ResponseEntity<?> getComplaint(
            @PathVariable String complaintId
    ) {

        return complaintRepository
                .findByComplaintId(complaintId)
                .<ResponseEntity<?>>map(
                        complaint ->
                                ResponseEntity.ok(
                                        complaint
                                )
                )
                .orElseGet(
                        () ->
                                ResponseEntity
                                        .status(
                                                HttpStatus.NOT_FOUND
                                        )
                                        .body(
                                                Map.of(
                                                        "message",
                                                        "Complaint not found."
                                                )
                                        )
                );
    }

    // =========================================================
    // UPDATE COMPLAINT
    // ADMIN
    //
    // Can update:
    // - status
    // - assignedStaff
    // =========================================================

    @PatchMapping("/{complaintId}")
    public ResponseEntity<?> updateComplaint(

            @PathVariable String complaintId,

            @RequestBody Map<String, Object> body
    ) {

        try {

            Complaint complaint =
                    complaintRepository
                            .findByComplaintId(
                                    complaintId
                            )
                            .orElse(null);

            if (complaint == null) {

                return ResponseEntity
                        .status(
                                HttpStatus.NOT_FOUND
                        )
                        .body(
                                Map.of(
                                        "message",
                                        "Complaint not found."
                                )
                        );
            }

            // -------------------------------------------------
            // UPDATE STATUS
            // -------------------------------------------------

            if (body.containsKey("status")) {

                Object statusValue =
                        body.get("status");

                if (statusValue != null) {

                    String statusText =
                            statusValue
                                    .toString()
                                    .trim()
                                    .toUpperCase();

                    try {

                        ComplaintStatus newStatus =
                                ComplaintStatus.valueOf(
                                        statusText
                                );

                        complaint.setStatus(
                                newStatus
                        );

                    } catch (IllegalArgumentException e) {

                        return ResponseEntity
                                .badRequest()
                                .body(
                                        Map.of(
                                                "message",
                                                "Invalid complaint status.",
                                                "allowed",
                                                List.of(
                                                        "PENDING",
                                                        "IN_PROGRESS",
                                                        "RESOLVED",
                                                        "REJECTED"
                                                )
                                        )
                                );
                    }
                }
            }

            // -------------------------------------------------
            // ASSIGN STAFF
            // -------------------------------------------------

            if (body.containsKey("assignedStaff")) {

                Object staffValue =
                        body.get("assignedStaff");

                if (staffValue != null) {

                    complaint.setAssignedStaff(
                            staffValue
                                    .toString()
                                    .trim()
                    );
                }
            }

            // -------------------------------------------------
            // SAVE UPDATED COMPLAINT
            // -------------------------------------------------

            Complaint updatedComplaint =
                    complaintRepository.save(
                            complaint
                    );

            return ResponseEntity.ok(
                    updatedComplaint
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            Map.of(
                                    "message",
                                    "Unable to update complaint.",
                                    "error",
                                    e.getMessage() != null
                                            ? e.getMessage()
                                            : "Unknown error"
                            )
                    );
        }
    }

    // =========================================================
    // GET PROOF IMAGE
    //
    // Example:
    // GET /api/complaints/CMP-12345678/proof
    // =========================================================

    @GetMapping("/{complaintId}/proof")
    public ResponseEntity<byte[]> getProof(
            @PathVariable String complaintId
    ) {

        Complaint complaint =
                complaintRepository
                        .findByComplaintId(
                                complaintId
                        )
                        .orElse(null);

        // -----------------------------------------------------
        // Complaint not found
        // -----------------------------------------------------

        if (complaint == null) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        // -----------------------------------------------------
        // No proof image
        // -----------------------------------------------------

        if (complaint.getProofImage() == null ||
                complaint.getProofImage().length == 0) {

            return ResponseEntity
                    .notFound()
                    .build();
        }

        // -----------------------------------------------------
        // Determine image type
        // -----------------------------------------------------

        MediaType mediaType =
                MediaType.IMAGE_JPEG;

        String contentType =
                complaint.getProofContentType();

        if (
                contentType != null &&
                !contentType.isBlank()
        ) {

            try {

                mediaType =
                        MediaType.parseMediaType(
                                contentType
                        );

            } catch (Exception ignored) {

                mediaType =
                        MediaType.IMAGE_JPEG;
            }
        }

        // -----------------------------------------------------
        // Filename
        // -----------------------------------------------------

        String fileName =
                complaint.getProofFileName();

        if (
                fileName == null ||
                fileName.isBlank()
        ) {

            fileName =
                    "complaint-proof.jpg";
        }

        // -----------------------------------------------------
        // Return image
        // -----------------------------------------------------

        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" +
                                fileName.replace(
                                        "\"",
                                        ""
                                ) +
                                "\""
                )
                .contentType(mediaType)
                .body(
                        complaint.getProofImage()
                );
    }
}