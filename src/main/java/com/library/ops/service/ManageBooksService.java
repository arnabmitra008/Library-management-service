package com.library.ops.service;

import com.library.ops.bo.BookBO;
import com.library.ops.exception.ManageBooksException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface ManageBooksService {
    BookBO getBookByIsbn(String isbn) throws ManageBooksException;
    List<BookBO> searchBooksByParams(String title, String author) throws ManageBooksException;
    List<BookBO> searchBooksByTags(String tags) throws ManageBooksException;
    void insertBooksFromCSV(Path path) throws  ManageBooksException, IOException;
}
