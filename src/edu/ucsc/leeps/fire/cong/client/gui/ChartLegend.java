package edu.ucsc.leeps.fire.cong.client.gui;

import edu.ucsc.leeps.fire.config.Configurable;
import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.client.Client.PEmbed;
import edu.ucsc.leeps.fire.cong.config.Config;
import edu.ucsc.leeps.fire.cong.server.ThresholdPayoffFunction;
import processing.core.PApplet;

/**
 *
 * @author jpettit
 */
public class ChartLegend extends Sprite implements Configurable<Config> {

    private Config config;
    private Line youLine, otherLine, threshold;

    public ChartLegend(Sprite parent, int x, int y, int width, int height) {
        super(parent, x, y, width, height);
        FIRE.client.addConfigListener(this);
    }

    @Override
    public void draw(PEmbed applet) {

        applet.pushMatrix();
        applet.translate(origin.x, origin.y);

        if (youLine != null && otherLine != null) {
            String youLabel = "You";
            String otherLabel = "Other";
            String threshLabel = "Threshold";
            float w1 = applet.textWidth(youLabel);
            float w2 = applet.textWidth(otherLabel);
            float w3 = applet.textWidth(threshLabel);
            width = (int) (4 + 10 + 4 + w1 + 4 + 10 + 4 + w2 + 4);
            if (config.payoffFunction instanceof ThresholdPayoffFunction) {
                width += (int)(4 + 10 + 4 + w3);
            }
            applet.translate(-width, 0);

            applet.strokeWeight(10);
            applet.textAlign(PApplet.LEFT, PApplet.CENTER);

            applet.stroke(youLine.r, youLine.g, youLine.b);
            applet.line(4, height / 2f, 4 + 10, height / 2f);
            applet.text(youLabel,
                    width + 4 + 10 + 4 - width,
                    height / 2f);

            applet.stroke(otherLine.r, otherLine.g, otherLine.b);
            applet.line(4 + 10 + 4 + w1 + 4, height / 2f, 4 + 10 + 4 + w1 + 4 + 10, height / 2f);
            applet.text(otherLabel,
                    width + 4 + 10 + 4 + w1 + 4 + 10 + 4 - width,
                    height / 2f);

            if (config.payoffFunction instanceof ThresholdPayoffFunction) {
                applet.stroke(threshold.r, threshold.g, threshold.b);
                applet.line(4 + 10 + 4 + w1 + 4 + 10 + 4 + w2 + 4, height / 2f,
                        4 + 10 + 4 + w1 + 4 + 10 + 4 + w2 + 4 + 10, height / 2f);
                applet.text(threshLabel,
                        width + 4 + 10 + 4 + w1 + 4 + 10 + 4 + w2 + 4 + 10 + 4 - width,
                        height / 2f);
            }

            applet.strokeWeight(2);
        } else {
            width = 100;
            applet.translate(-width, 0);
        }

        height = (int) (9 + applet.textAscent() + applet.textDescent());
        applet.noFill();
        applet.stroke(0);
        applet.rect(0, 0, width, height);

        applet.popMatrix();
    }

    public void configChanged(Config config) {
        this.config = config;
        youLine = config.yourPayoff;
        otherLine = config.otherPayoff;
        threshold = config.thresholdLine;
    }
}
