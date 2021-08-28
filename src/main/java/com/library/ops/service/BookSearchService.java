package com.library.ops.service;

import com.library.ops.bo.BookBO;
import com.library.ops.exception.BookSearchException;

import java.util.List;

public interface BookSearchService {
    BookBO getBookByIsbn(String isbn) throws BookSearchException;
    List<BookBO> searchBooksByParams(String title, String author) throws BookSearchException;
    List<BookBO> searchBooksByTags(String tags) throws BookSearchException;
}
