package hexlet.code;

import hexlet.code.repository.BaseRepository;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.CookieManager;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
            assertTrue(response.body().contains("<h1 class=\"display-3\">Анализатор страниц</h1>"));
            assertTrue(response.body().contains("<form action=\"/urls\" method=\"post\" class=\"row\">"));
            assertTrue(response.body().contains("name=\"url\""));
        } finally {
            app.stop();
        }
    }

    @Test
    void appCreatesAndShowsUrl() throws Exception {
        var app = App.start(0);

        try {
            var client = HttpClient.newBuilder()
                    .cookieHandler(new CookieManager())
                    .followRedirects(Redirect.NORMAL)
                    .build();
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + app.port() + "/urls"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("url=https://example.com/path"))
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode());
            assertTrue(response.body().contains("Страница успешно добавлена"));
            assertTrue(response.body().contains("<table class=\"table table-bordered\" data-test=\"url\">"));
            assertTrue(response.body().contains("<td>https://example.com</td>"));

            var listRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + app.port() + "/urls"))
                    .GET()
                    .build();
            var listResponse = client.send(listRequest, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, listResponse.statusCode());
            assertTrue(listResponse.body().contains("<table class=\"table table-bordered\" data-test=\"urls\">"));
            assertTrue(listResponse.body().contains("<td><a href=\"/urls/1\">https://example.com</a></td>"));
        } finally {
            app.stop();
        }
    }

    @Test
    void appDoesNotCreateDuplicateUrl() throws Exception {
        var app = App.start(0);

        try {
            var client = HttpClient.newBuilder()
                    .cookieHandler(new CookieManager())
                    .followRedirects(Redirect.NORMAL)
                    .build();
            var uri = URI.create("http://localhost:" + app.port() + "/urls");
            var firstRequest = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("url=https://example.com/first"))
                    .build();
            var secondRequest = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("url=https://example.com/second"))
                    .build();

            client.send(firstRequest, HttpResponse.BodyHandlers.ofString());
            var response = client.send(secondRequest, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode());
            assertTrue(response.body().contains("Страница уже существует"));

            try (var connection = BaseRepository.getDataSource().getConnection();
                 var statement = connection.createStatement();
                 var resultSet = statement.executeQuery("SELECT COUNT(*) FROM urls")) {
                resultSet.next();
                assertEquals(1, resultSet.getInt(1));
            }
        } finally {
            app.stop();
        }
    }

    @Test
    void appReturnsValidationErrorForInvalidUrl() throws Exception {
        var app = App.start(0);

        try {
            var client = HttpClient.newHttpClient();
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + app.port() + "/urls"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("url=invalid-url"))
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(422, response.statusCode());
            assertTrue(response.body().contains("Некорректный URL"));
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
