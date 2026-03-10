package br.com.thomas.library.rental_service.service;

import br.com.thomas.library.rental_service.dto.reserve_result.ReserveResultPayload;
import br.com.thomas.library.rental_service.model.Rental;
import br.com.thomas.library.rental_service.model.RentalStatus;
import br.com.thomas.library.rental_service.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Processa evento de resultado da reserva (inventory → rental).
 * Atualiza Rental de PENDING para RESERVED (sucesso) ou RESERVE_FAILED (falha no inventory).
 * Idempotente: só atualiza quando status é PENDING.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReserveResultConsumerService {
//TODO: fazer entryPoints de SAGA nos services de consumo do processo
    private final RentalRepository rentalRepository;

    @Transactional
    public void processResult(ReserveResultPayload payload) {
        if (payload == null || payload.getRentalId() == null) {
            log.warn("Evento de resultado ignorado: payload ou rentalId nulo");
            return;
        }
        Rental rental = rentalRepository.findById(payload.getRentalId()).orElse(null);
        if (rental == null) {
            log.warn("Evento de resultado ignorado: Rental não encontrado rentalId={}", payload.getRentalId());
            return;
        }
        if (rental.getStatus() != RentalStatus.PENDING) {
            log.debug("Evento de resultado já aplicado (idempotência): rentalId={}, status={}", payload.getRentalId(), rental.getStatus());
            return;
        }
        RentalStatus newStatus = Boolean.TRUE.equals(payload.getSuccess()) ? RentalStatus.RESERVED : RentalStatus.RESERVE_FAILED;
        rental.setStatus(newStatus);
        rentalRepository.save(rental);
        log.info("Reserva atualizada: rentalId={}, eventId={}, novo status={}, reason={}", payload.getRentalId(), payload.getEventId(), newStatus, payload.getReason());
    }
}
