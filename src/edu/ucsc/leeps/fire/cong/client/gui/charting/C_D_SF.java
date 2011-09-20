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
        // x axis (time range (0 to config.length)
        drawXAxis(a);
        // y axis (strategy range (payoffFunction.min to payoffFunction.max))
        drawYAxis(a);
        if (config.indefiniteEnd == null) {
            if (config.subperiods == 0) {
                drawContinuousInfoDelayArea(a);
                synchronized (Client.state.strategiesTime) {
                    drawContinuousPayoffArea(a);
                    drawContinuousStrategyLines(a);
                }
            } else {
                drawSubperiodInfoDelayArea(a);
                drawStrategyPreview(a);
                drawSubperiodGrid(a);
                synchronized (Client.state.strategiesTime) {
                    drawSubperiodPayoffArea(a);
                    drawSubperiodStrategyLines(a);
                }
            }
        } else {
            if (config.subperiods == 0) {
                drawContinuousIndefiniteEndInfoDelayArea(a);
                synchronized (Client.state.strategiesTime) {
                    drawContinuousIndefiniteEndPayoffArea(a);
                    drawContinuousIndefiniteEndStrategyLines(a);
                }
            } else {
                drawSubperiodIndefiniteEndInfoDelayArea(a);
                drawIndefiniteEndStrategyPreview(a);
                drawSubperiodIndefiniteEndGrid(a);
                synchronized (Client.state.strategiesTime) {
                    drawSubperiodIndefiniteEndPayoffArea(a);
                    drawSubperiodIndefiniteEndStrategyLines(a);
                }
            }
        }
        // outline
        drawOutline(a);
        a.popMatrix();
    }

    private void drawContinuousStrategyLines(Client a) {
        // strategy lines, 1 per player in strategies, scaled from smin to smax
        a.stroke(0);
        a.fill(0);
        a.strokeWeight(2);
        float currX = width * Client.state.currentPercent;
        float lastX = 0;
        float lastNonDelayedX = 0;
        float[] lastY = null;
        float delayX;
        if (Client.state.currentPercent < 1) {
            delayX = width * (Client.state.currentPercent - ((float) config.infoDelay / config.length));
        } else {
            delayX = currX;
        }
        for (Strategy s : Client.state.strategiesTime) {
            float x = width * (s.timestamp / (float) (1e9 * config.length));
            if (lastY == null) {
                lastY = new float[s.strategies.size() + 1];
                for (int i = 0; i < lastY.length; i++) {
                    float initial = 0;
                    lastY[i] = scaledHeight * (1 - initial) + scaledMargin;
                }
            }
            for (int id : s.strategies.keySet()) {
                if (id != Client.state.id) {
                    if (s.delayed()) {
                        continue;
                    }
                    lastNonDelayedX = x;
                }
                if (id >= lastY.length) {
                    continue;
                }
                float[] f = s.strategies.get(id);
                float y = scaledHeight * (1 - f[0]) + scaledMargin;
                a.stroke(config.currColors.get(id).getRGB());
                a.line(lastX, lastY[id], x, lastY[id]);
                a.line(x, lastY[id], x, y);
                lastY[id] = y;
            }
            lastX = x;
        }
        if (delayX >= 0 && lastY != null) {
            for (int id = 1; id < lastY.length; id++) {
                if (id != Client.state.id) {
                    a.stroke(config.currColors.get(id).getRGB());
                    a.line(lastNonDelayedX, lastY[id], delayX, lastY[id]);
                }
            }
        }
        if (lastY != null && config.currColors.get(Client.state.id) != null) {
            a.stroke(config.currColors.get(Client.state.id).getRGB());
            a.line(lastX, lastY[Client.state.id], currX, lastY[Client.state.id]);
        }
    }

    private void drawContinuousPayoffArea(Client a) {
        // for all time, draw flow payoff area for this player
        float delayX;
        if (Client.state.currentPercent < 1) {
            delayX = width * (Client.state.currentPercent - ((float) config.infoDelay / config.length));
        } else {
            delayX = width;
        }
        if (delayX < 0) {
            return;
        }
        a.noStroke();
        a.fill(164, 218, 148, 200);
        float payoffMin = config.payoffFunction.getMin();
        float payoffMax = config.payoffFunction.getMax();
        float scaledMarginalCost = Client.map(config.marginalCost, payoffMin, payoffMax, 0, 1);
        float payoffFloor = scaledHeight * (1 - scaledMarginalCost) + scaledMargin;
        a.beginShape();
        a.vertex(0, payoffFloor);
        float lastY1 = 0;
        for (Strategy s : Client.state.strategiesTime) {
            float percent = s.timestamp / (float) (1e9 * config.length);
            float x = width * percent;
            if (s.delayed()) {
                break;
            }
            float payoff = config.payoffFunction.getPayoff(Client.state.id, percent, s.strategies, s.matchStrategies, config);
            float scaledPayoff = Client.map(payoff + config.marginalCost, payoffMin, payoffMax, 0, 1);
            float y = scaledHeight * (1 - scaledPayoff) + scaledMargin;
            a.vertex(x, lastY1);
            a.vertex(x, y);
            lastY1 = y;
        }
        a.vertex(delayX, lastY1);
        a.vertex(delayX, payoffFloor);
        a.endShape(Client.CLOSE);
    }

    private void drawContinuousInfoDelayArea(Client a) {
        float currX = width * Client.state.currentPercent;
        float delayX;
        if (Client.state.currentPercent < 1) {
            delayX = width * (Client.state.currentPercent - ((float) config.infoDelay / config.length));
        } else {
            delayX = width;
        }
        if (delayX < 0) {
            delayX = 0;
        }
        a.noStroke();
        a.fill(100, 50);
        a.beginShape();
        a.vertex(delayX, height);
        a.vertex(currX, height);
        a.vertex(currX, 0);
        a.vertex(delayX, 0);
        a.endShape(Client.CLOSE);
    }

    private void drawSubperiodStrategyLines(Client a) {
        // strategy lines, 1 per player in strategies, scaled from smin to smax
        a.stroke(0);
        a.fill(0);
        a.strokeWeight(2);
        int i = 0;
        for (Strategy s : Client.state.strategiesTime) {
            float x0 = width * ((s.timestamp - 1) / (float) config.subperiods);
            float x1 = width * (s.timestamp / (float) config.subperiods);
            for (int id : s.strategies.keySet()) {
                if (id != Client.state.id) {
                    if (s.delayed()) {
                        continue;
                    }
                }
                float[] f = s.strategies.get(id);
                float y = scaledHeight * (1 - f[0]) + scaledMargin;
                a.stroke(config.currColors.get(id).getRGB());
                a.line(x0, y, x1, y);
                if (i > 0) {
                    float lastY = scaledHeight * (1 - Client.state.strategiesTime.get(i - 1).strategies.get(id)[0]) + scaledMargin;
                    a.line(x0, y, x0, lastY);
                }
            }
            i++;
        }
    }

    private void drawSubperiodPayoffArea(Client a) {
        // delay off, payoff floor
        a.noStroke();
        a.fill(164, 218, 148, 200);
        float payoffMin = config.payoffFunction.getMin();
        float payoffMax = config.payoffFunction.getMax();
        float scaledMarginalCost = Client.map(config.marginalCost, payoffMin, payoffMax, 0, 1);
        float payoffFloor = scaledHeight * (1 - scaledMarginalCost) + scaledMargin;
        float lastNonDelayedX = 0;
        a.beginShape();
        a.vertex(0, payoffFloor);
        for (Strategy s : Client.state.strategiesTime) {
            float x0 = width * ((s.timestamp - 1) / (float) config.subperiods);
            float x1 = width * (s.timestamp / (float) config.subperiods);
            if (s.delayed()) {
                break;
            }
            lastNonDelayedX = x1;
            float payoff = config.payoffFunction.getPayoff(Client.state.id, s.timestamp / (float) config.subperiods, s.strategies, s.matchStrategies, config);
            float scaledPayoff = Client.map(payoff + config.marginalCost, payoffMin, payoffMax, 0, 1);
            float y = scaledHeight * (1 - scaledPayoff) + scaledMargin;
            a.vertex(x0, y);
            a.vertex(x1, y);
        }
        a.vertex(lastNonDelayedX, payoffFloor);
        a.endShape(Client.CLOSE);
    }

    private void drawStrategyPreview(Client a) {
        if (Client.state.subperiod < config.subperiods && Client.state.getMyStrategy() != null) {
            float f = Client.state.getMyStrategy()[0];
            float y = scaledHeight * (1 - f) + scaledMargin;
            a.stroke(114, 163, 219);
            float x0 = Client.map(Client.state.subperiod, 0, config.subperiods, 0, width);
            float x1 = Client.map(Client.state.subperiod + 1, 0, config.subperiods, 0, width);
            a.line(x0, y, x1, y);
        }
    }

    private void drawSubperiodGrid(Client a) {
        a.stroke(100, 100, 100, 50);
        for (int subperiod = 0; subperiod < config.subperiods; subperiod++) {
            float x = Client.map(subperiod, 0, config.subperiods, 0, width);
            a.line(x, 0, x, height);
        }
    }

    private void drawSubperiodInfoDelayArea(Client a) {
        float currX = width * (Client.state.subperiod / (float) config.subperiods);
        float delayX;
        if (Client.state.currentPercent < 1) {
            delayX = width * ((Client.state.subperiod / (float) config.subperiods) - ((float) config.infoDelay / config.subperiods));
        } else {
            delayX = width;
        }
        if (delayX < 0) {
            delayX = 0;
        }
        a.noStroke();
        a.fill(100, 50);
        a.beginShape();
        a.vertex(delayX, height);
        a.vertex(currX, height);
        a.vertex(currX, 0);
        a.vertex(delayX, 0);
        a.endShape(Client.CLOSE);
    }

    private void drawContinuousIndefiniteEndStrategyLines(Client a) {
        if (Client.state.strategiesTime.isEmpty()) {
            return;
        }
        // strategy lines, 1 per player in strategies, scaled from smin to smax
        float lastT = config.length * Client.state.currentPercent * 1e9f;
        float scaledWidth = width;
        float timeOffset = 0;
        scaledWidth *= config.indefiniteEnd.percentToDisplay;
        if (lastT - config.indefiniteEnd.displayLength * 1e9 > 0) {
            timeOffset = lastT - config.indefiniteEnd.displayLength * 1e9f;
        }
        a.stroke(0);
        a.fill(0);
        a.strokeWeight(2);
        float lastX = 0;
        float[] lastY = null;
        float lastNonDelayedX = 0;
        for (Strategy s : Client.state.strategiesTime) {
            float x = scaledWidth * ((s.timestamp - timeOffset) / (float) (1e9 * config.indefiniteEnd.displayLength));
            if (lastY == null) {
                lastY = new float[s.strategies.size() + 1];
                for (int i = 0; i < lastY.length; i++) {
                    float initial = 0;
                    lastY[i] = scaledHeight * (1 - initial) + scaledMargin;
                }
            }
            for (int id : s.strategies.keySet()) {
                if (id != Client.state.id) {
                    if (s.delayed()) {
                        continue;
                    }
                    lastNonDelayedX = x;
                }
                if (id >= lastY.length) {
                    continue;
                }
                float[] f = s.strategies.get(id);
                float y = scaledHeight * (1 - f[0]) + scaledMargin;
                a.stroke(config.currColors.get(id).getRGB());
                if (lastX >= 0 && x >= 0) {
                    a.line(lastX, lastY[id], x, lastY[id]);
                } else if (x >= 0) {
                    a.line(0, lastY[id], x, lastY[id]);
                }
                if (x >= 0) {
                    a.line(x, lastY[id], x, y);
                }
                lastY[id] = y;
            }
            lastX = x;
        }
        float currX = scaledWidth;
        if ((Client.state.currentPercent * config.length) < config.indefiniteEnd.displayLength) {
            currX = scaledWidth * ((Client.state.currentPercent * config.length) / config.indefiniteEnd.displayLength);
        }
        float delayX = currX - scaledWidth * (config.infoDelay / (float) config.indefiniteEnd.displayLength);
        if (Client.state.currentPercent >= 1) {
            delayX = currX;
        }
        // draw opponents from their last played strategy to the current delay point
        if (lastNonDelayedX >= 0 && delayX >= 0 && lastY != null) {
            for (int id = 1; id < lastY.length; id++) {
                if (id != Client.state.id) {
                    a.stroke(config.currColors.get(id).getRGB());
                    a.line(lastNonDelayedX, lastY[id], delayX, lastY[id]);
                }
            }
        }
        // draw from my last played strategy to the current point
        if (lastY != null && config.currColors.get(Client.state.id) != null && lastX >= 0) {
            a.stroke(config.currColors.get(Client.state.id).getRGB());
            a.line(lastX, lastY[Client.state.id], currX, lastY[Client.state.id]);
        }
    }

    private void drawContinuousIndefiniteEndPayoffArea(Client a) {
        if (Client.state.strategiesTime.isEmpty()) {
            return;
        }
        // strategy lines, 1 per player in strategies, scaled from smin to smax
        float lastT = config.length * Client.state.currentPercent * 1e9f;
        float scaledWidth = width * config.indefiniteEnd.percentToDisplay;
        float timeOffset = 0;
        if (lastT - config.indefiniteEnd.displayLength * 1e9 > 0) {
            timeOffset = lastT - config.indefiniteEnd.displayLength * 1e9f;
        }
        a.noStroke();
        a.fill(164, 218, 148, 200);
        float lastX = 0;
        float lastY = 0;
        float payoffMin = config.payoffFunction.getMin();
        float payoffMax = config.payoffFunction.getMax();
        float scaledMarginalCost = Client.map(config.marginalCost, payoffMin, payoffMax, 0, 1);
        float payoffFloor = scaledHeight * (1 - scaledMarginalCost) + scaledMargin;
        a.beginShape();
        a.vertex(0, payoffFloor);
        for (Strategy s : Client.state.strategiesTime) {
            float x = scaledWidth * ((s.timestamp - timeOffset) / (float) (1e9 * config.indefiniteEnd.displayLength));
            if (s.delayed()) {
                continue;
            }
            float payoff = config.payoffFunction.getPayoff(Client.state.id, s.timestamp / (float) config.subperiods, s.strategies, s.matchStrategies, config);
            float scaledPayoff = Client.map(payoff + config.marginalCost, payoffMin, payoffMax, 0, 1);
            float y = scaledHeight * (1 - scaledPayoff) + scaledMargin;
            if (lastX >= 0 && x >= 0) {
                a.vertex(lastX, lastY);
                a.vertex(x, lastY);
            } else if (x >= 0) {
                a.vertex(0, lastY);
                a.vertex(x, lastY);
            }
            if (x >= 0) {
                a.vertex(x, lastY);
                a.vertex(x, y);
            }
            lastY = y;
            lastX = x;
        }
        float currX = scaledWidth;
        if ((Client.state.currentPercent * config.length) < config.indefiniteEnd.displayLength) {
            currX = scaledWidth * ((Client.state.currentPercent * config.length) / config.indefiniteEnd.displayLength);
        }
        float delayX = currX - scaledWidth * (config.infoDelay / (float) config.indefiniteEnd.displayLength);
        if (Client.state.currentPercent >= 1) {
            delayX = currX;
        }
        if (lastX >= 0 && delayX >= 0) {
            a.vertex(lastX, lastY);
            a.vertex(delayX, lastY);
        }
        if (delayX >= 0) {
            a.vertex(delayX, payoffFloor);
        }
        a.endShape(Client.CLOSE);
    }

    private void drawContinuousIndefiniteEndInfoDelayArea(Client a) {
        float scaledWidth = width * config.indefiniteEnd.percentToDisplay;
        float currX = scaledWidth;
        if ((Client.state.currentPercent * config.length) < config.indefiniteEnd.displayLength) {
            currX = scaledWidth * ((Client.state.currentPercent * config.length) / config.indefiniteEnd.displayLength);
        }
        float delayX = currX - scaledWidth * (config.infoDelay / (float) config.indefiniteEnd.displayLength);
        if (Client.state.currentPercent >= 1) {
            delayX = currX;
        }
        if (delayX < 0) {
            delayX = 0;
        }
        a.noStroke();
        a.fill(100, 50);
        a.beginShape();
        a.vertex(delayX, height);
        a.vertex(currX, height);
        a.vertex(currX, 0);
        a.vertex(delayX, 0);
        a.endShape(Client.CLOSE);
    }

    private void drawSubperiodIndefiniteEndStrategyLines(Client a) {
        // strategy lines, 1 per player in strategies, scaled from smin to smax
        float timeOffset = Client.state.subperiod - ((float) config.indefiniteEnd.percentToDisplay * config.indefiniteEnd.displayLength);
        if (timeOffset < 0) {
            timeOffset = 0;
        }
        a.stroke(0);
        a.fill(0);
        a.strokeWeight(2);
        int i = 0;
        for (Strategy s : Client.state.strategiesTime) {
            float x0 = width * ((s.timestamp - 1 - timeOffset) / (float) config.indefiniteEnd.displayLength);
            float x1 = width * ((s.timestamp - timeOffset) / (float) config.indefiniteEnd.displayLength);
            for (int id : s.strategies.keySet()) {
                if (id != Client.state.id) {
                    if (s.delayed()) {
                        continue;
                    }
                }
                float[] f = s.strategies.get(id);
                float y = scaledHeight * (1 - f[0]) + scaledMargin;
                a.stroke(config.currColors.get(id).getRGB());
                if (x0 >= 0 && x1 >= 0) {
                    a.line(x0, y, x1, y);
                }
                if (i > 0) {
                    float lastY = scaledHeight * (1 - Client.state.strategiesTime.get(i - 1).strategies.get(id)[0]) + scaledMargin;
                    if (x0 >= 0) {
                        a.line(x0, y, x0, lastY);
                    }
                }
            }
            i++;
        }
    }

    private void drawSubperiodIndefiniteEndPayoffArea(Client a) {
        float timeOffset = Client.state.subperiod - ((float) config.indefiniteEnd.percentToDisplay * config.indefiniteEnd.displayLength);
        if (timeOffset < 0) {
            timeOffset = 0;
        }
        a.noStroke();
        a.fill(164, 218, 148, 200);
        float payoffMin = config.payoffFunction.getMin();
        float payoffMax = config.payoffFunction.getMax();
        float scaledMarginalCost = Client.map(config.marginalCost, payoffMin, payoffMax, 0, 1);
        float payoffFloor = scaledHeight * (1 - scaledMarginalCost) + scaledMargin;
        a.beginShape();
        a.vertex(0, payoffFloor);
        float lastX = 0;
        for (Strategy s : Client.state.strategiesTime) {
            float x0 = width * ((s.timestamp - 1 - timeOffset) / (float) config.indefiniteEnd.displayLength);
            float x1 = width * ((s.timestamp - timeOffset) / (float) config.indefiniteEnd.displayLength);
            if (s.delayed()) {
                break;
            }
            float payoff = config.payoffFunction.getPayoff(Client.state.id, s.timestamp / (float) config.subperiods, s.strategies, s.matchStrategies, config);
            float scaledPayoff = Client.map(payoff + config.marginalCost, payoffMin, payoffMax, 0, 1);
            float y = scaledHeight * (1 - scaledPayoff) + scaledMargin;
            if (x0 >= 0 && x1 >= 0) {
                a.vertex(x0, y);
                a.vertex(x1, y);
            }
            lastX = x1;
        }
        a.vertex(lastX, payoffFloor);
        a.endShape(Client.CLOSE);
    }

    private void drawIndefiniteEndStrategyPreview(Client a) {
        if (Client.state.subperiod < config.subperiods && Client.state.getMyStrategy() != null) {
            float f = Client.state.getMyStrategy()[0];
            float y = scaledHeight * (1 - f) + scaledMargin;
            int subperiod = Client.state.subperiod;
            if (subperiod > (int) (config.indefiniteEnd.displayLength * config.indefiniteEnd.percentToDisplay)) {
                subperiod = (int) (config.indefiniteEnd.displayLength * config.indefiniteEnd.percentToDisplay);
            }
            float x0 = Client.map(subperiod, 0, config.indefiniteEnd.displayLength, 0, width);
            float x1 = Client.map(subperiod + 1, 0, config.indefiniteEnd.displayLength, 0, width);
            a.stroke(114, 163, 219);
            a.line(x0, y, x1, y);
        }
    }

    private void drawSubperiodIndefiniteEndGrid(Client a) {
        a.stroke(100, 100, 100, 50);
        for (int subperiod = 0; subperiod < config.indefiniteEnd.displayLength; subperiod++) {
            float x = Client.map(subperiod, 0, config.indefiniteEnd.displayLength, 0, width);
            a.line(x, 0, x, height);
        }
    }

    private void drawSubperiodIndefiniteEndInfoDelayArea(Client a) {
        if (Client.state.subperiod == config.subperiods) {
            return;
        }
        float timeOffset = Client.state.subperiod - ((float) config.indefiniteEnd.percentToDisplay * config.indefiniteEnd.displayLength);
        if (timeOffset < 0) {
            timeOffset = 0;
        }
        float currX = width * ((Client.state.subperiod - timeOffset) / (float) config.indefiniteEnd.displayLength);
        float delayX;
        if (Client.state.currentPercent < 1) {
            delayX = width * (((Client.state.subperiod - timeOffset) / (float) config.indefiniteEnd.displayLength) - ((float) config.infoDelay / config.indefiniteEnd.displayLength));
        } else {
            delayX = width;
        }
        if (delayX < 0) {
            delayX = 0;
        }
        a.noStroke();
        a.fill(100, 50);
        a.beginShape();
        a.vertex(delayX, height);
        a.vertex(currX, height);
        a.vertex(currX, 0);
        a.vertex(delayX, 0);
        a.endShape(Client.CLOSE);
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
