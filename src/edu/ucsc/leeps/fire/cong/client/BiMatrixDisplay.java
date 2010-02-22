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
    private HeatmapHelper heatmap;
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
        heatmap = new HeatmapHelper(applet, width, height, 0xFF0000FF, 0xFFFFFF00, 0xFF00FF00);
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
            heatmap.setTwoStrategyPayoffFunction(payoffFunction);
            heatmap.updateTwoStrategyHeatmap(currentPercent);
        }
    }

    public void update() {
        if (visible) {
            heatmap.updateTwoStrategyHeatmap(currentPercent);
        }
    }

    private void drawOutline() {
        applet.fill(255);
        applet.noStroke();
        applet.rect(0, 0, width, height);
        applet.noFill();
        applet.stroke(0);
        applet.rect(0, 0, width, height);
    }

    private void drawHover() {
        if (applet.mousePressed && inRect(applet.mouseX, applet.mouseY)) {
            applet.noFill();
            applet.stroke(0);
            applet.strokeWeight(0.5f);
            applet.ellipse(origin.x + percent_a * width, applet.mouseY, 10, 10);
            applet.fill(0);
            float u = heatmap.getPayoff((int) (percent_a * width), (int) (applet.mouseY - origin.y));
            String label = String.format("%.0f", u);
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
        applet.stroke(0);
        applet.noFill();
        applet.line(0, (1 - percent_A) * height, width, (1 - percent_A) * height);
        applet.line(percent_a * width, 0, percent_a * width, height);
        applet.fill(0);
        applet.ellipse(percent_a * width, (1 - percent_A) * height, 10, 10);

    }

    @Override
    public void draw(PApplet applet) {
        if (!visible) {
            return;
        }
        applet.pushMatrix();
        applet.translate(origin.x, origin.y);

        drawOutline();

        applet.image(heatmap.getHeatmap(), 0, 0);

        drawCurrentStrategies();

        applet.popMatrix();

        drawHover();
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
