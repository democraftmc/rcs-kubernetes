package fun.democraft.smart.kubernetes;

import group.aelysium.rustyconnector.common.modules.ExternalModuleBuilder;
import group.aelysium.rustyconnector.common.modules.Module;
import fun.democraft.smart.api.workloads.ProviderBuilder;
import group.aelysium.rustyconnector.proxy.ProxyKernel;

public final class KubernetesBuilder extends ExternalModuleBuilder<Module> {
    @Override
    public Module onStart(Context context) {
        return ProviderBuilder.start("kubernetes", KubernetesProvider::new);
    }

    @Override
    public void bind(ProxyKernel kernel, Module module) {
        ProviderBuilder.bind(kernel, module);
    }
}
