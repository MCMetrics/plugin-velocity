package me.kicksquare.mcmvelocity.util;

import de.leonhard.storage.Config;
import me.kicksquare.mcmvelocity.MCMVelocity;

public class SetupUtil {

    private static MCMVelocity plugin = MCMVelocity.getPlugin();

    public static boolean isSetup() {
        Config config = plugin.getMainConfig();
        Config dataConfig = plugin.getDataConfig();

        return dataConfig.getBoolean("setup-complete") &&
                !(config.getString("server_id").equals("") || config.getString("uid") == "");
    }

    public static boolean shouldRecordPings() {
        Config config = plugin.getMainConfig();
        Config dataConfig = plugin.getDataConfig();
        return isSetup() && dataConfig.getBoolean("record-pings");
    }
}
