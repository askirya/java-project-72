package hexlet.code;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import hexlet.code.controller.UrlController;
import hexlet.code.repository.BaseRepository;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinJte;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Map;
import javax.sql.DataSource;

@Slf4j
public final class App {
    private static final int DEFAULT_PORT = 7070;
    private static final String DEFAULT_DATABASE_URL = "jdbc:h2:mem:project;DB_CLOSE_DELAY=-1";
    private static final String JDBC_DATABASE_URL = "JDBC_DATABASE_URL";

    private App() {
    }

    public static Javalin getApp() {
        return getApp(getDataSource());
    }

    static Javalin getApp(DataSource dataSource) {
        BaseRepository.setDataSource(dataSource);
        initializeDatabase(dataSource);

        return Javalin.create(config -> {
            config.bundledPlugins.enableDevLogging();
            config.fileRenderer(new JavalinJte(createTemplateEngine()));
            config.routes.get("/", ctx -> ctx.render("index.jte", UrlController.getModelWithFlash(ctx)));
            config.routes.post("/urls", UrlController::create);
            config.routes.post("/urls/{id}/checks", UrlController::check);
            config.routes.get("/urls", UrlController::index);
            config.routes.get("/urls/{id}", UrlController::show);
        });
    }

    private static TemplateEngine createTemplateEngine() {
        return TemplateEngine.createPrecompiled(ContentType.Html);
    }

    public static void main(String[] args) {
        start(getPort());
    }

    static Javalin start(int port) {
        return start(port, getDataSource());
    }

    static Javalin start(int port, DataSource dataSource) {
        var app = getApp(dataSource);

        app.start(port);
        log.info("Application started on port {}", port);

        return app;
    }

    static int getPort() {
        return getPort(System.getenv("PORT"));
    }

    static int getPort(String port) {
        return port == null ? DEFAULT_PORT : Integer.parseInt(port);
    }

    private static HikariDataSource getDataSource() {
        var config = new HikariConfig();
        config.setJdbcUrl(getDatabaseUrl(System.getenv()));

        return new HikariDataSource(config);
    }

    static String getDatabaseUrl(Map<String, String> environment) {
        return environment.getOrDefault(JDBC_DATABASE_URL, DEFAULT_DATABASE_URL);
    }

    private static void initializeDatabase(DataSource dataSource) {
        initializeDatabase(dataSource, "schema.sql");
    }

    static void initializeDatabase(DataSource dataSource, String schemaPath) {
        try (var connection = dataSource.getConnection();
             var inputStream = App.class.getClassLoader().getResourceAsStream(schemaPath)) {
            if (inputStream == null) {
                throw new IllegalStateException("schema.sql not found");
            }

            var reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            var sql = new StringBuilder();

            reader.lines().forEach(line -> sql.append(line).append(System.lineSeparator()));

            try (var statement = connection.createStatement()) {
                for (var query : sql.toString().split(";")) {
                    if (!query.isBlank()) {
                        statement.execute(query);
                    }
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to initialize database", exception);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to read database schema", exception);
        }
    }
}
