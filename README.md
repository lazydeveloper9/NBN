# NBN

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![License](https://img.shields.io/badge/license-MIT-green.svg)
![Platform](https://img.shields.io/badge/platform-Android-3DDC84.svg)

> **Democratizing enterprise-grade digital infrastructure for Micro, Small, and Medium Enterprises (MSMEs).**

The **"NBN"** is an agentic AI-driven Android application that effortlessly handles operations, marketing, and digital presence with near-zero technical knowledge required from the business owner. Instead of complex dashboards, users interact with an AI orchestrator that translates high-level business goals into autonomous actions across web deployment, inventory management, and customer communications.

---

## ✨ Key Features

### 🤖 The Multi-Agent System
Instead of a single monolithic AI, the app utilizes a specialized team of autonomous agents:

* **The Orchestrator Agent (The Interface):** The primary conversational interface. It translates natural language (e.g., "I need a website to sell my new clay pots") into technical commands.
* **DevOps Agent:** Automatically deploys E-Commerce PWAs, manages domains, maintains logs, and handles server auto-scaling.
* **Ops/ERP Agent:** Manages inventory databases, syncs stock across physical and digital storefronts, and utilizes Vision AI to parse new stock photos.
* **Comms Agent:** Connects to WhatsApp Business API, SMS, and Email to handle autonomous order confirmations, abandoned carts, and marketing blasts.

### 📦 Zero-to-Online Deployment
* Conversational onboarding to generate brand profiles and product catalogs.
* One-click, host-free E-Commerce Progressive Web App (PWA) deployment.

### 📱 Autonomous Order & Inventory Management
* **WhatsApp Bot Integration:** Customers order via WhatsApp; the bot checks inventory, creates the order, and sends payment links autonomously.
* **Smart Vault (Inventory):** A live-synced inventory grid. Use the built-in Android camera to snap photos of new shipments; the app encodes it to base64, pushes it to the Python backend, and the Ops Agent uses Vision AI to identify, count, and update the SQLite/PostgreSQL database instantly. Includes instant 1-click supply ordering from Dealers directly in the app.

---

## 🛠️ Technology Stack

### Mobile App (The Remote Control)
* **Language & UI:** Kotlin, Jetpack Compose
* **Local Storage:** Room Database (Offline-first architecture)

### Backend & Infrastructure (The Brain)
* **Architecture:** Serverless (AWS Lambda / Google Cloud Functions)
* **Database:** PostgreSQL (Relational ERP) + Pinecone/Weaviate (Vector DB for AI memory)

### AI & Agentic Framework
* **LLM Engine:** Gemini 2.5 Flash Lite (LangChain `ChatGoogleGenerativeAI`)
* **Agent Framework:** LangChain / LlamaIndex
* **Communications:** Twilio / Meta WhatsApp Cloud API

---

## 🚀 Getting Started

*(Note: These are placeholder instructions for the development environment)*

### Installation & Running Locally

#### 1. Backend (FastAPI & LangGraph)
Ensure you have `uv` (the fast Python package installer) installed.
```bash
cd backend
uv sync # Install dependencies
```

Create a `.env` file in the `backend/` directory:
```env
GEMINI_API_KEY=your_api_key_here
```

Run the backend server:
```bash
uv run uvicorn main:app --reload --host 0.0.0.0
```

#### 2. Android App (Jetpack Compose)
Open `android-app` in Android Studio.

If you are running the app on a physical Android device connected via USB, you must bridge the port so the app can talk to `localhost:8000`:
```bash
adb reverse tcp:8000 tcp:8000
```
Build and run the app from Android Studio!