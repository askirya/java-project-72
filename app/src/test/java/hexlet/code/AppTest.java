package hexlet.code;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.BaseRepository;
import io.javalin.Javalin;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppTest {
    private static MockWebServer mockServer;

    @BeforeAll
    static void startMockServer() throws Exception {
        mockServer = new MockWebServer();
        mockServer.start();
    }

    @AfterAll
    static void stopMockServer() throws Exception {
        mockServer.close();
    }

    @Test
    void appHasPackage() {
        assertEquals("hexlet.code", App.class.getPackageName());
    }

    @Test
    void modelsStoreFields() {
        var now = new Timestamp(System.currentTimeMillis());
        var url = new Url();
        url.setId(1L);
        url.setName("https://example.com");
        url.setCreatedAt(now);
        url.setLastCheckCreatedAt(now);
        url.setLastStatusCode(200);

        assertEquals(1L, url.getId());
        assertEquals("https://example.com", url.getName());
        assertEquals(now, url.getCreatedAt());
        assertEquals(now, url.getLastCheckCreatedAt());
        assertEquals(200, url.getLastStatusCode());

        var longValue = "a".repeat(201);
        var preview = "a".repeat(200) + "...";
        var check = new UrlCheck();
        check.setId(2L);
        check.setUrlId(1L);
        check.setStatusCode(200);
        check.setH1(longValue);
        check.setTitle(longValue);
        check.setDescription(longValue);
        check.setCreatedAt(now);

        assertEquals(2L, check.getId());
        assertEquals(1L, check.getUrlId());
        assertEquals(200, check.getStatusCode());
        assertEquals(longValue, check.getH1());
        assertEquals(longValue, check.getTitle());
        assertEquals(longValue, check.getDescription());
        assertEquals(now, check.getCreatedAt());
        assertEquals(preview, check.getH1Preview());
        assertEquals(preview, check.getTitlePreview());
        assertEquals(preview, check.getDescriptionPreview());
    }

    @Test
    void rootReturnsHelloWorld() throws Exception {
        var dataSource = getTestDataSource();
        var app = startTestApp(dataSource);

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
            dataSource.close();
        }
    }

    @Test
    void appCreatesAndShowsUrl() throws Exception {
        var dataSource = getTestDataSource();
        var app = startTestApp(dataSource);

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
            assertTrue(response.body().contains("<form method=\"post\" action=\"/urls/1/checks\">"));
            assertTrue(response.body().contains("value=\"Запустить проверку\""));
            assertTrue(response.body().contains("data-test=\"checks\""));
            assertEquals("/urls/1", response.uri().getPath());

            try (var connection = dataSource.getConnection();
                 var statement = connection.createStatement();
                 var resultSet = statement.executeQuery("SELECT id, name FROM urls")) {
                resultSet.next();
                assertEquals(1, resultSet.getLong("id"));
                assertEquals("https://example.com", resultSet.getString("name"));
            }

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
            dataSource.close();
        }
    }

    @Test
    void appDoesNotCreateDuplicateUrl() throws Exception {
        var dataSource = getTestDataSource();
        var app = startTestApp(dataSource);

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
            assertEquals("/urls/1", response.uri().getPath());

            try (var connection = dataSource.getConnection();
                 var statement = connection.createStatement();
                 var resultSet = statement.executeQuery("SELECT COUNT(*), MAX(name) FROM urls")) {
                resultSet.next();
                assertEquals(1, resultSet.getInt(1));
                assertEquals("https://example.com", resultSet.getString(2));
            }
        } finally {
            app.stop();
            dataSource.close();
        }
    }

    @Test
    void appReturnsValidationErrorForInvalidUrl() throws Exception {
        var dataSource = getTestDataSource();
        var app = startTestApp(dataSource);

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
            dataSource.close();
        }
    }

    @Test
    void appReturnsValidationErrorForUrlWithoutHost() throws Exception {
        var dataSource = getTestDataSource();
        var app = startTestApp(dataSource);

        try {
            var client = HttpClient.newHttpClient();
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + app.port() + "/urls"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("url=mailto:test@example.com"))
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(422, response.statusCode());
            assertTrue(response.body().contains("Некорректный URL"));
        } finally {
            app.stop();
            dataSource.close();
        }
    }

    @Test
    void appKeepsUrlPort() throws Exception {
        var dataSource = getTestDataSource();
        var app = startTestApp(dataSource);

        try {
            var client = HttpClient.newBuilder()
                    .cookieHandler(new CookieManager())
                    .followRedirects(Redirect.NORMAL)
                    .build();
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + app.port() + "/urls"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("url=https://example.com:8080/path"))
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode());
            assertTrue(response.body().contains("<td>https://example.com:8080</td>"));
        } finally {
            app.stop();
            dataSource.close();
        }
    }

    @Test
    void appReturnsNotFoundForMissingUrl() throws Exception {
        var dataSource = getTestDataSource();
        var app = startTestApp(dataSource);

        try {
            var client = HttpClient.newHttpClient();
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + app.port() + "/urls/999"))
                    .GET()
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(404, response.statusCode());
        } finally {
            app.stop();
            dataSource.close();
        }
    }

    @Test
    void appRedirectsFromCheckHandler() throws Exception {
        var dataSource = getTestDataSource();
        var app = startTestApp(dataSource);

        try {
            mockServer.enqueue(new MockResponse.Builder()
                    .code(200)
                    .body("<html><head><title>Checked page</title></head><body><h1>Hello</h1></body></html>")
                    .build());
            var client = HttpClient.newBuilder()
                    .cookieHandler(new CookieManager())
                    .followRedirects(Redirect.NEVER)
                    .build();
            var createRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + app.port() + "/urls"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("url=" + mockServer.url("/").toString()))
                    .build();
            client.send(createRequest, HttpResponse.BodyHandlers.ofString());

            var checkRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + app.port() + "/urls/1/checks"))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            var response = client.send(checkRequest, HttpResponse.BodyHandlers.ofString());

            assertEquals(302, response.statusCode());
            assertEquals("/urls/1", response.headers().firstValue("Location").orElse(""));
        } finally {
            app.stop();
            dataSource.close();
        }
    }

    @Test
    void appCreatesUrlCheck() throws Exception {
        var dataSource = getTestDataSource();
        var app = startTestApp(dataSource);
        var description = "Statements of great people";

        try {
            mockServer.enqueue(new MockResponse.Builder()
                    .code(200)
                    .body("""
                            <html>
                              <head>
                                <title>Awesome page</title>
                                <meta name="description" content="%s">
                              </head>
                              <body>
                                <h1>Do not expect a miracle, miracles yourself!</h1>
                              </body>
                            </html>
                            """.formatted(description))
                    .build());
            var client = HttpClient.newBuilder()
                    .cookieHandler(new CookieManager())
                    .followRedirects(Redirect.NORMAL)
                    .build();
            var createRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + app.port() + "/urls"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("url=" + mockServer.url("/success").toString()))
                    .build();
            client.send(createRequest, HttpResponse.BodyHandlers.ofString());

            var checkRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + app.port() + "/urls/1/checks"))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            var response = client.send(checkRequest, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode());
            assertTrue(response.body().contains("Страница успешно проверена"));
            assertTrue(response.body().contains("<td>200</td>"));
            assertTrue(response.body().contains("<td>Do not expect a miracle, miracles yourself!</td>"));
            assertTrue(response.body().contains("<td>Awesome page</td>"));
            assertTrue(response.body().contains("<td>" + description + "</td>"));

            try (var connection = dataSource.getConnection();
                 var statement = connection.createStatement();
                 var resultSet = statement.executeQuery("""
                         SELECT status_code, h1, title, description, url_id
                         FROM url_checks
                         """)) {
                resultSet.next();
                assertEquals(200, resultSet.getInt("status_code"));
                assertEquals("Do not expect a miracle, miracles yourself!", resultSet.getString("h1"));
                assertEquals("Awesome page", resultSet.getString("title"));
                assertEquals(description, resultSet.getString("description"));
                assertEquals(1, resultSet.getLong("url_id"));
            }

            var listRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + app.port() + "/urls"))
                    .GET()
                    .build();
            var listResponse = client.send(listRequest, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, listResponse.statusCode());
            assertTrue(listResponse.body().contains("<th>Последняя проверка</th>"));
            assertTrue(listResponse.body().contains("<td>200</td>"));
        } finally {
            app.stop();
            dataSource.close();
        }
    }

    @Test
    void appTruncatesLongCheckValues() throws Exception {
        var dataSource = getTestDataSource();
        var app = startTestApp(dataSource);
        var longValue = "a".repeat(201);
        var preview = "a".repeat(200) + "...";

        try {
            mockServer.enqueue(new MockResponse.Builder()
                    .code(200)
                    .body("""
                            <html>
                              <head>
                                <title>%s</title>
                                <meta name="description" content="%s">
                              </head>
                              <body><h1>%s</h1></body>
                            </html>
                            """.formatted(longValue, longValue, longValue))
                    .build());
            var client = HttpClient.newBuilder()
                    .cookieHandler(new CookieManager())
                    .followRedirects(Redirect.NORMAL)
                    .build();
            var createRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + app.port() + "/urls"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("url=" + mockServer.url("/long").toString()))
                    .build();
            client.send(createRequest, HttpResponse.BodyHandlers.ofString());

            var checkRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + app.port() + "/urls/1/checks"))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            var response = client.send(checkRequest, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode());
            assertTrue(response.body().contains("<td>" + preview + "</td>"));
        } finally {
            app.stop();
            dataSource.close();
        }
    }

    @Test
    void appDoesNotCreateUrlCheckWhenRequestFails() throws Exception {
        var dataSource = getTestDataSource();
        var app = startTestApp(dataSource);

        try {
            mockServer.enqueue(new MockResponse.Builder()
                    .code(500)
                    .body("Server error")
                    .build());
            var client = HttpClient.newBuilder()
                    .cookieHandler(new CookieManager())
                    .followRedirects(Redirect.NORMAL)
                    .build();
            var createRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + app.port() + "/urls"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("url=" + mockServer.url("/wrong").toString()))
                    .build();
            client.send(createRequest, HttpResponse.BodyHandlers.ofString());

            var checkRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + app.port() + "/urls/1/checks"))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            var response = client.send(checkRequest, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode());
            assertTrue(response.body().contains("Произошла ошибка при проверке"));

            try (var connection = dataSource.getConnection();
                 var statement = connection.createStatement();
                 var resultSet = statement.executeQuery("SELECT COUNT(*) FROM url_checks")) {
                resultSet.next();
                assertEquals(0, resultSet.getInt(1));
            }
        } finally {
            app.stop();
            dataSource.close();
        }
    }

    @Test
    void appReturnsNotFoundFromCheckHandlerForMissingUrl() throws Exception {
        var dataSource = getTestDataSource();
        var app = startTestApp(dataSource);

        try {
            var client = HttpClient.newHttpClient();
            var request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + app.port() + "/urls/999/checks"))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(404, response.statusCode());
        } finally {
            app.stop();
            dataSource.close();
        }
    }

    @Test
    void appInitializesDatabase() throws Exception {
        var dataSource = getTestDataSource();
        App.getApp(dataSource);

        try {
            assertNotNull(BaseRepository.getDataSource());

            try (var connection = dataSource.getConnection();
                 var statement = connection.createStatement();
                 var resultSet = statement.executeQuery("SELECT COUNT(*) FROM urls")) {
                resultSet.next();
                assertEquals(0, resultSet.getInt(1));
            }
        } finally {
            dataSource.close();
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
        var dataSource = getTestDataSource();

        try {
            var exception = assertThrows(RuntimeException.class, () ->
                    App.initializeDatabase(dataSource, "missing-schema.sql"));

            assertEquals("Failed to read database schema", exception.getMessage());
        } finally {
            dataSource.close();
        }
    }

    private static Javalin startTestApp(HikariDataSource dataSource) {
        return App.start(0, dataSource);
    }

    private static HikariDataSource getTestDataSource() {
        var config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:test" + System.nanoTime() + ";DB_CLOSE_DELAY=-1");

        return new HikariDataSource(config);
    }
}
