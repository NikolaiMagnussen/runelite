package net.runelite.client.plugins.profittracker;

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.StackFormatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ProfitTrackerPanel extends PluginPanel
{
    private static final int MAX_PROFIT_BOXES = 500;

    private static final String HTML_LABEL_TEMPLATE =
            "<html><body style='color:%s'>%s<span style='color:white'>%s</span></body></html>";

    // Handle profit boxes
    private final JPanel logsContainer = new JPanel();

    // Handle overall session data
    private final JPanel overallPanel = new JPanel();
    private final JLabel overallHaLabel = new JLabel();
    private final JLabel overallGeLabel = new JLabel();
    private final JLabel overallIcon = new JLabel();

    private final JLabel resetBtn = new JLabel();

    private final ProfitTrackerPlugin plugin;

    // Log collection
    private final List<ProfitTrackerRecord> records = new ArrayList<>();
    private final List<ProfitTrackerBox> boxes = new ArrayList<>();

    ProfitTrackerPanel(final ProfitTrackerPlugin plugin)
    {
        this.plugin = plugin;

        setBorder(new EmptyBorder(6, 6, 6, 6));
        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        setLayout(new BorderLayout());

        // Create layout panel for wrapping
        final JPanel layoutPanel = new JPanel();
        layoutPanel.setLayout(new BoxLayout(layoutPanel, BoxLayout.Y_AXIS));
        add(layoutPanel, BorderLayout.NORTH);


        final JPanel overallInfo = new JPanel();
        overallInfo.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        overallInfo.setLayout(new GridLayout(2, 1));
        overallInfo.setBorder(new EmptyBorder(2, 10, 2, 0));
        overallHaLabel.setFont(FontManager.getRunescapeSmallFont());
        overallGeLabel.setFont(FontManager.getRunescapeSmallFont());
        overallInfo.add(overallHaLabel);
        overallInfo.add(overallGeLabel);
        overallPanel.add(overallIcon, BorderLayout.WEST);
        overallPanel.add(overallInfo, BorderLayout.CENTER);
        overallPanel.setVisible(true);

        final JPanel viewControls = new JPanel(new GridLayout(1, 3, 10, 0));
        viewControls.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        resetBtn.setText("Reset current profits");
        resetBtn.setToolTipText("This will reset the current profits to 0, useful for when starting a new trip");
        resetBtn.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e) { plugin.resetProfits(); }
        });

        viewControls.add(resetBtn);

        layoutPanel.add(overallPanel);
        layoutPanel.add(viewControls);
    }

    void updateValue(final String type, final long value)
    {
        if (type.contains("HA")) {
            overallHaLabel.setText(htmlLabel(type, value));
        } else {
            overallGeLabel.setText(htmlLabel(type, value));
        }
    }


    void loadHeaderIcon(BufferedImage img)
    {
        overallIcon.setIcon(new ImageIcon(img));
    }

    private static String htmlLabel(String key, long value)
    {
        final String valueStr = StackFormatter.quantityToStackSize(value);
        return String.format(HTML_LABEL_TEMPLATE, ColorUtil.toHexColor(ColorScheme.LIGHT_GRAY_COLOR), key, valueStr);
    }
}
