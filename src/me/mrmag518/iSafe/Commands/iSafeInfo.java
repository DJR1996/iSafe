package me.mrmag518.iSafe.Commands;

import me.mrmag518.iSafe.iSafe;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;

public class iSafeInfo implements CommandExecutor {
public static iSafe plugin;
    public iSafeInfo(iSafe instance)
    {
        plugin = instance;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        if(cmd.getName().equalsIgnoreCase("iSafe-info")){
            if (args.length > 0) {
                sender.sendMessage(ChatColor.RED + "To many arguments!");
                return false;
            }
            PluginDescriptionFile pdffile = plugin.getDescription();
            if (sender instanceof Player) {
                Player player = (Player)sender;
                if (hasInfo(player)) { //player
                    sender.sendMessage(ChatColor.GRAY + "Name: "+ ChatColor.AQUA + pdffile.getName());
                    sender.sendMessage(ChatColor.GRAY + "Version: "+ ChatColor.AQUA + pdffile.getVersion());
                    sender.sendMessage(ChatColor.GRAY + "FullName: "+ ChatColor.AQUA + pdffile.getFullName());
                    sender.sendMessage(ChatColor.GRAY + "Authors: "+ ChatColor.AQUA + "mrmag518");
                    sender.sendMessage(ChatColor.GRAY + "Minecraft compitability: "+ ChatColor.AQUA + "1.1");
                    System.out.println("[iSafe] "+ (sender.getName() + " did the information command."));
                } else { //no permission
                    sender.sendMessage(ChatColor.RED + "You do not have access to that." );
                }
            } else { //console
                sender.sendMessage("Name: "+ pdffile.getName());
                sender.sendMessage("Version: "+ pdffile.getVersion());
                sender.sendMessage("FullName: "+ pdffile.getFullName());
                sender.sendMessage("Authors: "+ "mrmag518");
                sender.sendMessage("Minecraft compitability: "+ "1.1");
            }
            return true;
        }
        return false;
    }
    
    public boolean hasInfo(Player player) {
        if (player.hasPermission("iSafe.info")) {
            return true;
        } else if (player.hasPermission("iSafe.*")) {
            return true;
        }
        return false;
    }
}