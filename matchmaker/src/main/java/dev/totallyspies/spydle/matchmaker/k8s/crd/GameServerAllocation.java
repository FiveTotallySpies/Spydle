package dev.totallyspies.spydle.matchmaker.k8s.crd;

import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.openapi.models.V1ObjectMeta;

/**
 * Agones CRD for allocating game servers
 */
public class GameServerAllocation implements KubernetesObject {
    @Override
    public V1ObjectMeta getMetadata() {
        return new V1ObjectMeta();
    }

    @Override
    public String getApiVersion() {
        return "allocation.agones.dev/v1";
    }

    @Override
    public String getKind() {
        return "GameServerAllocation";
    }
}
