package edu.ucsc.leeps.fire.cong.client.gui.charting;

import edu.ucsc.leeps.fire.config.Configurable;
import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.client.Client;
import edu.ucsc.leeps.fire.cong.client.State.Strategy;
import edu.ucsc.leeps.fire.cong.client.gui.Sprite;
import edu.ucsc.leeps.fire.cong.config.Config;

/**
 *
 * @author leeps
 */
public class C_D_SF extends Sprite implements Configurable<Config> {

    private int NORMAL_ORIGIN_X, EXTENDED_ORIGIN_X, NORMAL_WIDTH, EXTENDED_WIDTH;
    private final float scaledHeight, scaledMargin;
    private float yVal, yMin, yMax;
    private Config config;

    @SuppressWarnings("LeakingThisInConstructor")
    public C_D_SF(Sprite parent, int x, int y, int width, int height, int extended_width) {
        super(parent, x, y, width, height);
        FIRE.client.addConfigListener(this);
        this.NORMAL_WIDTH = width;
        this.EXTENDED_WIDTH = extended_width;
        this.NORMAL_ORIGIN_X = x;
        this.EXTENDED_ORIGIN_X = x - (extended_width - width);
        scaledHeight = 0.9f * height;
        scaledMargin = (height - scaledHeight) / 2;
    }

    @Override
    public void draw(Client a) {
        if (!visible) {
            return;
        }
        a.pushMatrix();
        a.translate(origin.x, origin.y);
        // draw the continuous time, info delayed, strategy + flow chart
        // outline
        drawOutline(a);
        // x axis (time range (0 to config.length)
        drawXAxis(a);
        // y axis (strategy range (payoffFunction.min to payoffFunction.max))
        drawYAxis(a);
        // strategy lines, 1 per player in strategies, scaled from smin to smax
        a.stroke(0);
        a.fill(0);
        a.smooth();
        a.strokeWeight(1);
        float lastX = 0;
        float[] lastStrategy = null;
        float lastOtherX = 0;
        float currX = width * Client.state.currentPercent;
        float delayX;
        if (Client.state.currentPercent < 1) {
            delayX = width * (Client.state.currentPercent - ((float) config.infoDelay / config.length));
        } else {
            delayX = currX;
        }
        float currY = scaledHeight * (1 - Client.state.strategies.get(Client.state.id)[0]) + scaledMargin;
        synchronized (Client.state.strategiesTime) {
            for (Strategy s : Client.state.strategiesTime) {
                float x = width * (s.timestamp / (float) (1e9 * config.length));
                if (lastStrategy == null) {
                    lastStrategy = new float[s.strategies.size() + 1];
                }
                for (int id : s.strategies.keySet()) {
                    if (id != Client.state.id) {
                        if (delayed(s.timestamp)) {
                            continue;
                        } else {
                            lastOtherX = x;
                        }
                    }
                    float[] f = s.strategies.get(id);
                    float y = scaledHeight * (1 - f[0]) + scaledMargin;
                    float lastY = scaledHeight * (1 - lastStrategy[id]) + scaledMargin;
                    //if (lastX > 0) {
                        a.line(lastX, lastY, lastX, y);
                        a.line(lastX, y, x, y);
                        lastStrategy[id] = f[0];
                    //}
                }
                lastX = x;
            }
        }
        for (int i = 1; i < lastStrategy.length; i++) {
            float lastY = scaledHeight * (1 - lastStrategy[i]) + scaledMargin;
            if (i == Client.state.id) {
            } else {
                a.line(lastOtherX, lastY, delayX, lastY);
            }
        }
        // flow payoff area for this player
        a.popMatrix();
    }

    private boolean delayed(long timestamp) {
        return ((1e9 * (Client.state.currentPercent * config.length)) - timestamp) < 1e9 * config.infoDelay;
    }

    private void drawOutline(Client a) {
        a.stroke(0);
        a.strokeWeight(2);
        a.noFill();
        a.rectMode(Client.CORNER);
        a.rect(0, 0, width, height);
    }

    private void drawXAxis(Client a) {
        a.textAlign(Client.CENTER, Client.CENTER);
        for (float x = 0.0f; x <= 1.01f; x += 0.1f) {
            a.noFill();
            a.stroke(100, 100, 100);
            a.strokeWeight(2);
            float x0, y0, x1, y1;
            x0 = x * width;
            y0 = height;
            x1 = x * width;
            y1 = height + 10;
            a.line(x0, y0, x1, y1);
            a.fill(0);
            if (config.showPayoffTimeAxisLabels) {
                int percent = Math.round(x * 100);
                String label = String.format("%d%%", percent);
                a.text(label,
                        (int) x0,
                        (int) (y0 + 1.2f * a.textAscent() + a.textDescent()));
            }
        }
    }

    public void drawYAxis(Client a) {
        int labelX = (int) (-10 - 1.1f * a.textWidth(String.format("%.1f", yMax)) / 2f);
        for (float y = 0.0f; y <= 1.01f; y += 0.1f) {
            a.noFill();
            a.stroke(100, 100, 100);
            a.strokeWeight(2);
            float x0, y0, x1, y1;
            x0 = -10;
            y0 = y * scaledHeight + scaledMargin;
            x1 = 0;
            y1 = y * scaledHeight + scaledMargin;
            a.line(x0, y0, x1, y1);
            a.stroke(100, 100, 100, 50);
            a.line(x0, y0, width, y1);
            a.fill(0);
            yVal = (1 - y) * (yMax - yMin) + yMin;
            if (y <= 0.0f) {
                yVal = yMax;
            } else if (y >= 1.0f) {
                yVal = yMin;
            }
            a.text(String.format("%.1f", yVal), labelX, (int) y0);
        }
    }

    public void configChanged(Config config) {
        this.config = config;
        switch (config.selector) {
            case bubbles:
                visible = false;
                break;
            case heatmap2d:
            case pure:
            case qwerty:
            case simplex:
                width = NORMAL_WIDTH;
                origin.x = NORMAL_ORIGIN_X;
            case strip:
                visible = true;
                width = EXTENDED_WIDTH;
                origin.x = EXTENDED_ORIGIN_X;
                break;
        }
        yMin = config.payoffFunction.getMin();
        yMax = config.payoffFunction.getMax();
    }
}
