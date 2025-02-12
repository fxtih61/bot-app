# Berufsorientierungstag App

## 📌 Project Overview
The Berufsorientierungstag App is a Spring Boot application developed using Maven, Java 23, and JavaFX. It aims to fully automate the scheduling process for the annual Berufsorientierungstag, which is organized for students in different educational programs. The application simplifies student event preferences, assigns them to appropriate sessions, generates timetables, and produces printable attendance lists and itineraries.

## 🛠 Tech Stack
- **Java 23**
- **Maven**
- **Spring Boot**
- **JavaFX**

## 🎯 Features
- **Student Event Selection**: Students can choose their preferred events from a predefined list.
- **Automated Scheduling**: The system ensures that each student gets as many preferred events as possible while maintaining fair distribution.
- **Room and Time Management**: Assigns sessions to available rooms and optimizes time slots.
- **Attendance Tracking**: Generates printable attendance lists for each event.
- **Performance Metrics**: Calculates a fulfillment score to measure scheduling efficiency.
- **Data Import & Export**: Uses Excel files for importing student preferences and exporting final schedules.
- **Reproducibility**: Ensures identical results with the same input data.

## 🚀 Getting Started

### Prerequisites
Ensure you have the following installed:
- Java 23 (or later)
- Maven
- JavaFX
- IntelliJ IDEA (Recommended IDE) with the following plugins:
    - [.env files](https://plugins.jetbrains.com/plugin/9525--env-files)
    - [Atom Material Icons](https://plugins.jetbrains.com/plugin/10044-atom-material-icons)
    - [Git Commit Template](https://plugins.jetbrains.com/plugin/23641-git-commit-template)
    - [GitHub Copilot](https://plugins.jetbrains.com/plugin/17718-github-copilot)
    - Other recommended plugins should be installed automatically when selecting the JDK.

### 🔧 Installation
1. **Install Java 23.0.2**
    - Download from [Oracle Java Downloads](https://www.oracle.com/java/technologies/downloads/)
    - Note: You can run multiple Java versions on your machine. Ensure the correct version is set in your IDE and terminal.
    - On Linux, switch between Java versions using:
      ```sh
      sudo update-alternatives --config java
      sudo update-alternatives --config javac
      ```

2. **Install Maven**
   ```sh
   sudo apt-get -y install maven
   ```

3. **Clone the repos1itory (SSH recommended)**
   ```sh
   git clone git@github.com:fxtih61/bot-app.git
   cd bot-app
   ```

4. **Install dependencies**
   ```sh
   mvn clean install
   ```

5. **Run the application**
   ```sh
   mvn spring-boot:run
   ```

## 📊 Data Handling
- **Input:** Excel files containing student preferences, event details, and room availability.
- **Processing:** The application assigns students to events using an optimized algorithm.
- **Output:**
    - Printable itineraries for each student.
    - Attendance lists per event.
    - Room and event schedules.
    - Performance reports on fulfillment scores.

## 🔗 Useful Links
- [JavaFX Documentation](https://openjfx.io/)

---

### 📌 A Project by
- [MianGo7](https://github.com/MianGo7)
- [fxtih61](https://github.com/fxtih61)
- [Leokin4](https://github.com/Leokin4)
- [ymbatu64](https://github.com/ymbatu64)

---

This project aims to optimize and simplify the Berufsorientierungstag scheduling process, improving efficiency and user experience for students and organizers alike. 🚀

