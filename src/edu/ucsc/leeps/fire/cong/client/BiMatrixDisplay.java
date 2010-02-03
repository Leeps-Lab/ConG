/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.client;

import edu.ucsc.leeps.fire.cong.server.HomotopyPayoffFunction;
import edu.ucsc.leeps.fire.cong.server.ServerInterface;
import edu.ucsc.leeps.fire.cong.server.TwoStrategyPayoffFunction;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import processing.core.PApplet;

/**
 *
 * @author jpettit
 */
public class BiMatrixDisplay extends Sprite implements MouseListener {

    private PApplet applet;
    private HomotopyPayoffFunction payoffFunction;
    private ServerInterface server;
    private ClientInterface client;
    public float percent_A, percent_a;
    private int lowColor, midColor, highColor;
    private float[][] heatmap;
    private boolean visible = false;
    public float currentPercent;

    public BiMatrixDisplay(
            int x, int y,
            PApplet applet,
            ServerInterface server,
            ClientInterface client) {
        super(x, y, 200, 200);
        this.applet = applet;
        this.server = server;
        this.client = client;
        lowColor = 0xFF0000FF;
        midColor = 0xFFFFFF00;
        highColor = 0xFF00FF00;
        heatmap = new float[width][height];
        applet.addMouseListener(this);
    }

    public void setPayoffFunction(TwoStrategyPayoffFunction payoffFunction) {
        if (payoffFunction == null
                || payoffFunction.getClass() != HomotopyPayoffFunction.class) {
            visible = false;
            return;
        } else {
            visible = true;
            this.payoffFunction = (HomotopyPayoffFunction) payoffFunction;
            updateHeatmap();
        }
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

    public void updateHeatmap() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float tmpA, tmpB, tmpa, tmpb;
                tmpA = 1 - (y / (float) height);
                tmpB = 1 - tmpA;
                tmpa = 1 - (x / (float) width);
                tmpb = 1 - tmpa;
                float u = payoffFunction.getPayoff(currentPercent, tmpA, tmpB, tmpa, tmpb);
                float heat = u / payoffFunction.getMax();
                heatmap[x][y] = heat;
            }
        }
    }

    private void drawOutline() {
        applet.pushMatrix();
        applet.translate(origin.x, origin.y);
        applet.fill(255);
        applet.noStroke();
        applet.rect(0, 0, width, height);
        applet.noFill();
        applet.stroke(0);
        applet.rect(0, 0, width, height);
        applet.popMatrix();
    }

    private void drawHeatmap() {
        int tx, ty;
        tx = Math.round(origin.x);
        ty = Math.round(origin.y);
        applet.loadPixels();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int color = getRGB(heatmap[x][y]);
                applet.pixels[(y + ty) * applet.width + (x + tx)] = color;
            }
        }
        applet.updatePixels();
    }

    private void drawHover() {
        if (applet.mousePressed && inRect(applet.mouseX, applet.mouseY)) {
            applet.noFill();
            applet.stroke(0);
            applet.strokeWeight(0.5f);
            applet.ellipse(origin.x + percent_a * width, applet.mouseY, 10, 10);
            applet.fill(0);
            float u = heatmap[(int) (percent_a * width)][applet.mouseY - (int) origin.y];
            String label = String.format("%.0f", payoffFunction.getMax() * u);
            float tW = applet.textWidth(label);
            float tH = applet.textAscent() + applet.textDescent();
            float tX = origin.x + percent_a * width;
            float tY = applet.mouseY;
            applet.fill(255);
            applet.rect(tX - tW, tY - tH, tW, tH);
            applet.fill(0);
            applet.text(label, tX, tY);
        }
    }

    private void drawCurrentStrategies() {
        applet.pushMatrix();
        applet.translate(origin.x, origin.y);
        applet.stroke(0);
        applet.noFill();
        applet.line(0, (1 - percent_A) * height, width, (1 - percent_A) * height);
        applet.line(percent_a * width, 0, percent_a * width, height);
        applet.fill(0);
        applet.ellipse(percent_a * width, (1 - percent_A) * height, 10, 10);
        applet.popMatrix();
    }

    @Override
    public void draw(PApplet applet) {
        if (!visible) {
            return;
        }

        drawOutline();

        drawHeatmap();

        drawHover();

        drawCurrentStrategies();
    }

    private boolean inRect(int x, int y) {
        return (x > origin.x && x < origin.x + width
                && y > origin.y && y < origin.y + height);
    }

    @Override
    public void mouseClicked(MouseEvent me) {
        int mouseX = me.getX();
        int mouseY = me.getY();
        if (inRect(mouseX, mouseY)) {
            percent_A = 1 - ((mouseY - origin.y) / height);
            server.strategyChanged(client.getFullName());
        }
    }

    @Override
    public void mousePressed(MouseEvent me) {
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        int mouseX = me.getX();
        int mouseY = me.getY();
        if (inRect(mouseX, mouseY)) {
            percent_A = 1 - ((mouseY - origin.y) / height);
            server.strategyChanged(client.getFullName());
        }
    }

    @Override
    public void mouseEntered(MouseEvent me) {
    }

    @Override
    public void mouseExited(MouseEvent me) {
    }
}
