package fun.democraft.smart.kubernetes;

import fun.democraft.smart.api.SmartProvider;
import fun.democraft.smart.testing.ProviderFixture;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KubernetesTest extends ProviderFixture {
    private SmartProvider provider() {
        return track(new KubernetesProvider(config("kubernetes"), connection("kubernetes")));
    }

    @Test void submitsOnePodAndDeletesItByNamespaceAndName() throws Exception {
        var family = family(Map.of("memory", 1024, "cpu", 0.375, "kubernetes", Map.of("namespace", "games",
                "memory", 512, "image", "paper:21", "env", Map.of("MESSAGE", "$(literal)"))));
        reply(201, "{\"metadata\":{\"namespace\":\"games\",\"name\":\"smart-123xy\"}}");
        reply(404, "{}");

        String id = provider().provision(family);
        assertEquals("games/smart-123xy", id);
        provider().deprovision(id);
        assertCalls("POST /api/v1/namespaces/games/pods", "DELETE /api/v1/namespaces/games/pods/smart-123xy");
        assertEquals("Bearer fixture-token", requests.getFirst().headers().getFirst("Authorization"));
        var pod = requests.getFirst().body();
        assertEquals("v1", pod.get("apiVersion").getAsString());
        assertEquals("Pod", pod.get("kind").getAsString());
        var spec = pod.getAsJsonObject("spec");
        assertEquals("Never", spec.get("restartPolicy").getAsString());
        var container = spec.getAsJsonArray("containers").get(0).getAsJsonObject();
        assertEquals("paper:21", container.get("image").getAsString());
        assertEquals(json("{\"name\":\"MESSAGE\",\"value\":\"$$(literal)\"}"), container.getAsJsonArray("env").get(0));
        var resources = container.getAsJsonObject("resources");
        assertEquals(json("{\"memory\":\"512M\",\"cpu\":\"375m\"}"), resources.get("requests"));
        assertEquals(resources.get("requests"), resources.get("limits"));
    }

    @Test void rejectsInvalidNamespaceBeforeCreate() throws Exception {
        var family = family(Map.of("kubernetes", Map.of("namespace", "a/b", "image", "paper:21")));
        var error = assertThrows(IllegalArgumentException.class, () -> provider().provision(family));
        assertTrue(error.getMessage().contains("smart.kubernetes.namespace"));
        assertCalls();
    }
}
