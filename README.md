# Secure Application Design

**Author:** Daniel Rodriguez  
**Course:** TDSE 2026-1  
**Date:** March 2026

---

## Overview

This project implements a secure web application using **HTTPS**, **TLS certificates**, **Spring Boot**, and **AWS EC2**. It demonstrates secure communication between two independent backend services deployed on separate servers, following the **12-Factor App** methodology.

---

## Architecture

```
Browser (HTTPS 🔒)
       │
       ▼
┌──────────────────────────────────┐
│  EC2 - Apache Server             │
│  danieltdse.duckdns.org          │
│  login-service (Port 443)        │
│  Let's Encrypt Certificate ✅    │
│  Spring Boot + Spring Security   │
└──────────────┬───────────────────┘
               │ HTTPS (TLS)
               ▼
┌──────────────────────────────────┐
│  EC2 - SpringFramework Server    │
│  danieltdseother.duckdns.org     │
│  other-service (Port 443)        │
│  Let's Encrypt Certificate ✅    │
│  Spring Boot REST API            │
└──────────────────────────────────┘
```

### Security Features

- HTTPS with Let's Encrypt certificates on both servers
- HTTP Basic Authentication for user login
- TLS encryption for inter-service communication
- Passwords managed via environment variables (12-Factor App)
- Self-signed certificates generated with Java Keytool (PKCS12 format)

---

## Project Structure

```
Secure-Application-Design/
├── login-service/
│   ├── src/
│   │   └── main/
│   │       ├── java/co/escuelaing/edu/login/
│   │       │   ├── Application.java
│   │       │   ├── HelloController.java
│   │       │   ├── SecurityConfig.java
│   │       │   └── SecureServiceCaller.java
│   │       └── resources/
│   │           ├── application.properties
│   │           └── keystores/
│   │               ├── loginkeystore.p12       ← (not in repo, generated locally)
│   │               └── loginTrustStore.p12     ← (not in repo, generated locally)
│   └── pom.xml
│
├── other-service/
│   ├── src/
│   │   └── main/
│   │       ├── java/co/escuelaing/edu/other/
│   │       │   ├── Application.java
│   │       │   └── OtherController.java
│   │       └── resources/
│   │           ├── application.properties
│   │           └── keystores/
│   │               ├── otherkeystore.p12       ← (not in repo, generated locally)
│   │               └── otherTrustStore.p12     ← (not in repo, generated locally)
│   └── pom.xml
│
├── pom.xml                                     ← Multi-module Maven root
├── .gitignore
└── README.md
```

---

## Prerequisites

- Java 17+
- Maven 3.8+
- AWS Account with two EC2 instances (t3.micro)
- DuckDNS domains pointing to each EC2 instance
- Port 443 open in Security Groups

---

## Local Setup

### 1. Clone the repository

```bash
git clone https://github.com/DaniAleRoB/Secure-Application-Design.git
cd Secure-Application-Design
```

### 2. Create keystore directories

```bash
mkdir -p login-service/src/main/resources/keystores
mkdir -p other-service/src/main/resources/keystores
```

### 3. Generate key pairs and certificates

```bash
# Generate login-service keystore
keytool -genkeypair -alias loginkeypair -keyalg RSA -keysize 2048 \
  -storetype PKCS12 \
  -keystore login-service/src/main/resources/keystores/loginkeystore.p12 \
  -validity 3650

# Generate other-service keystore
keytool -genkeypair -alias otherkeypair -keyalg RSA -keysize 2048 \
  -storetype PKCS12 \
  -keystore other-service/src/main/resources/keystores/otherkeystore.p12 \
  -validity 3650
```

### 4. Export certificates

```bash
keytool -export \
  -keystore login-service/src/main/resources/keystores/loginkeystore.p12 \
  -alias loginkeypair -file logincert.cer

keytool -export \
  -keystore other-service/src/main/resources/keystores/otherkeystore.p12 \
  -alias otherkeypair -file othercert.cer
```

### 5. Create cross TrustStores

```bash
keytool -import -file othercert.cer -alias otherCA \
  -keystore login-service/src/main/resources/keystores/loginTrustStore.p12 \
  -storetype PKCS12

keytool -import -file logincert.cer -alias loginCA \
  -keystore other-service/src/main/resources/keystores/otherTrustStore.p12 \
  -storetype PKCS12
```

### 6. Run locally (two terminals)

**Terminal 1 — other-service:**
```bash
cd other-service
mvn spring-boot:run
```

**Terminal 2 — login-service:**
```bash
cd login-service
mvn spring-boot:run
```

### 7. Test locally

| URL | Description |
|-----|-------------|
| `https://localhost:5000/` | Login service greeting |
| `https://localhost:5000/hello` | Protected endpoint (requires auth) |
| `https://localhost:5000/call` | Calls other-service securely |
| `https://localhost:6000/data` | Other service data |

Credentials: `admin` / `secret`

---

## AWS Deployment

### Infrastructure

| Instance | Type | Service | Domain |
|----------|------|---------|--------|
| ApacheServer | t3.micro | login-service | danieltdse.duckdns.org |
| SpringFramework | t3.micro | other-service | danieltdseother.duckdns.org |

### Security Group Rules (each instance)

| Type | Port | Source |
|------|------|--------|
| SSH | 22 | Your IP |
| HTTP | 80 | 0.0.0.0/0 |
| HTTPS | 443 | 0.0.0.0/0 |

### 1. Install dependencies on both EC2 instances

```bash
sudo dnf install java-17-amazon-corretto maven git -y
```

### 2. Clone the repository on each EC2

```bash
git clone https://github.com/DaniAleRoB/Secure-Application-Design.git
cd Secure-Application-Design
```

### 3. Copy keystores to each EC2 (from local machine)

**Apache EC2 (login-service):**
```bash
scp -i "ApacheServer.pem" login-service/src/main/resources/keystores/loginkeystore.p12 \
  ec2-user@<APACHE_IP>:~/Secure-Application-Design/login-service/src/main/resources/keystores/

scp -i "ApacheServer.pem" login-service/src/main/resources/keystores/loginTrustStore.p12 \
  ec2-user@<APACHE_IP>:~/Secure-Application-Design/login-service/src/main/resources/keystores/
```

**SpringFramework EC2 (other-service):**
```bash
scp -i "SpringFramework.pem" other-service/src/main/resources/keystores/otherkeystore.p12 \
  ec2-user@<SPRING_IP>:~/Secure-Application-Design/other-service/src/main/resources/keystores/

scp -i "SpringFramework.pem" other-service/src/main/resources/keystores/otherTrustStore.p12 \
  ec2-user@<SPRING_IP>:~/Secure-Application-Design/other-service/src/main/resources/keystores/
```

### 4. Install Let's Encrypt on both EC2 instances

```bash
sudo dnf install python3-devel augeas-devel gcc -y
sudo python3 -m venv /opt/certbot/
sudo /opt/certbot/bin/pip install --upgrade pip
sudo /opt/certbot/bin/pip install certbot
sudo ln -s /opt/certbot/bin/certbot /usr/local/bin/certbot
```

**Apache EC2:**
```bash
# Stop Apache to free port 80
sudo systemctl stop httpd
sudo certbot certonly --standalone -d danieltdse.duckdns.org
```

**SpringFramework EC2:**
```bash
sudo certbot certonly --standalone -d danieltdseother.duckdns.org
```

### 5. Convert Let's Encrypt certificates to PKCS12

**Apache EC2:**
```bash
sudo openssl pkcs12 -export \
  -in /etc/letsencrypt/live/danieltdse.duckdns.org/fullchain.pem \
  -inkey /etc/letsencrypt/live/danieltdse.duckdns.org/privkey.pem \
  -out ~/Secure-Application-Design/login-service/src/main/resources/keystores/loginkeystore.p12 \
  -name loginkeypair -passout pass:123456
```

**SpringFramework EC2:**
```bash
sudo openssl pkcs12 -export \
  -in /etc/letsencrypt/live/danieltdseother.duckdns.org/fullchain.pem \
  -inkey /etc/letsencrypt/live/danieltdseother.duckdns.org/privkey.pem \
  -out ~/Secure-Application-Design/other-service/src/main/resources/keystores/otherkeystore.p12 \
  -name otherkeypair -passout pass:123456
```

### 6. Run services on port 443

**SpringFramework EC2 (start first):**
```bash
cd ~/Secure-Application-Design/other-service
sudo PORT=443 nohup mvn spring-boot:run &
```

**Apache EC2:**
```bash
cd ~/Secure-Application-Design/login-service
sudo PORT=443 mvn spring-boot:run
```

### 7. Test on AWS

| URL | Description |
|-----|-------------|
| `https://danieltdse.duckdns.org/` | Login service (no auth) |
| `https://danieltdse.duckdns.org/hello` | Protected endpoint |
| `https://danieltdse.duckdns.org/call` | Calls other-service |
| `https://danieltdseother.duckdns.org/data` | Other service data |

---

## Environment Variables (12-Factor App)

| Variable | Description | Default |
|----------|-------------|---------|
| `PORT` | Server port | `5000` (login), `6000` (other) |
| `KEYSTORE_PASSWORD` | Keystore password | `123456` |
| `TRUSTSTORE_PASSWORD` | TrustStore password | `123456` |
| `TRUSTSTORE_PATH` | TrustStore file path | classpath default |

---

## API Endpoints

### login-service (port 443 / 5000)

| Method | Endpoint | Auth Required | Description |
|--------|----------|---------------|-------------|
| GET | `/` | No | Service greeting |
| GET | `/hello` | Yes | Protected hello |
| GET | `/call` | Yes | Calls other-service |

### other-service (port 443 / 6000)

| Method | Endpoint | Auth Required | Description |
|--------|----------|---------------|-------------|
| GET | `/data` | No | Returns secure data |

---

## Technologies

- **Java 17** — Programming language
- **Spring Boot 3.2** — Application framework
- **Spring Security** — Authentication and authorization
- **Maven** — Build and dependency management
- **Keytool** — Certificate generation (PKCS12)
- **Let's Encrypt / Certbot** — Public TLS certificates
- **AWS EC2** — Cloud deployment (t3.micro)
- **DuckDNS** — Dynamic DNS for domains
- **Apache HTTP Server** — Web server on EC2

---

## Notes

- `.p12` and `.cer` files are excluded from the repository via `.gitignore` for security reasons. Generate them locally following the setup instructions.
- The `other-service` must be started before `login-service`.
- When restarting EC2 instances, update DuckDNS with the new public IP.
- Let's Encrypt certificates expire after 90 days. Set up auto-renewal with:

```bash
echo "0 0,12 * * * root /opt/certbot/bin/python -c 'import random; import time; time.sleep(random.random() * 3600)' && sudo certbot renew -q" | sudo tee -a /etc/crontab > /dev/null
```