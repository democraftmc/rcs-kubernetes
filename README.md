<div align="center">

<img src="https://raw.githubusercontent.com/democraftmc/rcm-smart/refs/heads/main/blob/images/banner.png" alt="Rusty Smart Banner" />

# ☸️ Rusty Smart – Kubernetes Provider

**Use Kubernetes to scale your Minecraft network with one Pod per server!**

Requires RustyConnector & Smart

<a href="https://modrinth.com/plugin/rustyconnector"><img src="https://cdn.modrinth.com/data/cached_images/2aa01497a14b7a742a356d7a686118470e0af1bb.png" alt="Get RustyConnector on Modrinth" /></a>
<a href="https://modrinth.com/plugin/rcm-smart"><img src="https://cdn.modrinth.com/data/cached_images/f61969842f4cda7476b6216876acf5fe634c4558.png" alt="Get Rusty Smart on Modrinth" /></a>

[📖 Documentation](https://smart.democraft.fr/docs/providers/official/kubernetes/) · [🧠 Smart Module](https://modrinth.com/plugin/rcm-smart) · [💬 Support](https://dsc.gg/drmcld)

</div>

---

## 📌 Overview

In Rusty Smart, a **provider** connects Smart's scaling logic to an infrastructure platform so it can provision and remove Minecraft servers as demand changes.

The **Kubernetes Provider** creates and deletes Minecraft server Pods through the Kubernetes API. Each server runs in its own Pod in the namespace configured for its family.

## ⚙️ Compatibility

| Requirement | Details |
| --- | --- |
| **Kubernetes** | A cluster exposing the core `v1` Pod API over HTTPS |
| **API access** | A bearer token with permission to create and delete Pods in the target namespace |
| **Server image** | A container image the cluster can pull |
| **Proxy modules** | RustyConnector, [Rusty Smart 2.0.0](https://modrinth.com/plugin/rcm-smart), and a configured Haze driver |

## 🧱 Installation

1. Place the provider JAR in the `rc-modules` folder of your Minecraft proxy.
2. Set `RC_KUBERNETES_ENDPOINT` to the API server base URL and `RC_KUBERNETES_TOKEN` to the bearer token in the proxy environment. Set `RC_KUBERNETES_CA` to the PEM CA bundle if the API uses a private CA.
3. Create `rustyconnector/smart/providers/kubernetes.yml` with `providerId: kubernetes` and include the families you want Smart to scale.
4. Set `namespace` and `image` under `metadata.smart.kubernetes` in each family's YAML.
5. Make sure the proxy can reach the Pod IPs on TCP 25565, then restart the proxy.

Use a RustyConnector-enabled server image or bootstrap so each new server can join its configured family.

## 🔨 Build from source

Run `gradle clean build` with Java 21 and Gradle. Gradle resolves Smart 2.0.0 from the Funa releases repository and writes the provider JAR to `build/libs/`.

## 💬 Need help?

- 📖 Read the [Kubernetes Provider Documentation](https://smart.democraft.fr/docs/providers/official/kubernetes/) for configuration examples and the metadata reference.
- 💬 Join the [Rusty Smart Discord](https://dsc.gg/drmcld) for community support.
