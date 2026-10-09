# Smart Complaint Management System — Full Stack

## Structure

- `frontend/` — React + Vite + Leaflet
- `backend/` — Spring Boot + MySQL + JPA

## Main features

### User
- PIN login (`1234`)
- Professional glass UI with floating animations
- Complaint submission
- Proof/evidence photo upload (max 10 MB)
- Free location picker using OpenStreetMap + Leaflet
- Browser GPS
- Automatic reverse-geocoded address
- Unique complaint ID
- Complaint status tracking

### Admin
- Password login (`admin123`, configurable)
- Glassmorphism control room
- Animated ambient background
- Dashboard statistics
- Search and status filters
- Staff assignment
- Status changes
- Location mini-maps
- Click **VIEW PROOF** to open the original uploaded evidence image

### Database
MySQL is used for complaint data. Proof images are stored as MySQL BLOBs with filename/content-type metadata. This makes the evidence part of the complaint record and lets the admin retrieve it through the proof endpoint.

For a production deployment, object storage such as S3/Cloudinary is usually more scalable for large media, while MySQL stores the URL/metadata. For a college/demo project, MySQL BLOB storage is straightforward.

## Start backend

Create a MySQL database/user and set the backend environment variables:

`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `ADMIN_PASSWORD`, `FRONTEND_ORIGIN`

Then:

```powershell
cd backend
mvn spring-boot:run
```

## Start frontend

Open a second terminal:

```powershell
cd frontend
npm install
npm run dev
```

Open:

`http://localhost:5173`

## Free map stack

Leaflet + OpenStreetMap tiles + Nominatim reverse geocoding. No Google Maps billing/API key is required for this demo. Follow the public service usage policies/rate limits.

## Demo credentials

User PIN: `1234`

Admin password: `admin123`
