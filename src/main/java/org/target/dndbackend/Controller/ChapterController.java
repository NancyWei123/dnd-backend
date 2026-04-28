package org.target.dndbackend.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.target.dndbackend.Dto.ChapterResponse;
import org.target.dndbackend.Dto.CreateChapterRequest;
import org.target.dndbackend.Dto.UpdateChapterRequest;
import org.target.dndbackend.Entity.Book;
import org.target.dndbackend.Entity.Chapter;
import org.target.dndbackend.Repository.BookReaderPermissionRepository;
import org.target.dndbackend.Repository.BookRepository;
import org.target.dndbackend.Repository.ChapterRepository;

import java.util.List;

@RestController
@RequestMapping("/api/books/{bookId}/chapters")
@RequiredArgsConstructor
public class ChapterController {

    private final ChapterRepository chapterRepository;
    private final BookRepository bookRepository;
    private final BookReaderPermissionRepository bookReaderPermissionRepository;

    @PostMapping
    public ResponseEntity<?> createChapter(
            Authentication authentication,
            @PathVariable Long bookId,
            @RequestBody CreateChapterRequest request
    ) {
        Long userId = (Long) authentication.getPrincipal();

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        if (!book.getUser().getId().equals(userId)) {
            return ResponseEntity.status(403).body("You cannot add chapters to this book");
        }

        Chapter chapter = new Chapter();
        chapter.setBook(book);
        chapter.setTitle(request.getTitle());
        chapter.setContentMd(request.getContentMd());
        chapter.setChapterOrder(request.getChapterOrder());

        Chapter savedChapter = chapterRepository.save(chapter);

        return ResponseEntity.ok(toResponse(savedChapter));
    }

    @GetMapping
    public ResponseEntity<?> getChaptersByBook(
            Authentication authentication,
            @PathVariable Long bookId
    ) {
        Long userId = (Long) authentication.getPrincipal();

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        boolean isOwner = book.getUser().getId().equals(userId);

        boolean isPublic = "public".equalsIgnoreCase(book.getPermission());

        boolean isProtectedReader =
                "protected".equalsIgnoreCase(book.getPermission())
                        && bookReaderPermissionRepository.existsByBook_IdAndUser_Id(bookId, userId);

        if (!isOwner && !isPublic && !isProtectedReader) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("You cannot view chapters of this book");
        }

        List<ChapterResponse> chapters = chapterRepository
                .findByBook_IdOrderByChapterOrderAsc(bookId)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(chapters);
    }

    @GetMapping("/{chapterId}")
    public ResponseEntity<?> getChapterById(
            Authentication authentication,
            @PathVariable Long bookId,
            @PathVariable Long chapterId
    ) {
        Long userId = (Long) authentication.getPrincipal();

        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));

        Book book = chapter.getBook();

        if (!book.getId().equals(bookId)) {
            return ResponseEntity
                    .badRequest()
                    .body("Chapter does not belong to this book");
        }

        boolean isOwner = book.getUser().getId().equals(userId);

        boolean isPublic = "public".equalsIgnoreCase(book.getPermission());

        boolean isProtectedReader =
                "protected".equalsIgnoreCase(book.getPermission())
                        && bookReaderPermissionRepository.existsByBook_IdAndUser_Id(bookId, userId);

        if (!isOwner && !isPublic && !isProtectedReader) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("You cannot view this chapter");
        }

        return ResponseEntity.ok(toResponse(chapter));
    }

    @PutMapping("/{chapterId}")
    public ResponseEntity<?> updateChapter(
            Authentication authentication,
            @PathVariable Long bookId,
            @PathVariable Long chapterId,
            @RequestBody UpdateChapterRequest request
    ) {
        Long userId = (Long) authentication.getPrincipal();

        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));

        if (!chapter.getBook().getId().equals(bookId)) {
            return ResponseEntity.badRequest().body("Chapter does not belong to this book");
        }

        if (!chapter.getBook().getUser().getId().equals(userId)) {
            return ResponseEntity.status(403).body("You cannot update this chapter");
        }

        chapter.setTitle(request.getTitle());
        chapter.setContentMd(request.getContentMd());
        chapter.setChapterOrder(request.getChapterOrder());
        chapter.setMusicUrl(request.getMusicUrl());
        Chapter updatedChapter = chapterRepository.save(chapter);

        return ResponseEntity.ok(toResponse(updatedChapter));
    }

    @DeleteMapping("/{chapterId}")
    public ResponseEntity<?> deleteChapter(
            Authentication authentication,
            @PathVariable Long bookId,
            @PathVariable Long chapterId
    ) {
        Long userId = (Long) authentication.getPrincipal();

        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter not found"));

        if (!chapter.getBook().getId().equals(bookId)) {
            return ResponseEntity.badRequest().body("Chapter does not belong to this book");
        }

        if (!chapter.getBook().getUser().getId().equals(userId)) {
            return ResponseEntity.status(403).body("You cannot delete this chapter");
        }

        chapterRepository.delete(chapter);

        return ResponseEntity.ok("Chapter deleted successfully");
    }

    private ChapterResponse toResponse(Chapter chapter) {
        return new ChapterResponse(
                chapter.getId(),
                chapter.getBook().getId(),
                chapter.getTitle(),
                chapter.getContentMd(),
                chapter.getChapterOrder(),
                chapter.getCreatedAt(),
                chapter.getUpdatedAt(),
                chapter.getMusicUrl()
        );
    }
}