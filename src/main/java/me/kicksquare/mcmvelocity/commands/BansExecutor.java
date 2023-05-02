package me.kicksquare.mcmvelocity.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import me.kicksquare.mcmvelocity.MCMVelocity;
import me.kicksquare.mcmvelocity.types.bans.GlobalBansResponseEntry;
import me.kicksquare.mcmvelocity.util.HttpUtil;
import me.kicksquare.mcmvelocity.util.LoggerUtil;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Date;

public class BansExecutor {
    private static final MCMVelocity plugin = MCMVelocity.getPlugin();
    static MiniMessage mm = MiniMessage.miniMessage();

    // returns false if the help message should be shown
    public static boolean executeBansSubcommand(CommandSource sender, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (!p.hasPermission("mcmetrics.bans")) {
                p.sendMessage(mm.deserialize("<red>You do not have permission to use this command."));
                return true;
            }
        }

        if (args[1].equalsIgnoreCase("add")) {
            if (args.length < 5) {
                sender.sendMessage(mm.deserialize("<red>Usage: <gray> • <aqua>/mcmetrics bans add <player name/uuid> <reason> <evidence> </gray>- Bans a player using MCMetrics Global Bans"));
                return true;
            }

            String player = args[2];
            String reason = args[3];
            // evidence is args[4] and beyond
            String evidence = "";
            for (int i = 4; i < args.length; i++) {
                evidence += args[i] + " ";
            }
            // remove the space at the end
            evidence = evidence.substring(0, evidence.length() - 1);

            // make sure reason is valid
            if (!(
                    reason.equalsIgnoreCase("DUPE") ||
                            reason.equalsIgnoreCase("LAG") ||
                            reason.equalsIgnoreCase("DISCRIMINATION") ||
                            reason.equalsIgnoreCase("BOTTING")
            )) {
                sender.sendMessage(mm.deserialize("<red>Invalid reason. Valid reasons: <gray>DUPE, LAG, DISCRIMINATION, BOTTING"));
                return true;
            }

            // make a request to the api to get a list of bans for this player
            final String bodyString = "{\"username\": \"" + player + "\", \"reason\": \"" + reason.toUpperCase() + "\", \"evidence\": \"" + evidence + "\"}";
            LoggerUtil.debug("Adding ban for player " + player + ". Body: " + bodyString);
            String finalEvidence = evidence;
            HttpUtil.makeAsyncPostRequest("api/bans/banPlayerFromServer", bodyString, HttpUtil.getAuthHeadersFromConfig())
                    .thenAccept(response -> {
                        if (response == null) {
                            LoggerUtil.warning("Failed to add ban for player " + player + ". Response was null.");
                            return;
                        }

                        sender.sendMessage(mm.deserialize("<green>Successfully banned player <green><bold>" + player + "</bold><green> for reason <green><bold>" + reason + "</bold><green> with evidence <green><bold>" + finalEvidence
                                + "</bold><gray>. Thank you for contributing to the MCMetrics Global Bans database!"));
                    });
            return true;
        } else if (args[1].equalsIgnoreCase("lookup")) {
            if (args.length != 3) {
                sender.sendMessage(mm.deserialize("<red>Usage: <gray> • <aqua>/mcmetrics bans lookup <player name/uuid> </gray>- Looks up a player's bans using MCMetrics Global Bans"));
                return true;
            }

            String player = args[2];

            // make a request to the api to get a list of bans for this player
            final String bodyString = "{\"uuid\": \"" + player + "\"}";
            LoggerUtil.debug("Checking global bans for player " + player + ". Body: " + bodyString);
            sender.sendMessage(mm.deserialize("<green>Checking global bans for player <green><bold>" + player + "</bold><green>..."));
            HttpUtil.makeAsyncPostRequest("api/bans/serverPlayerLookup", bodyString, HttpUtil.getAuthHeadersFromConfig())
                    .thenAccept(response -> {
                        if (response == null) {
                            LoggerUtil.warning("Failed to check global bans for player " + player + ". Response was null.");
                            return;
                        }

                        try {
                            ObjectMapper mapper = new ObjectMapper();

                            GlobalBansResponseEntry[] bans = mapper.readValue(response, GlobalBansResponseEntry[].class);

                            if (bans.length == 0) {
                                sender.sendMessage(mm.deserialize("<gray>Player <aqua><bold>" + player + "</bold><gray> has no bans on record."));
                                return;
                            }

                            sender.sendMessage(mm.deserialize("<gray>Player <aqua><bold>" + player + "</bold><gray> has the following bans on record:"));
                            for (GlobalBansResponseEntry ban : bans) {
                                sender.sendMessage(mm.deserialize("<gray> • <aqua><bold>" + ban.ban_reason + "</bold><gray> with evidence <aqua><bold>" + ban.evidence + "</bold><gray> by <aqua><bold>" + ban.global_bans_identifier + "</bold><gray> on <aqua><bold>" + prettifyDate(ban.ban_time)));
                            }

                        } catch (JsonProcessingException exception) {
                            // if the message contains "Invalid user or server id", don't spam the console and just send one custom error
                            if (response.contains("Invalid user or server id")) {
                                LoggerUtil.severe("Error occurred while fetching player ban status: Invalid user or server id");
                                LoggerUtil.severe("Make sure your server is properly set up by running /mcmetrics setup");
                                return;
                            }
                            if (plugin.getMainConfig().getBoolean("debug")) {
                                LoggerUtil.severe("Error occurred while fetching player ban status: " + exception.getMessage());
                                exception.printStackTrace();
                            }
                        }
                    });

            return true;
        }

        return false;
    }

    private static String prettifyDate(Date date) {
        // "01/01/2021"
        return date.getMonth() + "/" + date.getDay() + "/" + date.getYear();
    }
}