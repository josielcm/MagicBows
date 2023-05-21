package me.josielcm.magicbows.utils.enable;

import me.josielcm.magicbows.MagicBows;
import org.bukkit.plugin.Plugin;

public class Logs {

    public static void enableLogs(Plugin plugin){
        plugin.getLogger().info("");
        plugin.getLogger().info("MagicBows enabled!");
        plugin.getLogger().info("Fully loaded.");
        plugin.getLogger().info("");
    }
    public static void disablelogs(Plugin plugin){
        plugin.getLogger().info("");
        plugin.getLogger().info("MagicBows disabled!");
        plugin.getLogger().info("");
    }
    public static void loadLogs(Plugin plugin){
        plugin.getLogger().info("");
        plugin.getLogger().info("MagicBows loading...");
        plugin.getLogger().info("");
        plugin.getLogger().warning("Checking files...");
        plugin.getLogger().warning("Checking bows...");
        plugin.getLogger().warning("Checking configurations...");
        plugin.getLogger().warning("Checking versions...");
        plugin.getLogger().info("");
    }

}
