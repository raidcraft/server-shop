package de.raidcraft.servershop.commands;

import co.aikar.commands.BaseCommand;
import de.raidcraft.servershop.ServerShop;

public class PlayerCommands extends BaseCommand {

    private final ServerShop plugin;

    public PlayerCommands(ServerShop plugin) {
        this.plugin = plugin;
    }
}
