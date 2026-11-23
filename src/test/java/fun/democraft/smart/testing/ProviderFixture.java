package fun.democraft.smart.testing;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import group.aelysium.rustyconnector.common.modules.Module;
import fun.democraft.smart.ProviderConfig;
import fun.democraft.smart.api.workloads.Provider;
import group.aelysium.rustyconnector.proxy.family.Family;
import group.aelysium.rustyconnector.proxy.family.load_balancing.LoadBalancer;
import group.aelysium.rustyconnector.proxy.family.load_balancing.RoundRobin;
import group.aelysium.rustyconnector.proxy.family.scalar_family.ScalarFamily;
import group.aelysium.rustyconnector.shaded.com.google.code.gson.gson.JsonObject;
import group.aelysium.rustyconnector.shaded.com.google.code.gson.gson.JsonParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class ProviderFixture {
    private record Reply(int status, String body) {}
    protected record Request(String method, String path, Headers headers, JsonObject body) {}
    protected final List<Request> requests = new CopyOnWriteArrayList<>();
    private final Queue<Reply> replies = new ConcurrentLinkedQueue<>();
    private final List<Provider> providers = new ArrayList<>();
    private HttpServer server;

    @BeforeEach
    void startFixture() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            try (exchange) {
                String data = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                requests.add(new Request(exchange.getRequestMethod(), exchange.getRequestURI().toString(),
                        exchange.getRequestHeaders(), data.isEmpty() ? null : json(data)));
                Reply reply = replies.poll();
                if (reply == null) reply = new Reply(500, "unexpected request");
                byte[] bytes = reply.body.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(reply.status, bytes.length == 0 ? -1 : bytes.length);
                exchange.getResponseBody().write(bytes);
            }
        });
        server.start();
    }

    @AfterEach
    void closeFixture() {
        providers.forEach(Provider::close);
        server.stop(0);
    }

    protected Provider track(Provider provider) {
        providers.add(provider);
        return provider;
    }

    protected ProviderConfig config(String id) {
        var config = new ProviderConfig();
        config.providerId = id;
        return config;
    }

    protected Map<String, String> connection(String id) {
        String prefix = "RC_" + id.toUpperCase(Locale.ROOT).replace('-', '_');
        return Map.of(prefix + "_ENDPOINT", "http://127.0.0.1:" + server.getAddress().getPort(),
                prefix + "_TOKEN", "fixture-token");
    }

    protected static Family family(Map<String, Object> smart) throws Exception {
        return new ScalarFamily("lobby", null, null, Map.of("smart", smart),
                new Module.Builder<>("LoadBalancer", "test") {
                    @Override public LoadBalancer get() {
                        return new RoundRobin(false, false, 1, Map.of());
                    }
                });
    }

    protected void reply(int status, String body) {
        replies.add(new Reply(status, body));
    }

    protected static JsonObject json(String body) {
        return JsonParser.parseString(body).getAsJsonObject();
    }

    protected void assertCalls(String... expected) {
        assertEquals(List.of(expected), requests.stream().map(r -> r.method + " " + r.path).toList());
    }
}
