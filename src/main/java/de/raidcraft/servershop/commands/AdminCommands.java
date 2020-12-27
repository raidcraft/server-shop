package de.raidcraft.servershop.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import de.raidcraft.economy.wrapper.Economy;
import de.raidcraft.servershop.ServerShopPlugin;
import de.raidcraft.servershop.entities.Offer;
import de.raidcraft.servershop.entities.ServerShop;
import org.bukkit.ChatColor;
import org.bukkit.Material;

@CommandAlias("servershop:admin|rcssa|ssa")
public class AdminCommands extends BaseCommand {

    private final ServerShopPlugin plugin;

    public AdminCommands(ServerShopPlugin plugin) {
        this.plugin = plugin;
    }

    @CommandCompletion("* *")
    @CommandPermission("rcservershop.shop.create")
    public void createShop(String identifier, String name) {

        if (ServerShop.byIdentifier(identifier).isPresent()) {
            throw new ConditionFailedException("Es gibt bereits einen Server Shop mit dem identifier: " + identifier);
        }

        new ServerShop(identifier).name(name).save();

        getCurrentCommandIssuer().sendMessage(ChatColor.GREEN + "Du hast den Server Shop " + name + " (" + identifier + ") erstellt.");
    }

    @CommandCompletion("@shops sell|buy @materials *")
    @CommandPermission("rcservershop.shop.offer.add")
    public void addOffer(ServerShop shop, @Default("sell") String type, Material item, double amount) {

        Offer offer = new Offer(shop, item);
        if (type.equalsIgnoreCase("sell")) {
            offer.sellPrice(amount);
        } else if (type.equalsIgnoreCase("buy")) {
            offer.buyPrice(amount);
        }
        shop.offers().add(offer);
        offer.save();

        getCurrentCommandIssuer().sendMessage(ChatColor.GREEN + "Dem Shop wurde das Angebot für " + item.getKey().toString()
                + " für " + type.toUpperCase() + " " + Economy.get().format(amount) + " hinzugefügt.");
    }
}
