package me.kicksquare.mcmvelocity.types;

import com.fasterxml.jackson.annotation.JsonCreator;
import me.kicksquare.mcmvelocity.MCMVelocity;
import me.kicksquare.mcmvelocity.util.LoggerUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PlayerPayment {

    public final String platform;
    public final String username;
    public final String transaction_id;
    public final String amount;
    public final String currency;
    public final String dateTime;

    public final String bedrockPrefix;

    public final String uid;
    public final String server_id;

    @JsonCreator
    public PlayerPayment(MCMVelocity plugin, String platform, String username, String transaction_id, String amount, String currency) {

        this.platform = platform;
        this.username = username;
        this.transaction_id = transaction_id;
        this.amount = amount;
        this.currency = currency;
        this.dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        this.bedrockPrefix = plugin.getMainConfig().getString("bedrock-prefix");

        this.uid = plugin.getMainConfig().getString("uid");
        this.server_id = plugin.getMainConfig().getString("server_id");

        LoggerUtil.debug("Created new PlayerPayment object for " + username + " with transaction ID " + transaction_id + " and amount " + amount + " " + currency + ".");
    }
}
