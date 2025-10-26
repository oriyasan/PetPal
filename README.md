<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/e5747c66-cb22-462e-93ec-7dec9303da6a" />![PetPal Logo](src/main/webapp/resources/images/logo.png)  

**PetPal** is a web-based system for managing pets available for adoption.  
The system was built out of love for animals and provides the following features:
- User registration and login  
- Adding new pets for adoption  
- Viewing pet list with filtering and search options  
- Marking favorite pets  
- Internal mailbox for user-to-user messages  
- Personal profile management and password update  

---

## System Requirements
- Java JDK 17  
- Apache Tomcat 9  
- MySQL 8.x  
- Java EE–compatible IDE (Eclipse / IntelliJ / NetBeans)  
- Maven (installed locally or bundled in the IDE)  

---

## Installation & Run
1. Download or clone the project:
   - Option A: Clone with Git:  
     ```bash
     git clone https://github.com/oriyasan/PetPal.git
     ```  
   - Option B: Download ZIP from GitHub (`Code → Download ZIP`) and extract it.  

2. Open the project in your IDE (Import → Maven → Existing Maven Project).  
3. Create a new database named **`petpal_db`**.  
4. Import the included SQL file (`petpal_db.sql`) into the database  
   (via MySQL Workbench → Data Import).  
   > The file will create all required tables and insert demo data.  
5. Update the database connection details in:  
   `src/main/resources/META-INF/persistence.xml`  
   (username, password, DB URL).  
6. Deploy the project on Tomcat 9 (Run on Server)  
   or run `mvn clean package` to generate a WAR file and deploy manually.  
7. Open a browser and go to:  
   `http://localhost:8080/PetPal`

---

## Demo Users
For quick login, demo users are provided:
The demo images were taken from Pexels.


| User        | Password      |
|-------------|---------------|
| oriya       | temp937629A!  |
| yosef       | Yosef123!     |
| loveAnimals | Nisnis33#     |
| kortes      | temp515169A!  |
| arbelsa     | Arbelsanbato!1|
| dani12      | temp879622A!  |
| flafi       | temp530014A!  |
| mon         | Monmon12#     |
| david       | Dandan1!      |

---

## Additional Documents
[📘 User Guide](UserGuide.pdf) - in Hebrew



 
