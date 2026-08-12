# Ascend Invest - Advanced P2P Investment & Trading Platform

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-orange.svg)](https://firebase.google.com)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**Ascend Invest** is a sophisticated fintech ecosystem designed to bridge the gap between traditional investment and decentralized peer-to-peer (P2P) trading. It provides a robust platform for users to manage investments, engage in direct asset transfers, and trade within a secure, real-time P2P marketplace.

---

## 🚀 Key Features

### 💎 Advanced P2P Marketplace
Experience a high-performance P2P trading engine that enables seamless asset exchange between users.
*   **Real-time Order Matching:** Automated listing and discovery of P2P trade offers.
*   **Secure Escrow System:** Built-in safeguards to ensure trust and transparency in every transaction.
*   **Integrated P2P Chat:** Encrypted, real-time communication between buyers and sellers to coordinate transfers directly within the app.
*   **Rating & Verification:** Community-driven trust metrics for secure trading environments.

### 💸 Direct Transfers & Wallet Management
*   **Instant Peer-to-Peer Transfers:** Send and receive funds/assets instantly using internal handles or QR codes.
*   **QR Integration:** Quick-scan functionality for error-free transactions.
*   **Transaction History:** Deep insights into every inflow and outflow with detailed status tracking.
*   **Wallet Security:** Advanced security managers to protect user assets and private data.

### 📈 Investment & Portfolio Management
*   **Dynamic Investment Plans:** Browse and subscribe to various investment tiers with real-time profit calculations.
*   **Live Market Data:** Interactive charts and visualizations powered by MPAndroidChart to track performance.
*   **Profit Distribution:** Automated background workers (WorkManager) for consistent and accurate profit payouts.

### 🛡️ Comprehensive Admin Suite
A dedicated module for administrators to maintain ecosystem health:
*   **User Oversight:** Complete management of user accounts, KYC, and security status.
*   **Market Regulation:** Monitor and moderate P2P listings and active chat sessions.
*   **Financial Control:** Review and approve/reject withdrawal and deposit requests.
*   **Content Management:** Update FAQs, announcements, and investment plan configurations on the fly.

---

## 🛠 Tech Stack

*   **Language:** Java / Kotlin (Android)
*   **UI Framework:** XML with Material Design 3 Components & ViewBinding
*   **Architecture:** Clean Architecture / MVVM (Model-View-ViewModel)
*   **Backend:** Firebase Realtime Database (Live updates), Firebase Auth
*   **Data Visualization:** MPAndroidChart
*   **Utilities:** ZXing (QR Scanning), WorkManager (Background Processing), Jetpack Navigation

---

## 📂 Project Structure

The project is divided into two primary modules:

*   `app/`: The client-facing application where users trade, invest, and manage their wallets.
*   `admin/`: The internal management tool for platform operators to handle support, transactions, and system settings.

---

## 📸 Screenshots

| P2P Marketplace | Portfolio Tracking | Admin Dashboard |
| :---: | :---: | :---: |
| *[Add Screenshot]* | *[Add Screenshot]* | *[Add Screenshot]* |

---

## ⚙️ Getting Started

### Prerequisites
*   Android Studio Ladybug or later.
*   JDK 11+
*   A Firebase project with `google-services.json` placed in both `app/` and `admin/` folders.

### Installation
1.  Clone the repository:
    ```bash
    git clone https://github.com/your-username/AscendInvest.git
    ```
2.  Open the project in Android Studio.
3.  Sync Gradle and ensure all dependencies are resolved.
4.  Configure your Firebase credentials in the console.
5.  Build and Run.

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

Developed with ❤️ by the Ascend Invest Team.
