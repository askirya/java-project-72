package hexlet.code.repository;

import hexlet.code.model.UrlCheck;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UrlCheckRepository extends BaseRepository {
    public static UrlCheck save(UrlCheck urlCheck) {
        var sql = """
                INSERT INTO url_checks (url_id, status_code, h1, title, description, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            urlCheck.setCreatedAt(LocalDateTime.now());
            statement.setLong(1, urlCheck.getUrlId());
            statement.setInt(2, urlCheck.getStatusCode());
            statement.setString(3, urlCheck.getH1());
            statement.setString(4, urlCheck.getTitle());
            statement.setString(5, urlCheck.getDescription());
            statement.setTimestamp(6, Timestamp.valueOf(urlCheck.getCreatedAt()));
            statement.executeUpdate();

            try (var generatedKeys = statement.getGeneratedKeys()) {
                generatedKeys.next();
                urlCheck.setId(generatedKeys.getLong(1));
                return urlCheck;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to save url check", exception);
        }
    }

    public static Optional<UrlCheck> find(Long id) {
        var sql = """
                SELECT id, url_id, status_code, h1, title, description, created_at
                FROM url_checks
                WHERE id = ?
                """;

        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);

            try (var resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(new UrlCheck(
                        resultSet.getLong("id"),
                        resultSet.getLong("url_id"),
                        resultSet.getInt("status_code"),
                        resultSet.getString("h1"),
                        resultSet.getString("title"),
                        resultSet.getString("description"),
                        resultSet.getTimestamp("created_at").toLocalDateTime()
                ));
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to find url check", exception);
        }
    }

    public static List<UrlCheck> findByUrlId(Long urlId) {
        var sql = """
                SELECT id, url_id, status_code, h1, title, description, created_at
                FROM url_checks
                WHERE url_id = ?
                ORDER BY created_at DESC, id DESC
                """;
        var checks = new ArrayList<UrlCheck>();

        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, urlId);

            try (var resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    checks.add(new UrlCheck(
                            resultSet.getLong("id"),
                            resultSet.getLong("url_id"),
                            resultSet.getInt("status_code"),
                            resultSet.getString("h1"),
                            resultSet.getString("title"),
                            resultSet.getString("description"),
                            resultSet.getTimestamp("created_at").toLocalDateTime()
                    ));
                }

                return checks;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to find url checks", exception);
        }
    }
}
