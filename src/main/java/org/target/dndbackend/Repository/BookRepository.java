package org.target.dndbackend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.target.dndbackend.Entity.Book;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByUser_Id(Long userId);
}