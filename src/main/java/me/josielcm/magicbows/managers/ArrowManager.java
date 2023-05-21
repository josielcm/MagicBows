package me.josielcm.magicbows.managers;

import me.josielcm.magicbows.MagicBows;
import me.josielcm.magicbows.managers.trails.TrailManager;
import me.josielcm.magicbows.utils.msg.RainbowText;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOError;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.bukkit.Bukkit.getWorld;

public class ArrowManager implements Listener {
    MagicBows plugin = MagicBows.getPlugin();
    private final Map<Block, Material> originalBlocks = new HashMap<>();

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        if (!(projectile instanceof Arrow)) {
            return;
        }
        Arrow arrow = (Arrow) projectile;
        if (!(arrow.getShooter() instanceof Player)) {
            return;
        }

        Block hitBlock = arrow.getLocation().getBlock().getRelative(BlockFace.DOWN);

        Player player = (Player) arrow.getShooter();

        String soundErrorGeneral = plugin.getConfig().getString("sounds.error-general", "BLOCK_ANVIL_PLACE");
        int soundGeneralVol = plugin.getConfig().getInt("sounds.error-general", (int) 1.0f),
                soundGeneralPitch = plugin.getConfig().getInt("sounds.error-general", (int) 1.0f);

        ItemStack bow = player.getInventory().getItemInMainHand();
        if (bow.getType() != Material.BOW) {
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

                            CooldownManager cooldownManager = plugin.getCooldownManager();

                            String /*cooldownMessage = bowSection.getString("cooldown-message", "&7You must wait &6%timeleft% &7seconds before using this bow again."),*/
                                    noPermsMsg = bowSection.getString("permissions.message", "You do not have permission to use this bow."),
                                    permissionUseBow = bowSection.getString("permissions.perm", null);

                            boolean shouldTeleport = bowSection.getBoolean("teleport", false),
                                    explosionBool = bowSection.getBoolean("explosion", false),
                                    generateBlocks = bowSection.getBoolean("block-generation.enable", false),
                                    shouldMessageTeleport = bowSection.getBoolean("use-message-don't-teleport", false),
                                    permissionUse = bowSection.getBoolean("permissions.enable", false),
                                    useCooldown = bowSection.getBoolean("use-cooldown", false);


                            if (permissionUse && permissionUseBow != null && !player.hasPermission(permissionUseBow)) {
                                Component compNoPerms = Component.text(noPermsMsg);
                                player.playSound(player.getLocation(), soundErrorGeneral, soundGeneralVol, soundGeneralPitch);
                                player.sendMessage(compNoPerms);
                                return;
                            }

                            int cooldown = bowSection.getInt("cooldown", 5);

                            if (useCooldown){
                                if (cooldown > 0) {
                                    if (cooldownManager.isCooldownActive(player, bowName)) {
                                        /*long remainingTime = cooldownManager.getRemainingTime(player.getUniqueId(), bowName);*/
                                        /*String remainingTimeString = cooldownManager.formatTime(remainingTime);*/
                                        /*player.sendMessage(RainbowText.setTextColor(cooldownMessage).replace("%timeleft%", remainingTimeString));*/
                                        event.setCancelled(true);
                                        return;
                                    } else {
                                        cooldownManager.addCooldown(player.getUniqueId(), bowName, cooldown);
                                    }
                                }
                            }


                            if (shouldTeleport) {

                                String soundName = bowSection.getString("sound.sound", "ENTITY_ENDERMAN_TELEPORT"),
                                        particleType = bowSection.getString("particle.type", "PORTAL");

                                int soundVol = bowSection.getInt("sound.volume", (int) 1.0f),
                                        soundPitch = bowSection.getInt("sound.pitch", (int) 1.0f),
                                        particleAmount = bowSection.getInt("particle.amount", 50);

                                Location arrowLocation = arrow.getLocation();
                                arrowLocation.setYaw(player.getLocation().getYaw());
                                arrowLocation.setPitch(player.getLocation().getPitch());
                                arrowLocation.setY(arrowLocation.getY() + 1);

                                player.teleport(arrowLocation);

                                player.getWorld().playSound(arrowLocation, Sound.valueOf(soundName), soundVol, soundPitch);
                                player.spawnParticle(Particle.valueOf(particleType), arrowLocation, particleAmount);
                                arrow.remove();

                                arrow.remove();
                            } else if (shouldMessageTeleport) {
                                player.playSound(player.getLocation(), Sound.valueOf(soundErrorGeneral), soundGeneralVol, soundGeneralPitch);
                                player.sendMessage(RainbowText.setTextColor(bowSection.getString("message-no-tp")));
                                cooldownManager.removeCooldown(player.getUniqueId(), bowName);
                                arrow.remove();
                            }
                            if (explosionBool) {
                                Location arrowLocation = arrow.getLocation();
                                getWorld(arrowLocation.getWorld().getName()).createExplosion(arrowLocation, bowSection.getInt("explosion-radius", 5), true, true);
                                arrow.remove();
                            }

                            if (generateBlocks) {
                                if (!bowSection.isConfigurationSection("block-generation")) {
                                    plugin.getServer().getLogger().warning("Block-generation section is misconfigured or non-existent in bow.yml, please check and fix. Bow: " + bowDisplayName);
                                    arrow.remove();
                                    return;
                                }

                                int
                                        amountParticle = bowSection.getInt("block-generation.particle.amount", 1),
                                        volumeSound = bowSection.getInt("sound.volume", 1),
                                        pitchSound = bowSection.getInt("sound.pitch", 1);
                                long
                                        cooldownRegen = bowSection.getLong("block-generation.cooldown-regenerate", 200L);
                                boolean
                                        affect_blocks = bowSection.getBoolean("block-generation.affect-blocks", false),
                                        regenerate_blocks = bowSection.getBoolean("block-generation.regenerate-blocks", true);
                                String
                                        typeBlock = bowSection.getString("block-generation.type", "SPHERE"),
                                        materialBlocks = bowSection.getString("block-generation.material", "GLASS"),
                                        particleType = bowSection.getString("block-generation.particle.type", "SOUL_FIRE_FLAME"),
                                        soundType = bowSection.getString("sound.sound", "BLOCK_GLASS_BREAK");

                                double radius = bowSection.getDouble("block-generation.radius", 6.0);

                                if (typeBlock.equals("SPHERE")) {
                                    for (int x = (int) -radius; x <= radius; x++) {
                                        for (int y = (int) -radius; y <= radius; y++) {
                                            for (int z = (int) -radius; z <= radius; z++) {
                                                Block block = hitBlock.getRelative(x, y, z);
                                                double distanceSquared = x * x + y * y + z * z;
                                                if (distanceSquared <= radius * radius && distanceSquared > (radius - 1) * (radius - 1)) {
                                                    Material blockType = block.getType();
                                                    if (!affect_blocks && canReplaceBlock(blockType)) {
                                                        if (regenerate_blocks) {
                                                            originalBlocks.put(block, blockType);
                                                        }
                                                        block.setType(Material.valueOf(materialBlocks));

                                                        Location blocksLoc = block.getLocation();

                                                        getWorld(arrow.getLocation().getWorld().getName()).spawnParticle(Particle.valueOf(particleType), blocksLoc, amountParticle);
                                                        player.playSound(player.getLocation(), Sound.valueOf(soundType), volumeSound, pitchSound);
                                                        arrow.remove();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else if (typeBlock.equals("PYRAMID")) {
                                    int pyramidHeight = bowSection.getInt("block-generation.height", 5);
                                    int pyramidRadius = bowSection.getInt("block-generation.radius", 5);

                                    for (int y = 0; y < pyramidHeight; y++) {
                                        int currentRadius = pyramidRadius - y;

                                        for (int x = -currentRadius; x <= currentRadius; x++) {
                                            for (int z = -currentRadius; z <= currentRadius; z++) {
                                                Block block = hitBlock.getRelative(x, y, z);
                                                double distanceSquared = x * x + z * z;
                                                if (distanceSquared <= currentRadius * currentRadius && distanceSquared > (currentRadius - 1) * (currentRadius - 1)) {
                                                    Material blockType = block.getType();
                                                    if (!affect_blocks && canReplaceBlock(blockType)) {
                                                        if (regenerate_blocks) {
                                                            originalBlocks.put(block, blockType);
                                                        }
                                                        block.setType(Material.valueOf(materialBlocks));

                                                        Location blocksLoc = block.getLocation();

                                                        getWorld(arrow.getLocation().getWorld().getName()).spawnParticle(Particle.valueOf(particleType), blocksLoc, amountParticle);
                                                        player.playSound(player.getLocation(), Sound.valueOf(soundType), volumeSound, pitchSound);

                                                        arrow.remove();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (regenerate_blocks) {
                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            for (Block block : originalBlocks.keySet()) {
                                                Material originalType = originalBlocks.get(block);
                                                block.setType(originalType);
                                            }
                                            originalBlocks.clear();
                                        }
                                    }.runTaskLater(plugin, cooldownRegen * 20L);

                                }

                            }

                            arrow.remove();
                            return;
                        }
                    }
                }
                if (player.hasPermission("magicbows.error")) {
                    player.playSound(player.getLocation(), Sound.valueOf(soundErrorGeneral), soundGeneralVol, soundGeneralPitch);
                    String errormsg = plugin.getConfig().getString("bow-no-exists", "&5Magic Bows &7: &cThis bow is not in the configuration or this poorly configured.");
                    player.sendMessage(RainbowText.setTextColor(errormsg));
                    arrow.remove();
                }
            } else {
                if (player.hasPermission("magicbows.error")) {
                    player.playSound(player.getLocation(), Sound.valueOf(soundErrorGeneral), soundGeneralVol, soundGeneralPitch);
                    String errormsg = plugin.getConfig().getString("bows-no-configured", "&5Magic Bows &7: There are no bows configured.");
                    player.sendMessage(RainbowText.setTextColor(errormsg));
                    arrow.remove();
                }
            }
        }

    }

    private boolean canReplaceBlock(Material material) {
        return !isChest(material) &&
                !material.name().endsWith("_SIGN") &&
                !Tag.DOORS.isTagged(material) &&
                !Tag.FENCES.isTagged(material) &&
                !Tag.SLABS.isTagged(material) &&
                !Tag.TRAPDOORS.isTagged(material);
    }

    private boolean isChest(Material material) {
        return material == Material.CHEST ||
                material == Material.TRAPPED_CHEST;
    }

    @EventHandler
    public void onPlayerFallDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        ItemStack bow = player.getInventory().getItemInMainHand();

        ConfigurationSection bowsSection = plugin.bowsConfig.getConfigurationSection("bows");
        if (bowsSection != null) {
            for (String bowKey : bowsSection.getKeys(false)) {
                ConfigurationSection bowSection = bowsSection.getConfigurationSection(bowKey);
                if (bowSection != null) {
                    String bowDisplayName = ChatColor.translateAlternateColorCodes('&', bowSection.getString("display-name"));
                    boolean falldamageBole = bowSection.getBoolean("fall-damage", true);

                    if (bow.getItemMeta().displayName() == null || bow.getItemMeta() == null) {
                        return;
                    }

                    if (
                            event.getCause() == EntityDamageEvent.DamageCause.FALL &&
                                    bow.getType() == Material.BOW &&
                                    bowDisplayName.equals(bow.getItemMeta().getDisplayName()) &&
                                    !falldamageBole
                    ) {
                        event.setCancelled(true);
                    }
                }
            }

        }
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        int newSlot = event.getNewSlot();
        ItemStack newItem = player.getInventory().getItem(newSlot);
        ConfigurationSection bowsSection = plugin.bowsConfig.getConfigurationSection("bows");
        if (bowsSection != null) {
            for (String bowKey : bowsSection.getKeys(false)) {
                ConfigurationSection bowSection = bowsSection.getConfigurationSection(bowKey);
                if (bowSection != null) {
                    String bowDisplayName = ChatColor.translateAlternateColorCodes('&', bowSection.getString("display-name"));

                    if (newItem != null && newItem.getType() == Material.BOW && bowDisplayName.equals(newItem.getItemMeta().getDisplayName())) {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(RainbowText.setTextColor(plugin.getConfig().getString("on-hand").replace("%bow%", newItem.getItemMeta().getDisplayName()))));
                    } else {
                        ItemStack oldItem = player.getInventory().getItem(event.getPreviousSlot());

                        if (oldItem != null && oldItem.getType() == Material.BOW && bowDisplayName.equals(oldItem.getItemMeta().getDisplayName())) {
                            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(RainbowText.setTextColor(plugin.getConfig().getString("off-hand").replace("%bow%", oldItem.getItemMeta().getDisplayName()))));
                        }
                    }
                }
            }
        }
    }


}
