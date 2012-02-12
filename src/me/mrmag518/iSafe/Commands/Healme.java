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

package me.mrmag518.iSafe.Commands;

import me.mrmag518.iSafe.iSafe;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Healme implements CommandExecutor {
    public static iSafe plugin;
    public Healme(iSafe instance)
    {
        plugin = instance;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        if(cmd.getName().equalsIgnoreCase("healme")){
            if (args.length > 0) {
                sender.sendMessage(ChatColor.RED + "To many arguments!");
                return false;
            }
            if (sender instanceof Player) {
                Player player = (Player)sender;
                if (hasHealme(player)) { //player
                    sender.sendMessage(ChatColor.GRAY + "You healed yourself");
                    player.setFoodLevel(20);
                    player.setHealth(20);
                    player.setFireTicks(0);
                } else {
                    sender.sendMessage(ChatColor.RED + "You do not have access to that.");
                }
            } else {
                sender.sendMessage("[iSafe] You cannot do that from here.");
            }
            return true;
        }
        return false;
    }
    
    public boolean hasHealme(Player player) {
        if (player.hasPermission("iSafe.heal")) {
            return true;
        } else if  (player.hasPermission("iSafe.*")) {
            return true;
        }
        return false;
    }
}
