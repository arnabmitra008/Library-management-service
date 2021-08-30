# Library-Management-Service
Library Management Service API supports read and write operations for the management of books.

# Tech Stack
| Tech | Version |
|------|---------|
| Java | 8|
| Springboot | 2.5.4 |
| H2 (In memory DB)|1.4.200 |
| JUnit (with Mockito extn.) | 5|
| Swagger (OpenAPI) | 3.0.1 |
| Gradle | 6.7.1 |
| Intellij Idea (IDE) | 2020.3 |

# Git repository link
Below is the link to clone the Library-Management-Service repo in local IDE :

https://github.com/arnabmitra008/Library-management-service.git

# Build steps
To compile and run tests for the first time we can simply run a clean build in the terminal.
```sh
./gradlew clean build --refresh-dependencies
```

To compile and run tests we can simply run a clean build in the terminal.
```sh
./gradlew clean build
```

Starting up application on local
```sh
./gradlew bootRun
```

Generating a Spring boot jar
```sh
./gradlew bootJar
```
  
# Database Details
Once the server is started, H2 database UI can be accessed using the below link (no credentials required) :

http://localhost:8080/h2

# Swagger Documentation
Below is the link to view Swagger YAML file : 

https://github.com/arnabmitra008/Library-management-service/blob/master/LibraryManagement_Swagger.yaml


# Sample CSV file

The sample CSV file can also be accessed from the below link :
https://github.com/arnabmitra008/Library-management-service/blob/master/src/main/resources/books.csv

Each of the lines in CSV file is in below format :

{ISBN},{TITLE},{AUTHOR},{TAGS}

N.B. For providing multiple tags for a book, separate the tags by |(Pipe separator). 
E.g. Fiction|Mythology

# Future enhancement scopes
| Description | Checklist (Y/N) |
|-------------|-----------------|
| Adding messages in properties file |  |
| Logging enhancements |  |
| Unit test suite enrichment |  |
| CORS enhancement in Swagger | |

 
