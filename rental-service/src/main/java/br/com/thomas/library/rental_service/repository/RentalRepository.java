package br.com.thomas.library.rental_service.repository;

import br.com.thomas.library.rental_service.model.Rental;
import br.com.thomas.library.rental_service.model.RentalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    
    Page<Rental> findByStatus(RentalStatus status, Pageable pageable);

    Page<Rental> findByUserId(Long userId, Pageable pageable);

    Page<Rental> findByUserIdAndStatusIn(Long userId, List<RentalStatus> statuses, Pageable pageable);

    /**
     * Lista por usuário com intervalo de data (reservedAt) e status.
     * O service passa LocalDateTime.MIN/MAX quando um limite não foi informado, para evitar
     * "could not determine data type of parameter" no PostgreSQL com parâmetros null.
     * statuses não deve ser vazio.
     */
    @Query("SELECT r FROM Rental r WHERE r.userId = :userId " +
            "AND r.reservedAt >= :reservedAtFrom AND r.reservedAt <= :reservedAtTo " +
            "AND r.status IN :statuses")
    Page<Rental> findByUserIdAndReservedAtBetweenAndStatusIn(
            @Param("userId") Long userId,
            @Param("reservedAtFrom") LocalDateTime reservedAtFrom,
            @Param("reservedAtTo") LocalDateTime reservedAtTo,
            @Param("statuses") List<RentalStatus> statuses,
            Pageable pageable);
}
