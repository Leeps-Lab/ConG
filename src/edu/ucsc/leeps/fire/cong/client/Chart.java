/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.client;

import edu.ucsc.leeps.fire.cong.server.RPSPayoffFunction;
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
    final static Color Ab_PAYOFF_COLOR = new Color(50, 50, 200);
    final static Color Ba_PAYOFF_COLOR = new Color(200, 50, 50);
    final static Color Bb_PAYOFF_COLOR = new Color(50, 50, 200);
    // RPSD
    final static Color R_PAYOFF_COLOR = new Color(255, 25, 25);
    final static Color P_PAYOFF_COLOR = new Color(25, 25, 255);
    final static Color S_PAYOFF_COLOR = new Color(255, 0, 255);
    // Variables to modify that manipulate the chart
    public float currentPercent;
    public float maxPayoff;
    public float currentPayoff;
    // Two strategy
    private float percent_A;
    private float percent_B;
    private float percent_a;
    private float percent_b;
    private TwoStrategyPayoffFunction twoStrategyPayoffFunction;
    private float currentAPayoff;
    private float currentBPayoff;
    private float currentAaPayoff;
    private float currentAbPayoff;
    private float currentBaPayoff;
    private float currentBbPayoff;
    private Line futureAPayoff;
    private Line futureBPayoff;
    // RPSD
    private RPSPayoffFunction RPSPayoffFunction;
    public float currentRPayoff;
    public float currentPPayoff;
    public float currentSPayoff;
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
        futureAPayoff = new Line(0, 0, width, height, 1.0f, A_PAYOFF_COLOR, 100);
        futureBPayoff = new Line(0, 0, width, height, 1.0f, B_PAYOFF_COLOR, 100);
        actualAaPayoff = new Line(0, 0, width, height, 0.5f, Aa_PAYOFF_COLOR, 255);
        actualAbPayoff = new Line(0, 0, width, height, 0.5f, Ab_PAYOFF_COLOR, 255);
        actualBaPayoff = new Line(0, 0, width, height, 0.5f, Ba_PAYOFF_COLOR, 255);
        actualBbPayoff = new Line(0, 0, width, height, 0.5f, Bb_PAYOFF_COLOR, 255);
        futureAaPayoff = new Line(0, 0, width, height, 0.5f, A_PAYOFF_COLOR, 255);
        futureBbPayoff = new Line(0, 0, width, height, 0.5f, B_PAYOFF_COLOR, 255);
        // RPSD
        actualRPayoff = new Line(0, 0, width, height, 1.5f, R_PAYOFF_COLOR, 255);
        actualPPayoff = new Line(0, 0, width, height, 1.5f, P_PAYOFF_COLOR, 255);
        actualSPayoff = new Line(0, 0, width, height, 1.5f, S_PAYOFF_COLOR, 255);
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
        if (twoStrategyPayoffFunction != null) {
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
        } else if (RPSPayoffFunction != null) {
            actualRPayoff.draw(applet);
            actualPPayoff.draw(applet);
            actualSPayoff.draw(applet);
        }
        actualPayoff.draw(applet);
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
        futureBbPayoff.clear();
    }

    public void updateLines() {
        if (currentPercent < 1.0) {
            addPoint(actualPayoff, currentPercent, currentPayoff);
            if (twoStrategyPayoffFunction != null) {
                addTwoStrategyActualPayoffPoints();
            } else if (RPSPayoffFunction != null) {
                addRPSDActualPayoffPoints();
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

    private void addRPSDActualPayoffPoints() {
        addPoint(actualRPayoff, currentPercent, currentRPayoff);
        addPoint(actualPPayoff, currentPercent, currentPPayoff);
        addPoint(actualSPayoff, currentPercent, currentSPayoff);
    }

    public void updateStrategyAB(float A, float B, float a, float b) {
        percent_A = A;
        percent_B = B;
        percent_a = a;
        percent_b = b;
        if (twoStrategyPayoffFunction != null) {
            currentPayoff = twoStrategyPayoffFunction.getPayoff(currentPercent, A, B, a, b);
            currentAPayoff = a;
            currentBPayoff = b;
            currentAaPayoff = twoStrategyPayoffFunction.getPayoff(currentPercent, 1, 0, 1, 0);
            currentAbPayoff = twoStrategyPayoffFunction.getPayoff(currentPercent, 1, 0, 0, 1);
            currentBaPayoff = twoStrategyPayoffFunction.getPayoff(currentPercent, 0, 1, 1, 0);
            currentBbPayoff = twoStrategyPayoffFunction.getPayoff(currentPercent, 0, 1, 0, 1);
        }
    }

    public void setTwoStrategyPayoffFunction(TwoStrategyPayoffFunction twoStrategyPayoffFunction) {
        this.twoStrategyPayoffFunction = twoStrategyPayoffFunction;
    }

    public void setRPSPayoffFunction(RPSPayoffFunction RPSPayoffFunction) {
        this.RPSPayoffFunction = RPSPayoffFunction;
    }

    public void addPoint(Line line, float x, float y) {
        line.addPoint(
                line.width * x,
                line.height * (1 - (y / maxPayoff)));
    }
}
