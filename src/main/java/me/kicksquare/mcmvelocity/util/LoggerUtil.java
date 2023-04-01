package me.kicksquare.mcmvelocity.util;


import me.kicksquare.mcmvelocity.MCMVelocity;

public class LoggerUtil {

    private static final MCMVelocity plugin = MCMVelocity.getPlugin();

    public static void warning(String message) {
        plugin.getLogger().warn(message);
    }

    public static void info(String message) {
        plugin.getLogger().info(message);
    }

    public static void debug(String message) {
        if (plugin.getMainConfig().getBoolean("debug")) {
            plugin.getLogger().info("[DEBUG] " + message);
        }
    }

    public static void severe(String message) {
        plugin.getLogger().error(message);
    }

}