👓 Glasses Store E-Commerce Platform
This project is a modern e-commerce platform for an eyewear store, built using a microservices architecture. It offers robust functionality for managing products, users, orders, reviews, and includes an AI-powered chatbot for enhanced customer support.

🏗️ System Architecture
The application is designed with a microservices architecture, comprising the following components:
🔧 Infrastructure Services

Eureka Server – Service discovery and registration  
API Gateway – Unified entry point for client requests with routing capabilities  
Kafka & Zookeeper – Event-driven communication between services  
Redis – Caching and session management  
MariaDB – Relational database for structured data  
MongoDB – Document storage for the chatbot service

🧩 Core Microservices

Auth Service – Manages authentication, authorization, and user security  
User Service – Handles user profile management  
Product Service – Manages the eyewear product catalog  
Order Service – Processes and tracks orders  
Cart Service – Provides shopping cart functionality  
Review Service – Manages product reviews and ratings  
Email Service – Sends email notifications  
Notification Service – Handles system-wide notifications  
Chatbot Service – Delivers AI-powered assistance using Google's Gemini API


✨ Features

User Management – Seamless registration, login, and profile management  
Product Catalog – Comprehensive eyewear product management  
Shopping Experience – Intuitive cart and checkout process  
Order Management – Order creation with VNPay payment integration  
AI-Powered Chatbot – Smart product recommendations and customer support  
Email Notifications – Order confirmations and registration verification  
Reviews – Product ratings and customer feedback


🚀 Getting Started
📦 Prerequisites

Docker & Docker Compose  
Java 17+  
Maven

⚙️ Setup and Installation
# Clone the repository
git clone <repository-url>

# Build the services
mvn clean install

# Start the services using Docker Compose
docker-compose up

🌐 Service URLs
The services are accessible at the following endpoints:

API Gateway: http://localhost:8080  
Eureka Server: http://localhost:8761  
Auth Service: http://localhost:8081  
User Service: http://localhost:8082  
Product Service: http://localhost:8083  
Order Service: http://localhost:8084  
Review Service: http://localhost:8085  
Notification Service: http://localhost:8086  
Email Service: http://localhost:8087  
Cart Service: http://localhost:8088  
Chatbot Service: http://localhost:8089


📚 API Documentation
APIs are accessible via the API Gateway at http://localhost:8080. Key endpoints include:

Authentication: /api/auth/*  
User Management: /api/users/*  
Products: /api/products/*  
Orders: /api/orders/*  
Cart: /api/carts/*  
Reviews: /api/reviews/*  
Notifications: /api/notifications/*  
Chatbot: /api/chatbot/*

Refer to individual service documentation for detailed API specifications.

🔧 Environment Configuration
Services can be configured using environment variables or application properties. Default configurations are provided in the docker-compose.yml file.

🔒 Security
The platform implements robust security measures:

JWT-based Authentication – Secure user authentication  
OTP Verification – For registration security  
Password Encryption – Protects user credentials  
Role-based Authorization – Restricts access based on user roles  
Secure Communication – Encrypted inter-service communication


💻 Development
To set up a development environment:

Import the project into your IDE.  
Configure Java 17+ in your IDE.  
Set up Maven for dependency management.  
Run individual services for development and testing.


🚀 Deployment
The application is containerized for easy deployment with Docker Compose or Kubernetes. For production, consider:

Enabling HTTPS for secure communication  
Setting resource limits for containers  
Using a container orchestration tool (e.g., Kubernetes)  
Implementing monitoring and logging


📜 License
[Add your license information here]

🙌 Acknowledgements

Spring Boot & Spring Cloud – For the microservices framework  
Google's Gemini API – For AI-powered chatbot functionality  
The open-source community – For the libraries and tools used in this project

