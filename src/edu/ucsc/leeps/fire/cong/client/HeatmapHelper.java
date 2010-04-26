/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.client;

import edu.ucsc.leeps.fire.cong.client.Client.PEmbed;
import edu.ucsc.leeps.fire.cong.config.PeriodConfig;
import edu.ucsc.leeps.fire.server.BasePeriodConfig;
import edu.ucsc.leeps.fire.server.PeriodConfigurable;
import java.util.ArrayList;
import java.util.List;
import processing.core.PApplet;
import processing.core.PImage;

/**
 *
 * @author jpettit
 */
public class HeatmapHelper extends Sprite implements PeriodConfigurable {

    private PeriodConfig periodConfig;
    private float[][] payoff;
    //private PImage buffer;
    private List<PImage> buffers;
    private PImage currentBuffer;
    private boolean mine;
    private PApplet applet;

    public HeatmapHelper(int x, int y, int width, int height, boolean mine, PApplet applet) {
        super(x, y, width, height);
        this.width = width;
        this.height = height;
        this.payoff = new float[width][height];
        this.mine = mine;
        this.applet = applet;
    }

    public void setPeriodConfig(BasePeriodConfig basePeriodConfig) {
        periodConfig = (PeriodConfig) basePeriodConfig;

    }

    public void setTwoStrategyHeatmapBuffers(float[][][] payoff) {
        buffers = new ArrayList<PImage>();
        int size = payoff[0].length;
        for (int tick = 0; tick < periodConfig.length; tick++) {
            PImage buffer = applet.createImage(size, size, PEmbed.RGB);
            buffer.loadPixels();
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    float u;
                    if (mine) {
                        u = payoff[tick][x][y];
                    } else {
                        u = payoff[tick][y][x];
                    }
                    buffer.pixels[y * size + x] = getRGB(u);
                }
            }
            buffer.updatePixels();
            buffer.resize(width, height);
            buffers.add(buffer);
        }
    }

    // Chooses whether to interpolate between low and mid, or low and high
    public int getRGB(float u) {
        if (u < .5) {
            return interpolateRGB(u * 2.0f, periodConfig.heatmapColorLow, periodConfig.heatmapColorMid);
        } else {
            return interpolateRGB((u - 0.5f) * 2.0f, periodConfig.heatmapColorMid, periodConfig.heatmapColorHigh);
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
        currentBuffer = buffers.get(Math.round(currentPercent * periodConfig.length));
        /*
        buffer = applet.createImage(width, height, PEmbed.RGB);
        buffer.loadPixels();
        for (int x = 0; x < width; x++) {
        for (int y = 0; y < height; y++) {
        float tmpA, tmpB, tmpa, tmpb;
        tmpA = 1 - (y / (float) height);
        tmpB = 1 - tmpA;
        tmpa = 1 - (x / (float) width);
        tmpb = 1 - tmpa;
        float u, max;
        PayoffFunction payoffFunction;
        if (mine) {
        payoffFunction = periodConfig.payoffFunction;
        u = payoffFunction.getPayoff(
        currentPercent,
        new float[]{tmpA, tmpB},
        new float[]{tmpa, tmpb});
        } else {
        payoffFunction = periodConfig.counterpartPayoffFunction;
        u = payoffFunction.getPayoff(
        currentPercent,
        new float[]{tmpa, tmpb},
        new float[]{tmpA, tmpB});
        }
        max = payoffFunction.getMax();
        payoff[x][y] = u;
        buffer.pixels[y * width + x] = getRGB(u / max);
        }
        }
        buffer.updatePixels();
         *
         */
    }

    public void updateThreeStrategyHeatmap(
            float currentPercent,
            float r, float p, float s,
            ThreeStrategySelector threeStrategySelector) {
        /*
        buffer = applet.createGraphics(width, height, PEmbed.P2D);
        buffer.loadPixels();
        for (int x = 0; x < width; x++) {
        for (int y = 0; y < height; y++) {
        float[] RPS = threeStrategySelector.translate(x, height - y);
        if (RPS[0] >= 0 && RPS[1] >= 0 && RPS[2] >= 0) {
        float u, max;
        PayoffFunction payoffFunction;
        if (mine) {
        payoffFunction = periodConfig.payoffFunction;
        } else {
        payoffFunction = periodConfig.counterpartPayoffFunction;
        }
        u = payoffFunction.getPayoff(
        currentPercent,
        RPS, new float[]{r, p, s});
        max = payoffFunction.getMax();

        payoff[x][y] = u;
        buffer.pixels[y * width + x] = getRGB(u / max);
        } else {
        payoff[x][y] = 0;
        buffer.pixels[y * width + x] = applet.color(255, 0, 0, 0);
        }
        }
        }
        buffer.updatePixels();
         * 
         */
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

    @Override
    public void draw(PEmbed applet) {
        if (!visible) {
            return;
        }
        if (currentBuffer != null) {
            applet.image(currentBuffer, origin.x, origin.y);
        }
        //if (visible && buffer != null) {
        //    applet.image(buffer, origin.x, origin.y);
        //}
    }
}
