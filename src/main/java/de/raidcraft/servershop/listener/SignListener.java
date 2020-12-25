package de.raidcraft.servershop.listener;

import com.google.common.base.Strings;
import de.raidcraft.servershop.Constants;
import de.raidcraft.servershop.ServerShopPlugin;
import de.raidcraft.servershop.entities.ServerShop;
import de.raidcraft.servershop.entities.ShopSign;
import de.raidcraft.servershop.util.SignUtil;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class SignListener implements Listener {

    private final ServerShopPlugin plugin;

    public SignListener(ServerShopPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {

        if (!plugin.getPluginConfig().getSignIdentifier().equalsIgnoreCase(event.getLine(0))) {
            return;
        }

        if (!event.getPlayer().hasPermission(Constants.Permission.CREATE_SHOP_SIGN)) {
            event.getPlayer().sendMessage(ChatColor.RED + "Du hast nicht genügend Rechte um Server Shops zu platzieren.");
            event.setCancelled(true);
            SignUtil.breakAndDropSign(event.getBlock());
            return;
        }

        if (Strings.isNullOrEmpty(event.getLine(1))) {
            event.getPlayer().sendMessage(ChatColor.RED + "Bitte trage in der zweiten Zeile einen Server Shop ein.");
            event.setCancelled(true);
            SignUtil.breakAndDropSign(event.getBlock());
            return;
        }

        Optional<ServerShop> serverShop = ServerShop.byIdentifier(event.getLine(1));
        if (serverShop.isEmpty()) {
            event.getPlayer().sendMessage(ChatColor.RED + "Es gibt keinen Server Shop mit dem identifier: " + event.getLine(1));
            event.setCancelled(true);
            SignUtil.breakAndDropSign(event.getBlock());
        } else {
            ShopSign sign = serverShop.get().addSign(event.getBlock()).updateLines();
            event.getPlayer().sendMessage(ChatColor.GREEN + "Du hast erfolgreich ein Schild für den Server Shop \"" + sign.shop().name() + "\" erstellt.");
        }
    }
}
