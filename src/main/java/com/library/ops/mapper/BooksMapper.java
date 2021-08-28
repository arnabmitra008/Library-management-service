package com.library.ops.mapper;

import com.library.ops.bo.BookBO;
import com.library.ops.entity.Book;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class BooksMapper {

    public BookBO convertBookEntityToBookBO(Book book, List<String> tags){
        return BookBO.builder()
                .isbn(book.getIsbn())
                .author(book.getAuthor())
                .title(book.getTitle())
                .tags(tags)
                .build();
    }
}
