package me.josielcm.magicbows;

import me.josielcm.magicbows.managers.Bow;
import me.josielcm.magicbows.managers.CooldownManager;
import me.josielcm.magicbows.managers.update.UpdateChecker;
import me.josielcm.magicbows.utils.enable.Logs;
import me.josielcm.magicbows.utils.enable.Registers;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public final class MagicBows extends JavaPlugin {

    private static MagicBows plugin;
    public FileConfiguration bowsConfig;
    private List<Bow> bows = new ArrayList<>();
    private CooldownManager cooldownManager;

    @Override
    public void onEnable() {
        plugin = this;

        Registers.regCommands();
        Registers.regListeners();
        loadConfig();


        cooldownManager = new CooldownManager();

        Logs.enableLogs(this);

    }
    private void versionCheckUpdate(){
        UpdateChecker.init(this, 109937).requestUpdateCheck().whenComplete((result, exception) -> {
            if (result.requiresUpdate()) {
                this.getLogger().warning(String.format("An update is available! MagicBows %s | Download here: https://www.spigotmc.org/resources/magicbows-create-your-customs-bows-and-add-custom-mechanics.109937/", result.getNewestVersion()));
                return;
            }

            UpdateChecker.UpdateReason reason = result.getReason();
            if (reason == UpdateChecker.UpdateReason.UP_TO_DATE) {
                this.getLogger().info(String.format("Your version of MagicBows (%s) is up to date!", result.getNewestVersion()));
            } else if (reason == UpdateChecker.UpdateReason.UNRELEASED_VERSION) {
                this.getLogger().warning(String.format("Your version of MagicBows (%s) is more recent than the one publicly available. Are you on a development build?", result.getNewestVersion()));
            } else {
                this.getLogger().warning("Could not check for a new version of MagicBows. Reason: " + reason);
            }
        });
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    private void loadConfig() {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            getLogger().warning("Config file not found! Regenerating... ");
            getLogger().info("Config file has been regenerated");
            getConfig().options().copyDefaults(true);
            saveDefaultConfig();
        }

        File bowsFile = new File(getDataFolder(), "bows.yml");
        if (!bowsFile.exists()) {
            try {
                bowsFile.createNewFile();
                InputStream inputStream = getResource("bows.yml");
                OutputStream outputStream = new FileOutputStream(bowsFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                inputStream.close();
                outputStream.close();
                getLogger().warning("Bows file not found! Regenerating... ");
                getLogger().info("Created bows.yml file");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        bowsConfig = YamlConfiguration.loadConfiguration(bowsFile);

        if (bowsConfig.contains("bows")) {
            // Obtener la sección 'bows' de la configuración
            ConfigurationSection bowsSection = bowsConfig.getConfigurationSection("bows");

            // Recorrer todas las claves de la sección 'bows'
            for (String bowName : bowsSection.getKeys(false)) {
                // Obtener la sección correspondiente a este arco
                ConfigurationSection bowSection = bowsSection.getConfigurationSection(bowName);
                if (bowSection == null) {
                    getLogger().warning("Invalid configuration for bow " + bowName + "!");
                    continue;
                }

                // Obtener valores de configuración
                boolean enchanted = bowSection.getBoolean("enchanted", false);
                List<String> lore = bowSection.getStringList("lore");

                // Crear el objeto Bow
                Bow bow = new Bow(bowName, enchanted, lore);
                bows.add(bow);
            }
        } else {
            getLogger().warning("No 'bows' section found in bows.yml! Regenerating default config!");
        }
    }


    public static MagicBows getPlugin() {
        return plugin;
    }

    public void onLoad() {
        Logs.loadLogs(this);
        versionCheckUpdate();
    }

    @Override
    public void onDisable() {
        Logs.disablelogs(this);
    }

    public ItemStack getBow(String bowName) {
        ConfigurationSection bowsSection = bowsConfig.getConfigurationSection("bows");
        if (bowsSection == null) {
            return null;
        }
        for (String key : bowsSection.getKeys(false)) {
            if (key.equalsIgnoreCase(bowName)) {
                ConfigurationSection bowSection = bowsSection.getConfigurationSection(key);
                String materialName = bowSection.getString("material", "BOW");
                Material material = Material.getMaterial(materialName);
                if (material == null) {
                    material = Material.BOW;
                }
                int amount = bowSection.getInt("amount", 1);
                ItemStack bow = new ItemStack(material, amount);
                ItemMeta meta = bow.getItemMeta();
                if (meta == null) {
                    return null;
                }
                String displayName = bowSection.getString("display-name");
                if (displayName != null) {
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
                }
                List<String> lore = bowSection.getStringList("lore");
                if (!lore.isEmpty()) {
                    List<String> translatedLore = new ArrayList<>();
                    for (String line : lore) {
                        translatedLore.add(ChatColor.translateAlternateColorCodes('&', line));
                    }
                    meta.setLore(translatedLore);
                }
                Boolean hideEnchat = bowsConfig.getBoolean("bows." + bowName + ".hide-enchant");
                if (hideEnchat != null && hideEnchat) {
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }

                Boolean setUnbreak = bowsConfig.getBoolean("bows." + bowName + ".unbreakable");
                if (setUnbreak != null && setUnbreak) {
                    meta.setUnbreakable(setUnbreak);
                }
                Boolean hideUnbreak = bowsConfig.getBoolean("bows." + bowName + ".hide-unbreakable");
                if (hideUnbreak != null && hideUnbreak) {
                    meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
                }

                ConfigurationSection enchantmentsSection = bowSection.getConfigurationSection("enchantments");
                if (enchantmentsSection != null) {
                    for (String enchantmentName : enchantmentsSection.getKeys(false)) {
                        Enchantment enchantment = Enchantment.getByName(enchantmentName.toUpperCase());
                        if (enchantment == null) {
                            continue;
                        }
                        int level = enchantmentsSection.getInt(enchantmentName);
                        if (level < 1) {
                            continue;
                        }
                        meta.addEnchant(enchantment, level, true);
                    }
                }
                bow.setItemMeta(meta);
                return bow;
            }
        }
        return null;
    }
}
