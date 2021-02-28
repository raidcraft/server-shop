package de.raidcraft.servershop.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.annotation.*;
import de.raidcraft.economy.wrapper.Economy;
import de.raidcraft.servershop.ServerShopPlugin;
import de.raidcraft.servershop.entities.Offer;
import de.raidcraft.servershop.entities.ServerShop;
import org.bukkit.ChatColor;
import org.bukkit.Material;

@CommandAlias("servershop:admin|rcssa|ssa")
@CommandPermission("rcservershop.admin")
public class AdminCommands extends BaseCommand {

    private final ServerShopPlugin plugin;

    public AdminCommands(ServerShopPlugin plugin) {
        this.plugin = plugin;
    }

    @Subcommand("create")
    @CommandCompletion("identifier name")
    @CommandPermission("rcservershop.admin.create.shop")
    public void createShop(String identifier, String name) {

        if (ServerShop.byIdentifier(identifier).isPresent()) {
            throw new ConditionFailedException("Es gibt bereits einen Server Shop mit dem identifier: " + identifier);
        }

        new ServerShop(identifier).name(name).save();

        getCurrentCommandIssuer().sendMessage(ChatColor.GREEN + "Du hast den Server Shop " + name + " (" + identifier + ") erstellt.");
    }

    @Subcommand("offer")
    @CommandPermission("rcservershop.admin.offer")
    public class OfferCmd extends BaseCommand {

        @Subcommand("add")
        @CommandCompletion("@shops sell|buy @materials price limit")
        @CommandPermission("rcservershop.admin.offer.add")
        public void addOffer(ServerShop shop, @Default("sell") String type, Material item, double amount, @Default("-1") int limit) {

            Offer offer = new Offer(shop, item).sellLimit(limit);
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

        @Subcommand("set")
        @CommandPermission("rcservershop.admin.offer.set")
        public class SetOffer extends BaseCommand {

            @Subcommand("price")
            @CommandCompletion("@shops @materials price sell|buy")
            @CommandPermission("rcservershop.admin.offer.set.price")
            public void setPrice(ServerShop shop, Material item, @Default("sell") String type, double price) {

                Offer offer = shop.getOffer(item).orElseThrow(
                        () -> new InvalidCommandArgument("Es gibt kein Angebot für " + item.getKey()
                                + " in dem Shop " + shop.identifier()));
                if (type.equalsIgnoreCase("sell")) {
                    offer.sellPrice(price);
                } else if (type.equalsIgnoreCase("buy")) {
                    offer.buyPrice(price);
                }
                offer.save();

                getCurrentCommandIssuer().sendMessage(ChatColor.GREEN + "Das Shop Angebot für " + item.getKey().toString()
                        + " " + type.toUpperCase() + " wurde auf " + Economy.get().format(price) + " gesetzt.");
            }

            @Subcommand("limit")
            @CommandCompletion("@shops @materials limit")
            @CommandPermission("rcservershop.admin.offer.set.limit")
            public void setLimit(ServerShop shop, Material item, int limit) {

                shop.getOffer(item).orElseThrow(
                        () -> new InvalidCommandArgument("Es gibt kein Angebot für " + item.getKey()
                                + " in dem Shop " + shop.identifier()))
                        .sellLimit(limit)
                        .save();

                getCurrentCommandIssuer().sendMessage(ChatColor.GREEN + "Das Limit für " + item.getKey().toString()
                        + " wurde auf " + limit + " gesetzt.");
            }
        }
    }
}
