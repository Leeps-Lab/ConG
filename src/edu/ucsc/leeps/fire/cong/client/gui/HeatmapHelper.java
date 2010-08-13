package edu.ucsc.leeps.fire.cong.client.gui;

import edu.ucsc.leeps.fire.config.Configurable;
import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.client.Client;
import edu.ucsc.leeps.fire.cong.config.Config;
import edu.ucsc.leeps.fire.cong.server.PayoffFunction;
import java.util.ArrayList;
import java.util.List;
import processing.core.PApplet;
import processing.core.PImage;

/**
 *
 * @author jpettit
 */
public class HeatmapHelper extends Sprite implements Configurable<Config> {

    private Config config;
    private float[][] payoff;
    private List<PImage> buffers;
    private PImage currentBuffer;
    private boolean mine;
    private PApplet applet;
    private List<Integer> colors;

    public HeatmapHelper(Sprite parent, int x, int y, int width, int height,
            boolean mine,
            PApplet applet) {
        super(parent, x, y, width, height);
        this.width = width;
        this.height = height;
        this.payoff = new float[width][height];
        this.mine = mine;
        this.applet = applet;

        colors = new ArrayList<Integer>();
        colors.add(0xFFC24FED); // dark purple
        colors.add(0xFFE4B2FF); // light purple
        colors.add(0xFF214DC1); // dark blue
        colors.add(0xFF4099F5); // light blue
        colors.add(0xFF90C7FF); // sky blue
        colors.add(0xFF4DED43); // bright green
        colors.add(0xFFFAF567); // pastel yellow
        colors.add(0xFFF57023); // orange
        colors.add(0xFFEA3E05); // red

        FIRE.client.addConfigListener(this);
    }

    public void configChanged(Config config) {
        this.config = config;
    }

    public void setTwoStrategyHeatmapBuffers(float[][][] payoff) {
        buffers = new ArrayList<PImage>();
        int size = payoff[0].length;
        for (int tick = 0; tick < config.length; tick++) {
            PImage buffer = applet.createImage(size, size, Client.RGB);
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

    public int getRGB(float percent) {
        int floorIndex = PApplet.floor((colors.size() - 1) * percent);
        int ceilIndex = floorIndex + 1;
        int colorFloor = colors.get(floorIndex);
        int colorCeil = colors.get(ceilIndex);
        float ppf = 1 / (float) (colors.size() - 1);
        float amt = (percent - (ppf * floorIndex)) / ppf;
        int c = applet.lerpColor(colorFloor, colorCeil, amt);
        return c;
    }

    public synchronized void updateTwoStrategyHeatmap(float currentPercent) {
        if (buffers == null) {
            int size = 100;
            currentBuffer = applet.createGraphics(size, size, Client.P2D);
            currentBuffer.loadPixels();
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    PayoffFunction u;
                    float A = 1 - (y / (float) size);
                    float a = 1 - (x / (float) size);
                    if (mine) {
                        u = config.payoffFunction;
                    } else {
                        u = config.counterpartPayoffFunction;
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
            currentBuffer = buffers.get(Math.round(currentPercent * config.length));
        }
    }

    public synchronized void updateThreeStrategyHeatmap(
            float currentPercent,
            float r, float p, float s,
            ThreeStrategySelector threeStrategySelector) {
        currentBuffer = applet.createGraphics(width, height, Client.P2D);
        currentBuffer.loadPixels();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float[] RPS = threeStrategySelector.translate(x, height - y);
                if (RPS[0] >= 0 && RPS[1] >= 0 && RPS[2] >= 0) {
                    float u, max;
                    PayoffFunction payoffFunction;
                    if (mine) {
                        payoffFunction = config.payoffFunction;
                    } else {
                        payoffFunction = config.counterpartPayoffFunction;
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

    public void updateStripHeatmap(
            float currentPercent,
            float opponentStrat) {
        currentBuffer = applet.createGraphics(width, height, Client.P2D);
        currentBuffer.loadPixels();

        PayoffFunction payoffFunction = config.payoffFunction;
        float u;
        float max = payoffFunction.getMax();

        if (height > width) {
            for (int i = 0; i < currentBuffer.pixels.length; i += width) {
                int y = i / width;

                float myStrat = 1f - ((float)y / (float)height);
                u = payoffFunction.getPayoff(currentPercent,
                        new float[]{myStrat},
                        new float[]{opponentStrat});

                currentBuffer.pixels[i] = getRGB(u / max);
            }

            for (int y = 0; y < height; ++y) {
                for (int x = 1; x < width; ++x) {
                    currentBuffer.pixels[y * width + x] = currentBuffer.pixels[y * width];
                }
            }
        } else {
            for (int i = 0; i / width == 0; ++i) {
                float myStrat = (float)i / (float)width;
                u = payoffFunction.getPayoff(currentPercent,
                        new float[]{myStrat},
                        new float[]{opponentStrat});

                currentBuffer.pixels[i] = getRGB(u / max);
            }

            for (int x = 0; x < width; ++x) {
                for (int y = 1; y < height; ++y) {
                    currentBuffer.pixels[y * width + x] = currentBuffer.pixels[x];
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
    public synchronized void draw(Client applet) {
        if (!visible) {
            return;
        }
        if (currentBuffer != null) {
            applet.imageMode(Client.CORNER);
            applet.image(currentBuffer, origin.x, origin.y);
        }
        //if (visible && buffer != null) {
        //    applet.imageMode(Client.CORNER);
        //    applet.image(buffer, origin.x, origin.y);
        //}
    }
}
