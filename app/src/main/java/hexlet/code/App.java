package hexlet.code;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.ResourceCodeResolver;
import hexlet.code.repository.BaseRepository;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinJte;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Map;
import javax.sql.DataSource;

public final class App {
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
    private static final int DEFAULT_PORT = 7070;
    private static final String DEFAULT_DATABASE_URL = "jdbc:h2:mem:project;DB_CLOSE_DELAY=-1";
    private static final String JDBC_DATABASE_URL = "JDBC_DATABASE_URL";

    private App() {
    }

    public static Javalin getApp() {
        var dataSource = getDataSource();
        BaseRepository.setDataSource(dataSource);
        initializeDatabase(dataSource);

        return Javalin.create(config -> {
            config.bundledPlugins.enableDevLogging();
            config.fileRenderer(new JavalinJte(createTemplateEngine()));
            config.routes.get("/", ctx -> ctx.render("index.jte"));
        });
    }

    private static TemplateEngine createTemplateEngine() {
        ClassLoader classLoader = App.class.getClassLoader();
        ResourceCodeResolver codeResolver = new ResourceCodeResolver("templates", classLoader);
        TemplateEngine templateEngine = TemplateEngine.create(codeResolver, ContentType.Html);

        return templateEngine;
    }

    public static void main(String[] args) {
        start(getPort());
    }

    static Javalin start(int port) {
        var app = getApp();

        app.start(port);
        LOGGER.info("Application started on port {}", port);

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
