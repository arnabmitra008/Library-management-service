package com.library.ops.controller;

import com.library.ops.bo.BookBO;
import com.library.ops.exception.ManageBooksException;
import com.library.ops.service.ManageBooksService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/books")
public class ManageBooksController {

    @Autowired
    private ManageBooksService manageBooksService;

    @GetMapping(value = "/{isbn}",produces = {"application/JSON"})
    public ResponseEntity<BookBO> findBookByIsbn(@PathVariable(required = true) String isbn)
            throws ManageBooksException {
        BookBO bookBO = manageBooksService.getBookByIsbn(isbn);
        return new ResponseEntity<>(bookBO, HttpStatus.OK);
    }

    @GetMapping(produces = {"application/JSON"})
    public ResponseEntity<List<BookBO>> searchBooksByParams(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "tags", required = false) String tags) throws ManageBooksException {
        List<BookBO> bookBOList;
        if(tags!=null && !tags.isEmpty()){
            bookBOList = manageBooksService.searchBooksByTags(tags);
        }else{
            bookBOList = manageBooksService.searchBooksByParams(title, author);
        }
        return new ResponseEntity<>(bookBOList, HttpStatus.OK);
    }

    @PostMapping(consumes = {"multipart/form-data"}, produces = {"text/plain"})
    public ResponseEntity<String> insertBooks(
            @RequestPart(value = "file") MultipartFile file) throws ManageBooksException,IOException {
        if (file.getOriginalFilename()==null || file.getOriginalFilename().isEmpty()) {
            throw new ManageBooksException("File name is missing", HttpStatus.BAD_REQUEST);
        }
        if(file.isEmpty()){
            throw new ManageBooksException("File is empty", HttpStatus.BAD_REQUEST);
        }
        byte[] bytes = file.getBytes();
        Path path = Paths.get("./build/tmp/"+file.getOriginalFilename());
        Files.write(path, bytes);
        manageBooksService.insertBooksFromCSV(path);
        return new ResponseEntity<>("Books loaded successfully", HttpStatus.OK);
    }
}
