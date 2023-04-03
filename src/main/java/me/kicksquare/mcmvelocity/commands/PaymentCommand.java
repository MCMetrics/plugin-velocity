package me.kicksquare.mcmvelocity.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import me.kicksquare.mcmvelocity.MCMVelocity;
import me.kicksquare.mcmvelocity.types.PlayerPayment;
import me.kicksquare.mcmvelocity.util.HttpUtil;
import me.kicksquare.mcmvelocity.util.LoggerUtil;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PaymentCommand implements SimpleCommand {
    private static MCMVelocity staticPlugin = MCMVelocity.getPlugin();
    MiniMessage mm = MiniMessage.miniMessage();
    private static final ObjectMapper mapper = new ObjectMapper();
    private MCMVelocity plugin;

    public PaymentCommand(MCMVelocity plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof ConsoleCommandSource)) {
            invocation.source().sendMessage(mm.deserialize("<red>This command can only be run from console!"));
            return;
        }

        // mcmpayment <tebex|craftingstore> <player_uuid> <transaction_id> <amount> <currency> <package_id>
        if (invocation.arguments().length != 6) {
            invocation.source().sendMessage(mm.deserialize("<red>Invalid arguments! Correct usage: mcmpayment <tebex|craftingstore> <player_uuid> <transaction_id> <amount> <currency> <package_id>"));
            return;
        }

        String platform = invocation.arguments()[0];
        String player_uuid = invocation.arguments()[1];
        String transaction_id = invocation.arguments()[2];
        String amount = invocation.arguments()[3];
        String currency = invocation.arguments()[4];
        String package_id = invocation.arguments()[5];

        // make sure platform is either tebex or craftingstore
        if (!platform.equalsIgnoreCase("tebex") && !platform.equalsIgnoreCase("craftingstore")) {
            invocation.source().sendMessage(mm.deserialize("<red>Invalid platform! Correct usage: mcmpayment <tebex|craftingstore> <player_uuid> <transaction_id> <amount> <currency> <package_id>"));
            return;
        }

        PlayerPayment playerPayment = new PlayerPayment(plugin, platform, player_uuid, transaction_id, amount, currency, package_id);

        // get the payment as a json string
        String jsonString;
        try {
            jsonString = mapper.writeValueAsString(playerPayment);
        } catch (JsonProcessingException ex) {
            LoggerUtil.severe("Error converting incoming payment to json string.");
            throw new RuntimeException(ex);
        }

        LoggerUtil.debug("Uploading payment session now... " + jsonString);

        HttpUtil.makeAsyncPostRequest("api/payments/insertPayment", jsonString, HttpUtil.getAuthHeadersFromConfig());
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(final Invocation invocation) {
        return CompletableFuture.completedFuture(List.of("tebex|craftingstore", "player_uuid", "transaction_id", "amount", "currency", "package_id"));
    }
}