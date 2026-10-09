package com.smartcomplaint.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaints")
public class Complaint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String complaintId;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false, length = 4000)
    private String description;

    @Column(nullable = false)
    private String contact;

    private String assignedStaff;

    @Enumerated(EnumType.STRING)
    private ComplaintStatus status;

    private Double latitude;
    private Double longitude;

    @Column(length = 1500)
    private String address;

    @Column(length = 255)
    private String proofFileName;

    @Column(length = 100)
    private String proofContentType;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] proofImage;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) status = ComplaintStatus.PENDING;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getComplaintId() { return complaintId; }
    public void setComplaintId(String v) { complaintId = v; }
    public String getType() { return type; }
    public void setType(String v) { type = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { description = v; }
    public String getContact() { return contact; }
    public void setContact(String v) { contact = v; }
    public String getAssignedStaff() { return assignedStaff; }
    public void setAssignedStaff(String v) { assignedStaff = v; }
    public ComplaintStatus getStatus() { return status; }
    public void setStatus(ComplaintStatus v) { status = v; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double v) { latitude = v; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double v) { longitude = v; }
    public String getAddress() { return address; }
    public void setAddress(String v) { address = v; }
    public String getProofFileName() { return proofFileName; }
    public void setProofFileName(String v) { proofFileName = v; }
    public String getProofContentType() { return proofContentType; }
    public void setProofContentType(String v) { proofContentType = v; }
    public byte[] getProofImage() { return proofImage; }
    public void setProofImage(byte[] v) { proofImage = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
