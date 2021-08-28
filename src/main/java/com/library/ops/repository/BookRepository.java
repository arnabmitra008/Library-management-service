package com.library.ops.repository;

import com.library.ops.entity.Book;
import com.library.ops.entity.BookTagMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByIsbn(String isbn);
}
