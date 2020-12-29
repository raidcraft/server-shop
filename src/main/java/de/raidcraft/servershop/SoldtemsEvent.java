package de.raidcraft.servershop;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Data
@EqualsAndHashCode(callSuper = true)
public class SoldtemsEvent extends Event {

    @Getter
    private static final HandlerList handlerList = new HandlerList();

    private final Material item;
    private final int amount;
    private final double sellPrice;
    private final double total;

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
