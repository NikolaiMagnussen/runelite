package net.runelite.client.plugins.profittracker;

import lombok.Value;

@Value
public class ProfitTrackerRecord {
    private final String title;
    private final String subTitle;
    private final long geValue;
    private final long haValue;
    private final long timestamp;

    /**
     * Checks if this record matches specified id
     * @param id other record id
     * @return true if match is made
     */
    boolean matches(final String id)
    {
        if (id == null)
        {
            return true;
        }

        return title.equals(id);
    }
}
