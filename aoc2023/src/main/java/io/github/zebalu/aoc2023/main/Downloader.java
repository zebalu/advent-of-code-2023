package io.github.zebalu.aoc2023.main;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Instant;
import java.util.stream.IntStream;

class Downloader {

    private final String sessionId;

    Downloader(String sessionId) {
        this.sessionId = sessionId;
    }

    void downloadInputs() {
        var httpClient = createHttpClient();
        IntStream.rangeClosed(1, 25)
                .filter(i -> Instant.parse(String.format("2023-12-%02dT05:00:00Z", i)).isBefore(Instant.now()))
                .forEach(i -> {
                    try {
                        System.out.println("Downloading Day "+i);
                        var httpRequest = HttpRequest.newBuilder().header("session", sessionId).GET()
                                .uri(new URI("https://adventofcode.com/2023/day/" + i + "/input")).build();
                        var data = httpClient.send(httpRequest, BodyHandlers.ofString()).body();
                        var target = new File(String.format("day%02d.txt", i));
                        try (var pw = new PrintWriter(target)) {
                            pw.print(data);
                        }
                    } catch (IOException | InterruptedException | URISyntaxException e) {
                        throw new IllegalStateException("Could not download input: " + i, e);
                    }
                });
    }

    private HttpClient createHttpClient() {
        try {
            var sessionCookie = new HttpCookie("session", sessionId);
            sessionCookie.setPath("/");
            sessionCookie.setVersion(0);
            var cookieManager = new CookieManager();
            cookieManager.getCookieStore().add(new URI("https://adventofcode.com/"), sessionCookie);
            return HttpClient.newBuilder().cookieHandler(cookieManager).build();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
