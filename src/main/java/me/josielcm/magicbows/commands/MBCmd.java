package me.josielcm.magicbows.commands;

import me.josielcm.magicbows.MagicBows;
import me.josielcm.magicbows.commands.subs.GetBowSub;
import me.josielcm.magicbows.commands.subs.ReloadSub;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MBCmd implements TabExecutor {
    private ArrayList<SubCommand> subcommnads = new ArrayList<>();
    private final MagicBows plugin;

    public MBCmd(MagicBows plugin) {
        this.plugin = plugin;
        subcommnads.add(new ReloadSub(plugin));
        subcommnads.add(new GetBowSub(plugin));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (p.hasPermission("magicbows.command")) {
                if (args.length > 0) {
                    for (int i = 0; i < getSubcommands().size(); i++) {
                        if (args[0].equalsIgnoreCase(getSubcommands().get(i).getName())) {
                            getSubcommands().get(i).perform(p, args);
                        }
                    }
                } else {
                    if (p.hasPermission("magicbows.error")) {
                        String noArgsFound = plugin.getConfig().getString("error-arg", "&5Magic Bows &7: SubCommand not found.");

                        p.sendMessage(noArgsFound);
                    }
                }
            } else {
                String noPerms = plugin.getConfig().getString("no-perms-command", "&5Magic Bows &7: You do not have permission to access this command");

                p.sendMessage(noPerms);
            }
        }

        return false;
    }
    public ArrayList<SubCommand> getSubcommands() {
        return subcommnads;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (sender.hasPermission("magicbows.command.tabcomplete")) {
            if (args.length == 1) {

                ArrayList<String> subCommadsArgs = new ArrayList<>();
                for (int i = 0; i < getSubcommands().size(); i++) {
                    subCommadsArgs.add(getSubcommands().get(i).getName());
                }
                return subCommadsArgs;

            } else if (args.length >= 2) {
                for (int i = 0; i < getSubcommands().size(); i++) {
                    if (args[0].equalsIgnoreCase(getSubcommands().get(i).getName())) {
                        return getSubcommands().get(i).getSubArgs((Player) sender, args);
                    }
                }
            }
        }
        return null;
    }
}
