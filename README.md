Projet Microservices : Gestion de DoctoratCe projet démontre une architecture microservices complète basée sur Spring Boot et Spring Cloud pour gérer un processus d'inscription et de soutenance de doctorat.L'architecture met en œuvre la découverte de services, la configuration centralisée, un point d'entrée unique (Gateway), la sécurité (JWT) et la tolérance aux pannes.🏗️ Architecture des ComposantsVoici la nouvelle architecture du système :             [ Client (Postman/Navigateur) ]
                        |
                        v
+-----------------------+------------------------+
|           [ 🔒 API Gateway (Port: 8080) ]        |
| (Routage, Authentification JWT, Load Balancing)  |
+-----------------------+------------------------+
                        |
      (Appels API REST via Eureka Load Balancing)
                        |
+-----------+-----------+-----------+
|           |           |           |
v           v           v           v
[ User Svc ] [ Inscription Svc ] [ Soutenance Svc ] [ Config Server ]
(Port: 8081)  (Port: 8082)      (Port: 8084)       (Port: 8888)
     ^            |                                      |
     |            +----(Appel Feign)---------------------+
     |                     | (Resilience4J)
     +---------------------+
                                                       |
+------------------------------------------------------+
| [ 🔄 Eureka Server (Port: 8761) ]                    |
| (Tous les services s'enregistrent ici)               |
+------------------------------------------------------+
📦 Composants du ProjetServicePortRôleConfigServer8888Centralise la configuration de tous les microservices.Eureka-Server8761Service de découverte (annuaire).gateway-service8080Point d'entrée unique (API Gateway), sécurise les routes.user-service8081Gère les utilisateurs, l'inscription et l'authentification JWT1.inscription-service8082Gère les dossiers d'inscription et de réinscription.soutenance-service8084Gère les demandes de soutenance, le jury et la planification.🚀 Guide de DémarrageL'ordre de démarrage est critique. Vous devez démarrer les services d'infrastructure en premier.Étape 1 : Démarrer l'Infrastructure (Config & Eureka)Bash# Terminal 1 - Config Server (DOIT être démarré en premier)
cd ConfigServer
mvn spring-boot:run

# Terminal 2 - Eureka Server
cd Eureka-Server
mvn spring-boot:run
Étape 2 : Démarrer les Microservices MétierVous pouvez les démarrer dans n'importe quel ordre, après Eureka et Config.Bash# Terminal 3 - User Service
cd user-service
mvn spring-boot:run

# Terminal 4 - Inscription Service
cd inscription-service
mvn spring-boot:run

# Terminal 5 - Soutenance Service
cd soutenance-service
mvn spring-boot:run
Étape 3 : Démarrer la Gateway (en dernier)La Gateway a besoin que les autres services soient enregistrés dans Eureka pour créer ses routes.Bash# Terminal 6 - API Gateway
cd gateway-service
mvn spring-boot:run
🧪 Collection de Tests (API)Tous les tests se font via la Gateway sur le port 8080.Test 1 : Authentification (JWT)La plupart des routes sont maintenant sécurisées. Vous devez d'abord vous authentifier.1. Créez un compte utilisateur :(Note : La route /register est publique 2)Bashcurl -X POST http://localhost:8080/api/v1/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "doctorant.test@univ.ma",
    "password": "password123",
    "firstName": "Test",
    "lastName": "Doctorant",
    "phone": "0600000001",
    "role": "DOCTORANT"
  }'
2. Connectez-vous pour obtenir un Token JWT :(Note : La route /login est publique 3)Bash# Récupère le token et le stocke dans une variable $TOKEN
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "doctorant.test@univ.ma",
    "password": "password123"
  }' | jq -r '.token')

echo "Token obtenu: $TOKEN"
Test 2 : Test de Routage de Base (Protégé)Utilisez le $TOKEN obtenu à l'étape précédente pour authentifier votre requête.Bash# Récupérer tous les utilisateurs
curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8080/api/v1/users

# Récupérer toutes les inscriptions
curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8080/api/v1/inscriptions
Test 3 : Créer une Inscription (Protégé)Bashcurl -X POST http://localhost:8080/api/v1/inscriptions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "doctorantId": "DOC999",
    "directeurId": "DIR001",
    "type": "INSCRIPTION_INITIALE",
    "anneeAcademique": "ANNEE_2025_2026",
    "sujetThese": "Test via Gateway",
    "laboratoire": "Lab Test",
    "specialite": "Informatique"
  }'
Test 4 : Créer une Demande de Soutenance (Protégé)Bashcurl -X POST http://localhost:8080/api/v1/soutenances \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "doctorantId": "DOC999",
    "inscriptionId": "INS12345",
    "nbArticlesQ1Q2": 2,
    "nbConferences": 3,
    "nbCreditsFormation": 210
  }'
Test 5 : Test de Résilience (Resilience4J) 🛡️Ce test simule une panne du user-service pour vérifier que le inscription-service ne tombe pas en panne (grâce au Circuit Breaker).1. Assurez-vous que tout fonctionne :Lancez le curl du Test 3 (Créer une Inscription). Il doit réussir et renvoyer les détails du doctorant.2. Simulez la panne :Arrêtez le user-service (le Terminal 3).3. Relancez la création d'inscription :Lancez à nouveau le curl du Test 3.Observation : La requête va prendre quelques secondes (le Retry essaie 3 fois).Résultat : La requête va réussir (Code 201), mais la réponse sera en "mode dégradé" (Fallback). Vous verrez les valeurs par défaut que nous avons définies :JSON{
  "success": true,
  "message": "Inscription créée avec succès",
  "data": {
    "id": "...",
    "doctorantEmail": "email@inconnu.ma",
    "doctorantName": "Doctorant (Service indisponible)",
    "directeurName": "Directeur (Service indisponible)",
    "status": "SOUMISE",
    // ...
  }
}
4. Vérifiez l'état du Circuit Breaker :Accédez à l'endpoint Actuator du inscription-service (sur son port direct 8082).Bashcurl http://localhost:8082/actuator/circuitbreakers
Résultat : Vous verrez l'état du userServiceCB passer à "OPEN". Le service est protégé !
