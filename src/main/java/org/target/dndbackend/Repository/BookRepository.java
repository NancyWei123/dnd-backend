package org.target.dndbackend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.target.dndbackend.Entity.Book;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    @Query(value = """
    SELECT b.*
    FROM books b
    WHERE
        b.user_id = :userId
        OR b.permission = 'public'
        OR (
            b.permission = 'protected'
            AND EXISTS (
                SELECT 1
                FROM book_reader_permissions brp
                WHERE brp.book_id = b.id
                AND brp.user_id = :userId
            )
        )
    """, nativeQuery = true)
    List<Book> findReadableBooksByUserId(@Param("userId") Long userId);
    List<Book> findByUser_Id(Long userId);
}