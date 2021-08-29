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

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    @Override
    public void insertBooksFromCSV(Path path) throws ManageBooksException, IOException {
        List<Book> books = new ArrayList<>();
        List<BookTagMapping> bookTagMappings = new ArrayList<>();
        Set<String> isbns = new HashSet<>();
        try {
            BufferedReader br = Files.newBufferedReader(path,StandardCharsets.US_ASCII);
            String line = br.readLine();
            while (line != null) {
                String[] attributes = line.split(",");
                if(isAttributesValid(attributes)){
                    if(!isbns.contains(attributes[0])){
                        Book book = createBook(attributes);
                        books.add(book);
                        List<BookTagMapping> mappings = createBookTagMapping(attributes);
                        bookTagMappings.addAll(mappings);
                        isbns.add(attributes[0]);
                    }
                }else{
                    throw new ManageBooksException("The CSV file has improper data", HttpStatus.BAD_REQUEST);
                }
                line = br.readLine();
            }
            if(books.size()>0){
                bookRepository.saveAll(books);
            }
            if(bookTagMappings.size()>0){
                bookTagMappingRepository.saveAll(bookTagMappings);
            }
        } catch (IOException ioe) {
            throw new ManageBooksException("Exception while parsing CSV File during inserting data",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (ManageBooksException e) {
            throw e;
        } catch (Exception ex) {
            throw new ManageBooksException("Exception while inserting books in DB",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }finally {
            Files.delete(path);
        }
    }

    private Book createBook(String[] attributes){
        Book book = new Book();
        book.setIsbn(attributes[0]);
        book.setTitle(attributes[1]);
        book.setAuthor(attributes[2]);
        book.setCreatedBy("CSVFile");
        book.setUpdatedBy("CSVFile");
        Date date = new Date();
        book.setCreationDate(new Timestamp(date.getTime()));
        book.setUpdatedDate(new Timestamp(date.getTime()));
        return book;
    }

    private List<BookTagMapping> createBookTagMapping(String[] attributes){
        BookTagMapping bookTagMapping = null;
        List<BookTagMapping> mappings = new ArrayList<>();
        String[] bookTags = attributes[3].split("\\|");
        for(String bookTag : bookTags){
            bookTagMapping = new BookTagMapping();
            bookTagMapping.setIsbn(attributes[0]);
            bookTagMapping.setTagDesc(bookTag);
            bookTagMapping.setCreatedBy("CSVFile");
            bookTagMapping.setUpdatedBy("CSVFile");
            Date date = new Date();
            bookTagMapping.setCreationDate(new Timestamp(date.getTime()));
            bookTagMapping.setUpdatedDate(new Timestamp(date.getTime()));
            mappings.add(bookTagMapping);
        }
        return mappings;
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

    private boolean isAttributesValid(String[] attributes){
        if(attributes.length<4)
            return false;

        for(int i=0; i<attributes.length; i++){
            if(attributes[i]==null || attributes[i].isEmpty()){
                return false;
            }
        }
        return true;
    }

}
