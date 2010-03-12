/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.client;

import edu.ucsc.leeps.fire.cong.server.PayoffFunction;
import edu.ucsc.leeps.fire.cong.config.PeriodConfig;
import edu.ucsc.leeps.fire.cong.server.ServerInterface;
import edu.ucsc.leeps.fire.cong.server.TwoStrategyPayoffFunction;
import edu.ucsc.leeps.fire.server.BasePeriodConfig;
import edu.ucsc.leeps.fire.server.PeriodConfigurable;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import processing.core.PApplet;

/**
 *
 * @author jpettit
 */
public class TwoStrategySelector extends Sprite implements PeriodConfigurable, MouseListener {

    private PApplet applet;
    private PayoffFunction payoffFunction;
    private ServerInterface server;
    private ClientInterface client;
    private float percent_A, percent_a;
    private HeatmapHelper heatmap;
    private boolean visible = false;
    private float currentPercent;
    private Marker[] labels;

    public TwoStrategySelector(
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

        labels = new Marker[4];

        labels[0] = new Marker(10, 0, true, 1);
        labels[0].setColor(Color.black);
        labels[0].setLabel("(1, 1)");
        labels[0].setLabelMode(Marker.TOP);

        labels[1] = new Marker(width, 0, true, 1);
        labels[1].setColor(Color.black);
        labels[1].setLabel("(1, 0)");
        labels[1].setLabelMode(Marker.TOP);

        labels[2] = new Marker(10, height, true, 1);
        labels[2].setColor(Color.black);
        labels[2].setLabel("(0, 1)");
        labels[2].setLabelMode(Marker.BOTTOM);

        labels[3] = new Marker(width, height, true, 1);
        labels[3].setColor(Color.black);
        labels[3].setLabel("(0, 0)");
        labels[3].setLabelMode(Marker.BOTTOM);
    }

    public float[] getMyStrategy() {
        return new float[]{percent_A};
    }

    public void setPayoffFunction(PayoffFunction payoffFunction) {
        this.payoffFunction = payoffFunction;
        heatmap.setPayoffFunction(payoffFunction);
        heatmap.updateTwoStrategyHeatmap(currentPercent);
    }

    public void setCurrentPercent(float percent) {
        currentPercent = percent;
    }

    public void setMyStrategy(float A) {
        percent_A = A;
    }

    public void setOpponentStrategy(float a) {
        percent_a = a;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void update() {
        if (visible) {
            heatmap.updateTwoStrategyHeatmap(currentPercent);

            float payoff = payoffFunction.getPayoff(currentPercent,
                    new float[]{1}, new float[]{1});
            labels[0].setLabel(payoff);

            payoff = payoffFunction.getPayoff(currentPercent,
                    new float[]{1}, new float[]{0});
            labels[1].setLabel(payoff);

            payoff = payoffFunction.getPayoff(currentPercent,
                    new float[]{0}, new float[]{1});
            labels[2].setLabel(payoff);

            payoff = payoffFunction.getPayoff(currentPercent,
                    new float[]{0}, new float[]{0});
            labels[3].setLabel(payoff);
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
        if (applet.mousePressed && inRect((int) origin.x + width / 2, applet.mouseY)) {
            applet.fill(0, 0, 0, 100);
            applet.stroke(0);
            applet.strokeWeight(0.5f);
            applet.ellipse(origin.x + (1 - percent_a) * width, applet.mouseY, 10, 10);
            /*
            float u = heatmap.getPayoff((int) ((1 - percent_a) * width), (int) (applet.mouseY - origin.y));
            String label = String.format("%.0f", u);
            float tW = applet.textWidth(label);
            float tH = applet.textAscent() + applet.textDescent();
            float tX = origin.x + (1 - percent_a) * width;
            float tY = applet.mouseY;
            applet.fill(255);
            applet.rect(tX - tW, tY - tH, tW, tH);
            applet.fill(0);
            applet.text(label, tX, tY);
             */
        }
    }

    private void drawCurrentStrategies() {
        applet.stroke(0);
        applet.noFill();
        applet.line(0, (1 - percent_A) * height, width, (1 - percent_A) * height);
        applet.line((1 - percent_a) * width, 0, (1 - percent_a) * width, height);
        applet.fill(0);
        applet.ellipse((1 - percent_a) * width, (1 - percent_A) * height, 10, 10);

    }

    private void drawLabels() {
        for (int i = 0; i < 4; ++i) {
            labels[i].draw(applet);
        }
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

        drawLabels();

        applet.popMatrix();

        drawHover();
    }

    private boolean inRect(int x, int y) {
        return (x > origin.x && x < origin.x + width
                && y > origin.y && y < origin.y + height);
    }

    //@Override
    public void mouseClicked(MouseEvent me) {
        int mouseX = me.getX();
        int mouseY = me.getY();
        if (inRect(mouseX, mouseY)) {
            percent_A = 1 - ((mouseY - origin.y) / height);
            server.strategyChanged(client.getFullName());
        }
    }

    //@Override
    public void mousePressed(MouseEvent me) {
    }

    //@Override
    public void mouseReleased(MouseEvent me) {
        int mouseX = me.getX();
        int mouseY = me.getY();
        if (inRect(mouseX, mouseY)) {
            percent_A = 1 - ((mouseY - origin.y) / height);
            server.strategyChanged(client.getFullName());
        }
    }

    //@Override
    public void mouseEntered(MouseEvent me) {
    }

    //@Override
    public void mouseExited(MouseEvent me) {
    }

    public void setPeriodConfig(BasePeriodConfig basePeriodConfig) {
        PeriodConfig periodConfig = (PeriodConfig)basePeriodConfig;
        if (periodConfig.payoffFunction instanceof TwoStrategyPayoffFunction) {
            setPayoffFunction(periodConfig.payoffFunction);
            setVisible(true);
        } else {
            setVisible(false);
        }
    }
}
