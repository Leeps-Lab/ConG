package edu.ucsc.leeps.fire.cong.client.gui;

import edu.ucsc.leeps.fire.config.Configurable;
import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.client.Client;
import edu.ucsc.leeps.fire.cong.config.Config;

/**
 *
 * @author jpettit
 */
public class Countdown extends Sprite implements Configurable<Config> {

    private Config config;
    private int secondsLeft;

    public Countdown(Sprite parent, int x, int y, Client embed) {
        super(parent, x, y, (int) embed.textWidth("Seconds Left: 00"), (int) (embed.textAscent() + embed.textDescent()));
        secondsLeft = 0;
        FIRE.client.addConfigListener(this);
    }

    public void configChanged(Config config) {
        this.config = config;
    }

    @Override
    public void draw(Client applet) {
        if (config == null) {
            return;
        }
        String s;
        if (config.indefiniteEnd == null) {
            if (config.subperiods != 0) {
                s = String.format("Subperiods Left: %d", config.subperiods - Client.state.subperiod);
            } else {
                s = String.format("Seconds Left: %d", secondsLeft);
            }
        } else {
            if (config.subperiods != 0) {
                s = String.format("Subperiod: %d", Client.state.subperiod + 1);
            } else {
                s = String.format("Seconds Elapsed: %.0f", FIRE.client.getElapsedMillis() / 1000f);
            }
        }
        applet.fill(0);
        applet.textAlign(Client.LEFT);
        applet.text(s, origin.x, origin.y);
    }

    public void setSecondsLeft(int secondsLeft) {
        this.secondsLeft = secondsLeft;
    }
}
