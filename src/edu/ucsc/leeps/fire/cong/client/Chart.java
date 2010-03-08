/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.client;

import edu.ucsc.leeps.fire.cong.server.PeriodConfig;
import edu.ucsc.leeps.fire.cong.server.ThreeStrategyPayoffFunction;
import edu.ucsc.leeps.fire.cong.server.TwoStrategyPayoffFunction;
import java.awt.Color;
import processing.core.PApplet;

/**
 *
 * @author jpettit
 */
public class Chart extends Sprite {

    final static Color MY_PAYOFF_COLOR = new Color(20, 200, 20);
    // Two strategies
    final static Color A_PAYOFF_COLOR = new Color(200, 50, 50);
    final static Color B_PAYOFF_COLOR = new Color(50, 50, 200);
    final static Color Aa_PAYOFF_COLOR = new Color(200, 50, 50);
    //final static Color Ab_PAYOFF_COLOR = new Color(50, 50, 200);
    final static Color Ab_PAYOFF_COLOR = new Color(0, 0, 0);
    final static Color Ba_PAYOFF_COLOR = new Color(200, 50, 50);
    final static Color Bb_PAYOFF_COLOR = new Color(50, 50, 200);
    // RPSD
    final static Color R_PAYOFF_COLOR = new Color(255, 25, 25);
    final static Color P_PAYOFF_COLOR = new Color(25, 25, 255);
    final static Color S_PAYOFF_COLOR = new Color(255, 0, 255);

    final static Color TEST_COLOR = new Color(255, 0, 255);
    // Variables to modify that manipulate the chart
    public float currentPercent;
    private PeriodConfig periodConfig;
    private float currentPayoff;
    private float maxPayoff;
    private float futureDistance;
    // Two strategy
    private float percent_A;
    private float percent_B;
    private float percent_a;
    private float percent_b;
    private float currentAPayoff;
    private float currentBPayoff;
    private float currentAaPayoff;
    private float currentAbPayoff;
    private float currentBaPayoff;
    private float currentBbPayoff;
    private Line futureAPayoff;
    private Line futureBPayoff;
    // RPSD
    private float currentRPayoff;
    private float currentPPayoff;
    private float currentSPayoff;
    // Private controls accessed through public methods
    private Line actualPayoff;
    // Two strategy
    private Line actualAPayoff;
    private Line actualBPayoff;
    private Line actualAaPayoff;
    private Line actualAbPayoff;
    private Line actualBaPayoff;
    private Line actualBbPayoff;
    private Line futureAaPayoff;
    private Line futureAbPayoff;
    private Line futureBaPayoff;
    private Line futureBbPayoff;
    // RPSD
    private Line actualRPayoff;
    private Line actualPPayoff;
    private Line actualSPayoff;

    public Chart(int x, int y, int width, int height) {
        super(x, y, width, height);
        actualPayoff = new Line(0, 0, width, height, 1f, MY_PAYOFF_COLOR, 255);
        // Two strategy
        actualAPayoff = new Line(0, 0, width, height, 1.5f, A_PAYOFF_COLOR, 150);
        actualBPayoff = new Line(0, 0, width, height, 1.5f, B_PAYOFF_COLOR, 150);
        futureAPayoff = new Line(0, 0, width, height, 1.0f, TEST_COLOR, 255);
        futureBPayoff = new Line(0, 0, width, height, 1.0f, B_PAYOFF_COLOR, 100);
        actualAaPayoff = new Line(0, 0, width, height, 0.5f, Aa_PAYOFF_COLOR, 255);
        actualAbPayoff = new Line(0, 0, width, height, 0.5f, Ab_PAYOFF_COLOR, 255);
        actualBaPayoff = new Line(0, 0, width, height, 0.5f, Ba_PAYOFF_COLOR, 255);
        actualBbPayoff = new Line(0, 0, width, height, 0.5f, Bb_PAYOFF_COLOR, 255);
        futureAaPayoff = new Line(0, 0, width, height, 0.5f, A_PAYOFF_COLOR, 255);
        futureAbPayoff = new Line(0, 0, width, height, 0.5f, A_PAYOFF_COLOR, 255);
        futureBaPayoff = new Line(0, 0, width, height, 0.5f, B_PAYOFF_COLOR, 255);
        futureBbPayoff = new Line(0, 0, width, height, 0.5f, B_PAYOFF_COLOR, 255);
        // RPSD
        actualRPayoff = new Line(0, 0, width, height, 1.5f, R_PAYOFF_COLOR, 255);
        actualPPayoff = new Line(0, 0, width, height, 1.5f, P_PAYOFF_COLOR, 255);
        actualSPayoff = new Line(0, 0, width, height, 1.5f, S_PAYOFF_COLOR, 255);
        futureDistance = 0.3f;
        //futureAPayoff.addPoint(0.0f, 50.0f);
        //futureAPayoff.addPoint(20.0f, 50.0f);
    }

    private void drawAxis(PApplet applet) {
        //applet.textAlign(PApplet.CENTER, PApplet.CENTER);
        //applet.textFont(small);
        applet.fill(255);
        applet.noStroke();
        applet.rect(-40, 0, 38, height);
        applet.rect(0, height + 2, width, 40);
        for (float x = 0.1f; x < 1.0f; x += 0.1f) {
            applet.noFill();
            applet.stroke(100, 100, 100);
            applet.strokeWeight(2);
            applet.line(x * width, height, x * width, height + 10);
            applet.fill(0);
            //applet.text(String.format("%d%%", (int) (x * 100)), x * width, height + 10 + applet.textAscent() + applet.textDescent());
        }
        for (float y = 0.1f; y < 1.0f; y += 0.1f) {
            applet.noFill();
            applet.stroke(100, 100, 100);
            applet.strokeWeight(2);
            applet.line(-10, y * height, 0, y * height);
            applet.fill(0);
            //applet.text(String.format("%.1f", (1 - y) * maxPayoff), -12 - applet.textWidth("  "), y * height);
        }
        //applet.textFont(font);
    }

    private void drawPercentLine(PApplet applet) {
        applet.strokeWeight(2f);
        applet.stroke(150, 150, 150);
        applet.line(currentPercent * width, 0, currentPercent * width, height);
    }

    @Override
    public void draw(PApplet applet) {
        applet.rectMode(PApplet.CORNER);
        applet.pushMatrix();
        applet.translate(origin.x, origin.y);
        applet.fill(255);
        applet.noStroke();
        float currX = currentPercent * width;
        applet.rect(currX - 8, 0, width, height);
        drawPercentLine(applet);
        applet.noFill();
        applet.stroke(0);
        applet.strokeWeight(2);
        applet.rect(0, 0, width, height);

        if(periodConfig != null){
            float futureA0 = periodConfig.payoffFunction.getPayoff(
                    currentPayoff,
                    new float[]{percent_A, percent_B},
                    new float[]{percent_a, percent_b});
            float futureA1 = periodConfig.payoffFunction.getPayoff(
                    currentPayoff + 0.1f,
                    new float[]{1, 0},
                    new float[]{percent_a, percent_b});
            applet.fill(0);
            applet.pushMatrix();
            applet.translate(50, 10);
            applet.text(String.format("%f", futureA0));
            applet.translate(0, 10);
            applet.text(String.format("%f", futureA1));
            applet.popMatrix();
            applet.noFill();

        }

        if (periodConfig != null) {
            if (periodConfig.payoffFunction instanceof TwoStrategyPayoffFunction) {
                actualAPayoff.draw(applet);
                actualBPayoff.draw(applet);
                futureAPayoff.draw(applet);
                futureBPayoff.draw(applet);
                actualAaPayoff.draw(applet);
                actualAbPayoff.draw(applet);
                actualBaPayoff.draw(applet);
                actualBbPayoff.draw(applet);
                futureAaPayoff.draw(applet);
                futureBbPayoff.draw(applet);
            } else if (periodConfig.payoffFunction instanceof ThreeStrategyPayoffFunction) {
                actualRPayoff.draw(applet);
                actualPPayoff.draw(applet);
                actualSPayoff.draw(applet);
            }
            actualPayoff.draw(applet);
        }
        //drawAxis(applet);
        applet.popMatrix();
    }

    public void clearAll() {
        actualPayoff.clear();
        actualAPayoff.clear();
        actualBPayoff.clear();
        futureAPayoff.clear();
        futureBPayoff.clear();
        actualAaPayoff.clear();
        actualBbPayoff.clear();
        futureAaPayoff.clear();
        futureBbPayoff.clear();
    }

    public void clearFuture() {
        futureAPayoff.clear();
        futureBPayoff.clear();
        futureAaPayoff.clear();
        futureAbPayoff.clear();
        futureBaPayoff.clear();
        futureBbPayoff.clear();
    }

    private void setupFuture() {
        if(periodConfig != null) {
            //if(periodConfig.payoffFunction instanceof TwoStrategyPayoffFunction) {
                if(futureAPayoff.points.size() != 0) {
                    futureAPayoff.removeFirst();
                    futureAPayoff.removeFirst();
                }
                float futureA0 = periodConfig.payoffFunction.getPayoff(
                        currentPayoff,
                        new float[]{1},
                        new float[]{percent_a});
                float futureA1 = periodConfig.payoffFunction.getPayoff(
                        currentPayoff + 0.1f,
                        new float[]{1},
                        new float[]{percent_a});
                addPoint(futureAPayoff, currentPercent, futureA0 );
                addPoint(futureAPayoff, currentPercent + 0.1f, futureA1);
           // }
        }
    }


    public void updateLines() {
        if (currentPercent < 1.0) {
            addPoint(actualPayoff, currentPercent, currentPayoff);
            if (periodConfig.payoffFunction instanceof TwoStrategyPayoffFunction) {
                addTwoStrategyActualPayoffPoints();
                setupFuture();
            } else if (periodConfig.payoffFunction instanceof ThreeStrategyPayoffFunction) {
                addThreeStrategyActualPayoffPoints();
            }
        }
    }

    private void addTwoStrategyActualPayoffPoints() {
        addPoint(actualAPayoff, currentPercent, currentAPayoff);
        addPoint(actualBPayoff, currentPercent, currentBPayoff);

        addPoint(actualAaPayoff, currentPercent, currentAaPayoff);
        addPoint(actualAbPayoff, currentPercent, currentAbPayoff);
        addPoint(actualBaPayoff, currentPercent, currentBaPayoff);
        addPoint(actualBbPayoff, currentPercent, currentBbPayoff);
    }

    private void addThreeStrategyActualPayoffPoints() {
        addPoint(actualRPayoff, currentPercent, currentRPayoff);
        addPoint(actualPPayoff, currentPercent, currentPPayoff);
        addPoint(actualSPayoff, currentPercent, currentSPayoff);
    }

    private void twoStrategyChanged() {
        currentPayoff = periodConfig.payoffFunction.getPayoff(
                currentPercent,
                new float[]{percent_A, percent_B},
                new float[]{percent_a, percent_b});
        currentAPayoff = periodConfig.payoffFunction.getPayoff(
                currentPercent,
                new float[]{1, 0},
                new float[]{percent_a, percent_b});
        currentBPayoff = periodConfig.payoffFunction.getPayoff(
                currentPercent,
                new float[]{0, 1},
                new float[]{percent_a, percent_b});
        currentAaPayoff = periodConfig.payoffFunction.getPayoff(currentPercent,
                new float[]{1, 0},
                new float[]{1, 0});
        currentAbPayoff = periodConfig.payoffFunction.getPayoff(currentPercent,
                new float[]{1, 0},
                new float[]{0, 1});
        currentBaPayoff = periodConfig.payoffFunction.getPayoff(currentPercent,
                new float[]{0, 1},
                new float[]{1, 0});
        currentBbPayoff = periodConfig.payoffFunction.getPayoff(currentPercent,
                new float[]{0, 1},
                new float[]{0, 1});
    }

    private void threeStrategyChanged() {
        // FIXME
    }

    private void strategyChanged() {
        if (periodConfig.payoffFunction instanceof TwoStrategyPayoffFunction) {
            twoStrategyChanged();
        } else if (periodConfig.payoffFunction instanceof ThreeStrategyPayoffFunction) {
            threeStrategyChanged();
        } else {
            assert false;
        }
    }

    public void setMyStrategy(float[] s) {
        if (periodConfig.payoffFunction instanceof TwoStrategyPayoffFunction) {
            percent_A = s[0];
            percent_B = 1 - percent_A;
        } else if (periodConfig.payoffFunction instanceof ThreeStrategyPayoffFunction) {
            // FIXME
        } else {
            assert false;
        }
        strategyChanged();
    }

    public void setOpponentStrategy(float[] s) {
        if (periodConfig.payoffFunction instanceof TwoStrategyPayoffFunction) {
            percent_a = s[0];
            percent_b = 1 - percent_a;
        } else if (periodConfig.payoffFunction instanceof ThreeStrategyPayoffFunction) {
            // FIXME
        } else {
            assert false;
        }
        strategyChanged();
    }

    public void setPeriodConfig(PeriodConfig periodConfig) {
        this.periodConfig = periodConfig;
        maxPayoff = periodConfig.payoffFunction.getMax();
    }

    public void addPoint(Line line, float x, float y) {
        line.addPoint(
                line.width * x,
                line.height * (1 - (y / maxPayoff)));
    }
}
