package de.raidcraft.servershop;

import de.raidcraft.economy.wrapper.Economy;
import de.raidcraft.servershop.entities.Offer;
import de.raidcraft.servershop.entities.Transaction;
import de.raidcraft.servershop.util.InventoryUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public final class ShopManager {

    private final ServerShopPlugin plugin;

    ShopManager(ServerShopPlugin plugin) {
        this.plugin = plugin;
    }

    public Transaction.Result buyFrom(Player player, Offer offer, int amount) {

        Material material = offer.material();
        if (!player.getInventory().containsAtLeast(new ItemStack(material), amount)) {
            return new Transaction.Result("Nicht genügend " + material.name()
                    + " (" + InventoryUtil.countItems(player.getInventory(), material) + "/" + amount + ") im Inventar.");
        }

        HashMap<Integer, ItemStack> notRemovedItems = player.getInventory().removeItem(new ItemStack(material, amount));
        int remainingCount = notRemovedItems.values().stream()
                .filter(itemStack -> itemStack.getType().equals(material))
                .mapToInt(ItemStack::getAmount)
                .sum();

        amount -= remainingCount;
        double total = offer.buyPrice() * amount;

        Economy.get().depositPlayer(player, total,
                "Verkauf von " + amount + "x " + material.name() + " an die Bank",
                Map.of(
                        "item", material.getKey().toString(),
                        "amount", amount,
                        "shop", offer.shop().identifier(),
                        "shop_name", offer.shop().name(),
                        "offer_id", offer.id(),
                        "offer_buy_price", offer.buyPrice(),
                        "offer_sell_price", offer.sellPrice(),
                        "unremoved_amount", remainingCount,
                        "requested_amount", amount + remainingCount
                )
        );

        Transaction transaction = Transaction.create(player, offer, amount)
                .totalBuyPrice(total);
        transaction.save();

        return new Transaction.Result(transaction);
    }

    public Transaction.Result buyAllFrom(Player player, Offer offer) {

        return buyFrom(player, offer, InventoryUtil.countItems(player.getInventory(), offer.material()));
    }
}
