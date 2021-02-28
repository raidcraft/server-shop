package de.raidcraft.servershop.entities;

import com.google.common.base.Strings;
import de.raidcraft.servershop.ServerShopPlugin;
import io.ebean.Finder;
import io.ebean.annotation.Index;
import io.ebean.annotation.Transactional;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.silthus.ebean.BaseEntity;
import org.bukkit.Material;
import org.bukkit.block.Block;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Getter
@Setter
@Accessors(fluent = true)
@Entity
@Table(name = "rcss_shops")
public class ServerShop extends BaseEntity {

    public static final Finder<UUID, ServerShop> find = new Finder<>(ServerShop.class);

    public static ServerShop defaultShop() {

        return byIdentifier(ServerShopPlugin.DEFAULT_SHOP_IDENTIFIER)
                .orElseGet(() -> {
                    ServerShop serverShop = new ServerShop(ServerShopPlugin.DEFAULT_SHOP_IDENTIFIER);
                    serverShop.save();
                    return serverShop;
                });
    }

    /**
     * Tries to find a shop with the given identifier.
     * Empty will be returned if the identifier is null or empty.
     *
     * @param identifier the identifier of the shop
     * @return the shop or an empty optional
     */
    public static Optional<ServerShop> byIdentifier(String identifier) {

        if (Strings.isNullOrEmpty(identifier)) return Optional.empty();

        return find.query()
                .where().ieq("identifier", identifier)
                .findOneOrEmpty();
    }

    /**
     * Tries to find a server shop at the given block location.
     *
     * @param block the sign block to lookup
     * @return an empty optional if the shop sign does not exist
     */
    public static Optional<ServerShop> bySign(Block block) {

        if (block == null) return Optional.empty();

        return ShopSign.byLocation(block.getLocation())
                .map(ShopSign::shop);
    }

    @Index(unique = true)
    private String identifier;
    private String name;
    private boolean enabled = true;

    private boolean restricted = false;

    @OneToMany(cascade = CascadeType.REMOVE)
    private List<Offer> offers = new ArrayList<>();

    @OneToMany(cascade = CascadeType.REMOVE)
    private List<ShopSign> signs = new ArrayList<>();

    @OneToMany(cascade = CascadeType.REMOVE)
    private List<Transaction> transactions = new ArrayList<>();

    public ServerShop(String identifier) {
        this.identifier = identifier;
    }

    @Transactional
    public ShopSign addSign(Block block) {

        ShopSign sign = new ShopSign(this, block).updateLines();
        signs.add(sign);
        sign.save();

        return sign;
    }

    public Offer addOffer(Material material) {

        return getOffer(material).orElse(new Offer(this, material));
    }

    public Optional<Offer> getOffer(String material) {

        return getOffer(Material.matchMaterial(material));
    }

    public Optional<Offer> getOffer(Material material) {

        if (material == null) return Optional.empty();

        return offers().stream()
                .filter(offer -> offer.material().equals(material))
                .findFirst();
    }
}
