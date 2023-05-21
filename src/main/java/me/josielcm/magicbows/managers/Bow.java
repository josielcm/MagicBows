package me.josielcm.magicbows.managers;

import org.bukkit.entity.Arrow;
import org.bukkit.metadata.MetadataValue;

import java.util.ArrayList;
import java.util.List;

public class Bow {
    private final String name;
    private final boolean enchanted;
    private final List<String> lore;

    public Bow(String name, boolean enchanted, List<String> lore) {
        this.name = name;
        this.enchanted = enchanted;
        this.lore = lore;
    }

    // Verificar si el arco corresponde a una flecha
    public boolean matches(Arrow arrow) {
        // Verificar nombre del arco
        if (!arrow.hasMetadata("bow_name")) {
            return false;
        }
        if (!arrow.getMetadata("bow_name").get(0).asString().equals(name)) {
            return false;
        }

        // Verificar si está encantado
        if (enchanted && !arrow.hasMetadata("bow_enchanted")) {
            return false;
        }
        if (!enchanted && arrow.hasMetadata("bow_enchanted")) {
            return false;
        }

        // Verificar lore
        List<String> arrowLore = new ArrayList<>();
        if (arrow.hasMetadata("bow_lore")) {
            List<MetadataValue> metadataList = arrow.getMetadata("bow_lore");
            for (MetadataValue metadata : metadataList) {
                arrowLore.add(metadata.asString());
            }
            if (!arrowLore.equals(lore)) {
                return false;
            }
        } else {
            return false;
        }

        return true;
    }
}
