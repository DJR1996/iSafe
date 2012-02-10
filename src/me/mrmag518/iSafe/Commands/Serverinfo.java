
package me.mrmag518.iSafe.Commands;

import me.mrmag518.iSafe.iSafe;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Serverinfo implements CommandExecutor {
    public static iSafe plugin;
    public Serverinfo(iSafe instance)
    {
        plugin = instance;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){       
        if(cmd.getName().equalsIgnoreCase("serverinfo")){
            if (args.length > 0) {
                sender.sendMessage(ChatColor.RED + "To many arguments!");
                return false;
            }
            if (sender instanceof Player) {
                Player player = (Player)sender;
                if (hasServerinfo(player)) { //player
                    sender.sendMessage(ChatColor.GRAY + "Bukkit version: "+ ChatColor.AQUA + plugin.getServer().getBukkitVersion());
                    sender.sendMessage(ChatColor.GRAY + "Server IP: "+ ChatColor.AQUA + plugin.getServer().getIp());
                    sender.sendMessage(ChatColor.GRAY + "Server name: "+ ChatColor.AQUA + plugin.getServer().getName());
                    sender.sendMessage(ChatColor.GRAY + "Server ID: "+ ChatColor.AQUA + plugin.getServer().getServerId());
                    sender.sendMessage(ChatColor.GRAY + "Server version: "+ ChatColor.AQUA + plugin.getServer().getVersion());
                    sender.sendMessage(ChatColor.GRAY + "Default GameMode: "+ ChatColor.AQUA + plugin.getServer().getDefaultGameMode());
                    sender.sendMessage(ChatColor.GRAY + "Server port: "+ ChatColor.AQUA + plugin.getServer().getPort());
                    sender.sendMessage(ChatColor.GRAY + "Spawn radius: "+ ChatColor.AQUA + plugin.getServer().getSpawnRadius());
                    System.out.println("[iSafe] "+ (sender.getName() + " did the serverinfo command."));
                } else {
                    sender.sendMessage(ChatColor.RED + "You do not have access to that.");
                }
            } else {
                sender.sendMessage(ChatColor.GRAY + "Bukkit version: "+ ChatColor.AQUA + plugin.getServer().getBukkitVersion());
                    sender.sendMessage("Server IP: "+ plugin.getServer().getIp());
                    sender.sendMessage("Server name: "+ plugin.getServer().getName());
                    sender.sendMessage("Server ID: "+ plugin.getServer().getServerId());
                    sender.sendMessage("Server version: "+ plugin.getServer().getVersion());
                    sender.sendMessage("Default GameMode: "+ plugin.getServer().getDefaultGameMode());
                    sender.sendMessage("Server port: "+ plugin.getServer().getPort());
                    sender.sendMessage("Spawn radius: "+ plugin.getServer().getSpawnRadius());
            }
            return true;
        }
        return false;
    }
    
    public boolean hasServerinfo(Player player) {
        if (player.hasPermission("isafe.serverinfo")) {
            return true;
        } else if (player.hasPermission("iSafe.*")) {
            return true;
        }
        return false;
    }
}