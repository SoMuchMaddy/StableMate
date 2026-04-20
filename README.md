# StableMate – Console-Based Stable Management System

StableMate is a full-featured console application for managing Riders, Horses, Lessons, and Stable information.  
It supports CRUD operations, automatic care-queue calculations, input validation, persistent data storage, and full simulated-input testing.

---

# Project Description

StableMate helps stable managers organize core information:

- Rider records  
- Horse profiles  
- Lesson scheduling  
- Stable metadata  
- Automated care queues (Annual, Deworm, Farrier)  
- Persistent file storage across sessions  

All interaction happens through a menu-driven console interface using keyboard input.

---

# Dependencies & Installation  

StableMate requires:

- A Java compiler (any version that supports `java.time`)
- A terminal environment capable of redirecting input from `.txt` files

Directory structure:

StableMate-SoMuchMaddy/
│

├── data/ ← Active runtime data

├── data_baseline/ ← Clean snapshot used for resets

├── terminal/ ← Run StableMate + test input redirection here

└── testing/ ← All automated input test files

To compile:
cd terminal
javac *.java

To run:
java StableMate

**Note:** The compile and run commands shown are intended to be executed in
Windows PowerShell. 

---

# Configuration & Execution

### **Start the program normally**

cd terminal
java StableMate

### **Run automated tests (simulated keyboard input)**  
See full instructions in `TESTING.md`, but example usage:

java StableMate < ../testing/test_add_rider.txt

### **Resetting data to baseline**

cd finalcs2-SoMuchMaddy
Copy-Item .\data_baseline* .\data -Force

---

# Sample Output

<img width="497" height="249" alt="image" src="https://github.com/user-attachments/assets/e8fae2f5-ab09-4a15-8d98-eef5691f6045" />

-----------------------------------------------------------------------------------------------------------------------------------

<img width="665" height="923" alt="image" src="https://github.com/user-attachments/assets/8b17b12b-8c1c-4144-bfb7-2778052e8923" />

-----------------------------------------------------------------------------------------------------------------------------------

<img width="223" height="198" alt="image" src="https://github.com/user-attachments/assets/0eee0266-fb50-4222-9bb7-f303beb7f8c7" />

-----------------------------------------------------------------------------------------------------------------------------------

<img width="343" height="1209" alt="image" src="https://github.com/user-attachments/assets/6edcb342-d32c-401c-9af5-b5de918ef654" />

-----------------------------------------------------------------------------------------------------------------------------------

<img width="602" height="1279" alt="image" src="https://github.com/user-attachments/assets/a1e10d3b-398d-4344-bcde-528c0fc99888" />

-----------------------------------------------------------------------------------------------------------------------------------

<img width="1087" height="1081" alt="image" src="https://github.com/user-attachments/assets/09667d1d-88b6-4680-be55-8d8ce470d9f8" />

-----------------------------------------------------------------------------------------------------------------------------------

<img width="219" height="189" alt="image" src="https://github.com/user-attachments/assets/eb12bac9-3931-4733-9727-f4824a808e51" />

-----------------------------------------------------------------------------------------------------------------------------------

<img width="630" height="387" alt="image" src="https://github.com/user-attachments/assets/67438ec0-5d14-43fa-bf34-e7f6758dabbc" />

---

# Repository & Software Design

StableMate uses object-oriented design with the following major classes:

- **StableMate** – central controller class (menus, loading, saving, queues)
- **Rider** – stores rider identity and metadata  
- **Horse** – full horse profile including care cycles and compatibility lists  
- **Lesson** – stores scheduled lesson info  
- **Stable** – stores stable metadata  
- **Menu** / **MenuOption** – console UI menu system  
- **IDGenerator** – generates unique Rider/Horse IDs without collisions  

The program emphasizes:

- Encapsulation  
- Persistent file I/O  
- PriorityQueue/Heap usage  
- ArrayList and HashMap collections  
- Input validation loops  
- Clean separation of concerns  

---

# Javadoc Overview

The project includes detailed Javadoc for:

### **Classes**
- Rider  
- Horse  
- Lesson  
- Stable  
- Menu  
- MenuOption  
- StableMate  
- IDGenerator  

### **Methods**
- Constructor behavior  
- Accessors & mutators  
- Care queue helpers  
- File IO loaders/savers  
- Menu actions  
- ID generation  

### **Blocks of functionality explained**
- Validation loops  
- Queue initialization  
- Compatibility list structure  
- Overdue care checks  

<img width="1141" height="1059" alt="image" src="https://github.com/user-attachments/assets/82ce4f48-01c7-44f1-8ae3-31b978ec8df2" />

---

# Testing Overview (Simulated Keyboard Input)

StableMate includes **18 automated input tests**, each fully described in `TESTING.md`.  
These tests verify:

- Adding/editing/removing Riders, Horses, Lessons  
- Bad input handling  
- Care queue ordering + overdue detection  
- Menu navigation  
- Full program flow  
- Data persistence and file correctness

<img width="462" height="418" alt="image" src="https://github.com/user-attachments/assets/7d333891-673c-45d7-9915-eef8a34963ca" />

---------------------------------------------------------------------------------------------------------------------------------------------

<img width="1088" height="1204" alt="image" src="https://github.com/user-attachments/assets/08013d87-d14d-403a-97f3-b4e8614c5e6d" />

---------------------------------------------------------------------------------------------------------------------------------------------

<img width="292" height="204" alt="image" src="https://github.com/user-attachments/assets/35535035-3e74-4580-aa7d-9ba4f534633d" />

---

# UML Class Diagram  

<img width="1936" height="2283" alt="StableMateUML" src="https://github.com/user-attachments/assets/29f29ad1-c189-4741-b82b-06ef7784c479" /> 

---

# Citations & Influences

- Inspiration for logic based on real equine management practices 

---



