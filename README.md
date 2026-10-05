# HostelCare

A Java/Spring Boot hostel maintenance website for students, wardens, and hostel aunties.

## What it does

- Accepts accounts using the college email domain `@citchennai.net`.
- Lets students submit a category, description, and JPG/PNG/WEBP photo (up to 5 MB).
- Shows each student only their own complaints and their progress through the workflow.
- Lets a warden accept a new complaint before it reaches the hostel aunty's work queue.
- Lets the hostel aunty schedule a visit after the warden accepts the complaint.
- Lets the student confirm that a scheduled repair is actually solved; the resolved status appears in the warden's queue.
- Stores accounts, complaints, and uploaded photos in a local H2 database.

## Requirements

- Java 21
- Internet access the first time you run the Maven wrapper

## Run

```powershell
.\mvnw.cmd spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080), create a college account, and sign in. Register separate accounts for the student, warden, and hostel aunty to try each part of the workflow.

To configure another exact email domain, set `COLLEGE_EMAIL_DOMAIN` before starting the app. For example, in PowerShell:

```powershell
$env:COLLEGE_EMAIL_DOMAIN = "citchennai.net"
.\mvnw.cmd spring-boot:run
```

The database is created in `./data/hostel-maintenance`. This is a classroom prototype: registration currently lets a college-domain user choose a role, and matching an email domain does not verify mailbox ownership. A deployed college system should provision staff roles through an administrator and use institutional email verification/SSO.
