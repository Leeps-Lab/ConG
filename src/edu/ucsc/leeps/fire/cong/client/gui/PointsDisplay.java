package edu.ucsc.leeps.fire.cong.client.gui;

import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.client.Client.PEmbed;

/**
 *
 * @author jpettit
 */
public class PointsDisplay extends Sprite {

    private boolean displaySwitchCosts;
    private float switchCosts;
    private float periodPoints, totalPoints;

    public PointsDisplay(Sprite parent, int x, int y, PEmbed embed) {
        super(parent, x, y, (int) embed.textWidth("Period Payoff: 000"), (int) (2 * (embed.textAscent() + embed.textDescent())));
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

    public void update() {
        periodPoints = FIRE.client.getPeriodPoints();
        totalPoints = FIRE.client.getTotalPoints();
    }
}
