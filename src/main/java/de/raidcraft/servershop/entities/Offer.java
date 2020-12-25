package de.raidcraft.servershop.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.silthus.ebean.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Getter
@Setter
@Accessors(fluent = true)
@Entity
@Table(name = "rcss_offers")
public class Offer extends BaseEntity {

    @ManyToOne
    private ServerShop shop;
    private String type;
    private double sellPrice = -1;
    private double buyPrice = -1;

    public boolean isBuying() {

        return buyPrice >= 0;
    }

    public boolean isSelling() {

        return sellPrice >= 0;
    }
}
