package de.raidcraft.servershop.util;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Objects;

public final class InventoryUtil {

    public static int countItems(Inventory inventory, Material material) {

        return Arrays.stream(inventory.getContents())
                .filter(Objects::nonNull)
                .filter(itemStack -> itemStack.getType().equals(material))
                .mapToInt(ItemStack::getAmount)
                .sum();
    }
}
