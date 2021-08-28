package com.library.ops.service.impl;

import com.library.ops.bo.BookBO;
import com.library.ops.entity.Book;
import com.library.ops.entity.BookTagMapping;
import com.library.ops.exception.BookSearchException;
import com.library.ops.mapper.BooksMapper;
import com.library.ops.repository.BookRepository;
import com.library.ops.repository.BookTagMappingRepository;
import com.library.ops.service.BookSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BookSearchServiceImpl implements BookSearchService {

    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private BookTagMappingRepository bookTagMappingRepository;
    @Autowired
    private BooksMapper booksMapper;

    @Override
    public BookBO getBookByIsbn(String isbn) throws BookSearchException {
        if(isbn==null || isbn.isEmpty()){
            throw new BookSearchException("ISBN field is empty", HttpStatus.BAD_REQUEST);
        }
        BookBO bookBO = null;
        List<Book> books = bookRepository.findByIsbn(isbn);
        if(books!=null && books.size()>0){
            List<BookTagMapping> bookTags = bookTagMappingRepository.findAllByIsbn(isbn);
            List<String> tags = null;
            if(bookTags!=null && bookTags.size()>0){
                tags = bookTags.stream().map(p->p.getTagDesc()).collect(Collectors.toList());
            }
            bookBO = booksMapper.convertBookEntityToBookBO(books.get(0), tags);
        }
        return bookBO;
    }

    @Override
    public List<BookBO> searchBooksByParams(String title, String author) throws BookSearchException {
        return null;
    }
}
