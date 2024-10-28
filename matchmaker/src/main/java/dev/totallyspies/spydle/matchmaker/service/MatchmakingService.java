package dev.totallyspies.spydle.matchmaker.service;

import dev.totallyspies.spydle.matchmaker.k8s.crd.GameServerAllocation;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.ModelMapper;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Responsible for handling logic for all of our service endpoints
 */
@Service
public class MatchmakingService {

    @Autowired
    private SessionRepository sessionRepository;

    private CustomObjectsApi apiInstance;

    private Map<String, Object> allocation;

    public MatchmakingService(@Value("classpath:allocation.yml") Resource allocationResource) throws Exception {
        ApiClient client = Config.defaultClient();
        io.kubernetes.client.openapi.Configuration.setDefaultApiClient(client);
        apiInstance = new CustomObjectsApi(client);

        ModelMapper.addModelMap("allocation.agones.dev", "v1", "GameServerAllocation", "GameServerAllocations", false, GameServerAllocation.class);
        String allocationContent = IOUtils.toString(allocationResource.getInputStream(), Charset.defaultCharset());
//        allocation = (Map<String, Object>) Yaml.load(allocationContent);
        allocation = new Yaml().load(allocationContent);
    }

    public GameServerInfo createGame(String clientId) throws ApiException {
        // Check if client already has a session
        if (sessionRepository.sessionExists(clientId)) throw new IllegalStateException("Client is already in a game.");

        String namespace = "spydle";

        Map<String, Object> result = (Map<String, Object>) apiInstance.createNamespacedCustomObject(
                "allocation.agones.dev",
                "v1",
                namespace,
                "gameserverallocations",
                allocation
        ).execute();

        // TODO Verify this is the correct message schema
        Map<String, Object> status = (Map<String, Object>) result.get("status");
        if (status == null || !"Allocated".equals(status.get("State"))) {
            throw new IllegalStateException("No available GameServers.");
        }

        String gameServerName = (String) status.get("GameServerName");
        String address = (String) status.get("Address");
        Map<String, Object> ports = ((Map<String, Object>) ((java.util.List) status.get("Ports")).get(0));
        int port = ((Number) ports.get("port")).intValue();

        // Save client session
        ClientSession session = new ClientSession(clientId, gameServerName);
        sessionRepository.saveSession(session);

        // Return GameServer info
        GameServerInfo gameServerInfo = new GameServerInfo();
        gameServerInfo.setAddress(address);
        gameServerInfo.setPort(port);
        gameServerInfo.setGameServerName(gameServerName);

        return gameServerInfo;
    }

    public GameServerInfo joinGame(String clientId, String gameServerName) {
        // Check if client already has a session
        if (sessionRepository.sessionExists(clientId)) {
            throw new IllegalStateException("Client is already in a game.");
        }

        // TODO: Validate that the gameServerName is valid and has capacity

        // Save client session
        ClientSession session = new ClientSession(clientId, gameServerName);
        sessionRepository.saveSession(session);

        // TODO: Get NodePort address for gameservers and grab port from redis

        // Retrieve GameServer info (This assumes you have a way to get the address and port)
        // For demonstration purposes, we'll mock this data
        GameServerInfo gameServerInfo = new GameServerInfo();
        gameServerInfo.setAddress("game-server-address");
        gameServerInfo.setPort(12345);
        gameServerInfo.setGameServerName(gameServerName);

        return gameServerInfo;
    }

    public void leaveGame(String clientId) {
        // Delete client session
        sessionRepository.deleteSession(clientId);

        // TODO: Notify the GameServer (Optional)
        // TODO: Implement logic to notify the GameServer that the client is leaving
    }

    public Map<String, Object> autoscale(Map<String, Object> request) {
        System.out.println(prettyPrint(request));
        Map<String, Object> status = (Map<String, Object>) request.get("status");
        int allocatedReplicas = (int) status.get("allocatedReplicas");

        // TODO Load custom scaling logic from config
        int desiredIdleReplicas = Math.max(4, (int) (allocatedReplicas * 0.2));
        int desiredReplicas = allocatedReplicas + desiredIdleReplicas;

        Map<String, Object> response = new HashMap<>();
        Map<String, Integer> scale = new HashMap<>();
        scale.put("replicas", desiredReplicas);
        response.put("scale", scale);

        return response;
    }

    private static String prettyPrint(Map<String, Object> yaml) {
        StringBuilder builder = new StringBuilder("{");
        for (String key : yaml.keySet()) {
            Object val = yaml.get(key);
            builder.append(key).append(":");
            if (val instanceof Map) {
                builder.append(prettyPrint((Map<String, Object>) val));
            } else {
                builder.append(val);
            }
            builder.append(",");
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.append("}").toString();
    }
}