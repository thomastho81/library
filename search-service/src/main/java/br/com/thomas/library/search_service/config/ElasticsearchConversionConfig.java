package br.com.thomas.library.search_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchCustomConversions;

import java.util.Arrays;
import java.util.List;

/**
 * Registra conversores de data para o Elasticsearch:
 * <ul>
 *   <li><b>Escrita</b> LocalDateTime → String "yyyy-MM-dd'T'HH:mm:ss": repositório e sync (catalog/inventory) passam a gravar datas no formato correto.</li>
 *   <li><b>Leitura</b> String → LocalDateTime: documentos com datas em string são lidos sem erro na busca.</li>
 * </ul>
 */
@Configuration
public class ElasticsearchConversionConfig {

    @Bean
    public ElasticsearchCustomConversions elasticsearchCustomConversions() {
        List<Object> converters = Arrays.asList(
                new ElasticsearchDateTimeConverters.LocalDateTimeToStringConverter(),
                new ElasticsearchDateTimeConverters.StringToLocalDateTimeConverter()
        );
        return new ElasticsearchCustomConversions(converters);
    }
}
