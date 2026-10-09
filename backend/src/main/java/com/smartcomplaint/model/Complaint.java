package com.smartcomplaint.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "complaints")
public class Complaint {

    // =========================================================
    // PRIMARY DATABASE ID
    // =========================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =========================================================
    // PUBLIC COMPLAINT ID
    // Example: CMP-2E1BE4BB
    // =========================================================

    @Column(
        name = "complaint_id",
        unique = true,
        nullable = false
    )
    private String complaintId;


    // =========================================================
    // USER ID
    // Used to show complaints belonging to a specific user
    // =========================================================

    @Column(
        name = "user_id",
        nullable = false
    )
    private String userId;


    // =========================================================
    // COMPLAINT TYPE
    // =========================================================

    @Column(
        name = "type",
        nullable = false
    )
    private String type;


    // =========================================================
    // COMPLAINT DESCRIPTION
    // =========================================================

    @Column(
        name = "description",
        columnDefinition = "TEXT"
    )
    private String description;


    // =========================================================
    // CONTACT INFORMATION
    // =========================================================

    @Column(name = "contact")
    private String contact;


    // =========================================================
    // STAFF ASSIGNED BY ADMIN
    // =========================================================

    @Column(name = "assigned_staff")
    private String assignedStaff;


    // =========================================================
    // COMPLAINT STATUS
    // =========================================================

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ComplaintStatus status = ComplaintStatus.PENDING;


    // =========================================================
    // LOCATION
    // =========================================================

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;


    // =========================================================
    // LOCATION ADDRESS
    // =========================================================

    @Column(
        name = "address",
        columnDefinition = "TEXT"
    )
    private String address;


    // =========================================================
    // PROOF PHOTO INFORMATION
    // =========================================================

    @Column(name = "proof_file_name")
    private String proofFileName;

    @Column(name = "proof_content_type")
    private String proofContentType;


    // =========================================================
    // PROOF IMAGE
    // Stored as BLOB in MySQL
    // =========================================================

    @Lob
    @Column(
        name = "proof_image",
        columnDefinition = "LONGBLOB"
    )
    private byte[] proofImage;


    // =========================================================
    // TIMESTAMPS
    // =========================================================

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    // =========================================================
    // BEFORE INSERT
    // =========================================================

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (status == null) {
            status = ComplaintStatus.PENDING;
        }
    }


    // =========================================================
    // BEFORE UPDATE
    // =========================================================

    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }


    // =========================================================
    // GETTERS
    // =========================================================

    public Long getId() {
        return id;
    }


    public String getComplaintId() {
        return complaintId;
    }


    public String getUserId() {
        return userId;
    }


    public String getType() {
        return type;
    }


    public String getDescription() {
        return description;
    }


    public String getContact() {
        return contact;
    }


    public String getAssignedStaff() {
        return assignedStaff;
    }


    public ComplaintStatus getStatus() {
        return status;
    }


    public Double getLatitude() {
        return latitude;
    }


    public Double getLongitude() {
        return longitude;
    }


    public String getAddress() {
        return address;
    }


    public String getProofFileName() {
        return proofFileName;
    }


    public String getProofContentType() {
        return proofContentType;
    }


    public byte[] getProofImage() {
        return proofImage;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }


    // =========================================================
    // SETTERS
    // =========================================================

    public void setComplaintId(String complaintId) {
        this.complaintId = complaintId;
    }


    public void setUserId(String userId) {
        this.userId = userId;
    }


    public void setType(String type) {
        this.type = type;
    }


    public void setDescription(String description) {
        this.description = description;
    }


    public void setContact(String contact) {
        this.contact = contact;
    }


    public void setAssignedStaff(String assignedStaff) {
        this.assignedStaff = assignedStaff;
    }


    public void setStatus(ComplaintStatus status) {
        this.status = status;
    }


    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }


    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }


    public void setAddress(String address) {
        this.address = address;
    }


    public void setProofFileName(String proofFileName) {
        this.proofFileName = proofFileName;
    }


    public void setProofContentType(String proofContentType) {
        this.proofContentType = proofContentType;
    }


    public void setProofImage(byte[] proofImage) {
        this.proofImage = proofImage;
    }
}