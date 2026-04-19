package org.target.dndbackend.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.target.dndbackend.Dto.BookResponse;
import org.target.dndbackend.Dto.CreateBookRequest;
import org.target.dndbackend.Dto.UpdateBookRequest;
import org.target.dndbackend.Entity.Book;
import org.target.dndbackend.Entity.User;
import org.target.dndbackend.Repository.BookRepository;
import org.target.dndbackend.Repository.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;

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

        Book savedBook = bookRepository.save(book);

        return ResponseEntity.ok(toResponse(savedBook));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllBooks(Authentication authentication) {
        List<BookResponse> books = bookRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(books);
    }
    @GetMapping
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

        if (request.getStatus() != null) {
            book.setStatus(request.getStatus());
        }

        Book updatedBook = bookRepository.save(book);

        return ResponseEntity.ok(toResponse(updatedBook));
    }

    @DeleteMapping("/{bookId}")
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
                book.getCreatedAt(),
                book.getUpdatedAt()
        );
    }
}