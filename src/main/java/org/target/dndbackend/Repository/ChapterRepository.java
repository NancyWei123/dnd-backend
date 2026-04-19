package org.target.dndbackend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.target.dndbackend.Entity.Chapter;

import java.util.List;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {

    List<Chapter> findByBook_IdOrderByChapterOrderAsc(Long bookId);
}