package br.com.thomas.library.search_service.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DateFormatConstants {

    /** Padrão para LocalDateTime (ex.: 2025-02-17T14:30:00). */
    public static final String LOCAL_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss";

    /** Padrão para LocalDate (ex.: 2025-02-17). */
    public static final String LOCAL_DATE = "yyyy-MM-dd";
}
