package org.target.dndbackend.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.target.dndbackend.Dto.BookResponse;
import org.target.dndbackend.Dto.CreateBookRequest;
import org.target.dndbackend.Dto.UpdateBookReadersRequest;
import org.target.dndbackend.Dto.UpdateBookRequest;
import org.target.dndbackend.Entity.Book;
import org.target.dndbackend.Entity.BookReaderPermission;
import org.target.dndbackend.Entity.User;
import org.target.dndbackend.Repository.BookReaderPermissionRepository;
import org.target.dndbackend.Repository.BookRepository;
import org.target.dndbackend.Repository.UserRepository;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BookReaderPermissionRepository bookReaderPermissionRepository;

    @GetMapping("/{bookId}/readers")
    public ResponseEntity<?> getBookReaders(@PathVariable Long bookId) {
        List<BookReaderPermission> permissions =
                bookReaderPermissionRepository.findByBook_Id(bookId);

        List<Map<String, Object>> readers = permissions.stream()
                .map(permission -> {
                    User user = permission.getUser();

                    return Map.<String, Object>of(
                            "id", user.getId(),
                            "username", user.getUsername(),
                            "email", user.getEmail()
                    );
                })
                .toList();

        return ResponseEntity.ok(readers);
    }

    @PostMapping("/{bookId}/readers")
    public ResponseEntity<?> updateBookReaders(
            Authentication authentication,
            @PathVariable Long bookId,
            @RequestBody UpdateBookReadersRequest request
    ) {
        Long userId = (Long) authentication.getPrincipal();

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        if (!book.getUser().getId().equals(userId)) {
            return ResponseEntity.status(403).body("You cannot update readers of this book");
        }

        bookReaderPermissionRepository.deleteByBook_Id(bookId);

        for (Long readerId : request.getUserIds()) {
            User reader = userRepository.findById(readerId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            BookReaderPermission permission = new BookReaderPermission();
            permission.setBook(book);
            permission.setUser(reader);

            bookReaderPermissionRepository.save(permission);
        }

        return ResponseEntity.ok("Book readers updated successfully");
    }
    @PostMapping
    public ResponseEntity<?> createBook(
            Authentication authentication,
            @RequestBody CreateBookRequest request
    ) {
        Long userId = (Long) authentication.getPrincipal();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Book book = new Book();
        book.setUser(user);
        book.setTitle(request.getTitle());
        book.setDescription(request.getDescription());
        book.setCoverUrl(request.getCoverUrl());

        if (request.getStatus() != null) {
            book.setStatus(request.getStatus());
        }

        if (request.getPermission() == null || request.getPermission().isBlank()) {
            book.setPermission("private");
        } else {
            book.setPermission(request.getPermission());
        }

        Book savedBook = bookRepository.save(book);

        return ResponseEntity.ok(toResponse(savedBook));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllBooks(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<BookResponse> books = bookRepository.findReadableBooksByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(books);
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyBooks(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();

        List<BookResponse> books = bookRepository.findByUser_Id(userId)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(books);
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<?> getBookById(
            Authentication authentication,
            @PathVariable Long bookId
    ) {
        Long userId = (Long) authentication.getPrincipal();

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        if (!book.getUser().getId().equals(userId)) {
            return ResponseEntity.status(403).body("You cannot access this book");
        }

        return ResponseEntity.ok(toResponse(book));
    }

    @PutMapping("/{bookId}")
    @Transactional
    public ResponseEntity<?> updateBook(
            Authentication authentication,
            @PathVariable Long bookId,
            @RequestBody UpdateBookRequest request
    ) {
        Long userId = (Long) authentication.getPrincipal();

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        if (!book.getUser().getId().equals(userId)) {
            return ResponseEntity.status(403).body("You cannot update this book");
        }

        book.setTitle(request.getTitle());
        book.setDescription(request.getDescription());
        book.setCoverUrl(request.getCoverUrl());
        book.setPermission(request.getPermission());

        if (request.getStatus() != null) {
            book.setStatus(request.getStatus());
        }

        Book updatedBook = bookRepository.save(book);

        // Save selected readers
        bookReaderPermissionRepository.deleteByBook_Id(bookId);

        if ("protected".equalsIgnoreCase(request.getPermission())
                && request.getSelectedReaderIds() != null) {

            for (Long readerId : request.getSelectedReaderIds()) {
                User reader = userRepository.findById(readerId)
                        .orElseThrow(() -> new RuntimeException("User not found: " + readerId));

                BookReaderPermission permission = new BookReaderPermission();
                permission.setBook(updatedBook);
                permission.setUser(reader);

                bookReaderPermissionRepository.save(permission);
            }
        }

        return ResponseEntity.ok(toResponse(updatedBook));
    }

    @DeleteMapping("/{bookId}")
    @Transactional
    public ResponseEntity<?> deleteBook(
            Authentication authentication,
            @PathVariable Long bookId
    ) {
        Long userId = (Long) authentication.getPrincipal();

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        if (!book.getUser().getId().equals(userId)) {
            return ResponseEntity.status(403).body("You cannot delete this book");
        }

        bookReaderPermissionRepository.deleteByBook_Id(bookId);
        bookRepository.delete(book);

        return ResponseEntity.ok("Book deleted successfully");
    }

    private BookResponse toResponse(Book book) {
        return new BookResponse(
                book.getId(),
                book.getUser().getId(),
                book.getTitle(),
                book.getDescription(),
                book.getCoverUrl(),
                book.getStatus(),
                book.getPermission(),
                book.getCreatedAt(),
                book.getUpdatedAt()
        );
    }
}