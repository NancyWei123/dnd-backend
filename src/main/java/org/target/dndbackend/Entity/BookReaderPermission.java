package org.target.dndbackend.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.target.dndbackend.Entity.Book;
import org.target.dndbackend.Entity.User;

@Entity
@Table(
        name = "book_reader_permissions",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"book_id", "user_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookReaderPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many permission rows can belong to one book
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    // Many permission rows can belong to one user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}