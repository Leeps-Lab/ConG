package edu.ucsc.leeps.fire.cong.client;

import edu.ucsc.leeps.fire.cong.client.Client.PEmbed;

/**
 *
 * @author jpettit
 */
public class Countdown extends Sprite {

    private int secondsLeft;

    public Countdown(int x, int y) {
        super(x, y, 0, 0);
        secondsLeft = 0;
    }

    @Override
    public void draw(PEmbed applet) {
        String string = String.format("Seconds Left: %d", secondsLeft);
        applet.pushMatrix();
        applet.translate(origin.x, origin.y);
        applet.fill(255);
        applet.noStroke();
        applet.rectMode(PEmbed.CORNER);
        float w = applet.textWidth(string);
        float h = applet.textAscent() + applet.textDescent();
        applet.rect(-0.25f * w, 0.25f * h, 1.5f * w, -1.5f * h);
        applet.fill(0);
        applet.textAlign(PEmbed.LEFT);
        applet.text(string, origin.x, origin.y);
        applet.popMatrix();
    }

    public void setSecondsLeft(int secondsLeft) {
        this.secondsLeft = secondsLeft;
    }
}
