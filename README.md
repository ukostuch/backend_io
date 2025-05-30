# Lost & Found Service Backend

This repository contains the backend code for a **Lost and Found Service**, built with **Java Spring Boot** and **MongoDB**. The service provides a REST API to manage the creation, updating, searching, and deletion of lost and found items.

## Table of Contents
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [JWT and Spring Security](#jwt-and-spring-security)
- [Tests](#tests)

---

## Features
- **Add Found Items:** Users can report found items with similar details.
- **Search & Filter:** Users can search items based on categories, keywords, and location.
- **Search for offices:** Users can search for offices based on location.
- **Update & Delete:** Allows authorized updates and deletion of items or offices for users with admin permissions.
- **Ask questions:** Users can ask questions and expect staff to answer them.
- **Database Persistence:** Stores data persistently using MongoDB.

## Tech Stack
- **Java 11** or higher
- **Spring Boot** (2.x or 3.x)
  - Spring Data MongoDB
  - Spring Web
- **MongoDB**
- **Maven** for dependency management

## Prerequisites
- Java Development Kit (JDK) 11 or higher
- MongoDB installed and running
- Maven 3.6+ for dependency management

## Getting Started

### Clone the repository
```
git clone https://github.com/ukostuch/backend_io.git
```

### Install dependencies
```
mvn clean install
```

## Configuration

### MongoDB setup
Ensure MongoDB is installed and configure the application.properties file as explained below.

### Configure Application Properties
Open src/main/resources/application.properties and configure the MongoDB connection details:

```
# MongoDB settings
spring.data.mongodb.uri=mongodb+srv://<username>:<password>@cluster.2wv7i.mongodb.net/

```

### Run the application
Start the Spring Boot application:
```
mvn spring-boot:run
```
The application will start at http://localhost:8080.

## JWT and Spring Security
The Lost and Found Service uses JWT for stateless authentication and Spring Security for securing API endpoints.

### Key Components
 - AuthController
 - CustomUserDetailsService
 - JWTAuthenticationFilter
 - JWTGenerator
 - SecurityConfig


## Tests
Our project includes unit and integration tests to ensure the reliability and correctness of the application.

### Unit tests
  - to validate the functionality of individual components, such as services and controllers
  - are divided into two directories: 
    - controllerTests - provide tests regarding controllers, consists of 9 separate test files, each concentrated on different controller
    - serviceTests - provide tests regarding services, consists of 10 separate test files, each concentrated on different service

### Integration tests
  - to test the interaction between components
  - are divided into two files:
    - FaqControllerIntegrationTest
    - UserControllerIntegrationTest
      
