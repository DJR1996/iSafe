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
package com.mrmag518.iSafe;

import com.mrmag518.iSafe.Events.BlockEvents.*;
import com.mrmag518.iSafe.Events.EntityEvents.*;
import com.mrmag518.iSafe.Events.WorldEvents.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;
import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;

import com.mrmag518.iSafe.Blacklists.*;
import com.mrmag518.iSafe.Commands.*;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class iSafe extends JavaPlugin {
    //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    //Remember to change this on every version!

    private String fileversion = "iSafe v3.0 BETA";
    //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    private PlayerListener playerListener = null;
    private BlockListener blockListener = null;
    private EntityListener entityListener = null;
    private WeatherListener weatherListener = null;
    private InventoryListener inventoryListener = null;
    private VehicleListener vehicleListener = null;
    private WorldListener worldListener = null;
    private EnchantmentListener enchantmentListener = null;
    private DropListener dropListener = null;
    private UserFileCreator UFC = null;
    private SendUpdate sendUpdate = null;
    private DropBlacklist dropBlacklist = null;
    private PlaceBlacklist placeBlacklist = null;
    private BreakBlacklist breakBlacklist = null;
    private PickupBlacklist pickupBlacklist = null;
    private CommandBlacklist commandBlacklist = null;
    private MobSpawnBlacklist mobSpawnBlacklist = null;
    private Censor censor = null;
    private DispenseBlacklist dispenseBlacklist = null;
    private InteractBlacklist interactBlacklist = null;
    
    public String version = null;
    public String newVersion = null;
    public static iSafe plugin;
    public final Logger log = Logger.getLogger("Minecraft");
    public String DEBUG_PREFIX = "[iSafe DEBUG]" + " ";
    public static Permission perms = null;
    private FileConfiguration iSafeConfig = null;
    private File iSafeConfigFile = null;
    private FileConfiguration messages = null;
    private File messagesFile = null;
    private FileConfiguration creatureManager = null;
    private File creatureManagerFile = null;
    private FileConfiguration blacklist = null;
    private File blacklistFile = null;
    private FileConfiguration config;
    public boolean checkingUpdatePerms = false;
    public boolean cancelDamagePerms = false;

    @Override
    public void onLoad() {
        fileManagement();
    }

    @Override
    public void onDisable() {
        PluginDescriptionFile pdffile = this.getDescription();
        if (verboseLogging() == true) {
            verboseLog("v" + pdffile.getVersion() + " disabled.");
        } else {
            debugLog("Verbose logging is off.");
        }
    }

    @Override
    public void onEnable() {
        version = this.getDescription().getVersion();
        debugLog("Debug mode is enabled!");

        registerClasses();
        PluginDescriptionFile pdffile = this.getDescription();
        if (getISafeConfig().getBoolean("CheckForUpdates", true)) {
            //Update checker - From MilkBowl.
            this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {

                @Override
                public void run() {
                    try {
                        newVersion = updateCheck(version);
                        String oldVersion = version;

                        if (!newVersion.contains(oldVersion)) {
                            log.info(" ");
                            log.info("#######  iSafe UpdateChecker  #######");
                            log.info("A new update for iSafe was found! " + newVersion);
                            log.info("You are currently running iSafe v" + oldVersion);
                            log.info("You can find this new version at BukkitDev.");
                            log.info("http://dev.bukkit.org/server-mods/blockthattnt/");
                            log.info("#####################################");
                            log.info(" ");
                        }
                    } catch (Exception ignored) {
                        //Ignored
                    }
                }
            }, 0, 432000);
        }
        getCommand("iSafe").setExecutor(new Commands(this));
        checkMatch();

        initVault();

        checkingUpdatePerms = false;

        try {
            Metrics metrics = new Metrics(this);
            metrics.start();
        } catch (IOException e) {
            // Failed to submit the stats :-(
        }

        if (verboseLogging() == true) {
            verboseLog("v" + pdffile.getVersion() + " enabled.");
        } else {
            debugLog("Verbose logging is off.");
        }
    }

    public boolean verboseLogging() {
        return getISafeConfig().getBoolean("VerboseLogging");
    }

    public boolean debugMode() {
        return getISafeConfig().getBoolean("DebugMode");
    }
    
    public void initVault() {
        if(iSafeConfig.getBoolean("UseVaultForPermissions", true)) {
            if (getServer().getPluginManager().getPlugin("Vault") != null) {
                verboseLog("Using Vault for permissions!");
                setupPermissions();
            } else {
                log.severe("[iSafe] Vault.jar was NOT found in your plugins folder!");
                log.severe("[iSafe] You HAVE to have Vault.jar in the plugins folder if you use Vault for permissions!");
                log.warning("[iSafe] Settings UseVaultForPermissions in your iSafeConfig.yml to false ..");
                getISafeConfig().set("UseVaultForPermissions", false);
                saveISafeConfig();
                reloadISafeConfig();
            }
        }
    }

    private void registerClasses() {
        playerListener = new PlayerListener(this);
        blockListener = new BlockListener(this);
        entityListener = new EntityListener(this);
        worldListener = new WorldListener(this);
        vehicleListener = new VehicleListener(this);
        weatherListener = new WeatherListener(this);
        inventoryListener = new InventoryListener(this);
        enchantmentListener = new EnchantmentListener(this);
        dropListener = new DropListener(this);

        if (getISafeConfig().getBoolean("CreateUserFiles", true)) {
            UFC = new UserFileCreator(this);
        } else {
            UFC = null;
            debugLog("CreateUserFiles in the iSafeConfig.yml was disabled, therefor not register the UserFileCreator class.");
        }

        if (getISafeConfig().getBoolean("CheckForUpdates", true)) {
            sendUpdate = new SendUpdate(this);
        } else {
            sendUpdate = null;
            debugLog("CheckForUpdates in the iSafeConfig.yml was disabled, therefor not registering the sendUpdate class.");
        }

        dropBlacklist = new DropBlacklist(this);
        placeBlacklist = new PlaceBlacklist(this);
        breakBlacklist = new BreakBlacklist(this);
        pickupBlacklist = new PickupBlacklist(this);
        commandBlacklist = new CommandBlacklist(this);
        mobSpawnBlacklist = new MobSpawnBlacklist(this);
        censor = new Censor(this);
        dispenseBlacklist = new DispenseBlacklist(this);
        interactBlacklist = new InteractBlacklist(this);

        debugLog("Registered classes.");
    }

    private void fileManagement() {
        if (!(getDataFolder().exists())) {
            getDataFolder().mkdirs();
        }

        File usersFolder = new File(getDataFolder(), "Users");
        if (!(usersFolder.exists())) {
            usersFolder.mkdir();
        }

        File exaFile = new File(usersFolder + File.separator + "_example.yml");
        if (!(exaFile.exists())) {
            try {
                FileConfiguration exampFile = YamlConfiguration.loadConfiguration(exaFile);
                exampFile.options().header(Data.setExFileHeader());
                exampFile.set("Username", "example");
                exampFile.set("Displayname", "example");
                exampFile.set("IPAddress", "127.0.0.1");
                exampFile.set("Gamemode", "survival");
                exampFile.set("Level", "50");
                exampFile.save(exaFile);
            } catch (Exception e) {
                log.info("[iSafe] Error creating example user file. (_example.yml)");
                e.printStackTrace();
            }
        }

        reloadISafeConfig();
        loadISafeConfig();
        reloadISafeConfig();

        reloadConfig();
        loadConfig();
        reloadConfig();

        reloadBlacklist();
        loadBlacklist();
        reloadBlacklist();

        reloadCreatureManager();
        loadCreatureManager();
        reloadCreatureManager();

        reloadMessages();
        loadMessages();
        reloadMessages();
        
        verboseLog("Loaded all files.");
    }
    
    public void verboseLog(String string) {
        if(verboseLogging() == true) {
            log.info("[iSafe] " + string);
        }
    }
    
    public void debugLog(String string) {
        if(debugMode() == true) {
            log.info(DEBUG_PREFIX + string);
        }
    }

    /**
     * @param p
     * @param permission
     * @return 
     */
    public boolean hasPermission(CommandSender p, String permission) {
        if (iSafeConfig.getBoolean("UseVaultForPermissions", true)) {
            if (perms.has(p, permission)) {
                return true;
            } else {
                noCmdPermission(p);
                return false;
            }
        } else {
            if (p.hasPermission(permission)) {
                return true;
            } else {
                noCmdPermission(p);
                return false;
            }
        }
    }
    
    public boolean hasBlacklistPermission(Player p, String permission) {
        if (iSafeConfig.getBoolean("UseVaultForPermissions", true)) {
            if (perms.has(p, permission)) {
                return true;
            } else {
                noCmdPermission(p);
                return false;
            }
        } else {
            if (p.hasPermission(permission)) {
                return true;
            } else {
                //Send no perm message in the blacklist instead.
                return false;
            }
        }
    }

    public boolean hasPermission(Player p, String permission) {
        if (iSafeConfig.getBoolean("UseVaultForPermissions", true)) {
            if (perms.has(p, permission)) {
                return true;
            } else {
                if (checkingUpdatePerms == true) {
                    //nothing
                } else {
                    noPermission(p);
                }
                return false;
            }
        } else {
            if (p.hasPermission(permission)) {
                return true;
            } else {
                if (checkingUpdatePerms == true) {
                    //nothing
                } else {
                    noPermission(p);
                }
                return false;
            }
        }
    }

    private String scanVariables(String configString, String playerName, String cmd, String blockName, String item, String world, String word) {
        String result = configString;
        
        if(playerName != null) {
            if(configString.contains("%playername%")) {
                result = result.replaceAll("%playername%", playerName);
            }
        }
        if(cmd != null) {
            if(configString.contains("%command%")) {
                result = result.replaceAll("%command%", cmd);
            }
        }
        if(blockName != null) {
            if(configString.contains("%block%")) {
                result = result.replaceAll("%block%", blockName);
            }
        }
        if(item != null) {
            if(configString.contains("%item%")) {
                result = result.replaceAll("%item%", item);
            }
        }
        if(world != null) {
            if(configString.contains("%world%")) {
                result = result.replaceAll("%world%", world);
            }
        }
        if(word != null) {
            if(configString.contains("%word%")) {
                result = result.replaceAll("%word%", word);
            }
        }
        
        result = colorize(result);
        return result;
    }
    
    public String colorize(String s) {
        if (s == null) {
            return null;
        }
        return s.replaceAll("&([0-9a-f])", "\u00A7$1");
    }
    
    public void noPermission(Player p) {
        String no_permission = getMessages().getString("Permissions.DefaultNoPermission");
        p.sendMessage(colorize(no_permission));
    }

    public void noCmdPermission(CommandSender sender) {
        String no_permission = getMessages().getString("Permissions.NoCmdPermission");
        sender.sendMessage(colorize(no_permission));
    }

    public void kickMessage(Player p) {
        String kickMsg = getMessages().getString("KickMessage");
        Server s = p.getServer();
        s.broadcastMessage(scanVariables(kickMsg, p.getName(), null, null, null, p.getWorld().getName(), null));
    }

    public String sameNickPlaying(Player p) {
        String kickMsg = getMessages().getString("SameNickAlreadyPlaying");
        return scanVariables(kickMsg, p.getName(), null, null, null, p.getWorld().getName(), null);
    }

    public String denyNonOpsJoin() {
        String kickMsg = getMessages().getString("OnlyOpsCanJoin");
        return scanVariables(kickMsg, null, null, null, null, null, null);
    }

    public String commandLogger(Player p, PlayerCommandPreprocessEvent event) {
        String logged = getMessages().getString("CommandLogger");
        return scanVariables(logged, p.getName(), event.getMessage(), null, null, p.getWorld().getName(), null);
    }
    
    public String blacklistInteractKickMsg(Block b) {
        String kickMsg = getMessages().getString("Blacklists.Interact.KickMessage");
        return scanVariables(kickMsg, null, null, b.getType().name().toLowerCase(), null, b.getWorld().getName(), null);
    }
    
    public String blacklistPlaceKickMsg(Block b) {
        String kickMsg = getMessages().getString("Blacklists.Place.KickMessage");
        return scanVariables(kickMsg, null, null, b.getType().name().toLowerCase(), null, b.getWorld().getName(), null);
    }
    
    public String blacklistBreakKickMsg(Block b) {
        String kickMsg = getMessages().getString("Blacklists.Break.KickMessage");
        return scanVariables(kickMsg, null, null, b.getType().name().toLowerCase(), null, b.getWorld().getName(), null);
    }
    
    public String blacklistCensorKickMsg(String word) {
        String kickMsg = getMessages().getString("Blacklists.Censor.KickMessage");
        return scanVariables(kickMsg, null, null, null, null, null, word);
    }
    
    public String blacklistDropKickMsg(Item i) {
        String kickMsg = getMessages().getString("Blacklists.Drop.KickMessage");
        return scanVariables(kickMsg, null, null, null, i.getItemStack().getType().name().toLowerCase(), i.getWorld().getName(), null);
    }
    
    public String blacklistPickupKickMsg(String item) {
        String kickMsg = getMessages().getString("Blacklists.Pickup.KickMessage");
        return scanVariables(kickMsg, null, null, null, item, null, null);
    }
    
    public String blacklistCommandKickMsg(String cmd, String world) {
        String kickMsg = getMessages().getString("Blacklists.Command.KickMessage");
        return scanVariables(kickMsg, null, cmd, null, null, world, null);
    }
    
    
    public String blacklistInteractMsg(Block b) {
        String disallowedMsg = getMessages().getString("Blacklists.Interact.DisallowedMessage");
        return scanVariables(disallowedMsg, null, null, b.getType().name().toLowerCase(), null, b.getWorld().getName(), null);
    }
    
    public String blacklistPlaceMsg(Block b) {
        String disallowedMsg = getMessages().getString("Blacklists.Place.DisallowedMessage");
        return scanVariables(disallowedMsg, null, null, b.getType().name().toLowerCase(), null, b.getWorld().getName(), null);
    }
    
    public String blacklistBreakMsg(Block b) {
        String disallowedMsg = getMessages().getString("Blacklists.Break.DisallowedMessage");
        return scanVariables(disallowedMsg, null, null, b.getType().name().toLowerCase(), null, b.getWorld().getName(), null);
    }
    
    public String blacklistCensorMsg(String word) {
        String disallowedMsg = getMessages().getString("Blacklists.Censor.DisallowedMessage");
        return scanVariables(disallowedMsg, null, null, null, null, null, word);
    }
    
    public String blacklistDropMsg(String item) {
        String disallowedMsg = getMessages().getString("Blacklists.Drop.DisallowedMessage");
        return scanVariables(disallowedMsg, null, null, null, item, null, null);
    }
    
    /*public String blacklistPickupMsg(Item i) {
        String disallowedMsg = getMessages().getString("Blacklists.Pickup.DisallowedMessage");
        return scanVariables(disallowedMsg, null, null, null, i.getItemStack().getType().name().toLowerCase(), i.getWorld().getName(), null);
    }*/
    
    public String blacklistCommandMsg(String cmd, String world) {
        String disallowedMsg = getMessages().getString("Blacklists.Command.DisallowedMessage");
        return scanVariables(disallowedMsg, null, cmd, null, null, world, null);
    }
    
    private void loadConfig() {
        config = getConfig();
        config.options().header(Data.setConfigHeader());
        
        double ConfigVersion = 3.00;
        config.addDefault("ConfigVersion", Double.valueOf(ConfigVersion));
        if(config.getDouble("ConfigVersion") != Double.valueOf(ConfigVersion)) {
            log.warning("[iSafe] ConfigVersion was modified! Setting config version to right value ..");
            config.set("ConfigVersion", Double.valueOf(ConfigVersion));
        }
        
        config.addDefault("Fire.DisableFireSpread", false);
        config.addDefault("Fire.PreventFlintAndSteelUsage", false);
        config.addDefault("Fire.DisableLavaIgnition", false);
        config.addDefault("Fire.DisableFireballIgnition", false);
        config.addDefault("Fire.DisableLightningIgnition", false);
        config.addDefault("Fire.PreventBlockBurn", false);

        config.addDefault("Enchantment.PreventEnchantment", false);
        config.addDefault("Enchantment.PreventCreativeModeEnchanting", false);

        config.addDefault("Furnace.DisableFurnaceUsage", false);

        config.addDefault("Weather.DisableLightningStrike", false);
        config.addDefault("Weather.DisableThunder", false);
        config.addDefault("Weather.DisableStorm", false);

        config.addDefault("World.PreventChunkUnload", false);
        config.addDefault("World.MakeISafeLoadChunks", false);
        config.addDefault("World.DisableStructureGrowth", false);
        config.addDefault("World.PreventBonemealUsage", false);
        config.addDefault("World.DisablePortalGeneration", false);
        config.addDefault("World.DisableExpDrop", false);
        config.addDefault("World.DisableItemSpawn", false);
        config.addDefault("World.EnablePortalCreationPerms", false);

        config.addDefault("TreeGrowth.DisableFor.BigTree", false);
        config.addDefault("TreeGrowth.DisableFor.Birch", false);
        config.addDefault("TreeGrowth.DisableFor.BrownMushroom", false);
        config.addDefault("TreeGrowth.DisableFor.Redwood", false);
        config.addDefault("TreeGrowth.DisableFor.RedMushroom", false);
        config.addDefault("TreeGrowth.DisableFor.TallRedwood", false);
        config.addDefault("TreeGrowth.DisableFor.Tree", false);
        config.addDefault("TreeGrowth.DisableFor.Jungle", false);

        config.addDefault("Miscellaneous.DisableBlockGrow", false);
        config.addDefault("Miscellaneous.DisableLeavesDecay", false);
        config.addDefault("Miscellaneous.ForceBlocksToBeBuildable", false);
        config.addDefault("Miscellaneous.PreventExpBottleThrow", false);
        config.addDefault("Miscellaneous.ForcePermissionsToUseBed", false);
        config.addDefault("Miscellaneous.ForcePermissionsToFish", false);
        config.addDefault("Miscellaneous.OnlyLetOPsJoin", false);
        config.addDefault("Miscellaneous.DisableHunger", false);

        config.addDefault("AntiCheat/Sucurity.ForceLightLevel(Fullbright)", false);
        config.addDefault("AntiCheat/Sucurity.KickJoinerIfSameNickIsOnline", false);

        config.addDefault("Explosions.DisablePrimedExplosions", false);
        config.addDefault("Explosions.DisableAllExplosions", false);
        config.addDefault("Explosions.DisableCreeperExplosions", false);
        config.addDefault("Explosions.DisableEnderdragonBlockDamage", false);
        config.addDefault("Explosions.DisableTntExplosions", false);
        config.addDefault("Explosions.DisableFireballExplosions", false);
        config.addDefault("Explosions.DisableEnderCrystalExplosions", false);
        config.addDefault("Explosions.DebugExplosions", false);

        config.addDefault("Flow.DisableWaterFlow", false);
        config.addDefault("Flow.DisableLavaFlow", false);
        config.addDefault("Flow.DisableAirFlow", false);

        config.addDefault("Pistons.DisablePistonExtend", false);
        config.addDefault("Pistons.DisablePistonRetract", false);

        config.addDefault("BlockPhysics.DisableSandPhysics", false);
        config.addDefault("BlockPhysics.DisableGravelPhysics", false);

        config.addDefault("BlockFade.DisableIceMelting", false);
        config.addDefault("BlockFade.DisableSnowMelting", false);

        config.addDefault("ForceDrop.Glass", false);
        config.addDefault("ForceDrop.MobSpawner", false);
        config.addDefault("ForceDrop.Ice", false);
        config.addDefault("ForceDrop.Bedrock", false);

        config.addDefault("Buckets.Lava.Prevent", false);
        config.addDefault("Buckets.Lava.CheckedWorlds", Arrays.asList(Data.LavaBucketWorldList));
        Data.LavaBucketWorld = config.getStringList("Buckets.Lava.CheckedWorlds");
        config.addDefault("Buckets.Water.Prevent", false);
        config.addDefault("Buckets.Water.CheckedWorlds", Arrays.asList(Data.WaterBucketWorldList));
        Data.WaterBucketWorld = config.getStringList("Buckets.Water.CheckedWorlds");

        config.addDefault("Movement.DisableSprinting", false);
        config.addDefault("Movement.DisableSneaking", false);
        config.addDefault("Movement.PreventCropTrampling", false);

        config.addDefault("Gamemode.SwitchToSurvivalOnQuit", false);
        config.addDefault("Gamemode.SwitchToCreativeOnQuit", false);
        config.addDefault("Gamemode.DisableGamemodeChange", false);
        config.addDefault("Gamemode.DisableSurvivalToCreativeChange", false);
        config.addDefault("Gamemode.DisableCreativeToSurvivalChange", false);

        config.addDefault("Teleport.DisableAllTeleportCauses", false);
        config.addDefault("Teleport.Disable.CommandCause", false);
        config.addDefault("Teleport.Disable.EnderpearlCause", false);
        config.addDefault("Teleport.Disable.PluginCause", false);
        config.addDefault("Teleport.Disable.UnknownCause", false);
        config.addDefault("Teleport.Disable.NetherportalCause", false);
        config.addDefault("Teleport.Disable.CommandCause", false);

        config.addDefault("Chat.ForcePermissionToChat", false);
        config.addDefault("Chat.EnableKickMessages", true);
        config.addDefault("Chat.LogCommands", true);

        config.addDefault("VoidFall.TeleportPlayerToSpawn", false);
        config.addDefault("VoidFall.TeleportPlayerBackAndFixHole", true);
        config.addDefault("VoidFall.FixHoleWithGlass", true);
        config.addDefault("VoidFall.FixHoleWithBedrock", false);

        config.addDefault("Damage.EnablePermissions", false);
        config.addDefault("Damage.DisableVillagerDamage", false);
        config.addDefault("Damage.DisablePlayerDamage", false);
        config.addDefault("Damage.DisableExplosionDamage", false);
        config.addDefault("Damage.DisableFireDamage", false);
        config.addDefault("Damage.DisableContactDamage", false);
        config.addDefault("Damage.DisableCustomDamage", false);
        config.addDefault("Damage.DisableDrowningDamage", false);
        config.addDefault("Damage.DisableEntityAttackDamage", false);
        config.addDefault("Damage.DisableFallDamage", false);
        config.addDefault("Damage.DisableLavaDamage", false);
        config.addDefault("Damage.DisableLightningDamage", false);
        config.addDefault("Damage.DisableMagicDamage", false);
        config.addDefault("Damage.DisablePoisonDamage", false);
        config.addDefault("Damage.DisableProjectileDamage", false);
        config.addDefault("Damage.DisableStarvationDamage", false);
        config.addDefault("Damage.DisableSuffocationDamage", false);
        config.addDefault("Damage.DisableSuicideDamage", false);
        config.addDefault("Damage.DisableVoidDamage", false);

        config.addDefault("HealthRegen.DisableHealthRegeneration", false);
        config.addDefault("HealthRegen.DisableCustomHealthRegen", false);
        config.addDefault("HealthRegen.DisableEatingHealthRegen", false);
        config.addDefault("HealthRegen.DisableNaturalHealthRegen", false);
        config.addDefault("HealthRegen.DisableSatiatedHealthRegen", false);
        config.addDefault("HealthRegen.DisableMagicHealthRegen", false);

        this.getConfig().options().copyDefaults(true);
        saveConfig();
    }

    private void loadMessages() {
        messages = getMessages();
        messages.options().header(Data.setMessageHeader());
        
        double ConfigVersion = 3.00;
        messages.addDefault("ConfigVersion", Double.valueOf(ConfigVersion));
        if(messages.getDouble("ConfigVersion") != Double.valueOf(ConfigVersion)) {
            log.warning("[iSafe] ConfigVersion was modified! Setting config version to right value ..");
            messages.set("ConfigVersion", Double.valueOf(ConfigVersion));
        }

        messages.addDefault("Permissions.DefaultNoPermission", "&cNo permission.");
        messages.addDefault("Permissions.NoCmdPermission", "&cNo permission to do this command.");
        messages.addDefault("KickMessage", "&6%playername% was kicked from the game.");
        messages.addDefault("SameNickAlreadyPlaying", "&cThe username &f%playername% &cis already online!");
        messages.addDefault("OnlyOpsCanJoin", "&cOnly OPs can join this server!");
        messages.addDefault("CommandLogger", "%playername% did or tried to do the command %command%");
        //----
        messages.addDefault("Blacklists.Interact.KickMessage", "&cKicked for attempting to interact with &f%block%");
        messages.addDefault("Blacklists.Interact.DisallowedMessage", "&cYou do not have access to interact with &7%block% &cin world &7%world%");
        //----
        messages.addDefault("Blacklists.Place.KickMessage", "&cKicked for attempting to place &f%block%");
        messages.addDefault("Blacklists.Place.DisallowedMessage", "&cYou do not have access to place &7%block% &cin world &7%world%");
        //----
        messages.addDefault("Blacklists.Break.KickMessage", "&cKicked for attempting to break &f%block%");
        messages.addDefault("Blacklists.Break.DisallowedMessage", "&cYou do not have access to break &7%block% &cin world &7%world%");
        //----
        messages.addDefault("Blacklists.Censor.KickMessage", "&cKicked for attempting to send a message contaning &7%word%");
        messages.addDefault("Blacklists.Censor.DisallowedMessage", "&c%word% is censored!");
        //----
        messages.addDefault("Blacklists.Drop.KickMessage", "&cKicked for attempting to drop &f%item%");
        messages.addDefault("Blacklists.Drop.DisallowedMessage", "&cYou do not have access to drop &7%item%");
        //----
        messages.addDefault("Blacklists.Pickup.KickMessage", "&cKicked for attempting to pickup &f%item%");
        messages.addDefault("Blacklists.Pickup.DisallowedMessage", "&cYou do not have access to pickup &7%item%");
        //----
        messages.addDefault("Blacklists.Command.KickMessage", "&cKicked for attempting to do command &f%command%");
        messages.addDefault("Blacklists.Command.DisallowedMessage", "&cThe command %command% is disabled!");

        this.getMessages().options().copyDefaults(true);
        saveMessages();
    }

    private void loadISafeConfig() {
        iSafeConfig = getISafeConfig();
        // Header.
        
        double ConfigVersion = 3.00;
        iSafeConfig.addDefault("ConfigVersion", Double.valueOf(ConfigVersion));
        if(iSafeConfig.getDouble("ConfigVersion") != Double.valueOf(ConfigVersion)) {
            log.warning("[iSafe] ConfigVersion was modified! Setting config version to right value ..");
            iSafeConfig.set("ConfigVersion", Double.valueOf(ConfigVersion));
        }
        
        iSafeConfig.addDefault("VerboseLogging", false);
        iSafeConfig.addDefault("DebugMode", false);
        iSafeConfig.addDefault("CheckForUpdates", true);
        iSafeConfig.addDefault("UseVaultForPermissions", false);
        iSafeConfig.addDefault("CreateUserFiles", true);

        this.getISafeConfig().options().copyDefaults(true);
        saveISafeConfig();
    }

    private void loadCreatureManager() {
        creatureManager = getCreatureManager();
        creatureManager.options().header(Data.setCreatureManagerHeader());
        
        double ConfigVersion = 3.00;
        creatureManager.addDefault("ConfigVersion", Double.valueOf(ConfigVersion));
        if(creatureManager.getDouble("ConfigVersion") != Double.valueOf(ConfigVersion)) {
            log.warning("[iSafe] ConfigVersion was modified! Setting config version to right value ..");
            creatureManager.set("ConfigVersion", Double.valueOf(ConfigVersion));
        }
        
        creatureManager.addDefault("Creatures.CreatureTarget.Disable-closest_player-target", false);
        creatureManager.addDefault("Creatures.CreatureTarget.Disable-custom-target", false);
        creatureManager.addDefault("Creatures.CreatureTarget.Disable-forgot_target-target", false);
        creatureManager.addDefault("Creatures.CreatureTarget.Disable-owner_attacked_target-target", false);
        creatureManager.addDefault("Creatures.CreatureTarget.Disable-pig_zombie_target-target", false);
        creatureManager.addDefault("Creatures.CreatureTarget.Disable-random_target-target", false);
        creatureManager.addDefault("Creatures.CreatureTarget.Disable-target_attacked_entity-target", false);
        creatureManager.addDefault("Creatures.CreatureTarget.Disable-target_attacked_owner-target", false);
        creatureManager.addDefault("Creatures.CreatureTarget.Disable-target_died-target", false);
        
        creatureManager.addDefault("Creatures.PoweredCreepers.DisableLightningCause", false);
        creatureManager.addDefault("Creatures.PoweredCreepers.DisableSetOffCause", false);
        creatureManager.addDefault("Creatures.PoweredCreepers.DisableSetOnCause", false);
        
        creatureManager.addDefault("Creatures.Endermen.PreventEndermenGriefing", false);
        creatureManager.addDefault("Creatures.Tame.DisableTaming", false);
        creatureManager.addDefault("Creatures.Slime.DisableSlimeSplit", false);
        creatureManager.addDefault("Creatures.Pig.DisabletPigZap", false);
        creatureManager.addDefault("Creatures.Zombie.DisableDoorBreak", false);
        creatureManager.addDefault("Creatures.Death.DisableDrops", false);
        creatureManager.addDefault("Creatures.DisableCropTrampling", false);
        
        creatureManager.addDefault("Creatures.SheepDyeWool.TotallyDisable", false);
        creatureManager.addDefault("Creatures.SheepDyeWool.DisableColor.Black", false);
        creatureManager.addDefault("Creatures.SheepDyeWool.DisableColor.Blue", false);
        creatureManager.addDefault("Creatures.SheepDyeWool.DisableColor.Brown", false);
        creatureManager.addDefault("Creatures.SheepDyeWool.DisableColor.Cyan", false);
        creatureManager.addDefault("Creatures.SheepDyeWool.DisableColor.Gray", false);
        creatureManager.addDefault("Creatures.SheepDyeWool.DisableColor.Green", false);
        creatureManager.addDefault("Creatures.SheepDyeWool.DisableColor.Light_Blue", false);
        creatureManager.addDefault("Creatures.SheepDyeWool.DisableColor.Lime", false);
        creatureManager.addDefault("Creatures.SheepDyeWool.DisableColor.Magenta", false);
        creatureManager.addDefault("Creatures.SheepDyeWool.DisableColor.Orange", false);
        creatureManager.addDefault("Creatures.SheepDyeWool.DisableColor.Pink", false);
        creatureManager.addDefault("Creatures.SheepDyeWool.DisableColor.Purple", false);
        creatureManager.addDefault("Creatures.SheepDyeWool.DisableColor.Red", false);
        creatureManager.addDefault("Creatures.SheepDyeWool.DisableColor.Silver", false);
        creatureManager.addDefault("Creatures.SheepDyeWool.DisableColor.White", false);
        creatureManager.addDefault("Creatures.SheepDyeWool.DisableColor.Yellow", false);
        
        creatureManager.addDefault("Creatures.Combusting.DisableFor-allCreatures", false);
        creatureManager.addDefault("Creatures.Combusting.DisableFor.Giant", false);
        creatureManager.addDefault("Creatures.Combusting.DisableFor.PigZombie", false);
        creatureManager.addDefault("Creatures.Combusting.DisableFor.Skeleton", false);
        creatureManager.addDefault("Creatures.Combusting.DisableFor.Zombie", false);
        
        creatureManager.addDefault("Creatures.Damage.DisableFireDamage", false);
        creatureManager.addDefault("Creatures.Damage.DisableContactDamage", false);
        creatureManager.addDefault("Creatures.Damage.DisableCustomDamage", false);
        creatureManager.addDefault("Creatures.Damage.DisableDrowningDamage", false);
        creatureManager.addDefault("Creatures.Damage.DisableEntityAttackDamage", false);
        creatureManager.addDefault("Creatures.Damage.DisableFallDamage", false);
        creatureManager.addDefault("Creatures.Damage.DisableLavaDamage", false);
        creatureManager.addDefault("Creatures.Damage.DisableLightningDamage", false);
        creatureManager.addDefault("Creatures.Damage.DisableMagicDamage", false);
        creatureManager.addDefault("Creatures.Damage.DisablePoisonDamage", false);
        creatureManager.addDefault("Creatures.Damage.DisableProjectileDamage", false);
        creatureManager.addDefault("Creatures.Damage.DisableStarvationDamage", false);
        creatureManager.addDefault("Creatures.Damage.DisableSuffocationDamage", false);
        creatureManager.addDefault("Creatures.Damage.DisableSuicideDamage", false);
        creatureManager.addDefault("Creatures.Damage.DisableVoidDamage", false);

        //MobSpawn blacklists.
        //Natural
        creatureManager.addDefault("MobSpawn.Natural.Debug.ToConsole", false);
        creatureManager.addDefault("MobSpawn.Natural.EnabledWorlds", Arrays.asList(Data.NaturalMSBlacklistWorldList));
        Data.NaturalMSBlacklistWorld = creatureManager.getStringList("MobSpawn.Natural.Worlds");
        creatureManager.addDefault("MobSpawn.Natural.Blacklist", Arrays.asList(Data.NaturalMSBlacklistList));
        Data.NaturalMSBlacklist = creatureManager.getStringList("MobSpawn.Natural.Blacklist");
        
        //Spawner
        creatureManager.addDefault("MobSpawn.Spawner.Debug.ToConsole", false);
        creatureManager.addDefault("MobSpawn.Spawner.EnabledWorlds", Arrays.asList(Data.SpawnerMSBlacklistWorldList));
        Data.SpawnerMSBlacklistWorld = creatureManager.getStringList("MobSpawn.Spawner.Worlds");
        creatureManager.addDefault("MobSpawn.Spawner.Blacklist", Arrays.asList(Data.SpawnerMSBlacklistList));
        Data.SpawnerMSBlacklist = creatureManager.getStringList("MobSpawn.Spawner.Blacklist");
        
        //Custom
        creatureManager.addDefault("MobSpawn.Custom.Debug.ToConsole", false);
        creatureManager.addDefault("MobSpawn.Custom.EnabledWorlds", Arrays.asList(Data.CustomMSBlacklistWorldList));
        Data.CustomMSBlacklistWorld = creatureManager.getStringList("MobSpawn.Custom.Worlds");
        creatureManager.addDefault("MobSpawn.Custom.Blacklist", Arrays.asList(Data.CustomMSBlacklistList));
        Data.CustomMSBlacklist = creatureManager.getStringList("MobSpawn.Custom.Blacklist");
        
        //Egg(Chicken egg)
        creatureManager.addDefault("MobSpawn.Egg.Debug.ToConsole", false);
        creatureManager.addDefault("MobSpawn.Egg.EnabledWorlds", Arrays.asList(Data.EggMSBlacklistWorldList));
        Data.EggMSBlacklistWorld = creatureManager.getStringList("MobSpawn.Egg.Worlds");
        creatureManager.addDefault("MobSpawn.Egg.Blacklist", Arrays.asList(Data.EggMSBlacklistList));
        Data.EggMSBlacklist = creatureManager.getStringList("MobSpawn.Egg.Blacklist");
        
        //SpawnerEgg
        creatureManager.addDefault("MobSpawn.SpawnerEgg.Debug.ToConsole", false);
        creatureManager.addDefault("MobSpawn.SpawnerEgg.EnabledWorlds", Arrays.asList(Data.SpawnerEggMSBlacklistWorldList));
        Data.SpawnerEggMSBlacklistWorld = creatureManager.getStringList("MobSpawn.SpawnerEgg.Worlds");
        creatureManager.addDefault("MobSpawn.SpawnerEgg.Blacklist", Arrays.asList(Data.SpawnerEggMSBlacklistList));
        Data.SpawnerEggMSBlacklist = creatureManager.getStringList("MobSpawn.SpawnerEgg.Blacklist");

        this.getCreatureManager().options().copyDefaults(true);
        saveCreatureManager();
    }

    private void loadBlacklist() {
        blacklist = getBlacklist();
        blacklist.options().header(Data.setBlacklistHeader());
        
        double ConfigVersion = 3.00;
        blacklist.addDefault("ConfigVersion", Double.valueOf(ConfigVersion));
        if(blacklist.getDouble("ConfigVersion") != Double.valueOf(ConfigVersion)) {
            log.warning("[iSafe] ConfigVersion was modified! Setting config version to right value ..");
            blacklist.set("ConfigVersion", Double.valueOf(ConfigVersion));
        }
        
        blacklist.addDefault("Place.TotallyDisableBlockPlace", false);
        blacklist.addDefault("Place.KickPlayer", false);
        blacklist.addDefault("Place.Alert/log.ToConsole", true);
        blacklist.addDefault("Place.Alert/log.ToPlayer", true);
        blacklist.addDefault("Place.Gamemode.PreventFor.Survival", true);
        blacklist.addDefault("Place.Gamemode.PreventFor.Creative", true);
        blacklist.addDefault("Place.EnabledWorlds", Arrays.asList(Data.PlaceBlacklistWorldList));
        Data.PlaceBlacklistWorld = blacklist.getStringList("Place.EnabledWorlds");
        blacklist.addDefault("Place.Blacklist", Arrays.asList(Data.PlaceBlacklistList));
        Data.PlaceBlacklist = blacklist.getStringList("Place.Blacklist");
        
        
        blacklist.addDefault("Break.TotallyDisableBlockBreak", false);
        blacklist.addDefault("Break.KickPlayer", false);
        blacklist.addDefault("Break.Alert/log.ToConsole", true);
        blacklist.addDefault("Break.Alert/log.ToPlayer", true);
        blacklist.addDefault("Break.Gamemode.PreventFor.Survival", true);
        blacklist.addDefault("Break.Gamemode.PreventFor.Creative", true);
        blacklist.addDefault("Break.EnabledWorlds", Arrays.asList(Data.BreakBlacklistWorldList));
        Data.BreakBlacklistWorld = blacklist.getStringList("Break.EnabledWorlds");
        blacklist.addDefault("Break.Blacklist", Arrays.asList(Data.BreakBlacklistList));
        Data.BreakBlacklist = blacklist.getStringList("Break.Blacklist");
        
        
        blacklist.addDefault("Drop.TotallyDisableBlockDrop", false);
        blacklist.addDefault("Drop.KickPlayer", false);
        blacklist.addDefault("Drop.Alert/log.ToConsole", true);
        blacklist.addDefault("Drop.Alert/log.ToPlayer", true);
        blacklist.addDefault("Drop.Gamemode.PreventFor.Survival", true);
        blacklist.addDefault("Drop.Gamemode.PreventFor.Creative", true);
        blacklist.addDefault("Drop.EnabledWorlds", Arrays.asList(Data.DropBlacklistWorldList));
        Data.DropBlacklistWorld = blacklist.getStringList("Drop.EnabledWorlds");
        blacklist.addDefault("Drop.Blacklist", Arrays.asList(Data.DropBlacklistList));
        Data.DropBlacklist = blacklist.getStringList("Drop.Blacklist");
        
        
        blacklist.addDefault("Pickup.TotallyDisableBlockPickup", false);
        blacklist.addDefault("Pickup.KickPlayer", false);
        /*blacklist.addDefault("Pickup.Alert/log.To-console", true);
        blacklist.addDefault("Pickup.Alert/log.To-player", true);*/
        blacklist.addDefault("Pickup.Gamemode.PreventFor.Survival", true);
        blacklist.addDefault("Pickup.Gamemode.PreventFor.Creative", true);
        blacklist.addDefault("Pickup.EnabledWorlds", Arrays.asList(Data.PickupBlacklistWorldList));
        Data.PickupBlacklistWorld = blacklist.getStringList("Pickup.EnabledWorlds");
        blacklist.addDefault("Pickup.Blacklist", Arrays.asList(Data.PickupBlacklistList));
        Data.PickupBlacklist = blacklist.getStringList("Pickup.Blacklist");

        
        blacklist.addDefault("Command.TotallyDisallowCommands", false);
        blacklist.addDefault("Command.KickPlayer", false);
        blacklist.addDefault("Command.Alert/log.ToConsole", true);
        blacklist.addDefault("Command.Alert/log.ToPlayer", true);
        blacklist.addDefault("Command.EnabledWorlds", Arrays.asList(Data.CmdBlacklistWorldList));
        Data.CmdBlacklistWorld = blacklist.getStringList("Command.EnabledWorlds");
        blacklist.addDefault("Command.Blacklist", Arrays.asList(Data.CmdBlacklistList));
        Data.CmdBlacklist = blacklist.getStringList("Command.Blacklist");
        
        
        blacklist.addDefault("Censor.Alert/log.ToConsole", false);
        blacklist.addDefault("Censor.Alert/log.ToPlayer", true);
        blacklist.addDefault("Censor.Words/Blacklist", Arrays.asList(Data.WordBlacklistList));
        Data.WordBlacklist = blacklist.getStringList("Censor.Words/Blacklist");
        
        
        blacklist.addDefault("Dispense.Worlds", Arrays.asList(Data.DispenseBlacklistWorldList));
        Data.DispenseBlacklistWorld = blacklist.getStringList("Dispense.Worlds");
        blacklist.addDefault("Dispense.Blacklist", Arrays.asList(Data.DispenseBlacklistList));
        Data.DispenseBlacklist = blacklist.getStringList("Dispense.Blacklist");
        
        
        blacklist.addDefault("Interact.KickPlayer", false);
        blacklist.addDefault("Interact.Alert/log.ToPlayer", true);
        blacklist.addDefault("Interact.Alert/log.ToConsole", true);
        blacklist.addDefault("Interact.Gamemode.PreventFor.Survival", true);
        blacklist.addDefault("Interact.Gamemode.PreventFor.Creative", true);
        blacklist.addDefault("Interact.EnabledWorlds", Arrays.asList(Data.InteractBlacklistList));
        Data.InteractBlacklist = blacklist.getStringList("Interact.EnabledWorlds");
        blacklist.addDefault("Interact.Blacklist", Arrays.asList(Data.InteractBlacklistWorldList));
        Data.InteractBlacklistWorld = blacklist.getStringList("Interact.Blacklist");

        this.getBlacklist().options().copyDefaults(true);
        saveBlacklist();
    }

    public void reloadISafeConfig() {
        if (iSafeConfigFile == null) {
            iSafeConfigFile = new File(getDataFolder(), "iSafeConfig.yml");
        }
        iSafeConfig = YamlConfiguration.loadConfiguration(iSafeConfigFile);

        // Look for defaults in the jar
        InputStream defConfigStream = getResource("iSafeConfig.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            iSafeConfig.setDefaults(defConfig);
        }
    }

    public FileConfiguration getISafeConfig() {
        if (iSafeConfig == null) {
            reloadBlacklist();
        }
        return iSafeConfig;
    }

    public void saveISafeConfig() {
        if (iSafeConfig == null || iSafeConfigFile == null) {
            return;
        }
        try {
            iSafeConfig.save(iSafeConfigFile);
        } catch (IOException ex) {
            Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Error saving iSafeConfig to " + iSafeConfigFile, ex);
        }
    }

    public void reloadMessages() {
        if (messagesFile == null) {
            messagesFile = new File(getDataFolder(), "Messages.yml");
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);

        // Look for defaults in the jar
        InputStream defConfigStream = getResource("Messages.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            messages.setDefaults(defConfig);
        }
    }

    public FileConfiguration getMessages() {
        if (messages == null) {
            reloadBlacklist();
        }
        return messages;
    }

    public void saveMessages() {
        if (messages == null || messagesFile == null) {
            return;
        }
        try {
            messages.save(messagesFile);
        } catch (IOException ex) {
            Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Error saving Messages to " + messagesFile, ex);
        }
    }

    public void reloadBlacklist() {
        if (blacklistFile == null) {
            blacklistFile = new File(getDataFolder(), "blacklist.yml");
        }
        blacklist = YamlConfiguration.loadConfiguration(blacklistFile);

        // Look for defaults in the jar
        InputStream defConfigStream = getResource("blacklist.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            blacklist.setDefaults(defConfig);
        }
    }

    public FileConfiguration getBlacklist() {
        if (blacklist == null) {
            reloadBlacklist();
        }
        return blacklist;
    }

    public void saveBlacklist() {
        if (blacklist == null || blacklistFile == null) {
            return;
        }
        try {
            blacklist.save(blacklistFile);
        } catch (IOException ex) {
            Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Error saving blacklist to " + blacklistFile, ex);
        }
    }

    //MobsConfig re-do:
    public void reloadCreatureManager() {
        if (creatureManagerFile == null) {
            creatureManagerFile = new File(getDataFolder(), "creatureManager.yml");
        }
        creatureManager = YamlConfiguration.loadConfiguration(creatureManagerFile);

        InputStream defConfigStream = getResource("creatureManager.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            creatureManager.setDefaults(defConfig);
        }
    }

    public FileConfiguration getCreatureManager() {
        if (creatureManager == null) {
            reloadCreatureManager();
        }
        return creatureManager;
    }

    public void saveCreatureManager() {
        if (creatureManager == null || creatureManagerFile == null) {
            return;
        }
        try {
            creatureManager.save(creatureManagerFile);
        } catch (IOException ex) {
            Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Error saving creatureManager to " + creatureManagerFile, ex);
        }
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        verboseLog("[iSafe] Hooked to permissions plugin: " + perms.getName());
        return perms != null;
    }

    private void checkMatch() {
        PluginDescriptionFile pdffile = this.getDescription();
        if (!(pdffile.getFullName().equals(fileversion))) {
            log.info("-----  iSafe vMatchConflict  -----");
            log.warning("[iSafe] The version in the pdffile is not the same as the file.");
            log.info("[iSafe] pdffile version: " + pdffile.getFullName());
            log.info("[iSafe] File version: " + fileversion);
            log.warning("[iSafe] Please deliver this infomation to " + pdffile.getAuthors() + " at BukkitDev.");
            log.info("-----  --------------------  -----");
        }
    }

    //Update checker
    private String updateCheck(String currentVersion) throws Exception {
        String pluginUrlString = "http://dev.bukkit.org/server-mods/blockthattnt/files.rss";
        try {
            URL url = new URL(pluginUrlString);
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openConnection().getInputStream());
            doc.getDocumentElement().normalize();
            NodeList nodes = doc.getElementsByTagName("item");
            Node firstNode = nodes.item(0);
            if (firstNode.getNodeType() == 1) {
                Element firstElement = (Element) firstNode;
                NodeList firstElementTagName = firstElement.getElementsByTagName("title");
                Element firstNameElement = (Element) firstElementTagName.item(0);
                NodeList firstNodes = firstNameElement.getChildNodes();
                return firstNodes.item(0).getNodeValue();
            }
        } catch (Exception ignored) {
            //Ingored
        }
        return currentVersion;
    }
}
