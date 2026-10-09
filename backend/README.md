# Smart Complaint Management System — Backend

Spring Boot 3.3.5 + Java 17 + MySQL.

## Database
Create a MySQL database named `smart_complaint` or let `createDatabaseIfNotExist=true` create it.

Set environment variables from `.env.example`.

## Run
```powershell
mvn spring-boot:run
```

Backend: http://localhost:8080
Health: http://localhost:8080/health

User PIN: 1234
Admin password: admin123

Proof images are stored in MySQL as a BLOB (`proof_image`) with filename/content type metadata. The proof is retrieved at:
`GET /api/complaints/{complaintId}/proof`
