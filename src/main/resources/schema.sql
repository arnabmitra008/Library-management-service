DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS tags;
DROP TABLE IF EXISTS book_tag_mapping;

CREATE TABLE books (
               id INT AUTO_INCREMENT  PRIMARY KEY,
               isbn VARCHAR(250) NOT NULL,
               title VARCHAR(1000) NOT NULL,
               author VARCHAR(250) NOT NULL,
               createdBy VARCHAR(50) NOT NULL,
               creationDate TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
               updatedBy VARCHAR(50) NOT NULL,
               updatedDate TIMESTAMP NOT NULL
);

CREATE TABLE book_tag_mapping (
               id INT AUTO_INCREMENT  PRIMARY KEY,
               isbn VARCHAR(250) NOT NULL,
               tagDesc VARCHAR(250) NOT NULL,
               createdBy VARCHAR(50) NOT NULL,
               creationDate TIMESTAMP NOT NULL,
               updatedBy VARCHAR(50) NOT NULL,
               updatedDate TIMESTAMP NOT NULL
);