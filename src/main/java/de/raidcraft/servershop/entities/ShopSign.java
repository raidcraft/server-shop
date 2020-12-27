package de.raidcraft.servershop.entities;

import de.raidcraft.servershop.util.SignUtil;
import io.ebean.Finder;
import io.ebean.Transaction;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.silthus.ebean.BaseEntity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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

    public static final Finder<UUID, ShopSign> find = new Finder<>(ShopSign.class);

    /**
     * Tries to find a shop sign at the given location.
     * Returns empty if the location is null or no shop sign is found there.
     *
     * @param location the location of the shop sign
     * @return the shop sign or an empty optional
     */
    public static Optional<ShopSign> byLocation(Location location) {

        if (location == null) return Optional.empty();

        return find.query()
                .where().eq("x", location.getBlockX())
                .and().eq("y", location.getBlockY())
                .and().eq("z", location.getBlockZ())
                .and().eq("world_id", location.getWorld().getUID())
                .findOneOrEmpty();
    }

    @ManyToOne
    private ServerShop shop;
    @ManyToOne
    private Offer offer;
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
