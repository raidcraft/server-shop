package de.raidcraft.servershop;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import de.raidcraft.servershop.entities.Offer;
import de.raidcraft.servershop.entities.ServerShop;
import de.raidcraft.servershop.entities.ShopPlayer;
import de.raidcraft.servershop.entities.Transaction;
import io.ebean.Model;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShopManagerTest {

    private ServerMock server;
    private ServerShopPlugin plugin;
    private ShopManager shopManager;
    private Player player;

    @BeforeEach
    void setUp() {

        server = MockBukkit.mock();
        plugin = MockBukkit.load(ServerShopPlugin.class);
        shopManager = plugin.getShopManager();
        player = server.addPlayer();
    }

    @AfterEach
    void tearDown() {

        Transaction.find.all().forEach(Model::delete);
        ShopPlayer.find.all().forEach(Model::delete);
        ServerShop.find.all().forEach(Model::delete);
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("should sell items of player")
    void shouldSellItems() {

        player.getInventory().addItem(new ItemStack(Material.DIAMOND, 20));
        new Offer(ServerShop.defaultShop(), Material.DIAMOND).sellPrice(10).save();

        Transaction.Result result = shopManager.sellAllToServer(player,
                ServerShop.defaultShop().getOffer(Material.DIAMOND).get());

        assertThat(result.success()).isTrue();
        assertThat(Transaction.find.all()).hasSize(1);
    }
}