
# Grocery Store Sample Web API Testing

This repository contains a sample project for testing a Grocery Store Web API. The project is organized into three main components:

1. **Postman Collection**: A JSON file to test the API endpoints manually using Postman.
2. **Java REST Assured API Project**: A basic implementation of API calls using Java and Rest Assured.
3. **Java REST Assured API Test Suite**: Automated tests for all API endpoints structured using the Page Object Model (POM) and TestNG.

## Table of Contents
- [Introduction](#introduction)
- [Installation](#installation)
- [Usage](#usage)
- [Running Tests](#running-tests)

## Introduction
This project provides an example of testing a grocery store's Web API, which includes functionalities such as product management, cart operations, and order management.

The repository contains three files:
1. A **Postman** collection for manual API testing.
2. A **Java REST Assured** project for programmatically interacting with the API.
3. A **Java REST Assured Test Suite** for testing all API endpoints using structured tests with the Page Object Model.

## Installation

### Prerequisites
Ensure you have the following installed:
- Java 8 or higher
- Maven
- Postman (for manual testing)

### Clone the repository
```bash
git clone git@github.com:KarinyOliveira/WebAPITesting.git
cd WebAPITesting
```

### Set up the Java Project
1. Navigate to the project directory:
   ```bash
   cd SimpleGroceryStoreRestPartI
   ```
2. Install dependencies:
   ```bash
   mvn clean install
   ```

## Usage

### Running the Postman Collection
1. Open Postman and click on **Import**.
2. Select the `Grocery_Store.json` file located in the `SimpleGroceryStorePostman/` folder.
3. Once imported, you can run the collection and interact with the API endpoints manually.

### Running the Java API Project
The **Java REST Assured** project can be used to send requests programmatically to the API. You can extend it by adding new endpoints or updating existing ones as needed.

## Running Tests

1. Ensure that the API service is running on your local machine or is accessible remotely.
2. To execute all test cases in the **Java Rest Assured Test Suite**, run the tests using Run button in java IDE.
