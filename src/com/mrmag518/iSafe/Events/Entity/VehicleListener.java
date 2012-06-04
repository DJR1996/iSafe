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

package com.mrmag518.iSafe.Events.Entity;

import com.mrmag518.iSafe.*;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;

public class VehicleListener implements Listener {
    public static iSafe plugin;
    public VehicleListener(iSafe instance)
    {
        plugin = instance;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (event.isCancelled())
        {
            return;
        }
        
        Entity entity = event.getEntered();
        Player player = (Player) entity;
        Vehicle vec = event.getVehicle();
        
       if(plugin.getConfig().getBoolean("Vehicle.Prevent.enter.Minecarts", true))
       {
            if(player.hasPermission("iSafe.vehicle.enter.minecart")) {
                //access
                } else {
                try {
                    if(vec.getEntityId() == 40) {
                        if (event.getEntered() instanceof LivingEntity) {
                            if (entity instanceof Player) {
                                event.setCancelled(true);
                                player.sendMessage(ChatColor.RED + "You do not have access to enter that vehicle");
                            }
                        }
                    }
                } catch (NullPointerException npe) {
                    //ignored
                }
            }
        }
        if(plugin.getConfig().getBoolean("Vehicle.Prevent.enter.Boats", true))
        {
            if(player.hasPermission("iSafe.vehicle.enter.boat")) {
                //access
            } else {
                try {
                    if(vec.getEntityId() == 41) {
                        if (event.getEntered() instanceof LivingEntity) {
                            if (entity instanceof Player) {
                                event.setCancelled(true);
                                player.sendMessage(ChatColor.RED + "You do not have access to enter that vehicle");
                            }
                        } 
                    } 
                } catch (NullPointerException npe) {
                    //ignored
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        if (event.isCancelled())
        {
            return;
        }
        
        Entity entity = event.getAttacker();
        Vehicle vec = event.getVehicle();
        Player player = (Player) entity;
        
        if(plugin.getConfig().getBoolean("Vehicle.Prevent.destroy.Minecarts", true))
        {
            if(player.hasPermission("iSafe.vehicle.destory.minecart")) {
                //access
            } else {
                try {
                    if(vec.getEntityId() == 40) {
                        if (event.getAttacker() instanceof LivingEntity) {
                            if (entity instanceof Player) {
                                event.setCancelled(true);
                                player.sendMessage(ChatColor.RED + "You do not have access to destory that vehicle");
                            }
                        }
                    } 
                } catch (NullPointerException npe) {
                    //ignored
                }
            }
        }
            
        if(plugin.getConfig().getBoolean("Vehicle.Prevent.destroy.Boats", true))
        {
            if(player.hasPermission("iSafe.vehicle.destory.boat")) {
                //access
            } else {
                try {
                    if(vec.getEntityId() == 41) {
                        if (entity instanceof LivingEntity) {
                            if (event.getAttacker() instanceof Player) {
                                event.setCancelled(true);
                                player.sendMessage(ChatColor.RED + "You do not have access to destory that vehicle");
                            }
                        }
                    } 
                } catch (NullPointerException npe) {
                    //ignored
                }
            }
        }
    }
}