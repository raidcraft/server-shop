package de.raidcraft.servershop.commands;

import co.aikar.commands.BaseCommand;
import de.raidcraft.servershop.ServerShopPlugin;

public class PlayerCommands extends BaseCommand {

    private final ServerShopPlugin plugin;

    public PlayerCommands(ServerShopPlugin plugin) {
        this.plugin = plugin;
    }
}
