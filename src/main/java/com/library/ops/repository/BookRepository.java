package com.library.ops.repository;

import com.library.ops.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByIsbn(String isbn);
    List<Book> findByTitle(String title);
    List<Book> findAllByAuthor(String author);
    List<Book> findDistinctByIsbnIn(List<String> isbnList);
}
