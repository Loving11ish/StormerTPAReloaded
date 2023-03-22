package me.loving11ish.stormertpareloaded.lang;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

public enum Lang {

    PREFIX_COMMAND("§3[§6STPA§3]§2"),
    PREFIX_ERROR("§3[§4Error§3]§c"),

    ERROR_PLAYERONLY("Sorry, but only a player may run this command"),
    ERROR_MISSING_PLAYER_ARG("You need to specify the player you wish to send the request to"),
    ERROR_NOPLAYER("No player with such name"),
    ERROR_REQUEST_PENDING("This player already has a pending request"),
    ERROR_NO_REQUEST_PENDING("You currently do not have any request pending"),

    TPA_CANCELLED("The teleport request has been cancelled"),
    TPA_CANCELLED_MOVE("You moved! Teleportation cancelled..."),
    TPA_CANCELLED_UNSAFE("Teleport destination is unsafe, cancelling..."),
    TPA_REQUEST_SENT("Request sent to <PLAYER>, it will expire in <SECONDS> seconds"),
    TPA_REQUEST_RECEIVED_TPA("<PLAYER> has sent you a request to teleport to you, type /tpaccept to accept or /tpdeny to refuse, it will expire in <SECONDS> seconds"),
    TPA_REQUEST_RECEIVED_TPAHERE("<PLAYER> has sent you a request to teleport to you to them, type /tpaccept to accept or /tpdeny to refuse, it will expire in <SECONDS> seconds"),
    TPA_REQUEST_EXPIRED_RECEIVED("The request from <PLAYER> expired"),
    TPA_REQUEST_EXPIRED_SENT("The request sent to <PLAYER> expired"),
    TPA_REQUEST_REFUSED_RECEIVED("you have refused the teleport request of <PLAYER>"),
    TPA_REQUEST_REFUSED_SENT("<PLAYER> has refused your teleportation request"),
    TPA_REQUEST_ACCEPTED_FROM("Request accepted, teleporting you to <PLAYER>"),
    TPA_REQUEST_ACCEPTED_TO("Request accepted, teleporting <PLAYER> to you"),

    ;

    private String path;
    private String def;
    private static YamlConfiguration LANG;

    Lang(String d){
        this.path = this.name();
        this.def = d;
    }

    public static void setFile(YamlConfiguration config) {
        LANG = config;
    }

    @Override
    public String toString() {
        return ChatColor.translateAlternateColorCodes('&', LANG.getString(this.path, this.def));
    }

    public String getPath() {
        return path;
    }

    public String getDef() {
        return def;
    }
}
