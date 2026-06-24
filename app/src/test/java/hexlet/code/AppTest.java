package hexlet.code;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppTest {
    @Test
    void appHasPackage() {
        assertEquals("hexlet.code", App.class.getPackageName());
    }
}
