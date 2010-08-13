package edu.ucsc.leeps.fire.cong.client.gui;

import edu.ucsc.leeps.fire.cong.client.Client;

/**
 *
 * @author jpettit
 */
public class Countdown extends Sprite {

    private int secondsLeft;

    public Countdown(Sprite parent, int x, int y, Client embed) {
        super(parent, x, y, (int) embed.textWidth("Seconds Left: 00"), (int) (embed.textAscent() + embed.textDescent()));
        secondsLeft = 0;
    }

    @Override
    public void draw(Client applet) {
        String string = String.format("Seconds Left: %d", secondsLeft);
        applet.fill(0);
        applet.textAlign(Client.LEFT);
        applet.text(string, origin.x, origin.y);
    }

    public void setSecondsLeft(int secondsLeft) {
        this.secondsLeft = secondsLeft;
    }
}
