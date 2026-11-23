package fun.democraft.smart.kubernetes;

import fun.democraft.smart.ProviderConfig;
import fun.democraft.smart.api.workloads.Api;
import fun.democraft.smart.api.workloads.Provider;
import fun.democraft.smart.api.workloads.Settings;
import group.aelysium.rustyconnector.proxy.family.Family;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class KubernetesProvider extends Provider {
    public KubernetesProvider(ProviderConfig config) {
        this(config, System.getenv());
    }

    KubernetesProvider(ProviderConfig config, Map<String, String> connection) {
        super(config, Api.configured(config.providerId, false, "Authorization", connection));
    }

    private static String pods(String namespace) {
        return "/api/v1/namespaces/" + Api.identifier(namespace) + "/pods";
    }

    @Override
    public String provision(Family family) throws Exception {
        var s = Settings.read(family, id(), Set.of("image", "namespace"));
        String namespace = s.string("namespace");
        // Smart persists namespace/name in a 64-character column. smart- + five generated characters.
        if (namespace.length() > 52 || !namespace.matches("[a-z0-9](?:[-a-z0-9]*[a-z0-9])?"))
            throw new IllegalArgumentException(s.root() + ".namespace must be a DNS label of at most 52 characters");
        var quantities = Map.of("memory", s.memoryBytes() / Settings.BYTES_PER_MB + "M", "cpu", s.cpu(1000, 1000) + "m");
        var env = s.env().entrySet().stream().map(e -> Map.of("name", e.getKey(), "value", e.getValue().replace("$", "$$"))).toList();
        var container = Map.of("name", "server", "image", s.string("image"), "env", env,
                "resources", Map.of("requests", quantities, "limits", quantities));
        String reference = UUID.randomUUID().toString();
        var pod = Map.of("apiVersion", "v1", "kind", "Pod",
                "metadata", Map.of("generateName", "smart-", "labels", Map.of("smart-request", reference)),
                "spec", Map.of("restartPolicy", "Never", "automountServiceAccountToken", false, "containers", List.of(container)));
        return namespace + "/" + create(pods(namespace), pod, reference, "metadata", "name");
    }

    @Override
    public void deprovision(String id) throws Exception {
        String[] parts = id.split("/", -1);
        if (parts.length != 2) throw new IllegalArgumentException("Invalid namespaced Pod identifier");
        api.call("DELETE", pods(parts[0]) + "/" + Api.identifier(parts[1]), null, true);
    }
}
