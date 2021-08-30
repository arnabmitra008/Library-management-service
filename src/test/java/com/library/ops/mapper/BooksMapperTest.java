package com.library.ops.mapper;

import com.library.ops.bo.BookBO;
import com.library.ops.entity.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;


@ExtendWith(MockitoExtension.class)
public class BooksMapperTest {

    private Book book;
    private List<String> tags;

    @BeforeEach
    void setUp() {
        getBook();
        tags = new ArrayList<>();
        tags.add("Motivation");
        tags.add("Non fiction");
    }

    @InjectMocks
    private BooksMapper booksMapper = new BooksMapper();

    @Test
    public void shouldConvertBookEntityToBookBO() {
        BookBO bookBO = booksMapper.convertBookEntityToBookBO(book, tags);
        assertEquals(bookBO.getIsbn(), "634712ndjs");
        assertEquals(bookBO.getTitle(), "Wings of Fire");
        assertEquals(bookBO.getAuthor(), "APJ Abdul Kalam");
    }

    private void getBook(){
        book = new Book();
        book.setIsbn("634712ndjs");
        book.setTitle("Wings of Fire");
        book.setAuthor("APJ Abdul Kalam");
    }
}