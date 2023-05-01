package me.kicksquare.mcmvelocity.types.bans;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.kicksquare.mcmvelocity.MCMVelocity;

public class GlobalBansResponseEntry {
    private static final MCMVelocity plugin = MCMVelocity.getPlugin();

    @JsonProperty("player_uuid")
    public String player_uuid;
    @JsonProperty("ban_reason")
    public BanReason ban_reason;
    @JsonProperty("id")
    public String id;

    public GlobalBansResponseEntry(@JsonProperty("player_uuid") String player_uuid, @JsonProperty("ban_reason") BanReason ban_reason, @JsonProperty("id") String id) {
        this.player_uuid = player_uuid;
        this.ban_reason = ban_reason;
        this.id = id;
    }
}