package br.com.thomas.library.rental_service.service;

import br.com.thomas.library.rental_service.dto.return_result.ReturnResultPayload;
import br.com.thomas.library.rental_service.model.Rental;
import br.com.thomas.library.rental_service.model.RentalStatus;
import br.com.thomas.library.rental_service.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Processa evento de resultado da devolução (inventory → rental).
 * Atualiza Rental de RETURNING (ou RESERVED) para RETURNED (sucesso) e preenche data de devolução.
 * Idempotente: só atualiza quando status é RETURNING ou RESERVED.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReturnResultConsumerService {
//TODO: fazer entryPoints de SAGA nos services de consumo do processo

    private final RentalRepository rentalRepository;

    @Transactional
    public void processResult(ReturnResultPayload payload) {
        if (payload == null || payload.getRentalId() == null) {
            log.warn("Evento de resultado de devolução ignorado: payload ou rentalId nulo");
            return;
        }
        Rental rental = rentalRepository.findById(payload.getRentalId()).orElse(null);
        if (rental == null) {
            log.warn("Evento de resultado de devolução ignorado: Rental não encontrado rentalId={}", payload.getRentalId());
            return;
        }
        if (rental.getStatus() != RentalStatus.RETURNING && rental.getStatus() != RentalStatus.RESERVED) {
            log.debug("Evento de resultado de devolução já aplicado ou não aplicável (idempotência): rentalId={}, status={}", payload.getRentalId(), rental.getStatus());
            return;
        }
        if (!Boolean.TRUE.equals(payload.getSuccess())) {
            log.warn("Devolução rejeitada pelo inventory: rentalId={}, reason={}", payload.getRentalId(), payload.getReason());
            return;
        }
        rental.setStatus(RentalStatus.RETURNED);
        LocalDateTime returnedAt = payload.getProcessedAt() != null ? payload.getProcessedAt() : LocalDateTime.now();
        rental.setReturnedAt(returnedAt);
        rentalRepository.save(rental);
        log.info("Devolução confirmada: rentalId={}, eventId={}, returnedAt={}", payload.getRentalId(), payload.getEventId(), returnedAt);
    }
}
