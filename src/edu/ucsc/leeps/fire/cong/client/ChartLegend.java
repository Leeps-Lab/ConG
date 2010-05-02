/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.client;

import edu.ucsc.leeps.fire.cong.client.Client.PEmbed;
import edu.ucsc.leeps.fire.cong.config.PeriodConfig;
import edu.ucsc.leeps.fire.server.BasePeriodConfig;
import edu.ucsc.leeps.fire.server.PeriodConfigurable;
import java.awt.Color;
import processing.core.PApplet;

/**
 *
 * @author jpettit
 */
public class ChartLegend extends Sprite implements PeriodConfigurable {

    private PeriodConfig periodConfig;
    private Line youLine, otherLine;

    public ChartLegend(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public void draw(PEmbed applet) {

        applet.pushMatrix();
        applet.translate(origin.x, origin.y);

        if (youLine != null && otherLine != null) {
            String youLabel = "You";
            String otherLabel = "Other";
            float w1 = applet.textWidth(youLabel);
            float w2 = applet.textWidth(otherLabel);
            width = (int) (4 + 10 + 4 + w1 + 4 + 10 + 4 + w2 + 4);
            applet.translate(-width, 0);

            applet.strokeWeight(10);
            applet.textAlign(PApplet.LEFT, PApplet.CENTER);

            applet.stroke(youLine.r, youLine.g, youLine.b);
            applet.line(4, height / 2f, 4 + 10, height / 2f);
            applet.text(youLabel,
                    origin.x + 4 + 10 + 4 - width,
                    origin.y + (height / 2f));

            applet.stroke(otherLine.r, otherLine.g, otherLine.b);
            applet.line(4 + 10 + 4 + w1 + 4, height / 2f, 4 + 10 + 4 + w1 + 4 + 10, height / 2f);
            applet.text(otherLabel,
                    origin.x + 4 + 10 + 4 + w1 + 4 + 10 + 4 - width,
                    origin.y + (height / 2f));
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

    public void setPeriodConfig(BasePeriodConfig basePeriodConfig) {
        periodConfig = (PeriodConfig) basePeriodConfig;
        youLine = periodConfig.yourPayoff;
        otherLine = periodConfig.otherPayoff;
    }
}
