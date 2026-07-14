package hexlet.code.model;

import java.sql.Timestamp;

public class Url {
    private Long id;
    private String name;
    private Timestamp createdAt;
    private Timestamp lastCheckCreatedAt;
    private Integer lastStatusCode;

    public Url() {
    }

    public Url(Long id, String name, Timestamp createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public Url(Long id, String name, Timestamp createdAt, Timestamp lastCheckCreatedAt, Integer lastStatusCode) {
        this(id, name, createdAt);
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

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getLastCheckCreatedAt() {
        return lastCheckCreatedAt;
    }

    public void setLastCheckCreatedAt(Timestamp lastCheckCreatedAt) {
        this.lastCheckCreatedAt = lastCheckCreatedAt;
    }

    public Integer getLastStatusCode() {
        return lastStatusCode;
    }

    public void setLastStatusCode(Integer lastStatusCode) {
        this.lastStatusCode = lastStatusCode;
    }
}
