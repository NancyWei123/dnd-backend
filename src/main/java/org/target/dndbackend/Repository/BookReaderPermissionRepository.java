package org.target.dndbackend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.target.dndbackend.Entity.BookReaderPermission;

import java.util.List;

public interface BookReaderPermissionRepository
        extends JpaRepository<BookReaderPermission, Long> {

    boolean existsByBook_IdAndUser_Id(Long bookId, Long userId);

    void deleteByBook_IdAndUser_Id(Long bookId, Long userId);

    @Transactional
    void deleteByBook_Id(Long bookId);

    List<BookReaderPermission> findByBook_Id(Long bookId);
}