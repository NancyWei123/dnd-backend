package org.target.dndbackend.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.target.dndbackend.Dto.UserNameAndEmail;
import org.target.dndbackend.Entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("""
    SELECT new org.target.dndbackend.Dto.UserNameAndEmail(
        u.id,
        u.username,
        u.email
    )
    FROM User u
""")
    List<UserNameAndEmail> findAllNameAndEmail();
}