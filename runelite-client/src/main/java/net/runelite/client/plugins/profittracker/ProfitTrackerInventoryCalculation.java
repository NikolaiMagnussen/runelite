package net.runelite.client.plugins.profittracker;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.game.ItemManager;
import org.apache.commons.lang3.ArrayUtils;

import javax.inject.Inject;
import java.util.*;

import static net.runelite.api.ItemID.COINS_995;
import static net.runelite.api.ItemID.PLATINUM_TOKEN;

@Slf4j
public class ProfitTrackerInventoryCalculation
{
    private static final float HIGH_ALCHEMY_CONSTANT = 0.6f;

    private final ItemManager itemManager;
    private final Client client;

    // Used to avoid extra calculation if the inventory has not changed
    private int inventoryHash;

    @Getter
    private ArrayList<ProfitTrackerProfit> profits;

    @Getter
    private ProfitTrackerValue currVal;

    @Getter
    private ProfitTrackerValue prevVal;




    @Inject
    ProfitTrackerInventoryCalculation(ItemManager itemManager, Client client)
    {
        this.itemManager = itemManager;
        this.client = client;
        this.profits = new ArrayList();
        this.currVal = calculate(itemManager, client, null);
        if (this.currVal != null) {
            this.prevVal = currVal.clone();
        } else {
            this.prevVal = null;
        }
    }

    void update()
    {
        ProfitTrackerValue newVal = calculate();
        if (newVal != null) {
            log.debug("Updated current value");
            currVal = newVal;
            if (prevVal == null) {
                log.debug("Previous value was not set - cloned current value");
                prevVal = currVal.clone();
            }
        }
    }

    ProfitTrackerProfit getCurrentProfit()
    {
        if (currVal == null || prevVal == null) {
            return new ProfitTrackerProfit(0, 0);
        } else {
            return new ProfitTrackerProfit(currVal.getGe() - prevVal.getGe(), currVal.getHa() - prevVal.getHa());
        }
    }

    ProfitTrackerProfit getTotalProfit()
    {
        ProfitTrackerProfit currProfit = getCurrentProfit();
        return profits.stream()
                .reduce(currProfit,
                (prev, curr) -> prev.add(curr));
    }

    void shift()
    {
        ProfitTrackerValue newVal = calculate();
        if (currVal != null && prevVal != null) {
            profits.add(new ProfitTrackerProfit(currVal.getGe() - prevVal.getGe(), currVal.getHa() - prevVal.getHa()));
        }
        if (newVal != null) {
            currVal = newVal;
        }
        prevVal = currVal.clone();
    }

    private ProfitTrackerValue calculate()
    {
        return calculate(itemManager, client, currVal);
    }

    /**
     * Calculate the inventory and equipment value
     */
    private static ProfitTrackerValue calculate(ItemManager itemManager, Client client, ProfitTrackerValue currVal)
    {
        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);

        if (inventory == null || equipment == null)
        {
            return null;
        }

        Item[] items = ArrayUtils.addAll(equipment.getItems(), inventory.getItems());

        int hash = hashInventory(items);
        if (currVal != null && (items.length == 0 || hash == currVal.getHash())) {
            return null;
        }

        log.debug("Calculating new inventory and equipment value...");

        long gePrice = 0;
        long haPrice = 0;

        List<Integer> itemIds = new ArrayList<>();

        for (Item item : items)
        {
            int quantity = item.getQuantity();

            if (item.getId() <= 0 || quantity == 0)
            {
                continue;
            }

            if (item.getId() == COINS_995)
            {
                gePrice += quantity;
                haPrice += quantity;
                continue;
            }

            if (item.getId() == PLATINUM_TOKEN)
            {
                gePrice += quantity * 1000L;
                haPrice += quantity * 1000L;
                continue;
            }


            final ItemComposition itemComposition = itemManager.getItemComposition(item.getId());

            itemIds.add(item.getId());

            // Calculate HA of inventory
            int price = itemComposition.getPrice();
            if (price > 0)
            {
                haPrice += (long) Math.round(price * HIGH_ALCHEMY_CONSTANT) *
                        (long) quantity;
            }

        }
        // Do GE calculation
        if (!itemIds.isEmpty())
        {
            for (Item item : items)
            {
                int itemId = item.getId();
                int quantity = item.getQuantity();

                if (itemId <= 0 || quantity == 0 || itemId == COINS_995 || itemId == PLATINUM_TOKEN)
                {
                    continue;
                }
                gePrice += (long) itemManager.getItemPrice(itemId) * quantity;
            }
        }

        return new ProfitTrackerValue(gePrice, haPrice, hash);
    }

    static int hashInventory(Item[] items) {
        Map<Integer, Integer> map = new HashMap<>();

        for (Item item : items) {
            int quantity = map.getOrDefault(item.getId(), 0);
            map.put(item.getId(), quantity + item.getQuantity());
        }

        return map.hashCode();
    }
}
