package me.mrmag518.iSafe.Blacklists;

import java.util.ArrayList;
import java.util.List;
import me.mrmag518.iSafe.iSafe;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class PickupBlacklist implements Listener {
    public PickupBlacklist() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public static iSafe plugin;
    public PickupBlacklist(iSafe instance)
    {
        plugin = instance;
    }
    int message = 0;
    
    @EventHandler
    public void BlacklistPickup(PlayerPickupItemEvent event) {
        if (event.isCancelled())
        {
            return;
        }
        
        Player player = event.getPlayer();
        Server server = player.getServer();
        World world = player.getWorld();
        
        int itemID = event.getItem().getItemStack().getTypeId();
        String BlockNAME_Lowercase = event.getItem().getItemStack().getType().name().toLowerCase();
        String BlockNAME_Uppercase = event.getItem().getItemStack().getType().name().toUpperCase();
        String BlockNAME_Name = event.getItem().getItemStack().getType().name();
        String BlockNAME = event.getItem().toString();
        
        Location loc = player.getLocation();
        String worldname = world.getName();
        
        //Blacklist
        final List<Item> pickupedblocks = new ArrayList<Item>();
        if (plugin.getBlacklist().getList("Pickup.Blacklist", pickupedblocks).contains(itemID)
                || plugin.getBlacklist().getList("Pickup.Blacklist", pickupedblocks).contains(BlockNAME_Lowercase)
                || plugin.getBlacklist().getList("Pickup.Blacklist", pickupedblocks).contains(BlockNAME_Uppercase)
                || plugin.getBlacklist().getList("Pickup.Blacklist", pickupedblocks).contains(BlockNAME)
                || plugin.getBlacklist().getList("Pickup.Blacklist", pickupedblocks).contains(BlockNAME_Name))
        {
            if(player.hasPermission("iSafe.pickup.blacklist.bypass")) {
                //access
            } else {
                if (!event.isCancelled()) 
                {
                    final List<String> Pickupworlds = plugin.getBlacklist().getStringList("Pickup.Worlds");
                
                    if (plugin.getBlacklist().getList("Pickup.Worlds", Pickupworlds).contains(worldname))
                    {
                        event.setCancelled(true);
                    } else {
                        event.setCancelled(false);
                    }
                }
            }
            
            if (plugin.getBlacklist().getBoolean("Pickup.Kick-Player", true))
            {
                if (event.isCancelled())
                {
                    player.kickPlayer(ChatColor.RED + "You got kicked for attempting to pickup: "+ ChatColor.GRAY + event.getItem().getItemStack().getType().name().toLowerCase());
                }    
            }
            
            if (plugin.getBlacklist().getBoolean("Place.Kill-Player", true))
            {
                if (event.isCancelled())
                {
                    player.setHealth(0);
                    KillAlertPlayer(player, event, worldname);
                }    
            }
            
            if (plugin.getBlacklist().getBoolean("Pickup.Alert/log.To-console", true))
            {
                if (event.isCancelled()) 
                {
                    AlertConsole(player, event, worldname, loc);
                }
            }
            
            if (plugin.getBlacklist().getBoolean("Pickup.Alert/log.To-player", true))
            {
                if (event.isCancelled()) 
                {
                    AlertPlayer(player, event);
                }
            }
            
            if (plugin.getBlacklist().getBoolean("Pickup.Alert/log.To-server-chat", true))
            {
                if (event.isCancelled()) 
                {
                    if (message == 0) {
                        server.broadcastMessage(ChatColor.DARK_GRAY + player.getName() + " tried to pickup: "+ event.getItem().getItemStack().getType().name().toLowerCase());
                        message = 1;
                    }
                }
            }
        }
        
        if (plugin.getBlacklist().getBoolean("Pickup.Complete-Disallow-pickuping", true))
        {
            if (player.hasPermission("iSafe.pickup")) {
                //access
            } else {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot pickup objects.");
            }
        }
    }
    
    public void KillAlertPlayer(Player player, PlayerPickupItemEvent event, String worldname) {
        player.sendMessage(ChatColor.RED + "You got killed for attempting to pickup: "+ ChatColor.GRAY + event.getItem().getItemStack().getType().name().toLowerCase());
    }
    
    public void AlertPlayer(Player player, PlayerPickupItemEvent event) {
        if (message == 0) {
            player.sendMessage(ChatColor.RED + "You cannot pickup: "+ ChatColor.GRAY + event.getItem().getItemStack().getType().name().toLowerCase());
            message = 1;
        }
    }
    
    public void AlertConsole(Player player, PlayerPickupItemEvent event, String worldname, Location loc) {
        if (message == 0) {
            plugin.log.info("[iSafe] "+ player.getName() + " tried to pickup: "+ event.getItem().getItemStack().getType().name().toLowerCase() + ", At the location: "+ " X: "+ loc.getBlockX() +" Y: "+ loc.getBlockY() +" Z: "+ loc.getBlockZ()+ ", In the world: "+ worldname);
            message = 1;
        }
    }
}