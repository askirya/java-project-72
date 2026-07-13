package hexlet.code;

import hexlet.code.repository.BaseRepository;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppTest {
    @Test
    void appHasPackage() {
        assertEquals("hexlet.code", App.class.getPackageName());
    }

    @Test
    void rootReturnsHelloWorld() throws Exception {
        var app = App.start(0);

        try {
            var client = HttpClient.newHttpClient();
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + app.port() + "/"))
                    .GET()
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode());
            assertEquals("Hello World", response.body());
        } finally {
            app.stop();
        }
    }

    @Test
    void appInitializesDatabase() throws Exception {
        App.getApp();

        var dataSource = BaseRepository.getDataSource();
        assertNotNull(dataSource);

        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement();
             var resultSet = statement.executeQuery("SELECT COUNT(*) FROM urls")) {
            resultSet.next();
            assertEquals(0, resultSet.getInt(1));
        }
    }

    @Test
    void getPortReturnsDefaultPort() {
        assertEquals(7070, App.getPort(null));
    }

    @Test
    void getPortReturnsPortFromEnvironment() {
        assertEquals(7070, App.getPort());
    }

    @Test
    void getPortReturnsEnvironmentPort() {
        assertEquals(8080, App.getPort("8080"));
    }

    @Test
    void getDatabaseUrlReturnsDefaultUrl() {
        assertEquals("jdbc:h2:mem:project;DB_CLOSE_DELAY=-1", App.getDatabaseUrl(Map.of()));
    }

    @Test
    void getDatabaseUrlReturnsEnvironmentUrl() {
        var databaseUrl = "jdbc:postgresql://db:5432/postgres?password=password&user=postgres";
        var environment = Map.of("JDBC_DATABASE_URL", databaseUrl);

        assertEquals(databaseUrl, App.getDatabaseUrl(environment));
    }

    @Test
    void initializeDatabaseThrowsWhenSchemaIsMissing() {
        App.getApp();

        var dataSource = BaseRepository.getDataSource();
        var exception = assertThrows(RuntimeException.class, () ->
                App.initializeDatabase(dataSource, "missing-schema.sql"));

        assertEquals("Failed to read database schema", exception.getMessage());
    }
}
