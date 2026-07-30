# 💰 Family Wallet

Family Wallet is an Android application developed with Kotlin to help families manage their daily finances. The application enables parents to record income, distribute allowances to their children, and monitor family financial activities through a simple and integrated interface.

## ✨ Features

### 👨‍👩‍👧 Admin (Parent)

- Secure authentication
- Record family income
- Distribute allowances to children
- View total income, total allowances, and remaining balance
- Manage income records (Create, Read, Update, Delete)
- Manage children's allowance records (Create, Read, Update, Delete)
- View financial reports and transaction history
- Update profile photo using the camera or gallery
- Register a new admin account

### 👦 User (Child)

- Register and log in to an account
- Record income and expense transactions
- View current balance, total income, and total expenses
- Manage personal transactions (Create, Read, Update, Delete)
- View transaction history
- Update profile photo using the camera or gallery

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

## 🗄️ Database Structure

### Users

Stores user information, including:

- Name
- Email
- Profile picture
- User role (Admin/User)

### Transactions

Stores user financial transactions, including:

- Transaction type (Income/Expense)
- Category
- Amount
- Date
- Notes
- Location

### Admin Transactions

Stores financial data managed by parents, including:

- Income records
- Children's allowance distributions

---

## ☁️ Firebase Services

This project uses the following Firebase services:

- **Firebase Authentication** – User registration and authentication
- **Firebase Realtime Database** – Stores user profiles, transactions, income, and allowance data
- **Firebase Storage** – Stores profile images

---

## 🚀 Getting Started

1. Clone this repository.

```bash
git clone https://github.com/ys124xkd/Dompet_keluarga.git
```

2. Open the project in **Android Studio**.

3. Create your own Firebase project.

4. Add your `google-services.json` file to the project.

5. Sync the Gradle dependencies.

6. Build and run the application on an Android emulator or physical device.

---

## 📌 Important Note

The original Firebase project used during development is no longer available. To run this application successfully, configure your own Firebase project and replace the `google-services.json` file with your Firebase configuration.

---

## 📄 License

This project was developed for educational purposes and portfolio demonstration.
