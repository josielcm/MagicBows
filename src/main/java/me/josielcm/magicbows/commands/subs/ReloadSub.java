package me.josielcm.magicbows.commands.subs;

import me.josielcm.magicbows.MagicBows;
import me.josielcm.magicbows.commands.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class ReloadSub extends SubCommand {
    private final MagicBows plugin;

    public ReloadSub(MagicBows plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public boolean perform(Player p, String[] args) {
        Plugin pluginRe = Bukkit.getPluginManager().getPlugin("MagicBows");
        if (pluginRe != null) {
            Bukkit.getPluginManager().disablePlugin(plugin);
            Bukkit.getPluginManager().enablePlugin(plugin);
            p.sendMessage(ChatColor.GREEN + "Plugin reloaded.");
        } else {
            p.sendMessage(ChatColor.RED + "Plugin no found!");
        }
        return false;
    }

    @Override
    public List<String> getSubArgs(Player p, String[] args) {
        return null;
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return null;
    }
}
