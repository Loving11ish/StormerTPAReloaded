package me.loving11ish.stormertpareloaded;

import io.papermc.lib.PaperLib;
import me.loving11ish.stormertpareloaded.lang.Lang;
import me.loving11ish.stormertpareloaded.lang.Message;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;

public class TeleportRequest {

    public static Integer taskID1;
    public static Integer taskID2;

    public static enum TeleportRequestType{
        TPA,
        TPAHERE;
    }

    public static HashMap<Player, TeleportRequest> all = new HashMap<>();

    private Player sender;
    private Player receiver;
    private TeleportRequestType type;
    public boolean teleporting = false;
    public boolean processed = false;

    public static TeleportRequest createRequest(Player sender, Player receiver, TeleportRequestType type) {
        if(all.containsKey(receiver))
            if(all.get(receiver).processed) all.remove(receiver);
            else {
                Message.error(sender, Lang.ERROR_REQUEST_PENDING.toString());
                return null;
            }
        return new TeleportRequest(sender, receiver, type);
    }

    private TeleportRequest(Player sender, Player receiver, TeleportRequestType type) {
        this.sender = sender;
        this.receiver = receiver;
        this.type = type;
        all.put(receiver, this);
        Message.normal(sender, Lang.TPA_REQUEST_SENT.toString().replace("<PLAYER>", receiver.getName()).replace("<SECONDS>", ""+(StormerTPAReloaded.teleportRequestDuration / 20)));
        Message.normal(receiver, (type == TeleportRequestType.TPA ? Lang.TPA_REQUEST_RECEIVED_TPA : Lang.TPA_REQUEST_RECEIVED_TPAHERE).toString().replace("<PLAYER>", sender.getName()).replace("<SECONDS>", ""+(StormerTPAReloaded.teleportRequestDuration / 20)));


        taskID1 = Bukkit.getScheduler().scheduleSyncDelayedTask(StormerTPAReloaded.i, new Runnable() {
            @Override
            public void run() {
                if(TeleportRequest.this.processed || TeleportRequest.this.teleporting) {
                    cancel();
                    return;
                }
                TeleportRequest.this.processed = true;
                all.remove(TeleportRequest.this.receiver);
                Message.normal(TeleportRequest.this.receiver, Lang.TPA_REQUEST_EXPIRED_RECEIVED.toString().replace("<PLAYER>", sender.getName()));
                Message.normal(TeleportRequest.this.sender, Lang.TPA_REQUEST_EXPIRED_SENT.toString().replace("<PLAYER>", receiver.getName()));
            }
        }, StormerTPAReloaded.teleportRequestDuration);
    }

    public static void accept(Player p) {
        if(!all.containsKey(p) || all.get(p).processed) {
            all.remove(p);
            Message.error(p, Lang.ERROR_NO_REQUEST_PENDING.toString());
            return;
        }
        all.get(p).accept();
    }

    public void accept() {
        Player target = this.sender;
        Player destination = this.receiver;

        if (this.type == TeleportRequestType.TPAHERE) {
            target = this.receiver;
            destination = this.sender;
        }

        Message.normal(destination, Lang.TPA_REQUEST_ACCEPTED_TO.toString().replace("<PLAYER>", target.getName()));
        Message.normal(target, Lang.TPA_REQUEST_ACCEPTED_FROM.toString().replace("<PLAYER>", destination.getName()));

        if(StormerTPAReloaded.teleportationTicksDelay <= 0) {
            findSafeTeleportSpot();
            return;
        }

        this.teleporting = true;
        taskID2 = Bukkit.getScheduler().scheduleSyncDelayedTask(StormerTPAReloaded.i, new Runnable() {
            @Override
            public void run() {
                if(TeleportRequest.this.processed) {
                    cancel();
                    return;
                }
                findSafeTeleportSpot();
            }
        }, StormerTPAReloaded.teleportationTicksDelay);
    }

    public static void deny(Player p) {
        if(!all.containsKey(p) || all.get(p).processed) {
            all.remove(p);
            Message.error(p, Lang.ERROR_NO_REQUEST_PENDING.toString());
            return;
        }
        all.get(p).deny();
    }

    public void deny() {
        Message.normal(this.sender, Lang.TPA_REQUEST_REFUSED_SENT.toString().replace("<PLAYER>", receiver.getName()));
        Message.normal(this.receiver, Lang.TPA_REQUEST_REFUSED_RECEIVED.toString().replace("<PLAYER>", sender.getName()));
        this.processed = true;
        all.remove(this.receiver);
    }

    private void findSafeTeleportSpot() {
        Player target = this.sender;
        Player destination = this.receiver;
        if (this.type == TeleportRequestType.TPAHERE) {
            target = this.receiver;
            destination = this.sender;
        }

        Location loc = destination.getLocation();
        while(!isSafe(loc) && loc.getY() < 319) loc.add(0,1,0);
        if(isSafe(loc)) teleport(target, loc);
        else {
            Message.error(target, Lang.TPA_CANCELLED_UNSAFE.toString());
            Message.error(destination, Lang.TPA_CANCELLED_UNSAFE.toString());
        }
        this.processed = true;
        all.remove(this.receiver);
    }

    private static boolean isSafe(Location loc) {
        Block feet = loc.getBlock();
        Block head = feet.getRelative(0,1,0);
        Block below = feet.getRelative(0,-1,0);
        if(below.isPassable()) return false;
        if(!feet.isPassable()) return false;
        if(!head.isPassable()) return false;
        if(StormerTPAReloaded.unsafeTypes.contains(below.getType())) return false;
        return true;
    }

    @SuppressWarnings("unused")
    private static void teleport(Player target, Block b) {
        teleport(target, b.getLocation().getBlock().getLocation().add(0.5, 1, 0.5));
    }

    private static void teleport(Player target, Location loc) {
        PaperLib.teleportAsync(target, loc);
        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0, true));
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 255, true));
        target.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 60, 255, true));
        target.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 60, 0, true));
    }

    public void cancel() {
        this.processed = true;
        Message.normal(this.sender, Lang.TPA_CANCELLED.toString());
        Message.normal(this.receiver, Lang.TPA_CANCELLED.toString());
    }
}
