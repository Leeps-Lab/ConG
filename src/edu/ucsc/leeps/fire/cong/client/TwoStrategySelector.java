/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.client;

import edu.ucsc.leeps.fire.cong.client.Client.PEmbed;
import edu.ucsc.leeps.fire.cong.config.PeriodConfig;
import edu.ucsc.leeps.fire.cong.server.ServerInterface;
import edu.ucsc.leeps.fire.cong.server.TwoStrategyPayoffFunction;
import edu.ucsc.leeps.fire.server.BasePeriodConfig;
import edu.ucsc.leeps.fire.server.PeriodConfigurable;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 *
 * @author jpettit
 */
public class TwoStrategySelector extends Sprite implements PeriodConfigurable, MouseListener, KeyListener {

    private PEmbed applet;
    private PeriodConfig periodConfig;
    private ServerInterface server;
    private ClientInterface client;
    private float percent_A, percent_a;
    private float targetPercent_A;
    private StrategyChangeThread thread;
    private boolean enabled;
    private HeatmapHelper heatmap;
    private float currentPercent;
    private Marker heatmapSingleAa;
    private Marker heatmapSingleAb;
    private Marker heatmapSingleBa;
    private Marker heatmapSingleBb;
    private Marker heatmapBothAa;
    private Marker heatmapBothAb;
    private Marker heatmapBothBa;
    private Marker heatmapBothBb;
    private Marker matrixAa;
    private Marker matrixAb;
    private Marker matrixBa;
    private Marker matrixBb;
    private Marker current, planned, hover;

    public TwoStrategySelector(
            int x, int y,
            int width, int height,
            PEmbed applet,
            ServerInterface server,
            ClientInterface client) {
        super(x, y, width, height);
        this.applet = applet;
        this.server = server;
        this.client = client;
        heatmap = new HeatmapHelper(applet, width, height, 0xFF0000FF, 0xFFFFFF00, 0xFF00FF00);
        applet.addMouseListener(this);
        applet.addKeyListener(this);

        heatmapSingleAa = new Marker(this, 10, 0, true, 0);
        heatmapSingleAa.setLabelMode(Marker.LabelMode.Top);

        heatmapSingleAb = new Marker(this, width, 0, true, 0);
        heatmapSingleAb.setLabelMode(Marker.LabelMode.Top);

        heatmapSingleBa = new Marker(this, 10, height, true, 0);
        heatmapSingleBa.setLabelMode(Marker.LabelMode.Bottom);

        heatmapSingleBb = new Marker(this, width, height, true, 0);
        heatmapSingleBb.setLabelMode(Marker.LabelMode.Bottom);

        heatmapBothAa = new Marker(this, 10, 0, true, 0);
        heatmapBothAa.setLabelMode(Marker.LabelMode.Top);

        heatmapBothAb = new Marker(this, width, 0, true, 0);
        heatmapBothAb.setLabelMode(Marker.LabelMode.Top);

        heatmapBothBa = new Marker(this, 10, height, true, 0);
        heatmapBothBa.setLabelMode(Marker.LabelMode.Bottom);

        heatmapBothBb = new Marker(this, width, height, true, 0);
        heatmapBothBb.setLabelMode(Marker.LabelMode.Bottom);

        matrixAa = new Marker(this, width / 4, height / 4, true, 0);
        matrixAa.setLabelMode(Marker.LabelMode.Top);

        matrixAb = new Marker(this, 3 * width / 4, height / 4, true, 0);
        matrixAb.setLabelMode(Marker.LabelMode.Top);

        matrixBa = new Marker(this, width / 4, 3 * height / 4, true, 0);
        matrixBa.setLabelMode(Marker.LabelMode.Bottom);

        matrixBb = new Marker(this, 3 * width / 4, 3 * height / 4, true, 0);
        matrixBb.setLabelMode(Marker.LabelMode.Bottom);

        current = new Marker(this, 0, 0, true, 10);
        current.setLabelMode(Marker.LabelMode.Top);
        hover = new Marker(this, 0, 0, false, 10);
        hover.setLabelMode(Marker.LabelMode.Top);
        planned = new Marker(this, 0, 0, false, 10);
        planned.setLabelMode(Marker.LabelMode.Top);

        targetPercent_A = -1;

        thread = new StrategyChangeThread();
        thread.start();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void reset() {
        targetPercent_A = -1;
    }

    public float[] getMyStrategy() {
        return new float[]{percent_A};
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

    public void update() {
        if (visible) {
            heatmap.updateTwoStrategyHeatmap(currentPercent);
            updateLabels();
        }
    }

    private void updateLabels() {
        float myAa, counterAa, myAb, counterAb, myBa, counterBa, myBb, counterBb;
        myAa = periodConfig.payoffFunction.getPayoff(currentPercent, new float[]{1}, new float[]{1});
        counterAa = periodConfig.counterpartPayoffFunction.getPayoff(currentPercent, new float[]{1}, new float[]{1});
        myAb = periodConfig.payoffFunction.getPayoff(currentPercent, new float[]{1}, new float[]{0});
        counterAb = periodConfig.counterpartPayoffFunction.getPayoff(currentPercent, new float[]{1}, new float[]{0});
        myBa = periodConfig.payoffFunction.getPayoff(currentPercent, new float[]{0}, new float[]{1});
        counterBa = periodConfig.counterpartPayoffFunction.getPayoff(currentPercent, new float[]{0}, new float[]{1});
        myBb = periodConfig.payoffFunction.getPayoff(currentPercent, new float[]{0}, new float[]{0});
        counterBb = periodConfig.counterpartPayoffFunction.getPayoff(currentPercent, new float[]{0}, new float[]{0});

        heatmapSingleAa.setLabel(myAa);
        heatmapSingleAb.setLabel(myAb);
        heatmapSingleBa.setLabel(myBa);
        heatmapSingleBb.setLabel(myBb);

        heatmapBothAa.setLabel(myAa, counterAa);
        heatmapBothAb.setLabel(myAb, counterAb);
        heatmapBothBa.setLabel(myBa, counterBa);
        heatmapBothBb.setLabel(myBb, counterBb);

        matrixAa.setLabel(myAa, counterAa);
        matrixAb.setLabel(myAb, counterAb);
        matrixBa.setLabel(myBa, counterBa);
        matrixBb.setLabel(myBb, counterBb);
    }

    private void setModeMatrix() {
        heatmap.setVisible(false);
        heatmapSingleAa.setVisible(false);
        heatmapSingleAb.setVisible(false);
        heatmapSingleBa.setVisible(false);
        heatmapSingleBb.setVisible(false);
        heatmapBothAa.setVisible(false);
        heatmapBothAb.setVisible(false);
        heatmapBothBa.setVisible(false);
        heatmapBothBb.setVisible(false);
        matrixAa.setVisible(true);
        matrixAb.setVisible(true);
        matrixBa.setVisible(true);
        matrixBb.setVisible(true);
    }

    private void setModeHeatmapSingle() {
        heatmap.setVisible(true);
        heatmapSingleAa.setVisible(true);
        heatmapSingleAb.setVisible(true);
        heatmapSingleBa.setVisible(true);
        heatmapSingleBb.setVisible(true);
        heatmapBothAa.setVisible(false);
        heatmapBothAb.setVisible(false);
        heatmapBothBa.setVisible(false);
        heatmapBothBb.setVisible(false);
        matrixAa.setVisible(false);
        matrixAb.setVisible(false);
        matrixBa.setVisible(false);
        matrixBb.setVisible(false);
    }

    private void setModeHeatmapBoth() {
        heatmap.setVisible(true);
        heatmapSingleAa.setVisible(false);
        heatmapSingleAb.setVisible(false);
        heatmapSingleBa.setVisible(false);
        heatmapSingleBb.setVisible(false);
        heatmapBothAa.setVisible(true);
        heatmapBothAb.setVisible(true);
        heatmapBothBa.setVisible(true);
        heatmapBothBb.setVisible(true);
        matrixAa.setVisible(false);
        matrixAb.setVisible(false);
        matrixBa.setVisible(false);
        matrixBb.setVisible(false);
    }

    private void drawOutline() {
        applet.fill(255);
        applet.noStroke();
        applet.rect(0, 0, width, height);
        applet.noFill();
        applet.stroke(0);
        applet.rect(0, 0, width, height);
    }

    private void drawStrategyInfo() {
        applet.stroke(0);
        applet.noFill();
        applet.line(0, (1 - percent_A) * height, width, (1 - percent_A) * height);
        applet.line((1 - percent_a) * width, 0, (1 - percent_a) * width, height);
        applet.fill(0);

        current.update((1 - percent_a) * width, (1 - percent_A) * height);
        current.setLabel(periodConfig.payoffFunction.getPayoff(currentPercent, new float[]{percent_A}, new float[]{percent_a}));

        if (applet.mousePressed && inRect((int) origin.x + width / 2, applet.mouseY)) {
            hover.setVisible(true);
            hover.update((1 - percent_a) * width, applet.mouseY - origin.y);
            float hoverPercentA = 1 - ((applet.mouseY - origin.y) / height);
            hover.setLabel(periodConfig.payoffFunction.getPayoff(currentPercent, new float[]{hoverPercentA}, new float[]{percent_a}));
        } else {
            hover.setVisible(false);
            hover.update((1 - percent_a) * width, hover.origin.y);
        }

        if (targetPercent_A != percent_A) {
            planned.setVisible(true);
            planned.update((1 - percent_a) * width, (1 - targetPercent_A) * height);
            planned.setLabel(periodConfig.payoffFunction.getPayoff(currentPercent, new float[]{targetPercent_A}, new float[]{percent_a}));
        } else {
            planned.setVisible(false);
        }

        current.draw(applet);
        hover.draw(applet);
        planned.draw(applet);
    }

    private void drawLabels() {
        heatmapSingleAa.draw(applet);
        heatmapSingleAb.draw(applet);
        heatmapSingleBa.draw(applet);
        heatmapSingleBb.draw(applet);
        heatmapBothAa.draw(applet);
        heatmapBothAb.draw(applet);
        heatmapBothBa.draw(applet);
        heatmapBothBb.draw(applet);
        matrixAa.draw(applet);
        matrixAb.draw(applet);
        matrixBa.draw(applet);
        matrixBb.draw(applet);
    }

    private void drawMatrix() {
        applet.line(width / 2, 0, width / 2, height);
        applet.line(0, height / 2, width, height / 2);
    }

    @Override
    public void draw(PEmbed applet) {
        if (!visible) {
            return;
        }
        applet.pushMatrix();
        applet.translate(origin.x, origin.y);

        drawOutline();

        switch (periodConfig.twoStrategySelectionType) {
            case HeatmapSingle:
            case HeatmapBoth:
                applet.image(heatmap.getHeatmap(), 0, 0);
                break;
            case Matrix:
                drawMatrix();
        }

        drawStrategyInfo();

        drawLabels();

        applet.popMatrix();
    }

    private boolean inRect(int x, int y) {
        return (x > origin.x && x < origin.x + width
                && y > origin.y && y < origin.y + height);
    }

    public void mouseClicked(MouseEvent me) {
        if (!enabled) {
            return;
        }
        int mouseX = me.getX();
        int mouseY = me.getY();
        if (inRect(mouseX, mouseY)) {
            targetPercent_A = 1 - ((mouseY - origin.y) / height);
        }
    }

    public void mousePressed(MouseEvent me) {
    }

    public void mouseReleased(MouseEvent me) {
        if (!enabled) {
            return;
        }
        int mouseX = me.getX();
        int mouseY = me.getY();
        if (inRect(mouseX, mouseY)) {
            targetPercent_A = 1 - ((mouseY - origin.y) / height);
        }
    }

    public void mouseEntered(MouseEvent me) {
    }

    public void mouseExited(MouseEvent me) {
    }

    public void setPeriodConfig(BasePeriodConfig basePeriodConfig) {
        periodConfig = (PeriodConfig) basePeriodConfig;
        if (periodConfig.payoffFunction instanceof TwoStrategyPayoffFunction) {
            heatmap.setPeriodConfig(basePeriodConfig);
            setVisible(true);
            switch (periodConfig.twoStrategySelectionType) {
                case Matrix:
                    setModeMatrix();
                    break;
                case HeatmapSingle:
                    setModeHeatmapSingle();
                    break;
                case HeatmapBoth:
                    setModeHeatmapBoth();
                    break;
            }
        } else {
            setVisible(false);
        }
    }

    public void keyTyped(KeyEvent ke) {
    }

    public void keyPressed(KeyEvent ke) {
        if (!enabled) {
            return;
        }
        if (ke.isActionKey()) {
            if (ke.getKeyCode() == KeyEvent.VK_UP) {
                targetPercent_A += 0.01;
                targetPercent_A = PEmbed.constrain(targetPercent_A, 0, 1);
            } else if (ke.getKeyCode() == KeyEvent.VK_DOWN) {
                targetPercent_A -= 0.01;
                targetPercent_A = PEmbed.constrain(targetPercent_A, 0, 1);
            }
        }
    }

    public void keyReleased(KeyEvent ke) {
    }

    private class StrategyChangeThread extends Thread {

        public volatile boolean running = true;
        private final static long sleepTimeMillis = 50;

        @Override
        public void run() {
            while (running) {
                if (enabled) {
                    if (targetPercent_A != -1 && targetPercent_A != percent_A) {
                        if (periodConfig.percentChangePerSecond == -1) {
                            percent_A = targetPercent_A;
                        } else {
                            float percentChangePerTick = periodConfig.percentChangePerSecond * (sleepTimeMillis / 1000f);
                            if (targetPercent_A > percent_A) {
                                percent_A += percentChangePerTick;
                            } else {
                                percent_A -= percentChangePerTick;
                            }
                            if (Math.abs(percent_A - targetPercent_A) < percentChangePerTick) {
                                percent_A = targetPercent_A;
                            }
                        }
                        server.strategyChanged(client.getID());
                    }
                    try {
                        sleep(sleepTimeMillis);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
}
