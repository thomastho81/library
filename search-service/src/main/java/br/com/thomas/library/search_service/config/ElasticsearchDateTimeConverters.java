package br.com.thomas.library.search_service.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Conversores de data/hora para o Elasticsearch.
 * <ul>
 *   <li><b>Escrita</b>: LocalDateTime → string "yyyy-MM-dd'T'HH:mm:ss" (evita gravar só "yyyy-MM-dd" e quebrar a conversão na busca).</li>
 *   <li><b>Leitura</b>: string do ES → LocalDateTime; aceita "yyyy-MM-dd" ou "yyyy-MM-dd'T'HH:mm:ss".</li>
 * </ul>
 */
public final class ElasticsearchDateTimeConverters {

    /** Formato exigido pelo índice (pattern em BookDocument e conversão na busca). */
    public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT);
    private static final DateTimeFormatter DATE_ONLY = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ROOT);

    @WritingConverter
    public static class LocalDateTimeToStringConverter implements Converter<LocalDateTime, String> {
        @Override
        public String convert(LocalDateTime source) {
            return source == null ? null : source.format(DATE_TIME);
        }
    }

    @ReadingConverter
    public static class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {
        @Override
        public LocalDateTime convert(String source) {
            if (source == null || source.isBlank()) {
                return null;
            }
            String trimmed = source.trim();
            if (trimmed.length() == 10) {
                return LocalDate.parse(trimmed, DATE_ONLY).atStartOfDay();
            }
            return LocalDateTime.parse(trimmed, DATE_TIME);
        }
    }

    private ElasticsearchDateTimeConverters() {}
}
