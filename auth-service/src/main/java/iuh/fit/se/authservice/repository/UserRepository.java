package iuh.fit.se.authservice.repository;

import iuh.fit.se.authservice.entities.Role;
import iuh.fit.se.authservice.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);

    Optional<User> findByRole(Role role);

}
