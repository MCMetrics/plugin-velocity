package me.kicksquare.mcmvelocity.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import me.kicksquare.mcmvelocity.MCMVelocity;
import me.kicksquare.mcmvelocity.types.TaskList;
import me.kicksquare.mcmvelocity.util.HttpUtil;
import me.kicksquare.mcmvelocity.util.SetupUtil;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MCMCommand implements SimpleCommand {
    private static MCMVelocity staticPlugin = MCMVelocity.getPlugin();
    MiniMessage mm = MiniMessage.miniMessage();
    private MCMVelocity plugin;

    public MCMCommand(MCMVelocity plugin) {
        this.plugin = plugin;
    }

    public static CompletableFuture<Boolean> reloadConfigAndFetchData() {
        return CompletableFuture.supplyAsync(() -> {
            staticPlugin.getMainConfig().forceReload();

            if (SetupUtil.isSetup()) {
                TaskList.fetchTasks();
            } else {
                staticPlugin.getLogger().warn("Reloaded plugin, but the plugin is not configured! Please run /mcmetrics setup <user id> <server id> to configure the plugin.");
            }

            return true;
        });
    }

    @Override
    public void execute(Invocation invocation) {
        if (invocation.arguments().length == 1) {
            if (invocation.arguments()[0].equalsIgnoreCase("reload")) {
                reloadConfigAndFetchData().thenAccept((result) -> {
                    if (result) {
                        plugin.uploadPlayerCount(); // manually force upload player count
                        invocation.source().sendMessage(mm.deserialize("<green>Successfully reloaded the config!"));
                        if(plugin.getMainConfig().getBoolean("enable-sentry")) return; // already enabled

                        if (invocation.source() != null && !(invocation.source() instanceof ConsoleCommandSource)) {
                            invocation.source().sendMessage(
                                    mm.deserialize("<gray>Optional Sentry Opt-In: Click <blue>here</blue> to enable anonymous error-reporting via Sentry (you can change this later in the config).")
                                            .clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/mcmetrics enablesentry")));
                        } else {
                            invocation.source().sendMessage(mm.deserialize("<gray>Optional Sentry Opt-In: Run <blue>'mcmetrics enablesentry'</blue> to enable anonymous error-reporting via Sentry (you can change this later in the config)."));
                        }

                    } else {
                        invocation.source().sendMessage(mm.deserialize("<red>Failed to reload the config!"));
                    }
                });
                return;
            } else if (invocation.arguments()[0].equalsIgnoreCase("enablesentry")) {
                plugin.getMainConfig().set("enable-sentry", true);
                invocation.source().sendMessage(mm.deserialize("<green>Successfully enabled Sentry!"));
                return;
            } else if (invocation.arguments()[0].equalsIgnoreCase("setup")) {
                invocation.source().sendMessage(mm.deserialize("<red>Usage: /mcmetrics setup <user id> <server id>"));
                return;
            }
        } else if (invocation.arguments().length == 3) {
            if (invocation.arguments()[0].equalsIgnoreCase("setup")) {
                final String uid = invocation.arguments()[1];
                final String serverId = invocation.arguments()[2];

                if (uid.length() != 25) {
                    invocation.source().sendMessage(mm.deserialize("<red>Invalid user id! User ID must be 25 characters long!"));
                    return;
                } else if (serverId.length() != 36) {
                    invocation.source().sendMessage(mm.deserialize("<red>Invalid server id! Server ID must be 36 characters long!"));
                    return;
                }

                // set config key "uid" to uid, and "server_id" to serverId, and "setup-complete" to true
                plugin.getMainConfig().set("uid", uid);
                plugin.getMainConfig().set("server_id", serverId);
                plugin.getDataConfig().set("setup-complete", true);
                reloadConfigAndFetchData();

                // set server connected to true
                HttpUtil.makeAsyncGetRequest("https://dashboard.mcmetrics.net/api/server/setServerIsSetup", HttpUtil.getAuthHeadersFromConfig());

                invocation.source().sendMessage(mm.deserialize("<green>Successfully configured the plugin!"));
                return;
            }
        }

        invocation.source().sendMessage(mm.deserialize("<yellow><bold>MCMetrics</bold>"));
        invocation.source().sendMessage(mm.deserialize("<gray>Plugin Commands:"));
        invocation.source().sendMessage(mm.deserialize("<gray> • <blue>/mcmetrics reload</blue> - Reloads the config"));
        invocation.source().sendMessage(mm.deserialize("<gray> • <blue>/mcmetrics setup <user id> <server id></blue> - Automatically configures the plugin"));
    }

    @Override
    public boolean hasPermission(final Invocation invocation) {
        return invocation.source() instanceof ConsoleCommandSource || invocation.source().hasPermission("mcmetrics.command");
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(final Invocation invocation) {
        return CompletableFuture.completedFuture(List.of("reload"));
    }
}
