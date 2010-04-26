package edu.ucsc.leeps.fire.cong.client;

import edu.ucsc.leeps.fire.cong.client.Client.PEmbed;

/**
 *
 * @author jpettit
 */
public class Countdown extends Sprite {

    private int secondsLeft;

    public Countdown(int x, int y, PEmbed embed) {
        super(x, y, (int) embed.textWidth("Seconds Left: 00"), (int) (embed.textAscent() + embed.textDescent()));
        secondsLeft = 0;
    }

    @Override
    public void draw(PEmbed applet) {
        String string = String.format("Seconds Left: %d", secondsLeft);
        applet.fill(0);
        applet.textAlign(PEmbed.LEFT);
        applet.text(string, origin.x, origin.y);
    }

    public void setSecondsLeft(int secondsLeft) {
        this.secondsLeft = secondsLeft;
    }
}
