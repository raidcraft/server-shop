package de.raidcraft.servershop.util;

import de.raidcraft.servershop.entities.ShopSign;
import lombok.NonNull;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

public final class SignUtil {

    private SignUtil() {}

    public static void updateSign(@NonNull ShopSign shopSign) {

        shopSign.getSign().ifPresent(sign -> {

        });
    }

    public static void breakAndDropSign(@NonNull Block block) {

        if (!(block.getState() instanceof Sign)) {
            return;
        }

        World world = block.getLocation().getWorld();
        if (world == null) return;

        world.dropItemNaturally(block.getLocation(), new ItemStack(block.getType()));
    }
}
