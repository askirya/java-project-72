package hexlet.code.model;

import java.time.LocalDateTime;

public class Url {
    private Long id;
    private String name;
    private LocalDateTime createdAt;

    public Url() {
    }

    public Url(String name) {
        this.name = name;
    }

    public Url(Long id, String name, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
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
}
