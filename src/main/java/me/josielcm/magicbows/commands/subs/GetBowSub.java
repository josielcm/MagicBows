package me.josielcm.magicbows.commands.subs;

import me.josielcm.magicbows.MagicBows;
import me.josielcm.magicbows.commands.SubCommand;
import me.josielcm.magicbows.utils.msg.RainbowText;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GetBowSub extends SubCommand {
    private final MagicBows plugin;
    private final HashMap<String, Long> cooldowns = new HashMap<>();


    public GetBowSub(MagicBows plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "get";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public boolean perform(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage("Using the command: /magicbow get <bow name> (player | optional)");
            return true;
        }

        if (cooldowns.containsKey(p.getName())) {
            long secondsLeft = ((cooldowns.get(p.getName()) / 1000) + plugin.getConfig().getInt("cooldown-commands")) - (System.currentTimeMillis() / 1000);
            if (secondsLeft > 0) {
                p.sendMessage(RainbowText.setTextColor(plugin.getConfig().getString("cooldown-command-message").replace("%timeleft%", secondsLeft + "")));
                return true;
            }
        }

        cooldowns.put(p.getName(), System.currentTimeMillis());

        String bowName = args[1];
        ConfigurationSection bowsSection = plugin.bowsConfig.getConfigurationSection("bows");
        if (bowsSection == null || !bowsSection.contains(bowName)) {
            p.sendMessage(RainbowText.setTextColor(plugin.getConfig().getString("bow-not-found").replace("%argument%", bowName)));
            if (bowsSection != null) {
                p.sendMessage(RainbowText.setTextColor(plugin.getConfig().getString("bows-available").replace("%bows%", String.join(", ", bowsSection.getKeys(false)))));
            }
            cooldowns.remove(p.getName());
            return true;
        }

        ItemStack bow = plugin.getBow(bowName);

        if (bow == null) {
            p.sendMessage(RainbowText.setTextColor(plugin.getConfig().getString("bow-does-not-exist").
                    replace("%argument%", bowName)));
            cooldowns.remove(p.getName());
            return true;
        }

        if (args.length == 3){

            Player pVo = Bukkit.getPlayerExact(args[2]);

            if (pVo == null){
                p.sendMessage(Component.text(RainbowText.setTextColor(plugin.getConfig().getString("player-not-exist"))));
                cooldowns.remove(p.getName());
                return false;
            }

            if (!pVo.getInventory().addItem(bow).isEmpty()) {
                pVo.getWorld().dropItem(pVo.getLocation(), bow);
                pVo.sendMessage(RainbowText.setTextColor(plugin.getConfig().getString("full-inventory")));
            } else {
                String displayName = bow.getItemMeta().getDisplayName();
                if (displayName == null || displayName.isEmpty()) {
                    displayName = bow.getType().toString();
                }
                p.sendMessage(RainbowText.setTextColor(plugin.getConfig().getString("bow-received").
                        replace("%bow%", displayName)));
            }

        } else {
            if (!p.getInventory().addItem(bow).isEmpty()) {
                p.getWorld().dropItem(p.getLocation(), bow);
                p.sendMessage(RainbowText.setTextColor(plugin.getConfig().getString("full-inventory")));
            } else {
                String displayName = bow.getItemMeta().getDisplayName();
                if (displayName == null || displayName.isEmpty()) {
                    displayName = bow.getType().toString();
                }
                p.sendMessage(RainbowText.setTextColor(plugin.getConfig().getString("bow-received").replace("%bow%", displayName)));
            }
        }
        return false;
    }

    @Override
    public List<String> getSubArgs(Player p, String[] args) {
        if (args.length == 2){
            List<String> bowsCreated = new ArrayList<>();
            ConfigurationSection bowsSection = plugin.bowsConfig.getConfigurationSection("bows");
            bowsCreated.addAll(bowsSection.getKeys(false));
            return bowsCreated;
        }
        if (args.length == 3){
            List<String> players = new ArrayList<>();
            for (Player playersAll : Bukkit.getOnlinePlayers()){
                players.add(playersAll.getName());
                return players;
            }
        }
        return null;
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (args.length == 2) {
            return getSubArgs(player, args);
        }
        return null;
    }
}
