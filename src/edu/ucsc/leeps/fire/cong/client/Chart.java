/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.client;

import java.awt.Color;
import processing.core.PApplet;

/**
 *
 * @author jpettit
 */
public class Chart extends Sprite {

    final static Color MY_PAYOFF_COLOR = new Color(20, 200, 20);
    private boolean inTwoStrategyMode;
    // Two strategies
    final static Color A_PAYOFF_COLOR = new Color(200, 50, 50);
    final static Color B_PAYOFF_COLOR = new Color(50, 50, 200);
    // RPSD
    final static Color R_PAYOFF_COLOR = new Color(255, 25, 25);
    final static Color P_PAYOFF_COLOR = new Color(25, 25, 255);
    final static Color S_PAYOFF_COLOR = new Color(255, 0, 255);
    // Variables to modify that manipulate the chart
    public float currentPercent;
    public float maxPayoff;
    public float currentPayoff;
    // Two strategy
    public float currentAPayoff;
    public float currentBPayoff;
    public float currentAaPayoff;
    public float currentBbPayoff;
    public Line futureAPayoff;
    public Line futureBPayoff;
    // RPSD
    public float currentRPayoff;
    public float currentPPayoff;
    public float currentSPayoff;
    // Private controls accessed through public methods
    private Line actualPayoff;
    // Two strategy
    private Line actualAPayoff;
    private Line actualBPayoff;
    private Line actualAaPayoff;
    private Line actualBbPayoff;
    private Line futureAaPayoff;
    private Line futureBbPayoff;
    // RPSD
    private Line actualRPayoff;
    private Line actualPPayoff;
    private Line actualSPayoff;

    public Chart(int x, int y, int width, int height, boolean inTwoStrategyMode) {
        super(x, y, width, height);
        actualPayoff = new Line(0, 0, width, height, 1f, MY_PAYOFF_COLOR, 255);
        // Two strategy
        actualAPayoff = new Line(0, 0, width, height, 1.5f, A_PAYOFF_COLOR, 150);
        actualBPayoff = new Line(0, 0, width, height, 1.5f, B_PAYOFF_COLOR, 150);
        futureAPayoff = new Line(0, 0, width, height, 1.0f, A_PAYOFF_COLOR, 100);
        futureBPayoff = new Line(0, 0, width, height, 1.0f, B_PAYOFF_COLOR, 100);
        actualAaPayoff = new Line(0, 0, width, height, 0.5f, A_PAYOFF_COLOR, 255);
        actualBbPayoff = new Line(0, 0, width, height, 0.5f, B_PAYOFF_COLOR, 255);
        futureAaPayoff = new Line(0, 0, width, height, 0.5f, A_PAYOFF_COLOR, 255);
        futureBbPayoff = new Line(0, 0, width, height, 0.5f, B_PAYOFF_COLOR, 255);
        // RPSD
        actualRPayoff = new Line(0, 0, width, height, 1.5f, R_PAYOFF_COLOR, 255);
        actualPPayoff = new Line(0, 0, width, height, 1.5f, P_PAYOFF_COLOR, 255);
        actualSPayoff = new Line(0, 0, width, height, 1.5f, S_PAYOFF_COLOR, 255);
        this.inTwoStrategyMode = inTwoStrategyMode;
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
        if (inTwoStrategyMode) {
            actualAPayoff.draw(applet);
            actualBPayoff.draw(applet);
            futureAPayoff.draw(applet);
            futureBPayoff.draw(applet);
            actualAaPayoff.draw(applet);
            actualBbPayoff.draw(applet);
            futureAaPayoff.draw(applet);
            futureBbPayoff.draw(applet);
        } else {
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
            if (inTwoStrategyMode) {
                addTwoStrategyActualPayoffPoints();
            } else {
                addRPSDActualPayoffPoints();
            }
        }
    }

    public void addTwoStrategyActualPayoffPoints() {
        addPoint(actualPayoff, currentPercent, currentPayoff);
        addPoint(actualAPayoff, currentPercent, currentAPayoff);
        addPoint(actualBPayoff, currentPercent, currentBPayoff);

        addPoint(actualAaPayoff, currentPercent, currentAaPayoff);
        addPoint(actualBbPayoff, currentPercent, currentBbPayoff);
    }

    public void addRPSDActualPayoffPoints() {
        //addPoint(actualPayoff, currentPercent, currentPayoff);
        addPoint(actualRPayoff, currentPercent, currentRPayoff);
        addPoint(actualPPayoff, currentPercent, currentPPayoff);
        addPoint(actualSPayoff, currentPercent, currentSPayoff);
    }

    public void addPoint(Line line, float x, float y) {
        line.addPoint(
                line.width * x,
                line.height * (1 - (y / maxPayoff)));
    }
}
