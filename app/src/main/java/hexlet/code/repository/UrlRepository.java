package hexlet.code.repository;

import hexlet.code.model.Url;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UrlRepository extends BaseRepository {
    public static Url save(String name) {
        var sql = "INSERT INTO urls (name, created_at) VALUES (?, ?)";

        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, name);
            statement.setTimestamp(2, Timestamp.from(Instant.now()));
            statement.executeUpdate();

            try (var generatedKeys = statement.getGeneratedKeys()) {
                generatedKeys.next();

                return find(generatedKeys.getLong(1)).orElseThrow();
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to save url", exception);
        }
    }

    public static Optional<Url> find(Long id) {
        var sql = "SELECT id, name, created_at FROM urls WHERE id = ?";

        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);

            try (var resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(new Url(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getTimestamp("created_at")
                ));
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to find url", exception);
        }
    }

    public static Optional<Url> findByName(String name) {
        var sql = "SELECT id, name, created_at FROM urls WHERE name = ?";

        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);

            try (var resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(new Url(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getTimestamp("created_at")
                ));
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to find url by name", exception);
        }
    }

    public static List<Url> findAll() {
        var sql = "SELECT id, name, created_at FROM urls ORDER BY created_at DESC";
        var urls = new ArrayList<Url>();

        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(sql);
             var resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                urls.add(new Url(
                        resultSet.getLong("id"),
                        resultSet.getString("name"),
                        resultSet.getTimestamp("created_at")
                ));
            }

            return urls;
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to find urls", exception);
        }
    }
}
