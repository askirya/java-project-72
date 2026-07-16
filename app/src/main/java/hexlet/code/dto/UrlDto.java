package hexlet.code.dto;

import java.time.LocalDateTime;

public class UrlDto {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime lastCheckCreatedAt;
    private Integer lastStatusCode;

    public UrlDto() {
    }

    public UrlDto(Long id, String name, LocalDateTime createdAt,
                  LocalDateTime lastCheckCreatedAt, Integer lastStatusCode) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.lastCheckCreatedAt = lastCheckCreatedAt;
        this.lastStatusCode = lastStatusCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastCheckCreatedAt() {
        return lastCheckCreatedAt;
    }

    public void setLastCheckCreatedAt(LocalDateTime lastCheckCreatedAt) {
        this.lastCheckCreatedAt = lastCheckCreatedAt;
    }

    public Integer getLastStatusCode() {
        return lastStatusCode;
    }

    public void setLastStatusCode(Integer lastStatusCode) {
        this.lastStatusCode = lastStatusCode;
    }
}
