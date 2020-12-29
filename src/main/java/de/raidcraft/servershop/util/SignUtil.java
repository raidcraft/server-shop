package de.raidcraft.servershop.util;

import de.raidcraft.economy.wrapper.Economy;
import de.raidcraft.servershop.entities.ShopSign;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

public final class SignUtil {

    private SignUtil() {}

    public static void sendError(SignChangeEvent event, String error) {

        event.getPlayer().sendMessage(ChatColor.RED + error);
        event.setCancelled(true);
        SignUtil.breakAndDropSign(event.getBlock());
    }

    public static void updateSign(@NonNull ShopSign shopSign) {

        shopSign.getSign().ifPresent(sign -> {
            String[] lines = formatSign(shopSign);
            for (int i = 0; i < lines.length; i++) {
                sign.setLine(i, lines[i]);
            }
            sign.update(true);
        });
    }

    public static String[] formatSign(@NonNull ShopSign shopSign) {

        return new String[]{
                ChatColor.DARK_AQUA + "[" + ChatColor.WHITE + "SERVER-SHOP" + ChatColor.DARK_AQUA + "]",
                ChatColor.YELLOW + shopSign.shop().name(),
                shopSign.offer() != null ? ChatColor.AQUA + shopSign.offer().material().getKey().getKey() : "",
                shopSign.offer() != null ? ChatColor.GREEN + Economy.get().format(shopSign.offer().sellPrice()) : ""
        };
    }

    public static void breakAndDropSign(@NonNull Block block) {

        if (!(block.getState() instanceof Sign)) {
            return;
        }

        World world = block.getLocation().getWorld();
        if (world == null) return;

        if (block.getType() == Material.AIR) return;

        world.dropItemNaturally(block.getLocation(), new ItemStack(block.getType()));
        block.setType(Material.AIR);
    }
}
