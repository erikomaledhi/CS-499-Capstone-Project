# CS-499 Computer Science Capstone - Professional ePortfolio

**Student:** Eriko Maledhi  
**University:** Southern New Hampshire University  
**Program:** Bachelor of Science in Computer Science  
**Course:** CS-499 Computer Science Capstone  
**GitHub:** [@erikomaledhi](https://github.com/erikomaledhi)

---

## Table of Contents

- [Overview](#overview)
- [Professional Self-Assessment](#professional-self-assessment)
- [Code Review](#code-review)
- [Enhanced Artifacts](#enhanced-artifacts)
  - [Category One: Software Engineering and Design](#category-one-software-engineering-and-design)
  - [Category Two: Algorithms and Data Structures](#category-two-algorithms-and-data-structures)
  - [Category Three:  Databases](#category-three-databases)
- [Project Highlights](#project-highlights)
- [Technologies Used](#technologies-used)
- [Course Outcomes Alignment](#course-outcomes-alignment)
- [Repository Structure](#repository-structure)

---

## Overview

This repository contains my professional ePortfolio developed as the culminating project for the Computer Science program at Southern New Hampshire University. The ePortfolio showcases my technical skills, problem-solving abilities, and professional growth through enhanced software artifacts across three core computer science categories:  **Software Design and Engineering**, **Algorithms and Data Structures**, and **Databases**. 

---

## Professional Self-Assessment

### Program Journey

I have been enrolled in the Computer Science program at Southern New Hampshire University for about a year.  Throughout this time, I have progressed from foundational programming courses to advanced topics in software engineering, algorithms, databases, and system architecture. 

### Key Learning Areas

**1. Full-Stack Development and Software Architecture**

Through courses like CS 465 and CS 340, I gained experience in building end-to-end web applications.  I learned how to architect applications using the MVC pattern, implement RESTful APIs, integrate NoSQL databases like MongoDB, and create both server-side rendered applications and modern Single-Page Applications (SPAs) using frameworks like Angular and React.

**2. Data Structures and Algorithms**

My coursework emphasized the importance of algorithmic thinking and selecting appropriate data structures for solving computational problems efficiently. I learned to implement and analyze data structures such as binary search trees, hash tables, linked lists, and graphs, as well as sorting and searching algorithms.  

**3. Secure Software Development and Best Practices**

Through courses like CS 305, I developed a security-first mindset when approaching software development. I learned to identify common vulnerabilities (such as those in the OWASP Top 10), implement secure authentication and authorization mechanisms, validate and sanitize user input, and follow industry best practices for protecting sensitive data.

### Skills Demonstrated Through Enhancements

**Course Outcome 1: Collaborative Environments**
- Demonstrated proficiency in technologies (MERN stack) that facilitate team collaboration
- Implemented RESTful API that enables multiple developers to work on front-end and back-end independently
- Utilized version control (Git/GitHub) with professional commit practices and comprehensive documentation
- Created well-documented, modular code making it accessible for team collaboration

**Course Outcome 2: Professional Communications**
- Designed user-friendly interfaces following modern UI/UX principles
- Wrote ccode documentation with clear comments and annotations
- Created  technical narratives explaining architectural decisions

**Course Outcome 3: Algorithmic Principles**
- Implemented classic data structures (Binary Search Trees, Hash Tables) with proper time complexity analysis
- Analyzed algorithm optimization achieving 102,481x performance improvement for breed searches
- Demonstrated understanding of algorithm optimization through Big O analysis
- Applied algorithmic thinking to real-world problems (animal rescue matching, data filtering)

**Course Outcome 4: Well-Founded Techniques**
- Utilized modern development frameworks and tools (React, Node.js, Express, MongoDB)
- Implemented industry-standard design patterns (MVC, Repository Pattern, Service Layer)
- Applied professional development practices (testing, error handling, logging)
- Showcased mobile development skills with Android and backend integration

**Course Outcome 5: Security Mindset**
- Implemented JWT-based authentication with secure token management
- Applied input validation and sanitization to prevent injection attacks
- Designed secure database schemas with proper access control
- Implemented password hashing using BCrypt instead of SHA-256
- Configured secure API endpoints with rate limiting and authorization middleware
- Followed OWASP security guidelines throughout development

### Career Alignment

My career goal is to become a **full-stack software engineer**. The skills demonstrated through this capstone directly align with this objective:  

- **Technical Proficiency:** By migrating to MERN stack (MongoDB, Express, React, Node. js), I demonstrated proficiency in one of the most in-demand technology stacks in the industry.  

- **Problem-Solving and Algorithmic Thinking:** Employers value developers who can not only write code but also solve complex problems efficiently. By implementing data structures and algorithms and analyzing their performance characteristics, I demonstrated  computer science knowledge that resulted in measurable performance improvements of up to 102,481x.

- **Full-Stack Versatility:** My enhancements span front-end development (React UI/UX), back-end architecture (Node.js/Express APIs), and database design (MongoDB). 

- **Mobile Development Experience:** The Android Weight Tracker enhancement demonstrates my ability to work with mobile platforms and modern database solutions.

- **Security Consciousness:** As cybersecurity threats continue to grow, companies increasingly prioritize developers with security expertise. My implementation of BCrypt password hashing, JWT authentication, and input validation demonstrates this critical skill.

- **Professional Development Practices:** Through implementing testing, documentation, version control, and code quality tools, I demonstrated readiness to work in professional development environments.


## Code Review

An informal technical presentation where I:  
- Walked through the original artifacts and their functionality
- Identified areas for improvement and performance bottlenecks
- Explained planned enhancements for all three categories
- Demonstrated my ability to communicate technical concepts clearly

**[View Code Review](https://youtu.be/jmg1ZkJ-cJs)**

The code review video demonstrated my ability to analyze existing code and communicate technical concepts to both peers and managers. I walked through two artifacts:  the CS 340 Grazioso Salvare Dashboard and the CS 360 Weight Tracker application. I identified their current functionality, strengths, and weaknesses.  For the Grazioso Salvare Dashboard, I explained how the Python/Dash implementation used basic linear search operations with O(n) complexity and lacked optimized data structures. For the Weight Tracker app, I demonstrated the limitations of the current SQLite local-only database with SHA-256 password hashing.  

---

## Enhanced Artifacts

### Category One: Software Engineering and Design

#### Artifact: CS-340 Grazioso Salvare Animal Rescue Dashboard

**Origin:** CS 340 - Client/Server Development

**Original Implementation:**
- Python-based CRUD module (`animal_shelter. py`) using PyMongo for MongoDB operations
- Dash-based dashboard (`ProjectTwoDashboard.ipynb`) with interactive filtering
- Data table with sorting and pagination
- Pie chart showing breed distribution
- Interactive geolocation map with animal locations
- Radio button filters for different rescue types

**Enhancements Implemented:**

**1. Backend Architecture (Node.js + Express. js)**

Transformed the simple Python CRUD module into a professional RESTful API with:
- Express.js server with modular routing structure
- Mongoose ODM for MongoDB integration with schema validation
- Comprehensive error handling with custom error classes
- JWT-based authentication for admin operations
- Role-based access control (RBAC)
- Input validation and sanitization middleware

**2. Frontend Architecture (React. js)**

Rebuilt the Dash interface as a modern React Single-Page Application:  
- Component-based architecture with reusable UI elements
- React Router for client-side navigation
- Context API for state management
- Custom hooks for data fetching and business logic
- Material-UI for professional styling
- Form validation with real-time feedback
- Sortable tables with search functionality
- Interactive maps and data visualizations

**3. Professional Development Practices**
- Proper Git workflow with meaningful commits
- Comprehensive README documentation with setup instructions
- Code comments following professional standards

**Skills Demonstrated:**

1. **Full-Stack Web Development:** Built complete MERN stack application, created RESTful APIs, implemented client-server architecture
2. **Software Architecture and Design Patterns:** MVC architectural pattern, component-based architecture in React
3. **Database Design and Integration:** NoSQL database schema design with Mongoose, efficient database queries and indexes
4. **API Design and Development:** RESTful API principles and HTTP methods, API versioning and documentation
5. **Security Best Practices:** JWT authentication, RBAC, input validation and sanitization, password hashing with bcrypt
6. **Front-End Development:** React hooks, state management with Context API, client-side routing, responsive design
7. **Professional Development Practices:** Code organization, error handling, environment configuration, version control with Git

**Reflection:**

The enhancement process transformed my understanding of software engineering from writing code that works to designing systems that are secure and maintainable. The most significant learning occurred when I transitioned from feature-level thinking to architectural thinking. I discovered that good design is about creating predictable, organized code that reduces cognitive load.  I learned that security must be designed into the architecture from the beginning rather than added as features.  Technical challenges included debugging an infinite loop in React's useEffect hook, implementing role-based UI rendering, and handling messy production data that contained asterisks and numeric values like "019" instead of names. 

**[View Original Artifact](https://github.com/erikomaledhi/CS-499-Capstone-Project/tree/main/Original%20CS%20340%20Artifact)**  
**[View Enhanced Artifact](https://github.com/erikomaledhi/CS-499-Capstone-Project/tree/main/Enhanced%20CS%20340%20Artifact)**

---

### Category Two: Algorithms and Data Structures

#### Artifact:  CS-340 Grazioso Salvare Animal Rescue Dashboard

**Current Limitations (Before Enhancement):**

1. **Linear Search Operations:** All filtering and searching operations iterated through the entire dataset sequentially, resulting in O(n) time complexity
2. **No Data Structure Optimization:** Used basic Python lists and dictionaries without leveraging advanced data structures
3. **Inefficient Sorting:** Used Python's built-in sort without implementing custom algorithms optimized for specific use cases
4. **No Caching Mechanism:** Repeated queries fetched from database each time with no intelligent caching

**Enhancements Implemented:**

**1. Binary Search Tree (BST) for Animal ID Lookup**
- **Purpose:** Efficient O(log n) average-case search time vs.  current O(n) linear search
- **Result:** Reduced lookup comparisons from approximately 5,000 to just 17 for a dataset of 9,999 animals
- **Implementation:** Custom BST with insert, search, update, and delete operations

**2. Hash Table for Breed Lookup**
- **Purpose:** O(1) average-case lookup for breed filtering
- **Result:** Achieved 102,481x performance improvement for exact breed searches and 446x improvement for partial searches
- **Implementation:** Custom hash functions with collision handling via separate chaining

**3. Unified Cache Service**
- **Purpose:** Manage multiple data structure indexes with automatic synchronization
- **Implementation:** Automatic cache updates on CRUD operations maintaining consistency

**Skills Demonstrated:**

- **Algorithm Implementation:** BST recursive/iterative operations, hash function design with collision handling, custom data structure design
- **Complexity Analysis:** Big O notation for all operations, best/average/worst case analysis, time vs. space trade-offs
- **Data Structure Selection:** BST for ordered data, hash tables for categorical lookups, understanding when to apply specific structures
- **Professional Practices:** Algorithm documentation, benchmark testing, performance optimization

**Reflection:**

The enhancement process transformed my understanding of data structures from theoretical concepts to practical tools for solving real performance problems. The most significant learning occurred when I moved from treating data structures as academic exercises to recognizing them as strategic architectural decisions that change application scalability. I learned that choosing the right data structure requires analyzing access patterns rather than defaulting to databases for everything.  Implementing the BST taught me about tree balancing challenges.  My initial BST was unbalanced with depth 32 instead of the theoretical optimal 14.  This revealed that real-world tree structures rarely achieve perfect balance without self-balancing algorithms like AVL.  Creating Hash Tables revealed that constant-time performance requires careful hash function design to minimize collisions. 

**Performance Benchmarks:**

- **BST ID Lookup:** Reduced from approximately 5,000 comparisons (O(n)) to 17 comparisons (O(log n))
- **Hash Table Breed Search:** 102,481x improvement for exact matches
- **Hash Table Partial Search:** 446x improvement for partial string matching

**[View Implementation](https://github.com/erikomaledhi/CS-499-Capstone-Project/tree/main/Enhanced%20CS%20340%20Artifact)**

---

### Category Three: Databases

#### Artifact: CS-360 Weight Tracker Android Application

**Origin:** CS 360 - Mobile Architecture and Programming, August 2024

**Original Implementation:**
- Android app using SQLite local database
- Three tables: Users, WeightEntries, Goals
- All data stored on device only
- Password hashing using SHA-256

**Enhancements Implemented:**

**Migration:  SQLite to MongoDB Realm**

**Core Changes:**
1.  Replaced SQLite tables with MongoDB Realm collections
2. Redesigned schema using NoSQL best practices (embedded documents vs. references)
3. Implemented BCrypt password hashing replacing SHA-256
4. Created document-based NoSQL schema with embedded documents
5. Maintained all original DatabaseHelper method signatures for backward compatibility

**Schema Transformation:**

**Before (SQLite - Relational):**
```
Users:  id, username, password
WeightEntries: id, user_id, weight, date
Goals: id, user_id, target_weight, target_date
```

**After (MongoDB Realm - Document-Based):**
```java
// UserRealm
{
  _id: ObjectId (Primary Key),
  username: String (Required),
  passwordHash: String (Required, BCrypt),
  currentWeight: Float,
  goalWeight: Float,
  startingWeight: Float,
  createdAt: Date,
  weightEntries: RealmList<WeightEntryRealm> (Embedded)
}

// WeightEntryRealm (Embedded Document)
{
  _id: ObjectId (Primary Key),
  weight: Float (Required),
  date: String (Required),
  time: String,
  note: String
}
```

**Key Improvements:**
- Embedded documents (weight entries within user document)
- Eliminated JOIN operations improving read performance
- Proper data models with @PrimaryKey and @Required annotations
- BCrypt password hashing aligned with OWASP guidelines
- Adaptive cost factors for future-proof security

**Skills Demonstrated:**

- **NoSQL Database Design:** Document-based schema vs. relational tables, embedded documents (denormalization), reference patterns (one-to-many)
- **MongoDB Realm Expertise:** CRUD operations with Realm transactions, managed objects, automatic connection management
- **Database Migration:** ETL process, maintaining method signatures for backward compatibility, data validation
- **Security Enhancement:** BCrypt implementation replacing SHA-256, adaptive cost factors

**Reflection:**

The enhancement process deepened my understanding of the differences between relational and document-based storage.  The most significant learning occurred when I moved from thinking about data in normalized tables with foreign keys to understanding how document databases optimize for access patterns through denormalization and embedding. I learned that NoSQL schema design requires analyzing query patterns first.  Implementing the embedded document pattern taught me that storing weight entries directly within the user document eliminates JOIN operations and improves read performance. Technical challenges included discovering that Realm's automatic connection management meant I needed to remove databaseHelper. close() calls from four Activity files. I also debugged why new users saw all previous entries, which was caused by addSampleEntries() adding hardcoded demo data. I learned that Realm objects are live and auto-updating but cannot be passed between threads. 

**[View Original Artifact](https://github.com/erikomaledhi/CS-499-Capstone-Project/tree/main/Original%20CS%20360%20Artifact)**  
**[View Enhanced Artifact](https://github.com/erikomaledhi/CS-499-Capstone-Project/tree/main/Enhanced%20CS%20360%20Artifact)**

---

## Project Highlights

### Performance Achievements

- **102,481x improvement** for exact breed searches using Hash Tables
- **446x improvement** for partial breed searches
- **Reduced BST comparisons** from 5,000 to 17 for ID lookups in 9,999 animal dataset
- **Eliminated JOIN operations** through MongoDB embedded documents

### Security Enhancements

- **JWT-based authentication** with role-based access control
- **BCrypt password hashing** replacing SHA-256
- **Input validation and sanitization** at multiple layers
- **Rate limiting and CORS** configuration
- **OWASP security guidelines** implementation

### Architectural Improvements

- **Monolithic Python/Dash** to **MERN stack** full-stack application
- **SQLite local database** to **MongoDB Realm** with embedded documents
- **Linear search O(n)** to **BST O(log n) and Hash Table O(1)**
- **Tightly coupled code** to **Modular MVC architecture**

---

## Technologies Used

### MERN Stack
![MongoDB](https://img.shields.io/badge/MongoDB-47A248?style=for-the-badge&logo=mongodb&logoColor=white)
![Express.js](https://img.shields.io/badge/Express.js-000000?style=for-the-badge&logo=express&logoColor=white)
![React](https://img.shields.io/badge/React-61DAFB?style=for-the-badge&logo=react&logoColor=black)
![Node.js](https://img.shields.io/badge/Node.js-339933?style=for-the-badge&logo=node.js&logoColor=white)

### Mobile Development
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white)

### Tools & Libraries
![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black)
![Git](https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white)
![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)
---

## Course Outcomes Alignment

### Outcome 1: Collaborative Environments
- Implemented industry-standard MERN stack technologies facilitating team collaboration
- Created RESTful API allowing frontend and backend developers to work independently
- Developed modular architecture enabling different team members to own different parts of codebase
- Produced clear documentation enabling quick system understanding
- Followed professional Git practices tracking decision-making history

### Outcome 2: Professional Communications
- Designed visually appealing dashboard communicating complex data to non-technical stakeholders
- Wrote clear API documentation and code comments for developer audiences
- Created clean, well-organized code with consistent naming conventions
- Developed visual diagrams providing system design communication
- Implemented user-friendly error messages and validation feedback

### Outcome 3: Algorithmic Principles
- Designed solutions using classical algorithms (BST, hash tables)
- Analyzed time/space complexity with Big O notation
- Managed trade-offs (BST O(log n) vs Hash O(1))
- Achieved measurable performance improvements (102,481x for breed searches)

### Outcome 4: Well-Founded Techniques
- Utilized MERN stack representing current industry best practices
- Implemented MVC pattern demonstrating well-founded software engineering techniques
- Applied professional tools (Express.js, Mongoose, JWT, BCrypt)
- Demonstrated MongoDB as industry-standard NoSQL database

### Outcome 5: Security Mindset
- Implemented JWT-based authentication preventing unauthorized access
- Created role-based access control ensuring appropriate privilege levels
- Applied multi-layer input validation preventing injection attacks
- Used BCrypt password hashing protecting user credentials
- Designed careful error messages preventing information leakage
- Implemented security event logging enabling monitoring and incident response

---

## Repository Structure

```
CS-499-Capstone-Project/
│
├── README.md                          # This file
│
├── Original CS 340 Artifact/          # Original Python/Dash implementation
│   ├── animal_shelter.py              # Python CRUD module
│   ├── ProjectTwoDashboard. ipynb      # Dash dashboard notebook
│   └── aac_shelter_outcomes.csv       # Animal shelter dataset
│
├── Enhanced CS 340 Artifact/          # Enhanced MERN stack application
│   ├── backend/                       # Node.js/Express backend
│   │   ├── models/                    # Mongoose schemas
│   │   ├── routes/                    # API routes
│   │   ├── controllers/               # Request handlers
│   │   ├── middleware/                # Authentication, validation
│   │   └── server.js                  # Express server
│   ├── frontend/                      # React frontend
│   │   ├── src/
│   │   │   ├── components/            # React components
│   │   │   ├── hooks/                 # Custom hooks
│   │   │   ├── contexts/              # Context providers
│   │   │   └── App.jsx                # Main application
│   │   └── index.html
│   ├── Setup.md                       # Installation instructions
│   └── import_data.js                 # Data import script
│
├── Original CS 360 Artifact/          # Original SQLite Android app
│   └── WeightTrackingApp/
│       └── app/src/main/java/         # Android Java source
│
└── Enhanced CS 360 Artifact/          # MongoDB Realm migration
    └── WeightTrackingApp/
        └── app/src/main/java/         # Enhanced Android source
            └── com/zybooks/weighttrackingapp/
                ├── models/            # Realm models
                └── DatabaseHelper.java # MongoDB Realm implementation
```

**Last Updated:** December 2025
