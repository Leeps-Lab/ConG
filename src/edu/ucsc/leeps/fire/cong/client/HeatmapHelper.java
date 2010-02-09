/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.client;

import edu.ucsc.leeps.fire.cong.server.RPSPayoffFunction;
import edu.ucsc.leeps.fire.cong.server.TwoStrategyPayoffFunction;
import processing.core.PApplet;
import processing.core.PGraphics;

/**
 *
 * @author jpettit
 */
public class HeatmapHelper {

    private TwoStrategyPayoffFunction twoStrategyPayoffFunction;
    private RPSDisplay rpsDisplay;
    private int width, height;
    private int lowColor, midColor, highColor;
    private float[][] payoff;
    private PGraphics buffer;
    private PApplet applet;

    public HeatmapHelper(
            PApplet applet,
            int width, int height,
            int lowColor, int midColor, int highColor) {
        this.width = width;
        this.height = height;
        this.lowColor = lowColor;
        this.midColor = midColor;
        this.highColor = highColor;
        this.twoStrategyPayoffFunction = null;
        this.rpsDisplay = null;
        this.payoff = new float[width][height];
        this.applet = applet;
    }

    public void setTwoStrategyPayoffFunction(TwoStrategyPayoffFunction twoStrategyPayoffFunction) {
        this.twoStrategyPayoffFunction = twoStrategyPayoffFunction;
        this.rpsDisplay = null;
    }

    public void setRPSDisplay(RPSDisplay rpsDisplay) {
        this.twoStrategyPayoffFunction = null;
        this.rpsDisplay = rpsDisplay;
    }

    // Chooses whether to interpolate between low and mid, or low and high
    private int getRGB(float u) {
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
        buffer = applet.createGraphics(width, height, PApplet.P2D);
        buffer.loadPixels();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float tmpA, tmpB, tmpa, tmpb;
                tmpA = 1 - (y / (float) height);
                tmpB = 1 - tmpA;
                tmpa = 1 - (x / (float) width);
                tmpb = 1 - tmpa;
                float u = twoStrategyPayoffFunction.getPayoff(currentPercent, tmpA, tmpB, tmpa, tmpb);
                payoff[x][y] = u;
                buffer.pixels[y * width + x] = getRGB(u / twoStrategyPayoffFunction.getMax());
            }
        }
        buffer.updatePixels();
    }

    public void updateRPSHeatmap(float r, float p, float s) {
        RPSPayoffFunction payoffFunction = rpsDisplay.getPayoffFunction();
        buffer = applet.createGraphics(width, height, PApplet.P2D);
        buffer.loadPixels();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float[] RPS = rpsDisplay.translate(x, height - y);
                if (RPS[0] >= 0 && RPS[1] >= 0 && RPS[2] >= 0) {
                    float u = payoffFunction.getPayoff(RPS[0], RPS[1], RPS[2], r, p, s);
                    payoff[x][y] = u;
                    buffer.pixels[y * width + x] = getRGB(u / payoffFunction.getMax());
                } else {
                    payoff[x][y] = 0;
                    buffer.pixels[y * width + x] = applet.color(255, 0, 0, 0);
                }
            }
        }
        buffer.updatePixels();
    }

    public PGraphics getHeatmap() {
        return buffer;
    }

    public float getPayoff(int x, int y) {
        return payoff[x][y];
    }
}
