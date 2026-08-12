# Ascend Invest - Website Implementation

This folder contains the React-based website and a Node.js SQL-based backend for the Ascend Invest application.

## Structure
- `/src`: React frontend (Vite + TypeScript + Tailwind CSS)
- `/server`: Node.js backend (Express + Knex + MySQL/SQL)

## Prerequisites
- Node.js installed
- MySQL or MariaDB server running (for the SQL backend)

## How to Run

### 1. Setup the Backend (SQL)
1. Navigate to the server folder:
   ```bash
   cd website/server
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Configure your database in `.env` file:
   ```env
   DB_HOST=localhost
   DB_USER=your_username
   DB_PASSWORD=your_password
   DB_NAME=ascend_invest
   JWT_SECRET=your_secret_key
   ```
4. Create the database `ascend_invest` in your SQL server.
5. Start the server:
   ```bash
   node index.js
   ```
   *The server will automatically create the required tables on the first run.*

### 2. Setup the Frontend (React)
1. Navigate to the website root:
   ```bash
   cd website
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the development server:
   ```bash
   npm run dev
   ```
4. Open your browser at `http://localhost:5173`

## Features
- **Strict UI Match**: The design uses the same color palette and layout structure as the Android app (Sidebar, Dashboard cards, etc.).
- **SQL Backend**: Unlike the app which uses Firebase (NoSQL), this website uses a relational SQL database.
- **Responsive Design**: Built with Tailwind CSS to ensure the UI looks great on all screens.
