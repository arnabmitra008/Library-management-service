package com.library.ops.controller;

import com.library.ops.bo.BookBO;
import com.library.ops.exception.BookSearchException;
import com.library.ops.service.BookSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/books")
public class BookSearchController {

    @Autowired
    private BookSearchService bookSearchService;

    @GetMapping(value = "/{isbn}",produces = {"application/JSON"})
    public ResponseEntity<BookBO> findBookByIsbn(@PathVariable(required = true) String isbn)
            throws BookSearchException {
        BookBO bookBO = bookSearchService.getBookByIsbn(isbn);
        return new ResponseEntity<>(bookBO, HttpStatus.OK);
    }

    @GetMapping(produces = {"application/JSON"})
    public ResponseEntity<List<BookBO>> searchBooksByParams(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "tags", required = false) String tags) throws BookSearchException {
        List<BookBO> bookBOList;
        if(tags!=null && !tags.isEmpty()){
            bookBOList = bookSearchService.searchBooksByTags(tags);
        }else{
            bookBOList = bookSearchService.searchBooksByParams(title, author);
        }
        return new ResponseEntity<>(bookBOList, HttpStatus.OK);
    }
}
