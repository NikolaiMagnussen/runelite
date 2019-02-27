package net.runelite.client.plugins.profittracker;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ProfitTrackerProfit implements Cloneable {
    @Getter
    private final long ge;
    @Getter
    private final long ha;

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    public ProfitTrackerProfit add(ProfitTrackerProfit other)
    {
        return new ProfitTrackerProfit(ge + other.getGe(), ha + other.getHa());
    }
}
