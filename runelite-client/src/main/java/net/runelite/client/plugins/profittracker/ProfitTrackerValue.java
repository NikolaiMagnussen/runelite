package net.runelite.client.plugins.profittracker;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ProfitTrackerValue {
    @Getter
    private final long ge;
    @Getter
    private final long ha;
    @Getter
    private final int hash;

    @Override
    public ProfitTrackerValue clone()
    {
        return new ProfitTrackerValue(ge, ha, hash);
    }
}
