package hexlet.code.controller;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import kong.unirest.core.Unirest;
import org.jsoup.Jsoup;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public final class UrlController {
    private UrlController() {
    }

    public static void create(Context ctx) {
        var inputUrl = ctx.formParam("url");
        var parsedUrl = parseUrl(inputUrl);

        if (parsedUrl == null) {
            ctx.status(HttpStatus.UNPROCESSABLE_CONTENT).render("index.jte", Map.of(
                    "flash", "Некорректный URL",
                    "flashType", "danger"
            ));
            return;
        }

        var urlName = normalizeUrl(parsedUrl);
        var existingUrl = UrlRepository.findByName(urlName);
        var url = existingUrl.orElseGet(() -> UrlRepository.save(new Url(urlName)));
        var flash = existingUrl.isPresent() ? "Страница уже существует" : "Страница успешно добавлена";

        ctx.sessionAttribute("flash", flash);
        ctx.redirect("/urls/" + url.getId());
    }

    public static void index(Context ctx) {
        var model = getModelWithFlash(ctx);
        model.put("urls", UrlRepository.findAll());
        ctx.render("urls/index.jte", model);
    }

    public static void show(Context ctx) {
        var id = ctx.pathParamAsClass("id", Long.class).get();
        var url = UrlRepository.find(id);

        if (url.isEmpty()) {
            ctx.status(HttpStatus.NOT_FOUND);
            return;
        }

        var model = getModelWithFlash(ctx);
        model.put("url", url.get());
        model.put("checks", UrlCheckRepository.findByUrlId(id));
        ctx.render("urls/show.jte", model);
    }

    public static void check(Context ctx) {
        var id = ctx.pathParamAsClass("id", Long.class).get();
        var url = UrlRepository.find(id);

        if (url.isEmpty()) {
            ctx.status(HttpStatus.NOT_FOUND);
            return;
        }

        try {
            var response = Unirest.get(url.get().getName()).asString();
            var statusCode = response.getStatus();

            if (statusCode >= 400) {
                ctx.sessionAttribute("flash", "Произошла ошибка при проверке");
                ctx.redirect("/urls/" + id);
                return;
            }

            var document = Jsoup.parse(response.getBody());
            var h1 = document.selectFirst("h1");
            var description = document.selectFirst("meta[name=description]");

            UrlCheckRepository.save(new UrlCheck(
                    id,
                    statusCode,
                    h1 == null ? "" : h1.text(),
                    document.title(),
                    description == null ? "" : description.attr("content")
            ));
            ctx.sessionAttribute("flash", "Страница успешно проверена");
        } catch (Exception exception) {
            ctx.sessionAttribute("flash", "Произошла ошибка при проверке");
        }

        ctx.redirect("/urls/" + id);
    }

    public static Map<String, Object> getModelWithFlash(Context ctx) {
        var model = new HashMap<String, Object>();
        var flash = getFlash(ctx);

        if (flash != null && !flash.isBlank()) {
            model.put("flash", flash);
            model.put("flashType", flash.contains("ошибка") || flash.contains("Некорректный") ? "danger" : "success");
        }

        return model;
    }

    private static String getFlash(Context ctx) {
        var flash = ctx.<String>sessionAttribute("flash");
        ctx.sessionAttribute("flash", null);

        return flash;
    }

    private static URL parseUrl(String inputUrl) {
        try {
            var parsedUrl = URI.create(inputUrl).toURL();
            if (parsedUrl.getProtocol() == null || parsedUrl.getHost() == null || parsedUrl.getHost().isBlank()) {
                return null;
            }

            return parsedUrl;
        } catch (Exception exception) {
            return null;
        }
    }

    private static String normalizeUrl(URL parsedUrl) {
        var port = parsedUrl.getPort();
        var portPart = port == -1 ? "" : ":" + port;

        return "%s://%s%s".formatted(parsedUrl.getProtocol(), parsedUrl.getHost(), portPart);
    }
}
