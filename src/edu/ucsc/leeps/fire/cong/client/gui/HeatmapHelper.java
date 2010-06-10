/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.client.gui;

import edu.ucsc.leeps.fire.cong.client.Client;
import edu.ucsc.leeps.fire.cong.client.Client.PEmbed;
import edu.ucsc.leeps.fire.cong.server.PayoffFunction;
import java.util.ArrayList;
import java.util.List;
import processing.core.PApplet;
import processing.core.PImage;

/**
 *
 * @author jpettit
 */
public class HeatmapHelper extends Sprite {

    private float[][] payoff;
    //private PImage buffer;
    private List<PImage> buffers;
    private PImage currentBuffer;
    private boolean mine, isCounterpart;
    private PApplet applet;

    public HeatmapHelper(int x, int y, int width, int height,
            boolean mine,
            PApplet applet) {
        super(x, y, width, height);
        this.width = width;
        this.height = height;
        this.payoff = new float[width][height];
        this.mine = mine;
        this.applet = applet;
    }

    public void setIsCounterpart(boolean isCounterpart) {
        this.isCounterpart = isCounterpart;
    }

    public void setTwoStrategyHeatmapBuffers(float[][][] payoff) {
        buffers = new ArrayList<PImage>();
        int size = payoff[0].length;
        for (int tick = 0; tick < Client.state.getPeriodConfig().length; tick++) {
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
            return interpolateRGB(u * 2.0f,
                    Client.state.getPeriodConfig().heatmapColorLow,
                    Client.state.getPeriodConfig().heatmapColorMid);
        } else {
            return interpolateRGB((u - 0.5f) * 2.0f,
                    Client.state.getPeriodConfig().heatmapColorMid,
                    Client.state.getPeriodConfig().heatmapColorHigh);
        }
    }

    // Linearly interpolates u between low and high
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
        if (buffers == null) {
            int size = 100;
            currentBuffer = applet.createImage(size, size, PEmbed.RGB);
            currentBuffer.loadPixels();
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    PayoffFunction u;
                    float A = 1 - (y / (float) size);
                    float a = 1 - (x / (float) size);
                    if (mine && isCounterpart) {
                        u = Client.state.getPeriodConfig().counterpartPayoffFunction;
                    } else if (mine && !isCounterpart) {
                        u = Client.state.getPeriodConfig().payoffFunction;
                    } else if (!mine && isCounterpart) {
                        u = Client.state.getPeriodConfig().payoffFunction;
                    } else if (!mine && !isCounterpart) {
                        u = Client.state.getPeriodConfig().counterpartPayoffFunction;
                    } else {
                        assert false;
                        u = null;
                    }
                    float value;
                    if (mine) {
                        value = u.getPayoff(currentPercent,
                                new float[]{A}, new float[]{a}) / u.getMax();
                    } else {
                        value = u.getPayoff(currentPercent,
                                new float[]{a}, new float[]{A}) / u.getMax();
                    }
                    currentBuffer.pixels[y * size + x] = getRGB(value);
                }
            }
            currentBuffer.updatePixels();
            currentBuffer.resize(width, height);
        } else {
            currentBuffer = buffers.get(
                    Math.round(currentPercent * Client.state.getPeriodConfig().length));
        }
    }

    public void updateThreeStrategyHeatmap(
            float currentPercent,
            float r, float p, float s,
            ThreeStrategySelector threeStrategySelector) {
        currentBuffer = applet.createGraphics(width, height, PEmbed.P2D);
        currentBuffer.loadPixels();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float[] RPS = threeStrategySelector.translate(x, height - y);
                if (RPS[0] >= 0 && RPS[1] >= 0 && RPS[2] >= 0) {
                    float u, max;
                    PayoffFunction payoffFunction;
                    if (mine) {
                        payoffFunction = Client.state.getPeriodConfig().payoffFunction;
                    } else {
                        payoffFunction = Client.state.getPeriodConfig().counterpartPayoffFunction;
                    }
                    u = payoffFunction.getPayoff(
                            currentPercent,
                            RPS, new float[]{r, p, s});
                    max = payoffFunction.getMax();

                    payoff[x][y] = u;
                    currentBuffer.pixels[y * width + x] = getRGB(u / max);
                } else {
                    payoff[x][y] = 0;
                    currentBuffer.pixels[y * width + x] = applet.color(255, 0, 0, 0);
                }
            }
        }
        currentBuffer.updatePixels();
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
