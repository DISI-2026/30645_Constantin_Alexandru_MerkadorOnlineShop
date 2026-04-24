## Arhitectura Proiectului Merkador

Acesta este un sistem **microservices** containerizat cu API Gateway și mesagerie asincronă.

### 🏗️ **Componente Principale**

```
┌──────────────┐
│   Frontend   │ (React + Vite, port 5173)
│  (Nginx)     │
└──────┬───────┘
       │
       ▼
┌─────────────────────────────────────┐
│      TRAEFIK (API Gateway)          │
│   HTTP 80 → Middleware Stack        │
│  (JWT Auth, CORS, Route Stripping)  │
└─────────┬───────────────────────────┘
          │
    ┌─────┴──────┐
    ▼            ▼
┌─────────────┐ ┌──────────────┐
│  User MS    │ │Credential MS │
│  (8082)     │ │   (8081)     │
│ Spring Boot │ │ Spring Boot  │
└─────┬───────┘ └──────┬───────┘
      │                │
      └────┬──────┬────┘
           ▼      ▼
      ┌──────────────┐
      │  PostgreSQL  │
      │  (2 DB-uri) │
      └──────────────┘
      
      ┌──────────────┐
      │  RabbitMQ    │ (Message Broker)
      │  (5672 + UI) │
      └──────────────┘
```

---

### 🛠️ **Tehnologii Utilizate**

| Componenta | Tehnologie | Rol |
|----------|-----------|-----|
| **Backend** | Spring Boot 4.0 (Java) | 2 microservices |
| **Frontend** | React 19 + React Router | SPA cu autentificare |
| **Gateway** | Traefik v3.6.2 | Routing, middleware auth |
| **Message Queue** | RabbitMQ 3.12 | Async sync între servicii |
| **Database** | PostgreSQL (2 instanțe) | `user_db` + `credential_db` |
| **Container** | Docker + docker-compose | Orchestrare |
| **HTTP Client** | Fetch API (wrapper custom) | Comunicare frontend-backend |

---

### 📨 **Sistem de Queue-uri (RabbitMQ)**

**Topic Exchange Pattern:** `user-synchronization-exchange`

#### **Fluxul Data:**

1. **User Management MS (Producer)**
   - `UserSyncProducer` trimite evento la User creare/updare/ștergere
   - Routing keys: `user.created`, `user.updated`, `user.deleted`
   - Payload: `{ userId, userJsonDetails }`

2. **Message Queue**
   - Exchange: `user-synchronization-exchange` (TopicExchange)
   - Queue: `credentials-queue` (durable)
   - Binding: `user.#` (wild card pentru toate event-urile user)

3. **Credential Management MS (Consumer)**
   - `UserSyncConsumer` ascultă pe `credentials-queue`
   - La fiecare eveniment:
     - `user.created` → înregistrare automată în `credential_db`
     - `user.updated` → sincronizare automată
     - `user.deleted` → ștergere automată

**Avantaj:** Decuplare între servicii; nici o dependență sincrona; alta MS nu trebuie să-i contacteze pe asta direct.

---

### 🔐 **Securitate - Pipeline Complet**

#### **1. API Gateway (Traefik)**

```yaml
Middleware Stack:
├─ strip-api          → Elimina `/api` prefix din path
├─ jwt-auth (JWT Plugin) → Validează Bearer token
│  ├─ Header: Authorization: Bearer <token>
│  ├─ Signing Method: HS256
│  └─ Secret: din env var JWT_SECRET
├─ cors-frontend      → Allow origin: http://localhost:5173
└─ Rute publice (fără JWT)
   ├─ /api/users/add (sign-up)
   └─ /api/credentials/login
```

#### **2. Backend (Spring Boot)**

**JWT Token Generation:**
- Endpoint: `POST /credentials/login`
- Input: `{ username, password }`
- Proces:
  1. `CredentialService.auth()` - validare username/password din DB
  2. `JwtService.generateToken()` - creare JWT pe 1h
  3. Response: `{ token, userId, username, role }`

**Token lifespan:** 1 ora (configurabil)

**Security Config:**
- AppConfig.java: `permitAll()` pentru toate cererile (validarea JWT se face la Traefik)
- CSRF disabled (API stateless)
- Spring Security activat doar ca dependency

#### **3. Frontend (React)**

**AuthContext Pattern:**
```javascript
Storage: localStorage 
├─ jwtToken      → Bearer token
├─ userRole      → "ADMIN" | "CLIENT"
├─ userId        → UUID
└─ userUsername   → string

fetchWrapper():
├─ Adaugă header: Authorization: Bearer <token>
├─ La 401 Unauthorized →
│  ├─ Șterge localStorage
│  └─ Redirecționează la /login
└─ Handlează alte erori HTTP
```

**Route Protection:**
- `ProtectedRoute.jsx` - redirecționează auth/unauth users
- Roluri: ADMIN (acces AdminPage) vs CLIENT (acces ClientPage)

---

### 🔄 **Fluxul Complet al unei Cereri**

**Exemplu: Sign-up (creare user)**

```
┌─ Frontend: POST /api/users/add
│  Headers: { Authorization: Bearer JWT }
│  Body: { fullName, email, username, password, role }
│
├─ Traefik
│  ├─ Strip /api → /users/add
│  ├─ Validează JWT (dacă public, skip)
│  └─ Route → User MS (8082)
│
├─ User MS: POST /users/add
│  ├─ UserController.createUser()
│  ├─ UserService.create() → salvează în user_db
│  ├─ UserSyncProducer.sendUserCreated()
│  │  └─ RabbitMQ: publish(exchange=user-sync, key="user.created", message)
│  └─ Return 201 + Location header
│
├─ RabbitMQ
│  ├─ Route message la credentials-queue
│  └─ Credential MS primește
│
├─ Credential MS: Consumer
│  ├─ UserSyncConsumer.receiveMessage()
│  ├─ Detectează key="user.created"
│  ├─ CredentialService.register(userId, userData)
│  └─ Salvează în credential_db automático
│
└─ Frontend: status 201 ✓
```

---

### 🎯 **Separarea Datelor**

| Database | Tabele | Responsabil |
|----------|--------|-------------|
| `user_db` | users | User MS |
| `credential_db` | credentials | Credential MS (sync via queue) |

Fiecare serviciu are propriul schema; sincronizări prin RabbitMQ, **zero direct DB access** între MS-uri.

---

### ⚙️ **Variabile de Mediu (docker-compose)**

```env
JWT_SECRET=your-secret-key-here
DB_IP=postgres
DB_PORT=5432
POSTGRES_USER=postgres
POSTGRES_PASSWORD=***
USER_DB=user_db
BROKER_HOST=rabbitmq
BROKER_PORT=5672
```

---

**Concluzie:** Arhitectură moderna, scalabilă cu separare clară de responsabilități, securitate la nivel de gateway + token JWT, și decuplare prin mesagerie asincrona. Ușor de orizontal scale prin Docker.