package me.kicksquare.mcmvelocity.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.kicksquare.mcmvelocity.MCMVelocity;
import me.kicksquare.mcmvelocity.util.HttpUtil;
import me.kicksquare.mcmvelocity.util.LoggerUtil;

public class TaskList {
    private static MCMVelocity plugin = MCMVelocity.getPlugin();

    @JsonProperty("recordSessions")
    public boolean recordSessions;
    @JsonProperty("recordPings")
    public boolean recordPings;
    @JsonProperty("pingInterval")
    public int pingInterval;
    @JsonProperty("recordPayments")
    public boolean recordPayments;
    @JsonProperty("executeExperiments")
    public boolean executeExperiments;
    @JsonProperty("globalBans")
    public boolean globalBans;

    public TaskList(@JsonProperty("recordSessions") boolean recordSessions,
                    @JsonProperty("recordPings") boolean recordPings,
                    @JsonProperty("pingInterval") int pingInterval,
                    @JsonProperty("recordPayments") boolean recordPayments,
                    @JsonProperty("executeExperiments") boolean executeExperiments,
                    @JsonProperty("globalBans") boolean globalBans) {
        this.recordSessions = recordSessions;
        this.recordPings = recordPings;
        this.pingInterval = pingInterval;
        this.recordPayments = recordPayments;
        this.executeExperiments = executeExperiments;
        this.globalBans = globalBans;
    }

    public static void fetchTasks() {
        // fetch tasks for this server and save them to memory and config
        HttpUtil.makeAsyncGetRequest("https://dashboard.mcmetrics.net/api/server/getServerTasks", HttpUtil.getAuthHeadersFromConfig()).thenAccept(response -> {
            if (response != null) {
                if (response.contains("ERROR_")) {
                    plugin.getLogger().error("Failed to fetch tasks from server. Response: " + response);
                    return;
                }

                try {
                    ObjectMapper mapper = new ObjectMapper();

                    TaskList tasks = mapper.readValue(response, TaskList.class);
                    plugin.getDataConfig().set("record-pings", tasks.recordPings);
                    plugin.getDataConfig().set("ping-interval", tasks.pingInterval);
                    plugin.getDataConfig().set("global-bans", tasks.globalBans);
                } catch (JsonProcessingException exception) {
                    if (plugin.getMainConfig().getBoolean("debug")) {
                        LoggerUtil.severe("Error occurred while fetching task list: " + exception.getMessage());
                        exception.printStackTrace();
                    }
                }
            }
        });
    }
}
