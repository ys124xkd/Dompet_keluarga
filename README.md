# 💰 Family Wallet

Family Wallet is an Android application developed using **Kotlin** to help families manage their daily income and expenses. The application supports **multi-user authentication** with two roles: **Admin (Parent)** and **User (Child)**. Parents can record income, distribute allowances to their children, and monitor each child's financial activities, while children can manage their own income and expense transactions, track their balances, and view transaction history through an integrated financial management system.

---

## ✨ Features

### 👨‍👩‍👧 Admin (Parent)

- Secure admin authentication
- Record income
- Distribute allowances to children
- View total income, total allowances, and remaining balance
- Manage income records (Create, Read, Update, Delete)
- Manage children's allowance records (Create, Read, Update, Delete)
- View income history
- View each child's financial report and transaction history
- Update profile photo using the camera or gallery
- Register new admin accounts

### 👦 User (Child)

- Register and log in
- Record income and expense transactions
- View current balance, total income, total expenses, and received allowances
- Manage personal transactions (Create, Read, Update, Delete)
- View transaction history
- Update profile photo using the camera or gallery
- Upload transaction location images

---

## 🛠️ Technologies

- Kotlin
- Android Studio
- Firebase Authentication
- Firebase Realtime Database
- Firebase Storage
- RecyclerView
- Fragments
- Material Design

---

## 📱 Application Workflow

### Login & Registration

- Supports multi-user authentication.
- Registration is available for **User (Child)** accounts.
- During login:
  - **Admin** users are redirected to the **Admin Dashboard**.
  - **User** accounts are redirected to the **User Dashboard**.

### Admin Dashboard

The Admin Dashboard provides:

- Total income
- Total allowances distributed
- Remaining balance

Available actions:

- Add income or allowance
- Manage children's allowances
- View income history
- View children's financial reports
- Edit profile
- Register new admin accounts
- Logout

### User Dashboard

The User Dashboard provides:

- Total income
- Total expenses
- Total allowances received
- Current balance

Available actions:

- Add transactions
- Edit transactions
- Delete transactions
- View transaction history
- Edit profile
- Logout

---

## 🗄️ Firebase Database Structure

### Users

Stores user account information:

- Email
- Name
- Profile image
- User role (Admin/User)

### Transactions

Stores users' income and expense records:

- Amount
- Category
- Date
- Transaction ID
- Location image
- Note
- Transaction type
- User ID
- Username

### Transactions_Admin

Stores admin income and children's allowance records.

**Income**

- Amount
- Category
- Date
- Transaction ID
- Note

**Allowance**

- Amount
- Category
- Date
- Child's name
- Note
- User ID

---

## ☁️ Firebase Storage

Two folders are used:

- `images_profile/` – Stores profile images.
- `image_location/` – Stores transaction location images.

---

## 🔐 Firebase Authentication

Used for:

- User registration
- User authentication
- Admin authentication

---

## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/ys124xkd/Dompet_keluarga.git
```

### 2. Open the project

Open the project using **Android Studio**.

### 3. Configure Firebase

- Create your own Firebase project.
- Add the `google-services.json` file to the app module.

### 4. Sync Gradle

Sync the Gradle dependencies.

### 5. Run the application

Run the application on an Android emulator or a physical Android device.

---

## 📌 Important Note

The original Firebase project used during development is no longer available. To run this application successfully, create your own Firebase project and replace the `google-services.json` file with your Firebase configuration.

---

## 📄 License

This project was developed for educational and portfolio purposes.
