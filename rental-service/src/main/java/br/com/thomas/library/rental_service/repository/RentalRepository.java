package br.com.thomas.library.rental_service.repository;

import br.com.thomas.library.rental_service.model.Rental;
import br.com.thomas.library.rental_service.model.RentalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RentalRepository extends JpaRepository<Rental, Long> {

    List<Rental> findByBookIdAndStatus(Long bookId, RentalStatus status);
}
