# MultiverreReservationApp

Application Android de réservation de jeux de société et de tables pour le bar à jeux **Multiverre**. Elle permet aux utilisateurs de réserver facilement, et aux administrateurs de gérer les jeux et les disponibilités.

## Fonctionnalités principales

###  Authentification
- Connexion avec :
  - Email / Mot de passe
  - Google
  - Facebook

###  Réservations
- Réserver une **table** (nombre de personnes, date, heure)
- Réserver un **jeu de société** (jeu, nombre de joueurs, date, heure)
- Partage de la réservation par **SMS**, **email**, ou **WhatsApp**
- Notification locale de confirmation

###  Admin Panel
- Ajout, modification et suppression de jeux
- Gestion du stock
- Statut administrateur géré via Firestore

###  Notifications
- Envoi de notification locale lors d’une réservation réussie (Android 13+ avec permissions)

---

##  Structure du projet

app/
├── ui/
│ ├── screens/ // Écrans de l'application
│ ├── components/ // Composants UI réutilisables
│ └── navigation/ // Navigation entre écrans
├── model/ // Modèles de données (BoardGame, Reservation)
├── viewmodel/ // ViewModels pour logique UI et Firestore
└── MainActivity.kt // Initialisation des SDK et navigation

## 🛡 Sécurité Firestore

### 🔐 Règles Firestore configurées :
```js
// Accès aux jeux pour tous les utilisateurs connectés
match /boardGames/{gameId} {
  allow read: if request.auth != null;
  allow write: if isAdmin();
}

// Réservations : lecture et création réservées à l'utilisateur connecté
match /reservations/{reservationId} {
  allow create: if request.auth != null && request.resource.data.userId == request.auth.uid;
  allow read, update, delete: if resource.data.userId == request.auth.uid;
}

// Accès utilisateur limité à ses propres données
match /users/{userId} {
  allow read, write: if request.auth.uid == userId;
}

// Helper
function isAdmin() {
  return get(/databases/$(database)/documents/users/$(request.auth.uid)).data.isAdmin == true;
}
Lancer le projet
Prérequis :
Android Studio Arctic Fox ou +

Firebase project avec :

Authentication (Google, Facebook, Email/Password)

Firestore Database

SHA-1 ajouté pour login Google

Configuration :
Clone le repo

Remplir google-services.json (Firebase)

Remplir les IDs Facebook dans strings.xml

Ajouter la permission Android 13 :

xml
Copier
Modifier
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
Lancer sur un simulateur ou appareil Android

Contact
Pour toute question, contactez themultiverre@gmail.com

