package hexlet.code.dto;

import hexlet.code.model.Url;

import java.time.LocalDateTime;

public class UrlDto {
    private final Url url;
    private final LocalDateTime lastCheckCreatedAt;
    private final Integer lastStatusCode;

    public UrlDto(Url url, LocalDateTime lastCheckCreatedAt, Integer lastStatusCode) {
        this.url = url;
        this.lastCheckCreatedAt = lastCheckCreatedAt;
        this.lastStatusCode = lastStatusCode;
    }

    public Url getUrl() {
        return url;
    }

    public Long getId() {
        return url.getId();
    }

    public String getName() {
        return url.getName();
    }

    public LocalDateTime getLastCheckCreatedAt() {
        return lastCheckCreatedAt;
    }

    public Integer getLastStatusCode() {
        return lastStatusCode;
    }
}
