package br.com.thomas.library.rental_service.repository;

import br.com.thomas.library.rental_service.model.User;
import br.com.thomas.library.rental_service.model.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByIdAndActiveTrue(Long id);

    Page<User> findByProfileAndActiveTrue(UserProfile profile, Pageable pageable);
}
