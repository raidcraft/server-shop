package de.raidcraft.servershop.util;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InventoryUtilTest {

    private Inventory mockInventory(ItemStack... stacks) {

        Inventory inventory = mock(Inventory.class);
        when(inventory.getContents()).thenReturn(stacks);
        return inventory;
    }

    @Test
    @DisplayName("should count the correct amount of items in the inventory")
    void shouldCountTheCorrectAmountOfItems() {

        Inventory inventory = mockInventory(
                new ItemStack(Material.AIR),
                new ItemStack(Material.DIAMOND, 10),
                null,
                new ItemStack(Material.DIAMOND_ORE, 1),
                null,
                null,
                new ItemStack(Material.DIAMOND, 1),
                new ItemStack(Material.DIAMOND, 64)
        );

        assertThat(InventoryUtil.countItems(inventory, Material.DIAMOND)).isEqualTo(75);
    }
}