package net.runelite.client.plugins.profittracker;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.SpriteID;
import net.runelite.api.events.*;
import net.runelite.client.account.AccountSession;
import net.runelite.client.account.SessionManager;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import javax.swing.*;
import javax.xml.bind.annotation.XmlInlineBinaryData;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@PluginDescriptor(
        name = "Profit Tracker",
        description = "Tracks the profit of your current inventory",
        tags = {"slayer", "profit", "high", "alchemy", "prices", "grand", "exchange"}
)

@Slf4j
public class ProfitTrackerPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private SessionManager sessionManager;

    @Inject
    private SpriteManager spriteManager;

    @Inject
    private ClientThread clientThread;

    @Inject
    private ItemManager itemManager;

    @Inject
    private ProfitTrackerInventoryCalculation inventoryCalculation;

    private ProfitTrackerPanel panel;
    private NavigationButton navButton;

    @Subscribe
    public void onItemContainerChanged(final ItemContainerChanged itemContainerChanged) {
        inventoryCalculation.update();
        ProfitTrackerProfit profit = inventoryCalculation.getCurrentProfit();
        SwingUtilities.invokeLater(() -> panel.updateValue("Current GE Profit: ", profit.getGe()));
        SwingUtilities.invokeLater(() -> panel.updateValue("Current HA Profit: ", profit.getHa()));
    }

    public void resetProfits() {
        inventoryCalculation.shift();
        ProfitTrackerProfit profit = inventoryCalculation.getCurrentProfit();
        SwingUtilities.invokeLater(() -> panel.updateValue("Current GE Profit: ", profit.getGe()));
        SwingUtilities.invokeLater(() -> panel.updateValue("Current HA Profit: ", profit.getHa()));
    }

    @Override
    protected void startUp() throws Exception
    {
        log.debug("Should have created a new panel for the tracker");
        panel = new ProfitTrackerPanel(this);

        spriteManager.getSpriteAsync(SpriteID.TAB_INVENTORY, 0, panel::loadHeaderIcon);
        final BufferedImage icon = ImageUtil.getResourceStreamFromClass(getClass(), "panel_icon.png");


        navButton = NavigationButton.builder()
                .tooltip("Profit Tracker")
                .icon(icon)
                .priority(1)
                .panel(panel)
                .build();
        clientToolbar.addNavigation(navButton);
    }

    @Override
    protected void shutDown()
    {
        log.debug("Shutting down - removing panel");
        clientToolbar.removeNavigation(navButton);
    }
}
