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
public class SellItemEvent extends Event implements Cancellable {

    @Getter
    private static final HandlerList handlerList = new HandlerList();

    private final Material item;
    private final int amount;
    private double sellPrice;
    private double total;
    private boolean cancelled;

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}
