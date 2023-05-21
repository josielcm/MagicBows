package me.josielcm.magicbows.managers;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CooldownManager {
    private final Map<UUID, Map<String, Long>> cooldowns;

    public CooldownManager() {
        this.cooldowns = new HashMap<>();
    }

    public void setCooldown(UUID playerId, String cooldownName, long duration) {
        if (!cooldowns.containsKey(playerId)) {
            cooldowns.put(playerId, new HashMap<>());
        }

        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        playerCooldowns.put(cooldownName, System.currentTimeMillis() + (duration * 1000));
    }

    public long getRemainingTime(UUID playerId, String cooldownName) {
        if (!cooldowns.containsKey(playerId)) {
            return 0;
        }

        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        if (!playerCooldowns.containsKey(cooldownName)) {
            return 0;
        }

        long remainingTime = playerCooldowns.get(cooldownName) - System.currentTimeMillis();
        return remainingTime < 0 ? 0 : remainingTime;
    }

    public boolean isCooldownActive(Player player, String key) {
        if (!cooldowns.containsKey(player.getUniqueId()) || !cooldowns.get(player.getUniqueId()).containsKey(key)) {
            return false;
        }
        long cooldownEnd = cooldowns.get(player.getUniqueId()).get(key);
        return System.currentTimeMillis() < cooldownEnd;
    }
    public void addCooldown(UUID playerId, String cooldownId, long duration) {
        cooldowns.computeIfAbsent(playerId, p -> new HashMap<>());
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);

        long expirationTime = System.currentTimeMillis() + (duration * 1000);
        playerCooldowns.put(cooldownId, expirationTime);
    }
    String formatTime(long cooldownTime) {
        long days = TimeUnit.MILLISECONDS.toDays(cooldownTime);
        long hours = TimeUnit.MILLISECONDS.toHours(cooldownTime) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(cooldownTime) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(cooldownTime) % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (hours > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0) {
            sb.append(minutes).append("m ");
        }
        if (seconds > 0) {
            sb.append(seconds).append("s");
        }

        return sb.toString();
    }



    public boolean hasCooldown(UUID playerId, String cooldownName) {
        if (!cooldowns.containsKey(playerId)) {
            return false;
        }

        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        if (!playerCooldowns.containsKey(cooldownName)) {
            return false;
        }

        return playerCooldowns.get(cooldownName) > System.currentTimeMillis();
    }

    public void removeCooldown(UUID playerId, String cooldownName) {
        if (!cooldowns.containsKey(playerId)) {
            return;
        }

        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        playerCooldowns.remove(cooldownName);
    }
}

