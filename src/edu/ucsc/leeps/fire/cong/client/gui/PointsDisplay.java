package edu.ucsc.leeps.fire.cong.client.gui;

import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.client.Client;

/**
 *
 * @author jpettit
 */
public class PointsDisplay extends Sprite {

    private boolean displaySwitchCosts;
    private float switchCosts;
    private float periodPoints, periodCost;

    public PointsDisplay(Sprite parent, int x, int y, Client embed) {
        super(parent, x, y, (int) embed.textWidth("Period Payoff: 000"), (int) (2 * (embed.textAscent() + embed.textDescent())));
        periodPoints = 0;
        periodCost = 0;
        displaySwitchCosts = false;
    }

    @Override
    public void draw(Client applet) {
        String periodPayoffString = "";
        String periodCostString = "";
        String netPayoffString = "";
        periodPayoffString = String.format("Period Payoff: %.2f", periodPoints);
        try {
            if (FIRE.client.getConfig().changeCost != 0) {
                periodCostString = String.format("Gross Cost: %.2f", periodCost);
                netPayoffString = String.format("Net Payoff: %.2f", periodPoints - periodCost);
            }
        } catch (Exception e) {
        }
        float textHeight = applet.textAscent() + applet.textDescent();
        applet.fill(0);
        applet.textAlign(Client.LEFT);
        applet.text(periodPayoffString, origin.x, origin.y);
        applet.fill(200, 0, 0);
        applet.text(periodCostString, origin.x, origin.y + textHeight);
        if (periodCost <= periodPoints) {
            applet.fill(0);
        }
        applet.text(netPayoffString, origin.x, origin.y + 2 * textHeight);
        applet.fill(0);
        if (displaySwitchCosts) {
            /*
            String costString = String.format("Switch Costs: -%.2f", switchCosts);
            String totalString = String.format("Total: %.2f", points - switchCosts);
            applet.text(costString, origin.x, origin.y + textHeight);
            applet.text(totalString, origin.x, origin.y + 2 * textHeight);
             * 
             */
        }
    }

    public void update() {
        periodPoints = FIRE.client.getPeriodPoints();
        periodCost = FIRE.client.getClient().getCost();
    }
}
