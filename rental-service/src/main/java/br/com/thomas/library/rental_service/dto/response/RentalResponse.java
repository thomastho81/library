package br.com.thomas.library.rental_service.dto.response;

import br.com.thomas.library.rental_service.model.RentalStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RentalResponse {

    private Long id;
    private Long bookId;
    private Integer quantity;
    private RentalStatus status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime reservedAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime returnedAt;

    /** Link para consultar status (GET /api/rentals/{id}). */
    private Map<String, String> _links;
}
