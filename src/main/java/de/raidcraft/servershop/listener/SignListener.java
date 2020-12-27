package de.raidcraft.servershop.listener;

import com.google.common.base.Strings;
import de.raidcraft.economy.wrapper.Economy;
import de.raidcraft.servershop.Constants;
import de.raidcraft.servershop.ServerShopPlugin;
import de.raidcraft.servershop.entities.Offer;
import de.raidcraft.servershop.entities.ServerShop;
import de.raidcraft.servershop.entities.ShopSign;
import de.raidcraft.servershop.entities.Transaction;
import de.raidcraft.servershop.util.SignUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Optional;

public class SignListener implements Listener {

    private final ServerShopPlugin plugin;

    public SignListener(ServerShopPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onSignClick(PlayerInteractEvent event) {

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;


        if (!isShopSign(event.getClickedBlock())) return;

        Optional<ServerShop> serverShop = ServerShop.bySign(event.getClickedBlock());
        if (serverShop.isEmpty()) {
            SignUtil.breakAndDropSign(event.getClickedBlock());
            event.getPlayer().sendMessage(ChatColor.RED + "Der angeklickte Shop ist ungültig und wurde entfernt.");
            event.setCancelled(true);
        } else {
            event.setCancelled(true);
            String material = ChatColor.stripColor(((Sign) event.getClickedBlock().getState()).getLine(2));
            Optional<Offer> offer = serverShop.get().getOffer(material);
            if (offer.isEmpty()) {
                SignUtil.breakAndDropSign(event.getClickedBlock());
                event.getPlayer().sendMessage(ChatColor.RED + "Der angeklickte Shop ist ungültig und wurde entfernt.");
            } else {
                Transaction.Result result;
                if (event.getPlayer().isSneaking()) {
                    result = plugin.getShopManager().buyAllFrom(event.getPlayer(), offer.get());
                } else {
                    result = plugin.getShopManager().buyFrom(event.getPlayer(), offer.get(), 1);
                }
                if (result.success()) {
                    if (result.transaction().amount() > 0)
                        event.getPlayer().sendMessage(ChatColor.GREEN + "Du hast " + result.transaction().amount()
                                + "x " + offer.get().material().getKey().getKey()
                                + " für " + ChatColor.AQUA + Economy.get().format(result.transaction().totalBuyPrice()) + ChatColor.GREEN + " verkauft.");
                } else {
                    event.getPlayer().sendMessage(ChatColor.RED + "Du hast nicht genügend " + offer.get().material().getKey().getKey() + " die du verkaufen kannst.");
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {

        if (!isShopSign(event.getBlock())) return;

        if (!event.getPlayer().hasPermission(Constants.Permission.CREATE_SHOP_SIGN)) {
            SignUtil.sendError(event, "Du hast nicht genügend Rechte um Server Shops zu platzieren.");
            return;
        }

        ServerShop shop = ServerShop.byIdentifier(event.getLine(1))
                .orElse(ServerShop.defaultShop());

        String itemLine = event.getLine(2);
        if (Strings.isNullOrEmpty(itemLine)) {
            SignUtil.sendError(event, "Du musst in der dritten Zeile das Item eintragen was angekauft werden soll.");
            return;
        }

        Material material = Material.matchMaterial(itemLine);
        if (material == null) {
            SignUtil.sendError(event, "Das Item \"" + itemLine + "\" existiert nicht.");
            return;
        }

        Optional<Offer> offer = shop.getOffer(material);

        ShopSign shopSign = shop.addSign(event.getBlock());
        if (offer.isPresent() && Strings.isNullOrEmpty(event.getLine(3))) {
            shopSign.offer(offer.get()).save();
            event.getPlayer().sendMessage(ChatColor.GREEN + "Das Shop Schild wurde erfolgreich erstellt. " +
                    "Es wurde der existierende Preis von " + Economy.get().format(offer.get().buyPrice()) + " genommen.");
            return;
        }

        try {
            double price = Double.parseDouble(event.getLine(3));
            Offer signOffer = offer.orElseGet(() -> shop.addOffer(material));
            signOffer.buyPrice(price).save();
            shopSign.offer(signOffer).save();
            event.getPlayer().sendMessage(ChatColor.GREEN + "Das Shop Schild wurde erfolgreich erstellt. " +
                    "Der Preis beträgt: " + Economy.get().format(price) + ".");
        } catch (NullPointerException | NumberFormatException | IndexOutOfBoundsException e) {
            SignUtil.sendError(event, "In der vierten Zeile muss der Preis stehen.");
        }

        String[] lines = SignUtil.formatSign(shopSign);
        for (int i = 0; i < lines.length; i++) {
            event.setLine(i, lines[i]);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSignDestroy(BlockBreakEvent event) {

        if (!isShopSign(event.getBlock())) return;
        if (!event.getPlayer().hasPermission(Constants.Permission.DESTROY_SHOP_SIGN)) {
            event.setCancelled(true);
            return;
        }

        ShopSign.byLocation(event.getBlock().getLocation()).ifPresent(ShopSign::delete);
    }

    private boolean isShopSign(Block block) {

        if (!(block.getState() instanceof Sign)) {
            return false;
        }

        Sign sign = (Sign) block.getState();
        if (!plugin.getPluginConfig().getSignIdentifier().equalsIgnoreCase(sign.getLine(0))) {
            return false;
        }

        return true;
    }
}
