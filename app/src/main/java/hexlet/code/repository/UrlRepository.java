package hexlet.code.repository;

import hexlet.code.dto.UrlDto;
import hexlet.code.model.Url;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UrlRepository extends BaseRepository {
    public static Url save(Url url) {
        var sql = "INSERT INTO urls (name, created_at) VALUES (?, ?)";

        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            url.setCreatedAt(LocalDateTime.now());
            statement.setString(1, url.getName());
            statement.setTimestamp(2, Timestamp.valueOf(url.getCreatedAt()));
            statement.executeUpdate();

            try (var generatedKeys = statement.getGeneratedKeys()) {
                generatedKeys.next();
                url.setId(generatedKeys.getLong(1));
                return url;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to save url", exception);
        }
    }

    public static Optional<Url> find(Long id) {
        return findOne("SELECT id, name, created_at FROM urls WHERE id = ?",
                statement -> statement.setLong(1, id),
                "Failed to find url");
    }

    public static Optional<Url> findByName(String name) {
        return findOne("SELECT id, name, created_at FROM urls WHERE name = ?",
                statement -> statement.setString(1, name),
                "Failed to find url by name");
    }

    public static List<UrlDto> findAll() {
        var sql = """
                SELECT urls.id, urls.name, urls.created_at, checks.created_at AS last_check_created_at,
                       checks.status_code AS last_status_code
                FROM urls
                LEFT JOIN (
                    SELECT url_checks.*
                    FROM url_checks
                    INNER JOIN (
                        SELECT url_id, MAX(id) AS id
                        FROM url_checks
                        GROUP BY url_id
                    ) latest_checks ON latest_checks.id = url_checks.id
                ) checks ON checks.url_id = urls.id
                ORDER BY urls.created_at DESC
                """;
        var urls = new ArrayList<UrlDto>();

        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(sql);
             var resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                urls.add(new UrlDto(
                        mapUrl(resultSet),
                        toLocalDateTime(resultSet.getTimestamp("last_check_created_at")),
                        resultSet.getObject("last_status_code", Integer.class)
                ));
            }

            return urls;
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to find urls", exception);
        }
    }

    private static Optional<Url> findOne(String sql, SqlConsumer binder, String errorMessage) {
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(sql)) {
            binder.accept(statement);

            try (var resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(mapUrl(resultSet));
            }
        } catch (SQLException exception) {
            throw new RuntimeException(errorMessage, exception);
        }
    }

    private static Url mapUrl(ResultSet resultSet) throws SQLException {
        return new Url(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                toLocalDateTime(resultSet.getTimestamp("created_at"))
        );
    }

    private static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    @FunctionalInterface
    private interface SqlConsumer {
        void accept(PreparedStatement statement) throws SQLException;
    }
}
