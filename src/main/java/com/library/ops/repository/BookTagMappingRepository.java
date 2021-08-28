package com.library.ops.repository;

import com.library.ops.entity.Book;
import com.library.ops.entity.BookTagMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
@Repository
public interface BookTagMappingRepository extends JpaRepository<BookTagMapping, Long> {
    List<BookTagMapping> findAllByIsbn(String isbn);
    List<BookTagMapping> findAllByTagDesc(String tagDesc);
    List<BookTagMapping> findAllByTagDescIn(List<String> tags);
}
