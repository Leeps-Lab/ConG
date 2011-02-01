package edu.ucsc.leeps.fire.cong.client.gui;

import edu.ucsc.leeps.fire.config.Configurable;
import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.client.Client;
import edu.ucsc.leeps.fire.cong.config.Config;
import edu.ucsc.leeps.fire.cong.server.PayoffFunction;
import edu.ucsc.leeps.fire.cong.server.PricingPayoffFunction;
import edu.ucsc.leeps.fire.cong.server.ThreeStrategyPayoffFunction;
import edu.ucsc.leeps.fire.cong.server.ThresholdPayoffFunction;
import edu.ucsc.leeps.fire.cong.server.TwoStrategyPayoffFunction;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jpettit
 */
public class Chart extends Sprite implements Configurable<Config> {

    // Variables to modify that manipulate the chart
    private Config config;
    private float minPayoff, maxPayoff;
    private int scaledMargin;
    private int scaledHeight;
    // Either two or three strategies
    private Line yourPayoff;
    private Line matchPayoff;
    private Line yourStrategy;
    private Line matchStrategy;
    private Line yourR;
    private Line matchR;
    private Line yourP;
    private Line matchP;
    private Line yourS;
    private Line matchS;
    // prices
    private Map<Integer, Line> prices;
    private Map<Integer, Line> marginalCosts;
    // threshold
    private Line threshold;
    private HeatmapLegend heatmapLegend;
    private int subperiod;
    private final Object lock = new Object();

    /**
     *A list of modes. Payoff, two-strategy and three-strategy. 
     */
    public enum Mode {

        Payoff, TwoStrategy, RStrategy, PStrategy, SStrategy,
    };
    private Mode mode;

    /**
     * Creates the chart. Scales the height and margin. Shows line depicting
     * actual payoff for subject and subject's counterpart.
     *
     * For 2 strategy payoff, shows actual and future payoffs for your and
     * counterpart's strategy, Using different combinations: A, B, Aa ,Ab, Ba, Bb.
     *
     * For a 3 strategy, shows actual RPS payoffs. Shows future payoffs for
     * various combinations of R, P, and S. Shows your R, P and S over time, and
     * shows counterpart's R, P and S over time.
     *
     * Shows threshold and draws a simplex. Adds a configListener.
     * 
     * @param parent
     * @param x x-coordinate
     * @param y y-coordinate
     * @param width width of display
     * @param height height of display
     * @param simplex simplex is selected for the strategy changer-- 3 strategies used
     * @param mode mode used
     * @param strategyChanger
     */
    public Chart(Sprite parent, int x, int y, int width, int height, ThreeStrategySelector simplex, Mode mode) {
        super(parent, x, y, width, height);

        scaledHeight = Math.round(0.9f * height);
        scaledMargin = Math.round((height - scaledHeight) / 2f);

        yourPayoff = new Line(this, 0, scaledMargin, width, scaledHeight);
        matchPayoff = new Line(this, 0, scaledMargin, width, scaledHeight);
        yourStrategy = new Line(this, 0, scaledMargin, width, scaledHeight);
        matchStrategy = new Line(this, 0, scaledMargin, width, scaledHeight);

        yourR = new Line(this, 0, scaledMargin, width, scaledHeight);
        matchR = new Line(this, 0, scaledMargin, width, scaledHeight);
        yourP = new Line(this, 0, scaledMargin, width, scaledHeight);
        matchP = new Line(this, 0, scaledMargin, width, scaledHeight);
        yourS = new Line(this, 0, scaledMargin, width, scaledHeight);
        matchS = new Line(this, 0, scaledMargin, width, scaledHeight);

        prices = new HashMap<Integer, Line>();
        marginalCosts = new HashMap<Integer, Line>();

        heatmapLegend = new HeatmapLegend(this, -9, 1, 8, height);

        // Threshold
        threshold = new Line(this, 0, 0, width, height);

        this.mode = mode;

        FIRE.client.addConfigListener(this);
    }

    private void drawShockZone(Client applet) {
        if (config.shock.showZone) {
            applet.fill(100, 100, 100, 50);
            applet.noStroke();
            float x0, y0, x1, y1;
            x0 = width * FIRE.client.getConfig().shock.start;
            y0 = 0;
            x1 = width * FIRE.client.getConfig().shock.end;
            y1 = scaledHeight + scaledMargin * 2;
            applet.rect(x0, y0, x1 - x0, y1);
        }
    }

    private void drawAxis(Client applet) {
        applet.rectMode(Client.CORNER);
        applet.noFill();
        applet.stroke(0);
        applet.strokeWeight(2);
        applet.rect(0, 0, width, height);

        applet.textAlign(Client.CENTER, Client.CENTER);
        applet.fill(255);
        applet.noStroke();
        applet.rect(-40, 0, 38, height);
        applet.rect(0, height + 2, width, 40);
        if (mode == Mode.Payoff) {
            for (float x = 0.0f; x <= 1.01f; x += 0.1f) {
                applet.noFill();
                applet.stroke(100, 100, 100);
                applet.strokeWeight(2);
                float x0, y0, x1, y1;
                x0 = x * width;
                y0 = height;
                x1 = x * width;
                y1 = height + 10;
                applet.line(x0, y0, x1, y1);
                applet.fill(0);
                if (config.showPayoffTimeAxisLabels) {
                    int percent = Math.round(x * 100);
                    String label = String.format("%d%%", percent);
                    applet.text(label,
                            Math.round(x0),
                            Math.round(y0 + 1.2f * applet.textAscent() + applet.textDescent()));
                }
            }
            String maxPayoffLabel = String.format("%.1f", maxPayoff);
            float labelX = -10 - 1.1f * applet.textWidth(maxPayoffLabel) / 2f;
            heatmapLegend.origin.x = -10 - 1.1f * applet.textWidth(maxPayoffLabel)
                    - heatmapLegend.width;
            heatmapLegend.draw(applet);
            for (float y = 0.0f; y <= 1.01f; y += 0.1f) {
                applet.noFill();
                applet.stroke(100, 100, 100);
                applet.strokeWeight(2);
                float x0, y0, x1, y1;
                x0 = -10;
                y0 = y * scaledHeight + scaledMargin;
                x1 = 0;
                y1 = y * scaledHeight + scaledMargin;
                applet.line(x0, y0, x1, y1);
                applet.stroke(100, 100, 100, 50);
                applet.line(x0, y0, width, y1);
                applet.fill(0);
                float payoff = (1 - y) * (maxPayoff - minPayoff) + minPayoff;
                if (payoff < 0) {
                    payoff = 0f;
                }
                String label = String.format("%.1f", payoff);
                applet.text(label, Math.round(labelX), Math.round(y0));
            }
        } else {
            applet.textAlign(Client.RIGHT);
            applet.fill(0);
            if (mode == Mode.RStrategy) {
                applet.text(config.rLabel, -10,
                        Math.round(height / 2f + (applet.textAscent() + applet.textDescent()) / 2f));
            } else if (mode == Mode.PStrategy) {
                applet.text(config.pLabel, -10,
                        Math.round(height / 2f + (applet.textAscent() + applet.textDescent()) / 2f));
            } else if (mode == Mode.SStrategy) {
                applet.text(config.sLabel, -10,
                        Math.round(height / 2f + (applet.textAscent() + applet.textDescent()) / 2f));
            }
        }
    }

    private void drawPercentLine(Client applet) {
        applet.strokeWeight(2f);
        applet.stroke(116, 202, 200);
        applet.line(Client.state.currentPercent * width, 0, Client.state.currentPercent * width, height);
    }

    private void drawTwoStrategyLines(Client applet) {
        yourStrategy.draw(applet);
        matchStrategy.draw(applet);
    }

    private void drawThreeStrategyLines(Client applet) {
        if (mode == Mode.RStrategy) {
            yourR.draw(applet);
            matchR.draw(applet);
        } else if (mode == Mode.PStrategy) {
            yourP.draw(applet);
            matchP.draw(applet);
        } else if (mode == Mode.SStrategy) {
            yourS.draw(applet);
            matchS.draw(applet);
        }
    }

    private void drawPriceLines(Client applet) {
        synchronized (lock) {
            for (Line line : prices.values()) {
                line.draw(applet);
            }
            for (Line line : marginalCosts.values()) {
                line.draw(applet);
            }
        }
    }

    private void drawSubperiodInfo(Client applet) {
        if (config == null || config.subperiods == 0) {
            return;
        }
        // markers
        applet.strokeWeight(1f);
        applet.stroke(100, 100, 100);
        float interval = 1f / (float) config.subperiods;
        float offset = interval;
        for (int i = 0; i < config.subperiods; ++i) {
            applet.line(width * offset, 0, width * offset, height);
            offset += interval;
        }
        // live strategy preview
        if (Client.state.target != null
                && (mode == Mode.TwoStrategy
                || (mode == Mode.Payoff && config.payoffFunction instanceof PricingPayoffFunction))
                && config.mixedStrategySelection) {
            int start = Math.round(subperiod * interval * width);
            int end = Math.round((subperiod + 1) * interval * width);
            int y = height - Math.round(Client.state.target[0] * scaledHeight) - scaledMargin;
            for (int x = start; x < end; x++) {
                applet.point(x, y);
                applet.point(x, y + 1);
            }
        }
    }

    /**
     * Draw chart. Embed the applet in the corner. Translate the origin of the
     * applet to x and y.
     *
     * If the config is not null, draw shockZone applet. When using the two
     * strategy payoff function, if the mode is set to payoff, Draw two strategy
     * payoff lines. Otherwise, if the mode is TwoStrategy, use  drawTwoStrategyLines.
     * Use threshold. If the config of the payoff function is an instance of
     * ThresholdPayoffFunction, have no borders and fill with a transparent
     * yellow. Embed applet in corner, 
     *
     * @param applet a sub-program to be run in Cong.
     */
    @Override
    public void draw(Client applet) {
        if (config == null || !visible) {
            return;
        }
        applet.rectMode(Client.CORNER);
        applet.pushMatrix();
        applet.translate(origin.x, origin.y);
        drawShockZone(applet);
        if (config.payoffFunction instanceof TwoStrategyPayoffFunction) {
            if (mode == Mode.Payoff) {
                yourPayoff.draw(applet);
                if (config.payoffFunction instanceof PricingPayoffFunction) {
                    drawPriceLines(applet);
                } else {
                    matchPayoff.draw(applet);
                }
            } else if (mode == Mode.TwoStrategy) {
                drawTwoStrategyLines(applet);
                if (config.payoffFunction instanceof ThresholdPayoffFunction) {
                    threshold.draw(applet);
                    applet.noStroke();
                    applet.fill(255, 255, 0, 75);
                    applet.rectMode(Client.CORNER);
                    applet.rect(0, 0, width,
                            height * (1 - ((ThresholdPayoffFunction) config.payoffFunction).threshold));
                }
            }
        } else if (config.payoffFunction instanceof ThreeStrategyPayoffFunction) {
            if (mode == Mode.Payoff) {
                yourPayoff.draw(applet);
                matchPayoff.draw(applet);
            } else if (mode == Mode.RStrategy
                    || mode == Mode.PStrategy
                    || mode == Mode.SStrategy) {
                drawThreeStrategyLines(applet);
            }
        }
        if (mode == Mode.Payoff) {
            yourPayoff.drawCostArea(applet, Client.state.strategyChanger.getCost());
        }
        if (!(config.payoffFunction instanceof PricingPayoffFunction && config.subperiods == 0)) {
            drawPercentLine(applet);
        }
        drawSubperiodInfo(applet);
        drawAxis(applet);
        applet.popMatrix();
    }

    /**
     * Clear actual payoff for you, your counterpart,and strategies over time for
     * combinations of strategies for two or three strategy payoff functions.
     */
    public void clearAll() {
        synchronized (lock) {
            yourPayoff.clear();
            matchPayoff.clear();
            yourStrategy.clear();
            matchStrategy.clear();
            yourP.clear();
            yourR.clear();
            yourS.clear();
            matchR.clear();
            matchP.clear();
            matchS.clear();
            prices.clear();
            marginalCosts.clear();
        }
    }

    public void updateLines() {
        updateLines(Client.state.currentPercent);
        updateMarginalCostLines(Client.state.currentPercent);
    }

    private void initializePriceLine(int id) {
        Line priceLine = new Line(this, 0, scaledMargin, width, scaledHeight);
        if (id == Client.state.id) {
            priceLine.configure(FIRE.client.getConfig().yourStrategy);
        } else {
            priceLine.configure(FIRE.client.getConfig().matchStrategy);
        }
        Color color;
        if (id == FIRE.client.getID()) {
            color = Color.BLACK;
        } else {
            color = new Color(200, 100, 0);
        }
        priceLine.r = color.getRed();
        priceLine.g = color.getGreen();
        priceLine.b = color.getBlue();
        priceLine.alpha = 255;
        prices.put(id, priceLine);
    }

    public void updateLines(float percent) {
        if (!visible) {
            return;
        }
        if (percent <= 1f) {
            if (mode == Mode.Payoff) {
                if (config.subperiods != 0) {
                    yourPayoff.addPayoffPoint(percent, Client.state.subperiodPayoff);
                } else {
                    yourPayoff.addPayoffPoint(percent, PayoffFunction.Utilities.getPayoff());
                }
                if (FIRE.client.getConfig().payoffFunction instanceof PricingPayoffFunction) {
                    PricingPayoffFunction pf = (PricingPayoffFunction) FIRE.client.getConfig().payoffFunction;
                    Map<Integer, float[]> currentPrices = Client.state.strategies;
                    for (int id : currentPrices.keySet()) {
                        synchronized (lock) {
                            if (!prices.containsKey(id)) {
                                initializePriceLine(id);
                            }
                            Line priceLine = prices.get(id);
                            priceLine.addPayoffPoint(percent, pf.getMax() * currentPrices.get(id)[0] - pf.getMin());
                        }
                    }
                } else {
                    if (config.subperiods != 0) {
                        matchPayoff.addPayoffPoint(percent, Client.state.subperiodMatchPayoff);
                    } else {
                        matchPayoff.addPayoffPoint(percent, PayoffFunction.Utilities.getMatchPayoff());
                    }
                }
            } else {
                float[] you = Client.state.getMyStrategy();
                float[] match = PayoffFunction.Utilities.getAverageMatchStrategy();
                if (mode == Mode.TwoStrategy) {
                    yourStrategy.addStrategyPoint(percent, you[0]);
                    matchStrategy.addStrategyPoint(percent, match[0]);
                } else if (mode == Mode.RStrategy) {
                    yourR.addStrategyPoint(percent, you[0]);
                    matchR.addStrategyPoint(percent, match[0]);
                } else if (mode == Mode.PStrategy) {
                    yourP.addStrategyPoint(percent, you[1]);
                    matchP.addStrategyPoint(percent, match[1]);
                } else if (mode == Mode.SStrategy) {
                    yourS.addStrategyPoint(percent, you[2]);
                    matchS.addStrategyPoint(percent, match[2]);
                }
            }
        }
    }

    public void updateMarginalCostLines(float percent) {
        if (!visible || percent > 1f || mode != Mode.Payoff || !(FIRE.client.getConfig().payoffFunction instanceof PricingPayoffFunction)) {
            return;
        }
        if (!marginalCosts.containsKey(Client.state.id)) {
            Line marginalCostLine = new Line(this, 0, scaledMargin, width, scaledHeight);
            marginalCostLine.configure(FIRE.client.getConfig().yourStrategy);
            marginalCostLine.mode = Line.Mode.Dashed;
            synchronized (lock) {
                marginalCosts.put(Client.state.id, marginalCostLine);
            }
        }
        marginalCosts.get(Client.state.id).addPayoffPoint(percent, config.marginalCost);
    }

    /**
     * Sets strategy at end of period. In the event that a two strategy payoff
     * function is being used, sets subject's strategy as subperiodStrategy, and
     * sets counterpart's strategy as counterpartSubperiodStrategy. If a three
     * strategy payoff function is used, sets RPS as the first, second and third
     * elements of subperiodStrategy, respectively. Similarly, the rps elements
     * are set as the first, second and third elements of the
     * counterpartSubperiodStrategy.
     *
     * Calculates the percent start using the quotient if one less than the
     * subperiod and the total number of subperiods. Calculates the percent end
     * using the quotient of the subperiod and the total number of subperiods.
     * The current percent is called tmpCurrentPercent.
     *
     * Your current payoff is based on the payoffFunction using tmpCurrentPercent,
     * subperiodStrategy, and counterpartSubperiodStrategy. Counterpart's payoff
     * is determined by counterpartPayoffFunction, using tmpCurrentPercent,
     * counterpartSubperiodStrategy, and subperiodStrategy.
     *
     * Update lines using percent start and percent end. Sets current percent to
     * tmpCurrentPercent.
     *
     * @param subperiod number of subperiod.
     * @param subperiodStrategy strategy selected by subject for subperiod
     * @param counterpartSubperiodStrategy counterpart's strategy for subperiod.
     */
    public void endSubperiod(int subperiod) {
        this.subperiod = subperiod;
        float percentStart = (float) (subperiod - 1) / FIRE.client.getConfig().subperiods;
        float percentEnd = (float) subperiod / FIRE.client.getConfig().subperiods;
        float marginalCostStart = (float) (subperiod) / FIRE.client.getConfig().subperiods;
        float marginalCostEnd = (float) (subperiod + 1) / FIRE.client.getConfig().subperiods;
        float diff = percentEnd - percentStart;
        for (float offset = 0; offset < diff; offset += 0.01) {
            if (subperiod != 0) {
                updateLines(percentStart + offset);
            }
            if (subperiod != FIRE.client.getConfig().subperiods) {
                updateMarginalCostLines(marginalCostStart + offset);
            }
        }
        if (subperiod != 0) {
            updateLines(percentEnd);
        }
        updateMarginalCostLines(marginalCostEnd);
    }

    public void configChanged(Config config) {
        this.config = config;
        assert config != null;
        minPayoff = config.payoffFunction.getMin();
        maxPayoff = config.payoffFunction.getMax();
        yourPayoff.configure(config.yourPayoff);
        matchPayoff.configure(config.matchPayoff);
        yourStrategy.configure(config.yourStrategy);
        matchStrategy.configure(config.matchStrategy);
        yourR.configure(config.yourPayoff);
        yourR.mode = Line.Mode.Solid;
        yourR.weight = 2f;
        matchR.configure(config.matchPayoff);
        matchR.mode = Line.Mode.Solid;
        yourP.configure(config.yourPayoff);
        yourP.mode = Line.Mode.Solid;
        yourP.weight = 2f;
        matchP.configure(config.matchPayoff);
        matchP.mode = Line.Mode.Solid;
        yourS.configure(config.yourPayoff);
        yourS.mode = Line.Mode.Solid;
        yourS.weight = 2f;
        matchS.configure(config.matchPayoff);
        matchS.mode = Line.Mode.Solid;
        threshold.configure(config.thresholdLine);
        if (config.payoffFunction instanceof ThresholdPayoffFunction) {
            threshold.clear();
            for (float percent = 0f; percent < 1.0f; percent += .01f) {
                threshold.setPoint(
                        Math.round(threshold.width * percent),
                        Math.round(threshold.height * (1 - ((ThresholdPayoffFunction) config.payoffFunction).threshold)),
                        true);
            }
            threshold.visible = true;
        } else {
            threshold.visible = false;
        }
        yourPayoff.width = width;
        matchPayoff.width = width;
        yourStrategy.width = width;
        matchStrategy.width = width;
        yourR.width = width;
        matchR.width = width;
        yourP.width = width;
        matchP.width = width;
        yourS.width = width;
        matchS.width = width;
        threshold.width = width;
        if (config.payoffFunction instanceof PricingPayoffFunction) {
            float ratio =
                    (config.marginalCost - config.payoffFunction.getMin())
                    / (config.payoffFunction.getMax() - config.payoffFunction.getMin());
            yourPayoff.origin.x = 0;
            yourPayoff.origin.y = scaledMargin - Math.round(ratio * scaledHeight);
            yourPayoff.width = width;
            yourPayoff.height = scaledHeight;
            yourPayoff.r = Config.colors[0].getRed();
            yourPayoff.g = Config.colors[0].getGreen();
            yourPayoff.b = Config.colors[0].getBlue();
        } else {
            yourPayoff.origin.x = 0;
            yourPayoff.origin.y = scaledMargin;
            yourPayoff.width = width;
            yourPayoff.height = scaledHeight;
            yourPayoff.configure(config.yourPayoff);
        }
    }
}
