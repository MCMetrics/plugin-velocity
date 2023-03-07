package me.kicksquare.mcmvelocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import de.leonhard.storage.Config;
import de.leonhard.storage.SimplixBuilder;
import de.leonhard.storage.internal.settings.DataType;
import de.leonhard.storage.internal.settings.ReloadSettings;
import io.sentry.Sentry;
import me.kicksquare.mcmvelocity.commands.MCMCommand;
import me.kicksquare.mcmvelocity.util.HttpUtil;
import me.kicksquare.mcmvelocity.util.Metrics;
import me.kicksquare.mcmvelocity.util.SetupUtil;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.time.Duration;

@Plugin(
        id = "mcmetrics",
        name = "MCM-Velocity-Plugin",
        version = "1.0.0"
)
public class MCMVelocity {

    private static MCMVelocity plugin;
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private final Metrics.Factory metricsFactory; // bstats
    private Config config;
    private Config dataConfig;

    @Inject
    public MCMVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory, Metrics.Factory metricsFactory) {
        this.server = server;
        this.logger = logger;
        this.metricsFactory = metricsFactory;
        this.dataDirectory = dataDirectory;

        logger.info("MCM-Velocity is starting.");
    }

    public static MCMVelocity getPlugin() {
        return plugin;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        plugin = this;

        logger.info("MCM-Velocity is starting.");

        // setup config
        config = SimplixBuilder
                .fromFile(new File(dataDirectory.toFile(), "config.yml"))
                .addInputStreamFromResource("config.yml")
                .setDataType(DataType.SORTED)
                .setReloadSettings(ReloadSettings.MANUALLY)
                .createConfig();

        dataConfig = SimplixBuilder
                .fromFile(new File(dataDirectory.toFile(), "data/data.yml"))
                .addInputStreamFromResource("data.yml")
                .setDataType(DataType.SORTED)
                .setReloadSettings(ReloadSettings.MANUALLY)
                .createConfig();

        // fetch tasks and save to config
        MCMCommand.reloadConfigAndFetchData();

        // register commands
        CommandManager commandManager = server.getCommandManager();
        CommandMeta mcmMeta = commandManager.metaBuilder("mcmetrics")
                // This will create a new alias for the command "/test"
                // with the same arguments and functionality
                .aliases("mcm", "mcmetricsvelocity", "mcmv", "/mcm", "/mcmetrics")
                .build();
        MCMCommand mcmCommand = new MCMCommand(this);
        commandManager.register(mcmMeta, mcmCommand);

        // enable bstats
        if (getConfig().getBoolean("enable-bstats")) {
            metricsFactory.make(this, 17871);
        }

        // enable sentry error reporting
        if (getConfig().getBoolean("enable-sentry")) {
            Sentry.init(options -> {
                options.setDsn("https://fec1aed58a6f49a19804a5cc71d1a9cb@o4504532201046017.ingest.sentry.io/4504788999077888");
                options.setTracesSampleRate(0.1);
                options.setDebug(false);
            });

            // checks for exceptions matching this plugin name and uploads them to sentry
            Thread.setDefaultUncaughtExceptionHandler(new SentryExceptionHandler());
        }

        // insert pings every 5 mins
        server.getScheduler().buildTask(this, () -> {
                    if (!SetupUtil.shouldRecordPings()) return;

                    try {
                        System.out.println("uploading player count");
                        final String bodyString = "{\"playercount\": \"" + server.getPlayerCount() + "\"}";
                        HttpUtil.makeAsyncPostRequest("https://dashboard.mcmetrics.net/api/pings/insertPing", bodyString, HttpUtil.getAuthHeadersFromConfig());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
                .repeat(Duration.ofMinutes(5))
                .schedule();
    }

    public Logger getLogger() {
        return logger;
    }
    public Config getConfig() {
        return config;
    }
    public Config getDataConfig() {
        return dataConfig;
    }
}