package me.josielcm.magicbows.managers.trails;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class TrailManager {
    public static void createArrowTrail(Location location, ParticleEffect particleEffect, Plugin plugin) {
        // Crea una tarea programada para ejecutar el efecto de trayectoria de la flecha
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                // Calcula la posición de la flecha en función del tiempo transcurrido (ticks)
                double x = location.getX() + Math.cos(Math.toRadians(ticks * 5)) * 0.5;
                double y = location.getY() + Math.sin(Math.toRadians(ticks * 5)) * 0.5;
                double z = location.getZ() + Math.sin(Math.toRadians(ticks * 5)) * 0.5;

                // Genera las partículas en la posición calculada
                location.getWorld().spawnParticle(particleEffect.getParticle(), x, y, z, 0, 0, 0, particleEffect.getCount(), particleEffect.getData());

                // Incrementa el contador de ticks
                ticks++;

                // Cancela la tarea después de un número determinado de ticks
                if (ticks >= 360) {
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L); // Ejecuta la tarea cada tick (20 veces por segundo)
    }

    public enum ParticleEffect {
        SPHERE(Particle.REDSTONE, 0, 0, 0, 1),
        TRAIL(Particle.FLAME, 0, 0, 0, 1),
        CREEPER_FACE(Particle.HEART, 0, 0, 0, 1);

        private Particle particle;
        private float offsetX;
        private float offsetY;
        private float offsetZ;
        private int count;

        ParticleEffect(Particle particle, float offsetX, float offsetY, float offsetZ, int count) {
            this.particle = particle;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
            this.count = count;
        }

        public Particle getParticle() {
            return particle;
        }

        public float getOffsetX() {
            return offsetX;
        }

        public float getOffsetY() {
            return offsetY;
        }

        public float getOffsetZ() {
            return offsetZ;
        }

        public int getCount() {
            return count;
        }

        public Particle.DustOptions getData() {
            return new Particle.DustOptions(Color.GREEN, 1);
        }
    }

}
