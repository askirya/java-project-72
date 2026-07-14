package hexlet.code.repository;

import hexlet.code.model.UrlCheck;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class UrlCheckRepository extends BaseRepository {
    public static UrlCheck save(Long urlId, Integer statusCode, String h1, String title, String description) {
        var sql = """
                INSERT INTO url_checks (url_id, status_code, h1, title, description, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, urlId);
            statement.setInt(2, statusCode);
            statement.setString(3, h1);
            statement.setString(4, title);
            statement.setString(5, description);
            statement.setTimestamp(6, Timestamp.from(Instant.now()));
            statement.executeUpdate();

            try (var generatedKeys = statement.getGeneratedKeys()) {
                generatedKeys.next();

                return find(generatedKeys.getLong(1));
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to save url check", exception);
        }
    }

    public static UrlCheck find(Long id) {
        var sql = """
                SELECT id, url_id, status_code, h1, title, description, created_at
                FROM url_checks
                WHERE id = ?
                """;

        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);

            try (var resultSet = statement.executeQuery()) {
                resultSet.next();

                return new UrlCheck(
                        resultSet.getLong("id"),
                        resultSet.getLong("url_id"),
                        resultSet.getInt("status_code"),
                        resultSet.getString("h1"),
                        resultSet.getString("title"),
                        resultSet.getString("description"),
                        resultSet.getTimestamp("created_at")
                );
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
                            resultSet.getTimestamp("created_at")
                    ));
                }

                return checks;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to find url checks", exception);
        }
    }
}
