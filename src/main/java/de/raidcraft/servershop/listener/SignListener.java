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
import lombok.extern.java.Log;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
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

import java.util.*;

import static net.kyori.adventure.text.format.NamedTextColor.*;

@Log(topic = "RCServerShop")
public class SignListener implements Listener {

    private final ServerShopPlugin plugin;
    private final Map<UUID, Integer> sellClicks = new HashMap<>();
    private final Set<UUID> sentTips = new HashSet<>();

    public SignListener(ServerShopPlugin plugin) {
        this.plugin = plugin;

        long delay = plugin.getPluginConfig().getSellTipTask();
        Bukkit.getScheduler().runTaskTimer(plugin, sellClicks::clear, delay, delay);
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
            log.warning("removed ServerShop sign at " + event.getClickedBlock().getLocation() + ": shop not found!");
        } else {
            event.setCancelled(true);
            String material = ChatColor.stripColor(((Sign) event.getClickedBlock().getState()).getLine(2));
            Optional<Offer> offer = serverShop.get().getOffer(material);
            if (offer.isEmpty()) {
                SignUtil.breakAndDropSign(event.getClickedBlock());
                event.getPlayer().sendMessage(ChatColor.RED + "Der angeklickte Shop ist ungültig und wurde entfernt.");
                log.warning("removed ServerShop sign at " + event.getClickedBlock().getLocation() + ": no offer for " + material + " found!");
            } else {
                if (!event.getPlayer().hasPermission(Constants.Permission.SHOP_PREFIX + serverShop.get().identifier() + ".sell")) {
                    event.getPlayer().sendMessage(ChatColor.RED + "Du hast nicht genügend Rechte um an diesen Server Shop zu verkaufen.");
                    event.setCancelled(true);
                    return;
                }
                Transaction.Result result;
                if (event.getPlayer().isSneaking()) {
                    result = plugin.getShopManager().sellAllToServer(event.getPlayer(), offer.get());
                } else {
                    result = plugin.getShopManager().sellToServer(event.getPlayer(), offer.get(), 1);
                }
                if (result.success()) {
                    if (result.transaction().amount() > 0) {
                        event.getPlayer().sendMessage(ChatColor.GREEN + "Du hast " + result.transaction().amount()
                                + "x " + material
                                + " für " + ChatColor.AQUA + Economy.get().format(result.transaction().totalSellPrice()) + ChatColor.GREEN + " verkauft.");
                        Integer clicks = sellClicks.getOrDefault(event.getPlayer().getUniqueId(), 0);
                        sellClicks.put(event.getPlayer().getUniqueId(), ++clicks);
                        if (clicks >= plugin.getPluginConfig().getTipThreshold()) {
                            if (!sentTips.contains(event.getPlayer().getUniqueId())) {
                                sentTips.add(event.getPlayer().getUniqueId());
                                TextComponent text = Component.text().append(Component.text("[TIPP]: ", YELLOW, TextDecoration.BOLD))
                                        .append(Component.text("Du kannst mit ", GREEN)
                                                .append(Component.keybind("key.sneak", GOLD))
                                                .append(Component.text(" alle Items auf einmal verkaufen.", GREEN))).build();
                                Audience audience = BukkitAudiences.create(plugin).player(event.getPlayer());
                                audience.sendMessage(text);
                                audience.sendActionBar(text);
                            }
                        }
                    }
                } else {
                    event.getPlayer().sendMessage(ChatColor.RED + "Du kannst keine " + material + " verkaufen: " + result.error());
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {

        if (!plugin.getPluginConfig().getSignIdentifier().equalsIgnoreCase(event.getLine(0))) return;

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
                    "Es wurde der existierende Preis von " + Economy.get().format(offer.get().sellPrice()) + " genommen.");
        } else {
            try {
                double price = Double.parseDouble(event.getLine(3));
                Offer signOffer = offer.orElseGet(() -> shop.addOffer(material));
                signOffer.sellPrice(price).save();
                shopSign.offer(signOffer).save();
                event.getPlayer().sendMessage(ChatColor.GREEN + "Das Shop Schild wurde erfolgreich erstellt. " +
                        "Der Preis beträgt: " + Economy.get().format(price) + ".");
            } catch (NullPointerException | NumberFormatException | IndexOutOfBoundsException e) {
                SignUtil.sendError(event, "In der vierten Zeile muss der Preis stehen.");
                return;
            }
        }

        String[] lines = SignUtil.formatSign(shopSign);
        for (int i = 0; i < lines.length; i++) {
            event.setLine(i, lines[i]);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSignDestroy(BlockBreakEvent event) {

        if (!isShopSign(event.getBlock())) return;
        if (!event.getPlayer().isSneaking()) {
            event.setCancelled(true);
            return;
        }
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
        return plugin.getPluginConfig().getSignIdentifier().equalsIgnoreCase(ChatColor.stripColor(sign.getLine(0)));
    }
}
