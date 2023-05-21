package me.josielcm.magicbows.managers;

import me.josielcm.magicbows.MagicBows;
import me.josielcm.magicbows.utils.msg.RainbowText;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CancelAction implements Listener {
    private final MagicBows plugin;

    public CancelAction(MagicBows plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteractP(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        ItemStack bow = e.getItem();

        if (bow == null || bow.getType() != Material.BOW || bow.getItemMeta() == null || e.getItem() == null || e.getItem().getItemMeta() == null) {
            return;
        }
        ItemMeta meta = bow.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            String bowName = ChatColor.translateAlternateColorCodes('&', meta.getDisplayName());
            ConfigurationSection bowsSection = plugin.bowsConfig.getConfigurationSection("bows");
            if (bowsSection != null) {

                for (String bowKey : bowsSection.getKeys(false)) {
                    ConfigurationSection bowSection = bowsSection.getConfigurationSection(bowKey);
                    if (bowSection != null) {
                        String bowDisplayName = ChatColor.translateAlternateColorCodes('&', bowSection.getString("display-name"));
                        if (bowDisplayName.equals(bowName)) {
                            if (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                                CooldownManager cooldownManager = plugin.getCooldownManager();

                                String cooldownMessage = bowSection.getString("cooldown-message", "&5Magic Bows &7: &7You must wait &6%timeleft% &7before using this bow again.");

                                int cooldown = bowSection.getInt("cooldown", 5);
                                boolean useCooldown = bowSection.getBoolean("use-cooldown", false);
                                String soundNm = plugin.getConfig().getString("sounds.error-message", "BLOCK_ANVIL_PLACE");
                                int
                                        vol = plugin.getConfig().getInt("sound.volume-general", (int) 1.0f),
                                        pitch = plugin.getConfig().getInt("sound.pitch-general", (int) 1.0f);


                                if (useCooldown){
                                    if (cooldown > 0) {
                                        if (cooldownManager.isCooldownActive(p, bowName)) {
                                            long remainingTime = cooldownManager.getRemainingTime(p.getUniqueId(), bowName);
                                            String remainingTimeString = cooldownManager.formatTime(remainingTime);
                                            p.sendMessage(RainbowText.setTextColor(cooldownMessage).replace("%timeleft%", remainingTimeString));
                                            p.playSound(p.getLocation(), Sound.valueOf(soundNm), vol,pitch);
                                            e.setCancelled(true);

                                            return;
                                        }
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }

    }

}
