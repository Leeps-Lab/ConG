package edu.ucsc.leeps.fire.cong.client;

import edu.ucsc.leeps.fire.cong.client.Client.PEmbed;

/**
 *
 * @author jpettit
 */
public class PointsDisplay extends Sprite {

    private boolean displaySwitchCosts;
    private float switchCosts;
    private float periodPoints, totalPoints;

    public PointsDisplay(int x, int y) {
        super(x, y, 0, 0);
        periodPoints = 0;
        totalPoints = 0;
        displaySwitchCosts = false;
    }

    @Override
    public void draw(PEmbed applet) {
        String periodPayoffString = String.format("Period Payoff: %.2f", periodPoints);
        String totalPayoffString = String.format("Total Payoff: %.2f", totalPoints);
        float textHeight = applet.textAscent() + applet.textDescent();
        applet.fill(0);
        applet.textAlign(PEmbed.LEFT);
        applet.text(periodPayoffString, origin.x, origin.y);
        applet.text(totalPayoffString, origin.x, origin.y + textHeight);
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

    public void setPoints(float periodPoints, float totalPoints) {
        this.periodPoints = periodPoints;
        this.totalPoints = totalPoints;
    }

    public void setDisplaySwitchCosts(boolean displaySwitchCosts) {
        this.displaySwitchCosts = displaySwitchCosts;
    }

    public void setSwitchCosts(float switchCosts) {
        this.switchCosts = switchCosts;
    }
}
