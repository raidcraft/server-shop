package de.raidcraft.servershop.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import de.raidcraft.servershop.ServerShopPlugin;

@CommandAlias("servershop:admin|rcssa|ssa")
public class AdminCommands extends BaseCommand {

    private final ServerShopPlugin plugin;

    public AdminCommands(ServerShopPlugin plugin) {
        this.plugin = plugin;
    }


}
