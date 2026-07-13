package hexlet.code;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppTest {
    @Test
    void appHasPackage() {
        assertEquals("hexlet.code", App.class.getPackageName());
    }

    @Test
    void rootReturnsHelloWorld() throws Exception {
        var app = App.getApp().start(0);

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
}
