package me.loving11ish.stormertpareloaded;

import me.loving11ish.stormertpareloaded.lang.Lang;
import me.loving11ish.stormertpareloaded.lang.Message;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class StormerTPAReloaded extends JavaPlugin implements Listener {

    public static StormerTPAReloaded i;

    public static int teleportationTicksDelay = 0;
    public static int teleportRequestDuration = 0;
    public static boolean requiresImmobile = false;

    public static List<Material> unsafeTypes = new ArrayList<>();

    @Override
    public void onEnable() {
        //Plugin startup logic
        i = this;
        super.onEnable();

        getCommand("tpa").setExecutor(this);
        getCommand("tpa").setTabCompleter(this);

        ArrayList<String> tpahere_aliases = new ArrayList<>();
        tpahere_aliases.add("tpah");
        getCommand("tpahere").setAliases(tpahere_aliases);
        getCommand("tpahere").setExecutor(this);
        getCommand("tpahere").setTabCompleter(this);

        ArrayList<String> tpaccept_aliases = new ArrayList<>();
        tpaccept_aliases.add("tpyes");
        getCommand("tpaccept").setAliases(tpaccept_aliases);
        getCommand("tpaccept").setExecutor(this);
        getCommand("tpaccept").setTabCompleter(this);

        ArrayList<String> tpdeny_aliases = new ArrayList<>();
        tpdeny_aliases.add("tpno");
        getCommand("tpdeny").setAliases(tpdeny_aliases);
        getCommand("tpdeny").setExecutor(this);
        getCommand("tpdeny").setTabCompleter(this);

        getServer().getPluginManager().registerEvents(this, this);
        loadConfig();
        Message.instantiateLang(i);
    }

    @Override
    public void onDisable() {
        //Plugin shutdown logic

        //Stop background tasks
        try {
            if (Bukkit.getScheduler().isCurrentlyRunning(TeleportRequest.taskID1)||Bukkit.getScheduler().isQueued(TeleportRequest.taskID1)){
                Bukkit.getScheduler().cancelTask(TeleportRequest.taskID1);
            }
            if (Bukkit.getScheduler().isCurrentlyRunning(TeleportRequest.taskID2)||Bukkit.getScheduler().isQueued(TeleportRequest.taskID2)){
                Bukkit.getScheduler().cancelTask(TeleportRequest.taskID2);
            }
            Message.systemNormal("All background tasks disabled successfully!");
        }catch (Exception e){
            Message.systemNormal("All background tasks disabled successfully!");
        }
    }

    public void loadConfig() {
        getConfig().options().copyDefaults(true);
        saveConfig();
        teleportationTicksDelay = getConfig().getInt("teleportationTicksDelay");
        teleportRequestDuration = getConfig().getInt("teleportRequestDuration");
        requiresImmobile = getConfig().getBoolean("requiresImmobile");
        ArrayList<String> configBadBlocksList = new ArrayList<>(getConfig().getStringList("unsafe-blocks-list"));
        if (!configBadBlocksList.isEmpty()){
            for (String string : configBadBlocksList){
                Material material = Material.getMaterial(string);
                unsafeTypes.add(material);
            }
        }else {
            unsafeTypes.add(Material.LAVA);
            unsafeTypes.add(Material.MAGMA_BLOCK);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            Message.error(sender, Lang.ERROR_PLAYERONLY.toString());
            return false;
        }
        Player p = (Player) sender;
        if(command.getName().equalsIgnoreCase("tpa") || command.getName().equalsIgnoreCase("tpahere")) {
            if(args.length == 0) {
                Message.error(p, Lang.ERROR_MISSING_PLAYER_ARG.toString());
                return false;
            }
            for(Player pls : Bukkit.getOnlinePlayers()) {
                if(pls.getName().equals(args[0])) {
                    TeleportRequest.createRequest(p, pls, command.getName().equalsIgnoreCase("tpa") ? TeleportRequest.TeleportRequestType.TPA : TeleportRequest.TeleportRequestType.TPAHERE);
                    return true;
                }
            }
            Message.error(p, Lang.ERROR_NOPLAYER.toString());
            return false;
        }
        if(command.getName().equalsIgnoreCase("tpaccept")) {
            TeleportRequest.accept(p);
            return true;
        }
        if(command.getName().equalsIgnoreCase("tpdeny")) {
            TeleportRequest.deny(p);
            return true;
        }
        return super.onCommand(sender, command, label, args);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if(!requiresImmobile) return;
        if(TeleportRequest.all.containsKey(e.getPlayer()) && TeleportRequest.all.get(e.getPlayer()).teleporting) {
            Message.normal(Lang.TPA_CANCELLED_MOVE.toString());
            TeleportRequest.all.get(e.getPlayer()).cancel();
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if(TeleportRequest.all.containsKey(e.getPlayer())) TeleportRequest.all.get(e.getPlayer()).cancel();
    }
}
