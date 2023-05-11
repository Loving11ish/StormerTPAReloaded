package me.loving11ish.stormertpareloaded;

import com.rylinaux.plugman.api.PlugManAPI;
import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.wrapper.WrappedTask;
import io.papermc.lib.PaperLib;
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
import java.util.logging.Logger;

public final class StormerTPAReloaded extends JavaPlugin implements Listener {

    public static StormerTPAReloaded i;
    private FoliaLib foliaLib = new FoliaLib(this);
    Logger logger = this.getLogger();

    public static int teleportationDelay = 0;
    public static int teleportRequestDuration = 0;
    public static boolean requiresImmobile = false;

    public static List<Material> unsafeTypes = new ArrayList<>();

    @Override
    public void onEnable() {
        //Plugin startup logic
        i = this;

        //Suggest PaperMC if not using
        if (foliaLib.isUnsupported()||foliaLib.isSpigot()){
            PaperLib.suggestPaper(this);
        }

        //Check if PlugManX is enabled
        if (isPlugManXEnabled()) {
            if (!PlugManAPI.iDoNotWantToBeUnOrReloaded("StormerTPAReloaded")) {
                logger.severe(ColorUtils.translateColorCodes("&c-------------------------------------------"));
                logger.severe(ColorUtils.translateColorCodes("&c-------------------------------------------"));
                logger.severe(ColorUtils.translateColorCodes("&4WARNING WARNING WARNING WARNING!"));
                logger.severe(ColorUtils.translateColorCodes("&c-------------------------------------------"));
                logger.severe(ColorUtils.translateColorCodes("&6StormerTPAReloaded: &4You appear to be using an unsupported version of &d&lPlugManX"));
                logger.severe(ColorUtils.translateColorCodes("&6StormerTPAReloaded: &4Please &4&lDO NOT USE PLUGMANX TO LOAD/UNLOAD/RELOAD THIS PLUGIN!"));
                logger.severe(ColorUtils.translateColorCodes("&6StormerTPAReloaded: &4Please &4&lFULLY RESTART YOUR SERVER!"));
                logger.severe(ColorUtils.translateColorCodes("&c-------------------------------------------"));
                logger.severe(ColorUtils.translateColorCodes("&6StormerTPAReloaded: &4This plugin &4&lHAS NOT &4been validated to use this version of PlugManX!"));
                logger.severe(ColorUtils.translateColorCodes("&6StormerTPAReloaded: &4&lNo official support will be given to you if you use this!"));
                logger.severe(ColorUtils.translateColorCodes("&6StormerTPAReloaded: &4&lUnless Loving11ish has explicitly agreed to help!"));
                logger.severe(ColorUtils.translateColorCodes("&6StormerTPAReloaded: &4Please add StormerTPAReloaded to the ignored-plugins list in PlugManX's config.yml"));
                logger.severe(ColorUtils.translateColorCodes("&c-------------------------------------------"));
                logger.severe(ColorUtils.translateColorCodes("&6StormerTPAReloaded: &6Continuing plugin startup"));
                logger.severe(ColorUtils.translateColorCodes("&c-------------------------------------------"));
                logger.severe(ColorUtils.translateColorCodes("&c-------------------------------------------"));
            }else {
                logger.info(ColorUtils.translateColorCodes("&a-------------------------------------------"));
                logger.info(ColorUtils.translateColorCodes("&6StormerTPAReloaded: &aSuccessfully hooked into PlugManX"));
                logger.info(ColorUtils.translateColorCodes("&6StormerTPAReloaded: &aSuccessfully added StormerTPAReloaded to ignoredPlugins list."));
                logger.info(ColorUtils.translateColorCodes("&6StormerTPAReloaded: &6Continuing plugin startup"));
                logger.info(ColorUtils.translateColorCodes("&a-------------------------------------------"));
            }
        }else {
            logger.info(ColorUtils.translateColorCodes("-------------------------------------------"));
            logger.info(ColorUtils.translateColorCodes("&6StormerTPAReloaded: &cPlugManX not found!"));
            logger.info(ColorUtils.translateColorCodes("&6StormerTPAReloaded: &cDisabling PlugManX hook loader"));
            logger.info(ColorUtils.translateColorCodes("&6StormerTPAReloaded: &6Continuing plugin startup"));
            logger.info(ColorUtils.translateColorCodes("-------------------------------------------"));
        }
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
        try {
            WrappedTask wrappedTask1 = TeleportRequest.getWrappedTaskOne();
            WrappedTask wrappedTask2 = TeleportRequest.getWrappedTaskTwo();
            wrappedTask1.cancel();
            wrappedTask2.cancel();
            if (foliaLib.isUnsupported()){
                Bukkit.getScheduler().cancelTasks(this);
            }
            logger.info(ColorUtils.translateColorCodes("&6StormerTPAReloaded: &aAll background task disabled successfully."));
        } catch (Exception e){
            logger.info(ColorUtils.translateColorCodes("&6StormerTPAReloaded: &aAll background task disabled successfully."));
        }
    }

    public void loadConfig() {
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        teleportationDelay = getConfig().getInt("teleportationDelay");
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
            TeleportRequest.all.get(e.getPlayer()).cancel();
            TeleportRequest.all.remove(e.getPlayer());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if(TeleportRequest.all.containsKey(e.getPlayer())){
            TeleportRequest.all.get(e.getPlayer()).cancel();
            TeleportRequest.all.remove(e.getPlayer());
        }
    }

    public boolean isPlugManXEnabled() {
        try {
            Class.forName("com.rylinaux.plugman.PlugMan");
            logger.info(ColorUtils.translateColorCodes("&6StormerWarpsReloaded: &aFound PlugManX main class at:"));
            logger.info(ColorUtils.translateColorCodes("&6StormerWarpsReloaded: &dcom.rylinaux.plugman.PlugMan"));
            return true;
        } catch (ClassNotFoundException e) {
            logger.warning(ColorUtils.translateColorCodes("&6StormerWarpsReloaded: &aCould not find PlugManX main class at:"));
            logger.warning(ColorUtils.translateColorCodes("&6StormerWarpsReloaded: &dcom.rylinaux.plugman.PlugMan"));
            return false;
        }
    }
}
