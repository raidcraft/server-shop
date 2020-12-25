package de.raidcraft.servershop.entities;

import io.ebean.Finder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.silthus.ebean.BaseEntity;
import org.bukkit.OfflinePlayer;

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
@Table(name = "rcss_players")
public class ShopPlayer extends BaseEntity {

    public static final Finder<UUID, ShopPlayer> find = new Finder<>(ShopPlayer.class);

    /**
     * Gets an existing player from the database or creates a new record from the given player.
     * <p>This method takes an {@link OfflinePlayer} for easier access to skills while players are offline.
     * However the skill can only be applied to the player if he is online. Any interaction will fail silently while offline.
     *
     * @param player the player that should be retrieved or created
     * @return a skilled player from the database
     */
    public static ShopPlayer getOrCreate(OfflinePlayer player) {

        return Optional.ofNullable(find.byId(player.getUniqueId()))
                .orElseGet(() -> {
                    ShopPlayer skilledPlayer = new ShopPlayer(player);
                    skilledPlayer.insert();
                    return skilledPlayer;
                });
    }

    private String name;

    @OneToMany(cascade = CascadeType.REMOVE)
    private List<Transaction> transactions = new ArrayList<>();

    public ShopPlayer(OfflinePlayer player) {
        this.id(player.getUniqueId());
        this.name(player.getName());
    }
}
