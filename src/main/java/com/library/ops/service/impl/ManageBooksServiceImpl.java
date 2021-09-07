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
        log.debug("Entered : getBookByIsbn for ISBN : "+isbn);
        if(isNullOrEmpty(isbn)){
            log.error("ISBN field is empty");
            throw new ManageBooksException("ISBN field is empty", HttpStatus.BAD_REQUEST);
        }
        BookBO bookBO = null;
        List<Book> books = bookRepository.findByIsbn(isbn);
        if(books!=null && books.size()>0){
            List<String> tags = getTagsForBook(books.get(0));
            bookBO = booksMapper.convertBookEntityToBookBO(books.get(0), tags);
        }else{
            log.error("No book found for ISBN "+isbn);
            throw new ManageBooksException("No book found for ISBN "+isbn, HttpStatus.BAD_REQUEST);
        }
        return bookBO;
    }

    @Override
    public List<BookBO> searchBooksByParams(String title, String author) throws ManageBooksException {
        log.debug("Entered : searchBooksByParams for title : "+title+" and author : "+author);
        List<BookBO> bookBOList = new ArrayList<>();
        if(!isNullOrEmpty(title)){
            List<Book> books = bookRepository.findByTitle(title);
            if(books!=null && !books.isEmpty()){
                if(!isNullOrEmpty(author) && !books.get(0).getAuthor().equalsIgnoreCase(author)){
                    log.error("The title and author combination does not match. Title : "+title+
                            " and author : "+author);
                    throw new ManageBooksException("The title and author combination does not match",
                            HttpStatus.BAD_REQUEST);
                }
                List<String> tags = getTagsForBook(books.get(0));
                BookBO bookBO = booksMapper.convertBookEntityToBookBO(books.get(0), tags);
                bookBOList.add(bookBO);
            }else{
                log.error("No book found with the given title "+title);
                throw new ManageBooksException("No book found with the given title "+title,
                        HttpStatus.BAD_REQUEST);
            }
        }else if(!isNullOrEmpty(author)){
            List<Book> books = bookRepository.findAllByAuthor(author);
            if(books!=null && !books.isEmpty()){
                for(Book book : books){
                    List<String> tags = getTagsForBook(book);
                    BookBO bookBO = booksMapper.convertBookEntityToBookBO(book, tags);
                    bookBOList.add(bookBO);
                }
            }else{
                log.error("No book found with the given author "+author);
                throw new ManageBooksException("No book found with the given author "+author,
                        HttpStatus.BAD_REQUEST);
            }
        }else{
            log.error("No title or author provided");
            throw new ManageBooksException("No search parameters provided", HttpStatus.BAD_REQUEST);
        }
        return bookBOList;
    }

    @Override
    public List<BookBO> searchBooksByTags(String tags) throws ManageBooksException {
        log.debug("Entered : searchBooksByTags for tags : "+tags);
        List<BookBO> bookBOList = new ArrayList<>();
        List<String> tagList = Arrays.asList(tags.split(","));
        List<BookTagMapping> mappings = bookTagMappingRepository.findAllByTagDescIn(tagList);
        if(mappings!=null && !mappings.isEmpty()){
            List<Book> books = mappings.stream().map(p->p.getBook()).collect(Collectors.toList());
            for(Book book : books){
                List<String> bookTags = getTagsForBook(book);
                BookBO bookBO = booksMapper.convertBookEntityToBookBO(book, bookTags);
                bookBOList.add(bookBO);
            }
        }else{
            log.error("No books found with the provided tags");
            throw new ManageBooksException("No books found with the provided tags", HttpStatus.BAD_REQUEST);
        }
        return bookBOList;
    }

    @Override
    public void insertBooksFromCSV(Path path) throws ManageBooksException, IOException {
        log.debug("Entered : insertBooksFromCSV");
        Set<String> isbns = new HashSet<>();
        try {
            isbns = getAllIsbns();
            BufferedReader br = Files.newBufferedReader(path,StandardCharsets.US_ASCII);
            String line = br.readLine();
            while (line != null) {
                String[] attributes = line.split(",");
                if(isAttributesValid(attributes)){
                    if(!isbns.contains(attributes[0])){
                        Book book = createBook(attributes);
                        book = bookRepository.save(book);
                        List<BookTagMapping> mappings = createBookTagMapping(attributes, book);
                        bookTagMappingRepository.saveAll(mappings);
                        isbns.add(attributes[0]);
                    }else{
                        log.error("The ISBN "+attributes[0]+" is already present in DB. Hence skipping insertion.");
                    }
                }else{
                    log.error("The CSV file has improper data as one of the attributes is missing in the file");
                    throw new ManageBooksException("The CSV file has improper data", HttpStatus.BAD_REQUEST);
                }
                line = br.readLine();
            }
        } catch (IOException ioe) {
            log.error("Exception while parsing CSV File during inserting data", ioe);
            throw new ManageBooksException("Exception while parsing CSV File during inserting data",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (ManageBooksException e) {
            throw e;
        } catch (Exception ex) {
            log.error("Exception while inserting books in DB", ex);
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

    private List<BookTagMapping> createBookTagMapping(String[] attributes, Book book){
        BookTagMapping bookTagMapping = null;
        List<BookTagMapping> mappings = new ArrayList<>();
        String[] bookTags = attributes[3].split("\\|");
        for(String bookTag : bookTags){
            bookTagMapping = new BookTagMapping();
            bookTagMapping.setBook(book);
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

    private List<String> getTagsForBook(Book book) {
        List<String> tags = null;
        if(book.getBookTagMappingList()!=null && book.getBookTagMappingList().size()>0){
            tags = book.getBookTagMappingList().stream().map(p->p.getTagDesc()).collect(Collectors.toList());
        }
        return tags;
    }

    private Set<String> getAllIsbns() {
        List<Book> books = bookRepository.findAll();
        Set<String> isbns = new HashSet<>();
        if(books!=null && books.size()>0){
            isbns = books.stream().map(p->p.getIsbn()).collect(Collectors.toSet());
        }
        return isbns;
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
