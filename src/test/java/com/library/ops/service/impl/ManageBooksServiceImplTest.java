package com.library.ops.service.impl;

import com.library.ops.bo.BookBO;
import com.library.ops.entity.Book;
import com.library.ops.entity.BookTagMapping;
import com.library.ops.exception.ManageBooksException;
import com.library.ops.mapper.BooksMapper;
import com.library.ops.repository.BookRepository;
import com.library.ops.repository.BookTagMappingRepository;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManageBooksServiceImplTest {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private BookTagMappingRepository bookTagMappingRepository;
    @Mock
    private BooksMapper booksMapper;

    @InjectMocks
    private ManageBooksServiceImpl manageBooksService = new ManageBooksServiceImpl();

    private List<Book> books = new ArrayList<>();
    private List<Book> bookList = new ArrayList<>();
    private List<BookTagMapping> bookTagMappings = new ArrayList<>();
    private List<BookTagMapping> bookTagMappings2 = new ArrayList<>();
    private BookBO bookBO;
    private BookBO bookBO2;
    private Path path;

    @BeforeEach
    void setUp() {
        getBooks();
        getBookList();
        getBookTagMappings();
        getBookTagMappings2();
        getBookBO();
        getBookBO2();
        path = Paths.get("./build/tmp/books.csv");
    }

    @Test
    public void shouldGetBookForValidIsbn() throws ManageBooksException {
        when(bookRepository.findByIsbn(anyString())).thenReturn(books);
        when(bookTagMappingRepository.findAllByIsbn(anyString())).thenReturn(bookTagMappings);
        when(booksMapper.convertBookEntityToBookBO(any(Book.class),anyList())).thenReturn(bookBO);
        BookBO bookBO = manageBooksService.getBookByIsbn("3721hfda");
        assertEquals("3721hfda", bookBO.getIsbn());
        assertEquals("Amish", bookBO.getAuthor());
        assertEquals("Immortals of Meluha", bookBO.getTitle());
        assertEquals(Arrays.asList("Myth"), bookBO.getTags());
    }

    @Test
    public void shouldThrowExceptionWhileSearchingBooksByInvalidIsbn() {
        when(bookRepository.findByIsbn(anyString())).thenReturn(null);
        Throwable exception = assertThrows(ManageBooksException.class, () -> manageBooksService.getBookByIsbn("3721hfda"));
        assertEquals("No book found for ISBN 3721hfda", exception.getMessage());
    }

    @Test
    public void shouldGetBooksForValidTitleOnly() throws ManageBooksException {
        when(bookRepository.findByTitle(anyString())).thenReturn(books);
        when(bookTagMappingRepository.findAllByIsbn(anyString())).thenReturn(bookTagMappings);
        when(booksMapper.convertBookEntityToBookBO(any(Book.class),anyList())).thenReturn(bookBO);
        List<BookBO> bookBOList = manageBooksService.searchBooksByParams("Immortals of Meluha", null);
        assertEquals(1, bookBOList.size());
        assertEquals("3721hfda", bookBOList.get(0).getIsbn());
        assertEquals("Amish", bookBOList.get(0).getAuthor());
        assertEquals("Immortals of Meluha", bookBOList.get(0).getTitle());
    }

    @Test
    public void shouldThrowExceptionWhileSearchingBooksByInvalidTitle() {
        when(bookRepository.findByTitle(anyString())).thenReturn(null);
        Throwable exception = assertThrows(ManageBooksException.class, () -> manageBooksService.searchBooksByParams("Random", null));
        assertEquals("No book found with the given title Random", exception.getMessage());
    }

    @Test
    public void shouldGetBooksForValidTitleAndAuthor() throws ManageBooksException {
        when(bookRepository.findByTitle(anyString())).thenReturn(books);
        when(bookTagMappingRepository.findAllByIsbn(anyString())).thenReturn(bookTagMappings);
        when(booksMapper.convertBookEntityToBookBO(any(Book.class),anyList())).thenReturn(bookBO);
        List<BookBO> bookBOList = manageBooksService.searchBooksByParams("Immortals of Meluha", "Amish");
        assertEquals(1, bookBOList.size());
        assertEquals("3721hfda", bookBOList.get(0).getIsbn());
        assertEquals("Amish", bookBOList.get(0).getAuthor());
        assertEquals("Immortals of Meluha", bookBOList.get(0).getTitle());
    }

    @Test
    public void shouldThrowExceptionWhileSearchingBooksByTitleAndInvalidAuthor() {
        when(bookRepository.findByTitle(anyString())).thenReturn(books);
        Throwable exception = assertThrows(ManageBooksException.class, () -> manageBooksService.searchBooksByParams(
                "Immortals of Meluha", "Test"));
        assertEquals("The title and author combination does not match", exception.getMessage());
    }

    @Test
    public void shouldGetBooksForValidAuthor() throws ManageBooksException {
        when(bookRepository.findAllByAuthor(anyString())).thenReturn(books);
        when(bookTagMappingRepository.findAllByIsbn(anyString())).thenReturn(bookTagMappings);
        when(booksMapper.convertBookEntityToBookBO(any(Book.class),anyList())).thenReturn(bookBO);
        List<BookBO> bookBOList = manageBooksService.searchBooksByParams(null, "Amish");
        assertEquals(1, bookBOList.size());
        assertEquals("3721hfda", bookBOList.get(0).getIsbn());
        assertEquals("Amish", bookBOList.get(0).getAuthor());
        assertEquals("Immortals of Meluha", bookBOList.get(0).getTitle());
    }

    @Test
    public void shouldThrowExceptionWhileSearchingBooksByInvalidAuthor() {
        when(bookRepository.findAllByAuthor(anyString())).thenReturn(null);
        Throwable exception = assertThrows(ManageBooksException.class, () ->
                manageBooksService.searchBooksByParams(null, "Test"));
        assertEquals("No book found with the given author Test", exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhileSearchingBooksWithoutTitleAndAuthor() {
        Throwable exception = assertThrows(ManageBooksException.class, () -> manageBooksService.searchBooksByParams(
                null, null));
        assertEquals("No search parameters provided", exception.getMessage());
    }

    @Test
    public void shouldGetBooksForValidTags() throws ManageBooksException {
        List<BookTagMapping> mappings = new ArrayList<>();
        mappings.addAll(bookTagMappings);
        mappings.addAll(bookTagMappings2);
        when(bookTagMappingRepository.findAllByTagDescIn(anyList())).thenReturn(mappings);
        when(bookRepository.findDistinctByIsbnIn(anyList())).thenReturn(bookList);
        when(bookTagMappingRepository.findAllByIsbn(anyString())).thenReturn(bookTagMappings, bookTagMappings2);
        when(booksMapper.convertBookEntityToBookBO(any(Book.class),anyList())).thenReturn(bookBO, bookBO2);
        List<BookBO> bookBOList = manageBooksService.searchBooksByTags("Myth,Motivation");
        assertEquals(2, bookBOList.size());
    }

    @Test
    public void shouldThrowExceptionWhileSearchingBooksByInvalidTags() {
        when(bookTagMappingRepository.findAllByTagDescIn(anyList())).thenReturn(null);
        Throwable exception = assertThrows(ManageBooksException.class, () ->
                manageBooksService.searchBooksByTags("SciFi"));
        assertEquals("No books found with the provided tags", exception.getMessage());
    }

    @Test
    public void shouldInsertBooksFromCSVSuccessfullyIfValidPathIsGiven() throws ManageBooksException, IOException {
        createFile();
        when(bookRepository.saveAll(anyList())).thenReturn(bookList);
        when(bookTagMappingRepository.saveAll(anyList())).thenReturn(bookTagMappings);
        manageBooksService.insertBooksFromCSV(path);
        verify(bookRepository).saveAll(anyList());
        verify(bookTagMappingRepository).saveAll(anyList());
    }

    @Test
    public void shouldFailToInsertBooksFromCSVIfIncorrectCSVIsGiven() throws IOException {
        createCorruptFile();
        Throwable exception = assertThrows(ManageBooksException.class, () -> manageBooksService.insertBooksFromCSV(path));
        assertEquals("The CSV file has improper data",exception.getMessage());
        verify(bookRepository,times(0)).saveAll(anyList());
        verify(bookTagMappingRepository, times(0)).saveAll(anyList());
    }

    private void getBooks(){
        Book book = new Book();
        book.setIsbn("3721hfda");
        book.setAuthor("Amish");
        book.setTitle("Immortals of Meluha");
        books.add(book);
    }

    private void getBookList(){
        Book book = new Book();
        book.setIsbn("3721hfda");
        book.setAuthor("Amish");
        book.setTitle("Immortals of Meluha");
        bookList.add(book);
        Book book2 = new Book();
        book2.setIsbn("843239nfd");
        book2.setAuthor("APJ Abdul Kalam");
        book2.setTitle("Wings of Fire");
        bookList.add(book2);
    }

    private void getBookTagMappings(){
        BookTagMapping bookTagMapping = new BookTagMapping();
        bookTagMapping.setIsbn("3721hfda");
        bookTagMapping.setTagDesc("Myth");
        bookTagMappings.add(bookTagMapping);
    }

    private void getBookTagMappings2(){
        BookTagMapping bookTagMapping = new BookTagMapping();
        bookTagMapping.setIsbn("843239nfd");
        bookTagMapping.setTagDesc("Motivation");
        bookTagMappings2.add(bookTagMapping);
    }

    private void getBookBO(){
        bookBO = BookBO.builder()
                .isbn("3721hfda")
                .author("Amish")
                .title("Immortals of Meluha")
                .tags(Arrays.asList("Myth"))
                .build();
    }

    private void getBookBO2(){
        bookBO2 = BookBO.builder()
                .isbn("843239nfd")
                .author("APJ Abdul Kalam")
                .title("Wings of Fire")
                .tags(Arrays.asList("Motivation"))
                .build();
    }

    private void createFile() throws IOException {
        File file = new File("./src/test/java/samplebooksfortest.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);
        Path path = Paths.get("./build/tmp/books.csv");
        Files.write(path, bytes);
    }

    private void createCorruptFile() throws IOException {
        File file = new File("./src/test/java/samplebooksfortestcorrupt.csv");
        byte[] bytes = FileUtils.readFileToByteArray(file);
        Path path = Paths.get("./build/tmp/books.csv");
        Files.write(path, bytes);
    }
}