
## Timecast Frontend
This application is made in order of a school project.

The application is made to manage employees with their corresponding projects. It is possible to create new Employees
and new Projects. An Employee needs a Contract to be able to be assigned to a project. With creating Allocations an
employee can then be assigned to a project according to the employees pensum.

<br>

#### Technologies used in this project
* Java
* Gradle
* SpringMVC
* SpringBoot
* JPA

<br>

#### Environment
Run the application with
<br>
``` gradlew bootRun ```

<br>

Configurations (API, Port, Logging, etc.) can be found in the File<br>
``` src / main / resources / ```**``` template.application.properties ```**<br>
Copy that file, remove the prefix **```template.```** and configure the properties.
* Set server address and port
* Set the API URL
* Set database connection and credentials
* Define the correct keys for authentication and SSL (See also the chapters below)
* Set logging level
* OPTIONAL: For generating dummy data, set spring.datasource.initialization-mode to always and boot. Set back to never to avoid duplicated data.


``` Build Project Automatically. ```
<br>

Press **Ctrl + Shift + A** and search **Registry** Select **Registry...** and enable:<br>
``` compiler.automake.allow.when.app.running ``` 

<br>

#### Database
The timecast backend runs with a postgreSQL database. 
Download and setup postgreSQL from https://www.postgresql.org/ and create 
a database that fits the connection details in the application.properties
(You can customize the connection, or use the predefined one).
Remember that you can use the delieverd pgAdmin application to manage your
database and data.
<br>

#### SSL Certificate
Command to generate: ``` keytool -genkeypair -alias timecast -keyalg RSA -keysize 2048 -storetype PKCS12 -key
store timecast.p12 -validity 3650 ```<br>
(If you've been asked for Firstname and Lastname just enter your domain name, e.g. localhost)

Put the generated SSL certificate into to the truststore. Just put the generated certificate
Files in ```/src/main/resources/keystore/``` and adjust the ```application.properties``` if necessary.

To run the application without certificates just comment the ssl properties in ```application.properties```.

<br>

#### Using JWT
To use the application it is necessary that the respective backend API is providing the authentication in form of a JWT
Token. Because the signature will be checked this Token must be signed using RSA512 algorithm.

(If it's necessary to change that behaviour the respective code in **```wodss.timecastfrontend.security.JwtUtil```** file.)

Command to generate RSA keys:  ```openssl genpkey -algorithm RSA -out private_key.pem -pkeyopt rsa_keygen_bits:2048```

Command to extract public key from private key: ```openssl rsa -pubout -in private_key.pem -out public_key.pem```

<br>

#### Project Structure

Java class structure

    .
    └──timecastfrontend
       ├── advice                              # The advice classes, in this case only to catch exceptions
       ├── domain                              # The entities used in the application
       ├── dto                                 # The DTO entities which are used for the communication to the backend
       ├── exception                           # The Exception definitions which are specific for the application
       ├── persistence                         # The JPA Repository interfaces, one Repo for each model (Except Role)
       ├── security                            # All Security related classes, such as Authentication, JWT and RSA utils.
       ├── service                             # Services for each resource to handle business logic between models
       ├── util                                # Additional utils specific for the application
       ├── web                                 # The Web Controllers which are entry point for requests.
       │
       ├── TimecastBackendApplication.java     # The main application class ( + Resttemplate configuration).

<br>
Example keystore structure

    .
    └──keystore
       ├── jwt_privkey.pem                     # The entities used in the application
       ├── timecast_backend.p12                # The Exception definitions which are specific for the application
       └── timecast_frontend.p12               # All Security related classes, such as Authentication, JWT and RSA utils.

## References
* [Spring MVC Doc](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-spring-mvc)
* [PostgreSQL Doc](https://www.postgresql.org/docs/)