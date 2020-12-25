package de.raidcraft.servershop.entities;

import com.google.common.base.Strings;
import io.ebean.Finder;
import io.ebean.annotation.Index;
import io.ebean.annotation.Transactional;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.silthus.ebean.BaseEntity;
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

    public static Optional<ServerShop> byIdentifier(String identifier) {

        if (Strings.isNullOrEmpty(identifier)) return Optional.empty();

        return find.query()
                .where().ieq("identifier", identifier)
                .findOneOrEmpty();
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

    @Transactional
    public ShopSign addSign(Block block) {

        ShopSign sign = new ShopSign(this, block);
        signs.add(sign);
        sign.save();

        return sign;
    }
}
