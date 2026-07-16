package hexlet.code.model;

import java.time.LocalDateTime;

public class UrlCheck {
    private static final int PREVIEW_LENGTH = 200;

    private Long id;
    private Long urlId;
    private Integer statusCode;
    private String h1;
    private String title;
    private String description;
    private LocalDateTime createdAt;

    public UrlCheck() {
    }

    public UrlCheck(Long urlId, Integer statusCode, String h1, String title, String description) {
        this.urlId = urlId;
        this.statusCode = statusCode;
        this.h1 = h1;
        this.title = title;
        this.description = description;
    }

    public UrlCheck(Long id, Long urlId, Integer statusCode, String h1, String title,
                    String description, LocalDateTime createdAt) {
        this.id = id;
        this.urlId = urlId;
        this.statusCode = statusCode;
        this.h1 = h1;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUrlId() {
        return urlId;
    }

    public void setUrlId(Long urlId) {
        this.urlId = urlId;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public String getH1() {
        return h1;
    }

    public void setH1(String h1) {
        this.h1 = h1;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getH1Preview() {
        return truncate(h1);
    }

    public String getTitlePreview() {
        return truncate(title);
    }

    public String getDescriptionPreview() {
        return truncate(description);
    }

    private static String truncate(String value) {
        if (value == null || value.length() <= PREVIEW_LENGTH) {
            return value;
        }

        return value.substring(0, PREVIEW_LENGTH) + "...";
    }
}
