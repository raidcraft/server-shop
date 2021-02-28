package de.raidcraft.servershop;

import de.raidcraft.economy.wrapper.Economy;
import de.raidcraft.servershop.entities.Offer;
import de.raidcraft.servershop.entities.ServerShop;
import de.raidcraft.servershop.entities.ShopPlayer;
import de.raidcraft.servershop.entities.Transaction;
import de.raidcraft.servershop.events.SellItemEvent;
import de.raidcraft.servershop.util.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.HashMap;
import java.util.Map;

public final class ShopManager {

    private final ServerShopPlugin plugin;

    ShopManager(ServerShopPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {

        for (ServerShop serverShop : ServerShop.find.all()) {
            try {
                Permission sell = new Permission(Constants.Permission.SHOP_PREFIX + serverShop.identifier() + ".sell", PermissionDefault.OP);
                Permission buy = new Permission(Constants.Permission.SHOP_PREFIX + serverShop.identifier() + ".buy", PermissionDefault.OP);
                Bukkit.getPluginManager().addPermission(sell);
                Bukkit.getPluginManager().addPermission(buy);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public Transaction.Result sellToServer(Player player, Offer offer, int amount) {

        Material material = offer.material();
        String name = material.getKey().getKey();

        if (amount < 1) {
            return new Transaction.Result("Du hast keine " + name + " die du verkaufen kannst.");
        }
        if (!player.getInventory().containsAtLeast(new ItemStack(material), amount)) {
            return new Transaction.Result("Nicht genügend " + name
                    + " (" + InventoryUtil.countItems(player.getInventory(), material) + "/" + amount + ") im Inventar.");
        }

        if (offer.hasLimit() && !player.hasPermission(Constants.Permission.BYPASS_LIMIT)) {
            ShopPlayer shopPlayer = ShopPlayer.getOrCreate(player);
            int soldItemAmountToday = shopPlayer.soldItemAmountToday(material);
            if (soldItemAmountToday >= offer.sellLimit()) {
                return new Transaction.Result("Du hast dein Limit von " + offer.sellLimit() + " Verkäufen für heute erreicht.");
            } else if (soldItemAmountToday + amount > offer.sellLimit()) {
                amount = amount - ((soldItemAmountToday + amount) - offer.sellLimit());
            }
        }

        HashMap<Integer, ItemStack> notRemovedItems = player.getInventory().removeItem(new ItemStack(material, amount));
        int remainingCount = notRemovedItems.values().stream()
                .filter(itemStack -> itemStack.getType().equals(material))
                .mapToInt(ItemStack::getAmount)
                .sum();

        amount -= remainingCount;
        double sellPrice = offer.sellPrice();
        double total = sellPrice * amount;

        SellItemEvent event = new SellItemEvent(player, material, amount);
        event.setSellPrice(sellPrice);
        event.setTotal(total);

        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return new Transaction.Result("Der Verkauf wurde von einem Plugin abgebrochen.");
        }

        amount = event.getAmount();
        total = event.getTotal();
        sellPrice = event.getSellPrice();

        Economy.get().depositPlayer(player, total,
                "Verkauf von " + amount + "x " + name + " an die Bank",
                Map.of(
                        "item", material.getKey().toString(),
                        "amount", amount,
                        "shop", offer.shop().identifier(),
                        "shop_name", offer.shop().name(),
                        "offer_id", offer.id(),
                        "offer_buy_price", offer.buyPrice(),
                        "offer_sell_price", sellPrice,
                        "unremoved_amount", remainingCount,
                        "requested_amount", amount + remainingCount
                )
        );

        Transaction transaction = Transaction.create(player, offer, amount)
                .totalSellPrice(total);
        transaction.save();

        Bukkit.getPluginManager().callEvent(new SoldtemsEvent(player, material, amount, sellPrice, total));

        return new Transaction.Result(transaction);
    }

    public Transaction.Result sellAllToServer(Player player, Offer offer) {

        return sellToServer(player, offer, InventoryUtil.countItems(player.getInventory(), offer.material()));
    }
}
