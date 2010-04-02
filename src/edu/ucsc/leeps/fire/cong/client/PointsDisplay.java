package edu.ucsc.leeps.fire.cong.client;

import edu.ucsc.leeps.fire.cong.client.Client.PEmbed;

/**
 *
 * @author jpettit
 */
public class PointsDisplay extends Sprite {

    private boolean displaySwitchCosts;
    private float switchCosts;
    private float points;

    public PointsDisplay(int x, int y) {
        super(x, y, 0, 0);
        points = 0;
        displaySwitchCosts = false;
    }

    @Override
    public void draw(PEmbed applet) {
        String payoffString = String.format("Period Payoff: %.2f", points);
        applet.pushMatrix();
        applet.translate(origin.x, origin.y);
        applet.rectMode(PEmbed.CORNER);
        applet.fill(255);
        applet.noStroke();
        float w = applet.textWidth(payoffString);
        float textHeight = applet.textAscent() + applet.textDescent();
        float h = 4 * textHeight;
        applet.rect(0.25f * w, 0.5f * h, -1.5f * w, -1.5f * h);
        applet.fill(0);
        applet.textAlign(PEmbed.RIGHT);
        applet.text(payoffString, origin.x, origin.y);
        if (displaySwitchCosts) {
            String costString = String.format("Switch Costs: -%.2f", switchCosts);
            String totalString = String.format("Total: %.2f", points - switchCosts);
            applet.text(costString, origin.x, origin.y + textHeight);
            applet.text(totalString, origin.x, origin.y + 2 * textHeight);
        }
        applet.popMatrix();
    }

    public void setPeriodPoints(float points) {
        this.points = points;
    }

    public void setDisplaySwitchCosts(boolean displaySwitchCosts) {
        this.displaySwitchCosts = displaySwitchCosts;
    }

    public void setSwitchCosts(float switchCosts) {
        this.switchCosts = switchCosts;
    }
}
