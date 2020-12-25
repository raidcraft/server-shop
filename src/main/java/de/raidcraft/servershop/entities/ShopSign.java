package de.raidcraft.servershop.entities;

import de.raidcraft.servershop.util.SignUtil;
import io.ebean.Transaction;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.silthus.ebean.BaseEntity;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Optional;
import java.util.UUID;

@Getter
@Setter
@Accessors(fluent = true)
@Entity
@Table(name = "rcss_shop_signs")
public class ShopSign extends BaseEntity {

    @ManyToOne
    private ServerShop shop;
    private int x;
    private int y;
    private int z;
    private UUID worldId;
    private String world;

    ShopSign(ServerShop shop, Block block) {
        this.shop = shop;
        this.x = block.getX();
        this.y = block.getY();
        this.z = block.getZ();
        this.worldId = block.getWorld().getUID();
        this.world = block.getWorld().getName();
    }

    public Optional<Block> getBlock() {

        World world = Bukkit.getWorld(worldId);
        if (world == null) return Optional.empty();
        return Optional.of(world.getBlockAt(x, y, z));
    }

    public Optional<Sign> getSign() {

        return getBlock()
                .map(Block::getState)
                .filter(block -> block instanceof Sign)
                .map(blockState -> (Sign) blockState);
    }

    public ShopSign updateLines() {

        SignUtil.updateSign(this);

        return this;
    }

    @Override
    public boolean delete() {

        getBlock().ifPresent(block -> block.setType(Material.AIR));
        return super.delete();
    }

    @Override
    public boolean delete(Transaction transaction) {

        getBlock().ifPresent(block -> block.setType(Material.AIR));
        return super.delete(transaction);
    }
}
