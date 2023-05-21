package me.josielcm.magicbows.commands;

import org.bukkit.entity.Player;

import java.util.List;

public abstract class SubCommand {
    public abstract String getName();
    public abstract String getDescription();
    public abstract boolean perform(Player p, String[] args);
    public abstract List<String> getSubArgs(Player p, String args[]);

    public abstract List<String> tabComplete(Player player, String[] args);
}
