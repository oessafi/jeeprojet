# TP1 - Spring Cloud Gateway 

## 🎯 Objectif du TP

Démontrer le fonctionnement de **Spring Cloud Gateway** comme point d'entrée unique pour une architecture microservices, sans utiliser Eureka, Redis ou Resilience4j.

## 🏗️ Architecture

```
┌──────────────────────┐
│  Client (Postman)    │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│   API Gateway        │
│   Port: 8080         │
└──────────┬───────────┘
           │
     ┌─────┴─────┐
     │           │
     ▼           ▼
┌─────────┐ ┌───────────────┐
│  User   │ │ Inscription   │
│ Service │ │   Service     │
│  8081   │ │    8082       │
└─────────┘ └───────────────┘
```

## 📦 Structure des Projets

```
doctorat-microservices/
├── user-service/           (Port 8081)
├── inscription-service/    (Port 8082)
└── api-gateway/           (Port 8080)
```

## 🚀 Démarrage

### Étape 1 : Démarrer les microservices

```bash
# Terminal 1 - User Service
cd user-service
mvn spring-boot:run

# Terminal 2 - Inscription Service
cd inscription-service
mvn spring-boot:run
```

### Étape 2 : Démarrer la Gateway

```bash
# Terminal 3 - API Gateway
cd api-gateway
mvn spring-boot:run
```

### Étape 3 : Vérifier que tout fonctionne

```bash
# Health check de la gateway
curl http://localhost:8080/actuator/health

# Health check des services via la gateway
curl http://localhost:8080/health/users
curl http://localhost:8080/health/inscriptions

# Voir les routes configurées
curl http://localhost:8080/actuator/gateway/routes | json_pp
```

## 📋 Routes Disponibles

### Via la Gateway (Port 8080)

| Endpoint Gateway | Service Cible | Endpoint Service |
|-----------------|---------------|------------------|
| `GET /api/v1/users` | User Service | `GET /users` |
| `POST /api/v1/users` | User Service | `POST /users` |
| `GET /api/v1/inscriptions` | Inscription Service | `GET /inscriptions` |
| `POST /api/v1/inscriptions` | Inscription Service | `POST /inscriptions` |

## 🧪 Tests à Effectuer

### Test 1 : Routage de Base (User Service)

```bash
# Via la Gateway
curl http://localhost:8080/api/v1/users

# Direct (pour comparaison)
curl http://localhost:8081/users
```

**Observation** : Les deux retournent la même liste d'utilisateurs.

### Test 2 : Routage vers Inscription Service

```bash
# Via la Gateway
curl http://localhost:8080/api/v1/inscriptions

# Direct
curl http://localhost:8082/inscriptions
```

### Test 3 : Créer un utilisateur via la Gateway

```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test.gateway@univ.ma",
    "firstName": "Test",
    "lastName": "Gateway",
    "phone": "0612345678",
    "role": "CANDIDAT"
  }'
```

### Test 4 : Créer une inscription via la Gateway

```bash
curl -X POST http://localhost:8080/api/v1/inscriptions \
  -H "Content-Type: application/json" \
  -d '{
    "doctorantId": "DOC999",
    "directeurId": "DIR001",
    "type": "INSCRIPTION_INITIALE",
    "anneeAcademique": "ANNEE_2025_2026",
    "sujetThese": "Test via Gateway",
    "laboratoire": "Lab Test",
    "specialite": "Informatique"
  }'
```

### Test 5 : Vérifier les Headers Ajoutés par la Gateway

```bash
# Regarder les headers de la réponse
curl -v http://localhost:8080/api/v1/users

# Vous devriez voir:
# X-Gateway-Response: user-service
# X-Request-Id: <un UUID>
```

### Test 6 : Filtrage par Rôle

```bash
# Récupérer tous les doctorants
curl http://localhost:8080/api/v1/users/doctorants

# Récupérer tous les candidats
curl http://localhost:8080/api/v1/users/candidats
```

### Test 7 : Validation d'une Inscription

```bash
# 1. Créer une inscription
INSCRIPTION_ID=$(curl -X POST http://localhost:8080/api/v1/inscriptions \
  -H "Content-Type: application/json" \
  -d '{
    "doctorantId": "DOC100",
    "directeurId": "DIR001",
    "type": "INSCRIPTION_INITIALE",
    "anneeAcademique": "ANNEE_2025_2026",
    "sujetThese": "Test Validation",
    "laboratoire": "Lab",
    "specialite": "Info"
  }' | jq -r '.data.id')

# 2. Valider par le directeur
curl -X PUT http://localhost:8080/api/v1/inscriptions/$INSCRIPTION_ID/validate/directeur \
  -H "Content-Type: application/json" \
  -H "X-User-Role: ADMIN" \
  -d '{
    "approved": true,
    "commentaire": "Bon projet, approuvé"
  }'
```
