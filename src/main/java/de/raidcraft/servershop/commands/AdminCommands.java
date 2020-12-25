package de.raidcraft.servershop.commands;

import co.aikar.commands.BaseCommand;
import de.raidcraft.servershop.ServerShop;

public class AdminCommands extends BaseCommand {

    private final ServerShop plugin;

    public AdminCommands(ServerShop plugin) {
        this.plugin = plugin;
    }
}
