package edu.ucsc.leeps.fire.cong.client.gui;

import edu.ucsc.leeps.fire.config.Configurable;
import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.client.Client;
import edu.ucsc.leeps.fire.cong.client.State.Strategy;
import edu.ucsc.leeps.fire.cong.config.Config;
import edu.ucsc.leeps.fire.cong.server.SumPayoffFunction;
import java.util.Map;

/**
 *
 * @author jpettit
 */
public class PeriodInfo extends Sprite implements Configurable<Config> {

    private Config config;
    private int secondsLeft;
    private float totalPoints, periodPoints, multiplier;
    private int lineNumber;

    public PeriodInfo(Sprite parent, int x, int y, Client embed) {
        super(parent, x, y, (int) embed.textWidth("Current Earnings: 000"), (int) (embed.textAscent() + embed.textDescent()));
        secondsLeft = 0;
        FIRE.client.addConfigListener(this);
        totalPoints = 0;
        periodPoints = 0;
        multiplier = 1;
    }

    public void configChanged(Config config) {
        this.config = config;
    }

    private void drawSubperiodTicker(Client applet) {
        applet.pushMatrix();
        applet.translate(origin.x, origin.y + ++lineNumber * (applet.textAscent() + applet.textDescent()));
        applet.rectMode(Client.CORNERS);
        applet.noStroke();
        applet.fill(0, 50, 255, 50);
        float x = 0;
        if (Client.state.currentPercent >= 0 && Client.state.currentPercent <= 1) {
            float percentPerSub = 1f / config.subperiods;
            float percentElapsed = Client.state.subperiod * percentPerSub;
            float remainder = Client.state.currentPercent - percentElapsed;
            x = remainder / percentPerSub;
        }
        applet.rect(0, 0, x * 150, -20);
        applet.stroke(0);
        applet.strokeWeight(2f);
        applet.noFill();
        applet.rect(0, 0, 150, -20);
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
                if (Client.state.subperiod < config.subperiods) {
                    s = String.format("Subperiod: %d", Client.state.subperiod + 1);
                } else {
                    s = String.format("Subperiod: %d", Client.state.subperiod);
                }
            } else {
                s = String.format("Seconds Elapsed: %.0f", FIRE.client.getElapsedMillis() / 1000f);
            }
        }
        applet.fill(0);
        applet.textAlign(Client.LEFT);
        lineNumber = 0;
        float textHeight = applet.textAscent() + applet.textDescent();
        applet.text(s, (int) origin.x, (int) (origin.y + lineNumber++ * textHeight));
        String totalEarningsString = "";
        String periodEarningsString = "";
        String multiplierString = "";
        totalEarningsString = String.format("Previous Earnings: %.2f", totalPoints);
        periodEarningsString = String.format("Current Earnings: %.2f", periodPoints);
        applet.text(totalEarningsString, (int) origin.x, (int) (origin.y + lineNumber++ * textHeight));
        applet.text(periodEarningsString, (int) origin.x, (int) (origin.y + lineNumber++ * textHeight));
        if (FIRE.client.getConfig().showPGMultiplier) {
            multiplierString = String.format("Multipler: %.2f", multiplier);
            applet.fill(0);
            applet.text(multiplierString, (int) origin.x, (int) (origin.y + lineNumber++ * textHeight));
        }
        if (config.subperiods != 0 && FIRE.client.isRunningPeriod() && !FIRE.client.isPaused()) {
            drawSubperiodTicker(applet);
        }
    }

    public void setSecondsLeft(int secondsLeft) {
        this.secondsLeft = secondsLeft;
    }

    public void update() {
        totalPoints = FIRE.client.getTotalPoints();
        synchronized (Client.state.strategiesTime) {
            periodPoints = 0;
            float lastPercent = 0;
            Map<Integer, float[]> lastStrategies = null;
            Map<Integer, float[]> lastMatchStrategies = null;
            for (Strategy s : Client.state.strategiesTime) {
                float percent;
                if (config.subperiods == 0) {
                    percent = s.timestamp / (float) (config.length * 1e9);
                } else {
                    percent = s.timestamp / (float) config.subperiods;
                }
                if (lastPercent > 0) {
                    float flowPayoff = config.payoffFunction.getPayoff(
                            Client.state.id, percent, lastStrategies, lastMatchStrategies, config);
                    float points = flowPayoff * (percent - lastPercent);
                    periodPoints += points;
                }
                lastPercent = percent;
                lastStrategies = s.strategies;
                lastMatchStrategies = s.matchStrategies;
                if (s.delayed()) {
                    break;
                }
            }
            if (config.subperiods == 0 && lastStrategies != null && lastMatchStrategies != null) {
                float flowPayoff = config.payoffFunction.getPayoff(
                        Client.state.id, Client.state.currentPercent, lastStrategies, lastMatchStrategies, config);
                if (flowPayoff > 0) {
                    flowPayoff += config.marginalCost;
                }
                periodPoints += flowPayoff * (Client.state.currentPercent - lastPercent);
            }
        }
    }

    public void startPeriod() {
        if (FIRE.client.getConfig().payoffFunction instanceof SumPayoffFunction) { //payoff function dependent
            multiplier = ((SumPayoffFunction) FIRE.client.getConfig().payoffFunction).A;
            multiplier /= Client.state.strategies.size();
        }
        update();
    }

    public void endPeriod() {
        update();
    }
}
