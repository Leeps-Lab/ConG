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

    private void drawSubperiodTicker(Client applet) {
        applet.pushMatrix();
        applet.translate(origin.x, origin.y + 4 * (applet.textAscent() + applet.textDescent()));
        applet.strokeWeight(2f);
        applet.stroke(0);
        applet.fill(255);
        applet.rectMode(Client.CORNERS);
        applet.rect(0, 0, 150, -20);
        applet.noStroke();
        applet.fill(0, 50, 255, 50);
        float x = 0;
        if (Client.state.currentPercent >= 0 && Client.state.currentPercent <= 1) {
            float percentPerSub = 1f / config.subperiods;
            float percentElapsed = Client.state.subperiod * percentPerSub;
            float remainder = Client.state.currentPercent - percentElapsed;
            x = remainder / percentPerSub;
        }
        applet.rect(1, 1, x * 149, -19);
        applet.popMatrix();
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
        applet.text(s, (int) origin.x, (int) origin.y);
        if (config.subperiods != 0 && FIRE.client.isRunningPeriod() && !FIRE.client.isPaused()) {
            drawSubperiodTicker(applet);
        }
    }

    public void setSecondsLeft(int secondsLeft) {
        this.secondsLeft = secondsLeft;
    }
}
