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

        // mcmpayment <tebex|craftingstore> <username> <transaction_id> <amount> <currency>
        if (invocation.arguments().length != 5) {
            invocation.source().sendMessage(mm.deserialize("<red>Invalid arguments! Correct usage: mcmpayment <tebex|craftingstore> <username> <transaction_id> <amount> <currency>"));
            return;
        }

        final String platform = invocation.arguments()[0];
        final String username = invocation.arguments()[1];
        final String transaction_id = invocation.arguments()[2];
        String amount = invocation.arguments()[3];
        final String currency = invocation.arguments()[4];

        // transaction fee option from config
        double amountDouble = Double.parseDouble(amount);
        final double paymentFeeOption = plugin.getMainConfig().getDouble("payment-fee");
        if (paymentFeeOption > 0) {
            amountDouble = amountDouble * (1 - paymentFeeOption);
            amount = String.valueOf(amountDouble);
        }

        // validate platform
        if (!platform.equalsIgnoreCase("tebex") && !platform.equalsIgnoreCase("craftingstore")) {
            invocation.source().sendMessage(mm.deserialize("<red>Invalid platform! Correct usage: mcmpayment <tebex|craftingstore> <player_uuid> <transaction_id> <amount> <currency> <package_id>"));
            return;
        }

        PlayerPayment playerPayment = new PlayerPayment(plugin, platform, username, transaction_id, amount, currency);

        // get the payment as a json string
        String jsonString;
        try {
            jsonString = mapper.writeValueAsString(playerPayment);
        } catch (JsonProcessingException ex) {
            LoggerUtil.severe("Error converting incoming payment to json string.");
            throw new RuntimeException(ex);
        }

        LoggerUtil.debug("Uploading payment session now... " + jsonString);

        HttpUtil.makeAsyncPostRequest("https://dashboard.mcmetrics.net/api/payments/insertUsernamePayment", jsonString, HttpUtil.getAuthHeadersFromConfig());
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(final Invocation invocation) {
        return CompletableFuture.completedFuture(List.of("tebex|craftingstore", "username", "transaction_id", "amount", "currency"));
    }
}