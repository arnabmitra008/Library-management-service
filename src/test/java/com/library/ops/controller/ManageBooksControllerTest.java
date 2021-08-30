package com.library.ops.controller;

import com.library.ops.bo.BookBO;
import com.library.ops.entity.Book;
import com.library.ops.entity.BookTagMapping;
import com.library.ops.exception.ManageBooksException;
import com.library.ops.exception.ManageBooksExceptionHandler;
import com.library.ops.service.ManageBooksService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
@ContextConfiguration
@ExtendWith(MockitoExtension.class)
@WebMvcTest(value = {ManageBooksController.class, ManageBooksExceptionHandler.class})
public class ManageBooksControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private ManageBooksService manageBooksService;

    private BookBO bookBO;
    private List<Book> books = new ArrayList<>();
    private List<Book> bookList = new ArrayList<>();
    private List<BookTagMapping> bookTagMappings = new ArrayList<>();
    private List<BookTagMapping> bookTagMappings2 = new ArrayList<>();
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
    }

    @Test
    public void shouldFindBookByIsbn() throws Exception {
        String isbn = "jdafb8371";
        when(manageBooksService.getBookByIsbn(anyString())).thenReturn(bookBO);
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/books/"+isbn)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);
        MockHttpServletResponse response = this.mockMvc.perform(requestBuilder).andReturn().getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        String expected = "{\n" +
                "    \"isbn\": \"jdafb8371\",\n" +
                "    \"title\": \"Immortals of Meluha\",\n" +
                "    \"author\": \"Amish\",\n" +
                "    \"tags\": [\n" +
                "        \"Religion\",\n" +
                "        \"Myth\"\n" +
                "    ]\n" +
                "}";
        JSONAssert.assertEquals(expected, response.getContentAsString(), false);
    }

    @Test
    public void shouldNotFindBookByInvalidIsbn() throws Exception {
        String isbn = "3721hfd";
        when(manageBooksService.getBookByIsbn(anyString())).thenThrow(
                new ManageBooksException("No book found for ISBN 3721hfd", HttpStatus.BAD_REQUEST));
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/books/"+isbn)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);
        MockHttpServletResponse response = this.mockMvc.perform(requestBuilder).andReturn().getResponse();
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        String expected = "{\n" +
                "    \"userMessage\": \"No book found for ISBN 3721hfd\"\n" +
                "}";
        JSONAssert.assertEquals(expected, response.getContentAsString(), false);
    }

    @Test
    public void shouldFindBooksByValidTitle() throws Exception {
        String title = "Immortals of Meluha";
        List<BookBO> bookBOs = new ArrayList<>();
        bookBOs.add(bookBO);
        when(manageBooksService.searchBooksByParams(title, null)).thenReturn(bookBOs);
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/books")
                .param("title", title)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);
        MockHttpServletResponse response = this.mockMvc.perform(requestBuilder).andReturn().getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        String expected = "[\n" +
                "    {\n" +
                "        \"isbn\": \"jdafb8371\",\n" +
                "        \"title\": \"Immortals of Meluha\",\n" +
                "        \"author\": \"Amish\",\n" +
                "        \"tags\": [\n" +
                "            \"Religion\",\n" +
                "            \"Myth\"\n" +
                "        ]\n" +
                "    }\n" +
                "]";
        JSONAssert.assertEquals(expected, response.getContentAsString(), false);
    }

    @Test
    public void shouldFindBooksByValidAuthor() throws Exception {
        String author = "Amish";
        List<BookBO> bookBOs = new ArrayList<>();
        bookBOs.add(bookBO);
        when(manageBooksService.searchBooksByParams(null, author)).thenReturn(bookBOs);
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/books")
                .param("author", author)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);
        MockHttpServletResponse response = this.mockMvc.perform(requestBuilder).andReturn().getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        String expected = "[\n" +
                "    {\n" +
                "        \"isbn\": \"jdafb8371\",\n" +
                "        \"title\": \"Immortals of Meluha\",\n" +
                "        \"author\": \"Amish\",\n" +
                "        \"tags\": [\n" +
                "            \"Religion\",\n" +
                "            \"Myth\"\n" +
                "        ]\n" +
                "    }\n" +
                "]";
        JSONAssert.assertEquals(expected, response.getContentAsString(), false);
    }

    @Test
    public void shouldFindBooksByValidTitleAndValidAuthor() throws Exception {
        String title = "Immortals of Meluha";
        String author = "Amish";
        List<BookBO> bookBOs = new ArrayList<>();
        bookBOs.add(bookBO);
        when(manageBooksService.searchBooksByParams(title, author)).thenReturn(bookBOs);
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/books")
                .param("title", title)
                .param("author", author)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);
        MockHttpServletResponse response = this.mockMvc.perform(requestBuilder).andReturn().getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        String expected = "[\n" +
                "    {\n" +
                "        \"isbn\": \"jdafb8371\",\n" +
                "        \"title\": \"Immortals of Meluha\",\n" +
                "        \"author\": \"Amish\",\n" +
                "        \"tags\": [\n" +
                "            \"Religion\",\n" +
                "            \"Myth\"\n" +
                "        ]\n" +
                "    }\n" +
                "]";
        JSONAssert.assertEquals(expected, response.getContentAsString(), false);
    }

    @Test
    public void shouldNotFindBookByInvalidTitle() throws Exception {
        String title = "Wrong Title";
        when(manageBooksService.searchBooksByParams(title, null)).thenThrow(
                new ManageBooksException("No book found with the given title Wrong Title", HttpStatus.BAD_REQUEST));
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/books")
                .param("title", title)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);
        MockHttpServletResponse response = this.mockMvc.perform(requestBuilder).andReturn().getResponse();
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        String expected = "{\n" +
                "    \"userMessage\": \"No book found with the given title Wrong Title\"\n" +
                "}";
        JSONAssert.assertEquals(expected, response.getContentAsString(), false);
    }

    @Test
    public void shouldNotFindBookByInvalidAuthor() throws Exception {
        String author = "Wrong Author";
        when(manageBooksService.searchBooksByParams(null, author)).thenThrow(
                new ManageBooksException("No book found with the given author Wrong Author", HttpStatus.BAD_REQUEST));
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/books")
                .param("author", author)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);
        MockHttpServletResponse response = this.mockMvc.perform(requestBuilder).andReturn().getResponse();
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        String expected = "{\n" +
                "    \"userMessage\": \"No book found with the given author Wrong Author\"\n" +
                "}";
        JSONAssert.assertEquals(expected, response.getContentAsString(), false);
    }

    @Test
    public void shouldNotFindBookByInvalidTitleAndAuthorCombination() throws Exception {
        String title = "Immortals of Meluha";
        String author = "Wrong Author";
        when(manageBooksService.searchBooksByParams(title, author)).thenThrow(
                new ManageBooksException("The title and author combination does not match", HttpStatus.BAD_REQUEST));
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/books")
                .param("title", title)
                .param("author", author)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);
        MockHttpServletResponse response = this.mockMvc.perform(requestBuilder).andReturn().getResponse();
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        String expected = "{\n" +
                "    \"userMessage\": \"The title and author combination does not match\"\n" +
                "}";
        JSONAssert.assertEquals(expected, response.getContentAsString(), false);
    }

    @Test
    public void shouldFindBooksByValidTags() throws Exception {
        String tags = "Religion,Myth";
        List<BookBO> bookBOs = new ArrayList<>();
        bookBOs.add(bookBO);
        when(manageBooksService.searchBooksByTags(tags)).thenReturn(bookBOs);
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/books")
                .param("tags", tags)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);
        MockHttpServletResponse response = this.mockMvc.perform(requestBuilder).andReturn().getResponse();
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        String expected = "[\n" +
                "    {\n" +
                "        \"isbn\": \"jdafb8371\",\n" +
                "        \"title\": \"Immortals of Meluha\",\n" +
                "        \"author\": \"Amish\",\n" +
                "        \"tags\": [\n" +
                "            \"Religion\",\n" +
                "            \"Myth\"\n" +
                "        ]\n" +
                "    }\n" +
                "]";
        JSONAssert.assertEquals(expected, response.getContentAsString(), false);
    }

    @Test
    public void shouldNotFindBookByInvalidTags() throws Exception {
        String tags = "WrongTag";
        when(manageBooksService.searchBooksByTags(tags)).thenThrow(
                new ManageBooksException("No books found with the provided tags", HttpStatus.BAD_REQUEST));
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/books")
                .param("tags", tags)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);
        MockHttpServletResponse response = this.mockMvc.perform(requestBuilder).andReturn().getResponse();
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
        String expected = "{\n" +
                "    \"userMessage\": \"No books found with the provided tags\"\n" +
                "}";
        JSONAssert.assertEquals(expected, response.getContentAsString(), false);
    }

    @Test
    public void shouldSuccessfullyInsertBooksForCorrectCSVFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "books.csv",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );
        doNothing().when(manageBooksService).insertBooksFromCSV(any(Path.class));
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        mockMvc.perform(MockMvcRequestBuilders.multipart("/books")
                .file(file).contentType(MediaType.MULTIPART_FORM_DATA)).andExpect(status().isOk());
    }

    @Test
    public void shouldThrowExceptionWhenInsertingBooksForIncorrectCSVFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "books.csv",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );
        doThrow(new ManageBooksException("The CSV file has improper data", HttpStatus.BAD_REQUEST))
                .when(manageBooksService).insertBooksFromCSV(any(Path.class));
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        mockMvc.perform(MockMvcRequestBuilders.multipart("/books")
                .file(file).contentType(MediaType.MULTIPART_FORM_DATA)).andExpect(status().isBadRequest());
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
                .isbn("jdafb8371")
                .author("Amish")
                .title("Immortals of Meluha")
                .tags(Arrays.asList("Religion","Myth"))
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
}