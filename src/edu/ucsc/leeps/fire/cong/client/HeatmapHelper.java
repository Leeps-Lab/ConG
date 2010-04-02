/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.client;

import edu.ucsc.leeps.fire.cong.client.Client.PEmbed;
import edu.ucsc.leeps.fire.cong.config.PeriodConfig;
import edu.ucsc.leeps.fire.server.BasePeriodConfig;
import edu.ucsc.leeps.fire.server.PeriodConfigurable;
import processing.core.PGraphics;

/**
 *
 * @author jpettit
 */
public class HeatmapHelper implements PeriodConfigurable {

    private PeriodConfig periodConfig;
    private int width, height;
    private int lowColor, midColor, highColor;
    private float[][] payoff;
    private PGraphics buffer, invisibleBuffer;
    private PEmbed applet;
    private boolean visible;

    public HeatmapHelper(
            PEmbed applet,
            int width, int height,
            int lowColor, int midColor, int highColor) {
        this.width = width;
        this.height = height;
        this.lowColor = lowColor;
        this.midColor = midColor;
        this.highColor = highColor;
        this.payoff = new float[width][height];
        this.applet = applet;
        invisibleBuffer = applet.createGraphics(width, height, PEmbed.P2D);
        invisibleBuffer.beginDraw();
        invisibleBuffer.background(0, 0, 0, 0);
        invisibleBuffer.endDraw();
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setPeriodConfig(BasePeriodConfig basePeriodConfig) {
        this.periodConfig = (PeriodConfig) basePeriodConfig;
        buffer = applet.createGraphics(width, height, PEmbed.P2D);
    }

    // Chooses whether to interpolate between low and mid, or low and high
    public int getRGB(float u) {
        if (u < .5) {
            return interpolateRGB(u * 2.0f, lowColor, midColor);
        } else {
            return interpolateRGB((u - 0.5f) * 2.0f, midColor, highColor);
        }
    }

    // Linearly interpolates u% between low and high
    private static int interpolateRGB(float u, int low, int high) {
        int red = (high & 0x00FF0000) >> 16;
        red -= ((low & 0x00FF0000) >> 16);
        red *= u;
        red += ((low & 0x00FF0000) >> 16);

        int green = (high & 0x0000FF00) >> 8;
        green -= ((low & 0x0000FF00) >> 8);
        green *= u;
        green += ((low & 0x0000FF00) >> 8);

        int blue = (high & 0x000000FF);
        blue -= (low & 0x000000FF);
        blue *= u;
        blue += (low & 0x000000FF);

        int color = 0xFF000000;
        color += (red << 16);
        color += (green << 8);
        color += blue;

        return color;
    }

    public void updateTwoStrategyHeatmap(float currentPercent) {
        buffer = applet.createGraphics(width, height, PEmbed.P2D);
        buffer.loadPixels();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float tmpA, tmpB, tmpa, tmpb;
                tmpA = 1 - (y / (float) height);
                tmpB = 1 - tmpA;
                tmpa = 1 - (x / (float) width);
                tmpb = 1 - tmpa;
                float u = periodConfig.payoffFunction.getPayoff(
                        currentPercent,
                        new float[]{tmpA, tmpB},
                        new float[]{tmpa, tmpb});
                payoff[x][y] = u;
                buffer.pixels[y * width + x] = getRGB(u / periodConfig.payoffFunction.getMax());
            }
        }
        buffer.updatePixels();
    }

    public void updateThreeStrategyHeatmap(
            float currentPercent,
            float r, float p, float s,
            ThreeStrategySelector threeStrategySelector) {
        buffer = applet.createGraphics(width, height, PEmbed.P2D);
        buffer.loadPixels();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float[] RPS = threeStrategySelector.translate(x, height - y);
                if (RPS[0] >= 0 && RPS[1] >= 0 && RPS[2] >= 0) {
                    float u = periodConfig.payoffFunction.getPayoff(
                            currentPercent,
                            RPS, new float[]{r, p, s});
                    payoff[x][y] = u;
                    buffer.pixels[y * width + x] = getRGB(u / periodConfig.payoffFunction.getMax());
                } else {
                    payoff[x][y] = 0;
                    buffer.pixels[y * width + x] = applet.color(255, 0, 0, 0);
                }
            }
        }
        buffer.updatePixels();
    }

    public PGraphics getHeatmap() {
        if (visible) {
            return buffer;
        }
        return invisibleBuffer;
    }

    public float getPayoff(int x, int y) {
        if (x < 0) {
            x = 0;
        } else if (x >= payoff.length) {
            x = payoff.length - 1;
        }
        if (y < 0) {
            y = 0;
        } else if (y >= payoff.length) {
            y = payoff.length - 1;
        }
        return payoff[x][y];
    }
}
