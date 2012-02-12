/*
 * iSafe
 * Copyright (C) 2011-2012 mrmag518 <magnusaub@yahoo.no>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package me.mrmag518.iSafe.Blacklists;

import java.util.ArrayList;
import java.util.List;

import me.mrmag518.iSafe.iSafe;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class PlaceBlacklist implements Listener {
    public PlaceBlacklist() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    public static iSafe plugin;
    public PlaceBlacklist(iSafe instance)
    {
        plugin = instance;
    }
    int message = 0;
    
    @EventHandler
    public void PlaceBlacklist(BlockPlaceEvent event) {
        if (event.isCancelled())
        {
            return;
        }
        
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Server server = player.getServer();
        
        int blockID = event.getBlock().getTypeId();
        String BlockNAME_Lowercase = event.getBlock().getType().name().toLowerCase();
        String BlockNAME_Uppercase = event.getBlock().getType().name().toUpperCase();
        String BlockNAME_Name = event.getBlock().getType().name();
        String BlockNAME = event.getBlock().toString();
        
        World world = player.getWorld();
        Location loc = player.getLocation();
        String worldname = world.getName();
        
        //Blacklist
        final List<Block> placedblocks = new ArrayList<Block>();
        if (plugin.getBlacklist().getList("Place.Blacklist", placedblocks).contains(blockID)
                || plugin.getBlacklist().getList("Place.Blacklist", placedblocks).contains(BlockNAME_Lowercase) 
                || plugin.getBlacklist().getList("Place.Blacklist", placedblocks).contains(BlockNAME_Uppercase)
                || plugin.getBlacklist().getList("Place.Blacklist", placedblocks).contains(BlockNAME_Name)
                || plugin.getBlacklist().getList("Place.Blacklist", placedblocks).contains(BlockNAME))
        {
            if(player.hasPermission("iSafe.place.blacklist.bypass")) {
                //access
            } else {
                if (!event.isCancelled()) 
                {
                    final List<String> worlds = plugin.getBlacklist().getStringList("Place.Worlds");
                
                    if (plugin.getBlacklist().getList("Place.Worlds", worlds).contains(worldname))
                    {
                        event.setCancelled(true);
                    } else {
                        event.setCancelled(false);
                    }
                }
            }
            
            if (plugin.getBlacklist().getBoolean("Place.Kick-Player", true))
            {
                if (event.isCancelled())
                {
                    player.kickPlayer(ChatColor.RED + "You got kicked for attempting to place: "+ ChatColor.GRAY + block.getType().name().toLowerCase());
                }    
            }
            
            if (plugin.getBlacklist().getBoolean("Place.Kill-Player", true))
            {
                if (event.isCancelled())
                {
                    player.setHealth(0);
                    KillAlertPlayer(player, block, worldname);
                }    
            }
            
            if (plugin.getBlacklist().getBoolean("Place.Alert/log.To-console", true))
            {
                if (event.isCancelled()) 
                {
                    AlertConsole(player, block, loc, worldname);
                }
            }
            
            if (plugin.getBlacklist().getBoolean("Place.Alert/log.To-player", true))
            {
                if (event.isCancelled()) 
                {
                    AlertPlayer(player, block, worldname);
                }
            }
            
            if (plugin.getBlacklist().getBoolean("Place.Alert/log.To-server-chat", true))
            {
                if (event.isCancelled()) 
                {
                    AlertServer(server, block, worldname, player);
                }
            }
        }
        
        if (plugin.getBlacklist().getBoolean("Place.Complete-Disallow-placing", true))
        {
            if (player.hasPermission("iSafe.place")) {
                //access
            } else {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot place objects.");
            }
        }
    }
    
    public void KillAlertPlayer(Player player, Block block, String worldname) {
        player.sendMessage(ChatColor.RED + "You got killed for attempting to place: "+ ChatColor.GRAY + block.getType().name().toLowerCase());
    }
    
    public void AlertPlayer(Player player, Block block, String worldname) {
        player.sendMessage(ChatColor.RED + "You cannot place: "+ ChatColor.GRAY + block.getType().name().toLowerCase() + ChatColor.RED + " In world: "+ ChatColor.GRAY + worldname);
    }
    
    public void AlertServer(Server server, Block block, String worldname, Player player) {
        server.broadcastMessage(ChatColor.DARK_GRAY + player.getName() + " tried to place: "+ ChatColor.RED + block.getType().name().toLowerCase() + ChatColor.DARK_GRAY + " In the world: "+ ChatColor.RED + worldname);
    }
    
    public void AlertConsole(Player player, Block block, Location loc, String worldname) {
        plugin.log.info("[iSafe] "+ player.getName() + " tried to place: "+ block.getType().name().toLowerCase() + ", At the location: "+ " X: "+ loc.getBlockX() +" Y: "+ loc.getBlockY() +" Z: "+ loc.getBlockZ()+ ", In the world: "+ worldname);
    }
}
