package com.library.ops.service.impl;

import com.library.ops.bo.BookBO;
import com.library.ops.entity.Book;
import com.library.ops.entity.BookTagMapping;
import com.library.ops.exception.ManageBooksException;
import com.library.ops.mapper.BooksMapper;
import com.library.ops.repository.BookRepository;
import com.library.ops.repository.BookTagMappingRepository;
import com.library.ops.service.ManageBooksService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ManageBooksServiceImpl implements ManageBooksService {

    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private BookTagMappingRepository bookTagMappingRepository;
    @Autowired
    private BooksMapper booksMapper;

    @Override
    public BookBO getBookByIsbn(String isbn) throws ManageBooksException {
        if(isNullOrEmpty(isbn)){
            throw new ManageBooksException("ISBN field is empty", HttpStatus.BAD_REQUEST);
        }
        BookBO bookBO = null;
        List<Book> books = bookRepository.findByIsbn(isbn);
        if(books!=null && books.size()>0){
            List<String> tags = getTagsForBook(isbn);
            bookBO = booksMapper.convertBookEntityToBookBO(books.get(0), tags);
        }else{
            throw new ManageBooksException("No book found for ISBN "+isbn, HttpStatus.BAD_REQUEST);
        }
        return bookBO;
    }

    @Override
    public List<BookBO> searchBooksByParams(String title, String author) throws ManageBooksException {
        List<BookBO> bookBOList = new ArrayList<>();
        if(!isNullOrEmpty(title)){
            List<Book> books = bookRepository.findByTitle(title);
            if(books!=null && !books.isEmpty()){
                if(!isNullOrEmpty(author) && !books.get(0).getAuthor().equalsIgnoreCase(author)){
                    throw new ManageBooksException("The title and author combination does not match",
                            HttpStatus.BAD_REQUEST);
                }
                List<String> tags = getTagsForBook(books.get(0).getIsbn());
                BookBO bookBO = booksMapper.convertBookEntityToBookBO(books.get(0), tags);
                bookBOList.add(bookBO);
            }else{
                throw new ManageBooksException("No book found with the given title "+title,
                        HttpStatus.BAD_REQUEST);
            }
        }else if(!isNullOrEmpty(author)){
            List<Book> books = bookRepository.findAllByAuthor(author);
            if(books!=null && !books.isEmpty()){
                for(Book book : books){
                    List<String> tags = getTagsForBook(book.getIsbn());
                    BookBO bookBO = booksMapper.convertBookEntityToBookBO(book, tags);
                    bookBOList.add(bookBO);
                }
            }else{
                throw new ManageBooksException("No book found with the given author "+author,
                        HttpStatus.BAD_REQUEST);
            }
        }else{
            throw new ManageBooksException("No search parameters provided", HttpStatus.BAD_REQUEST);
        }
        return bookBOList;
    }

    @Override
    public List<BookBO> searchBooksByTags(String tags) throws ManageBooksException {
        List<BookBO> bookBOList = new ArrayList<>();
        List<String> tagList = Arrays.asList(tags.split(","));
        List<BookTagMapping> mappings = bookTagMappingRepository.findAllByTagDescIn(tagList);
        if(mappings!=null && !mappings.isEmpty()){
            List<String> isbns = mappings.stream().map(p->p.getIsbn()).collect(Collectors.toList());
            List<Book> books = bookRepository.findDistinctByIsbnIn(isbns);
            for(Book book : books){
                List<String> bookTags = getTagsForBook(book.getIsbn());
                BookBO bookBO = booksMapper.convertBookEntityToBookBO(book, bookTags);
                bookBOList.add(bookBO);
            }
        }else{
            throw new ManageBooksException("No books found with the provided tags", HttpStatus.BAD_REQUEST);
        }
        return bookBOList;
    }

    private List<String> getTagsForBook(String isbn) {
        List<BookTagMapping> bookTags = bookTagMappingRepository.findAllByIsbn(isbn);
        List<String> tags = null;
        if(bookTags!=null && bookTags.size()>0){
            tags = bookTags.stream().map(p->p.getTagDesc()).collect(Collectors.toList());
        }
        return tags;
    }

    private boolean isNullOrEmpty(String str){
        return (str==null || str.isEmpty());
    }

}
