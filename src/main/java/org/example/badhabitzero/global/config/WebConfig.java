package org.example.badhabitzero.global.config;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.example.badhabitzero.domain.ai.config.ChromaProperties;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebConfig {

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> {
            // 날짜 포맷 (LocalDate)
            builder.serializers(new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_FORMAT)));
            builder.deserializers(new LocalDateDeserializer(DateTimeFormatter.ofPattern(DATE_FORMAT)));

            // 날짜+시간 포맷 (LocalDateTime)
            builder.serializers(new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATETIME_FORMAT)));
            builder.deserializers(new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATETIME_FORMAT)));
        };
    }

    /**
     * Gemini API용 WebClient Bean
     * - 연결 풀링으로 재사용
     * - 타임아웃 설정 (60초 읽기, 30초 쓰기)
     */
    @Bean(name = "geminiWebClient")
    public WebClient geminiWebClient() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(60))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(60, TimeUnit.SECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(30, TimeUnit.SECONDS))
                );

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    /**
     * Chroma API용 WebClient Bean
     * - 연결 풀링으로 재사용
     * - 타임아웃 설정 (10초 읽기, 5초 쓰기)
     */
    @Bean(name = "chromaWebClient")
    public WebClient chromaWebClient(ChromaProperties chromaProperties) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(10))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(10, TimeUnit.SECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(5, TimeUnit.SECONDS))
                );

        return WebClient.builder()
                .baseUrl(chromaProperties.getHost())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}