/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.client;

import edu.ucsc.leeps.fire.cong.client.Client.PEmbed;
import edu.ucsc.leeps.fire.cong.config.PeriodConfig;
import edu.ucsc.leeps.fire.cong.config.TwoStrategySelectionType;
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
    private HeatmapHelper heatmap, counterpartHeatmap;
    private float currentPercent;
    private Marker myHeatmapAa;
    private Marker myHeatmapAb;
    private Marker myHeatmapBa;
    private Marker myHeatmapBb;
    private Marker counterpartHeatmapAa;
    private Marker cuonterpartHeatmapAb;
    private Marker counterpartHeatmapBa;
    private Marker counterpartHeatmapBb;
    private Marker matrixAa;
    private Marker matrixAb;
    private Marker matrixBa;
    private Marker matrixBb;
    private Marker current, planned, dragged, hover, counterpart;
    private long hoverTimestamp;
    private long hoverTimeMillis = 1000;

    public TwoStrategySelector(
            int x, int y,
            int matrixSize, int counterpartMatrixSize,
            PEmbed applet,
            ServerInterface server,
            ClientInterface client) {
        super(x, y, matrixSize, matrixSize);
        this.applet = applet;
        this.server = server;
        this.client = client;
        heatmap = new HeatmapHelper(
                0, 0, matrixSize, matrixSize,
                true,
                applet);
        counterpartHeatmap = new HeatmapHelper(
                0, -(counterpartMatrixSize + 30), counterpartMatrixSize, counterpartMatrixSize,
                false,
                applet);
        applet.addMouseListener(this);
        applet.addKeyListener(this);

        myHeatmapAa = new Marker(this, 10, 0, true, 0);
        myHeatmapAa.setLabelMode(Marker.LabelMode.Top);

        myHeatmapAb = new Marker(this, width, 0, true, 0);
        myHeatmapAb.setLabelMode(Marker.LabelMode.Top);

        myHeatmapBa = new Marker(this, 10, height, true, 0);
        myHeatmapBa.setLabelMode(Marker.LabelMode.Bottom);

        myHeatmapBb = new Marker(this, width, height, true, 0);
        myHeatmapBb.setLabelMode(Marker.LabelMode.Bottom);

        counterpartHeatmapAa = new Marker(this, counterpartHeatmap.origin.x + 10, counterpartHeatmap.origin.y, true, 0);
        counterpartHeatmapAa.setLabelMode(Marker.LabelMode.Top);

        cuonterpartHeatmapAb = new Marker(this, counterpartHeatmap.origin.x + counterpartHeatmap.width, counterpartHeatmap.origin.y, true, 0);
        cuonterpartHeatmapAb.setLabelMode(Marker.LabelMode.Top);

        counterpartHeatmapBa = new Marker(this, counterpartHeatmap.origin.x + 10, counterpartHeatmap.origin.y + counterpartHeatmap.height, true, 0);
        counterpartHeatmapBa.setLabelMode(Marker.LabelMode.Bottom);

        counterpartHeatmapBb = new Marker(this, counterpartHeatmap.origin.x + counterpartHeatmap.width, counterpartHeatmap.origin.y + counterpartHeatmap.height, true, 0);
        counterpartHeatmapBb.setLabelMode(Marker.LabelMode.Bottom);

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
        dragged = new Marker(this, 0, 0, false, 10);
        dragged.setLabelMode(Marker.LabelMode.Top);
        planned = new Marker(this, 0, 0, false, 10);
        planned.setLabelMode(Marker.LabelMode.Top);
        hover = new Marker(this, 0, 0, false, 10);
        hover.setLabelMode(Marker.LabelMode.Top);
        hoverTimestamp = System.currentTimeMillis();
        counterpart = new Marker(this, 0, 0, false, 10);
        counterpart.setLabelMode(Marker.LabelMode.Top);

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

    public void setCounterpartStrategy(float a) {
        percent_a = a;
    }

    public void setIsCounterpart(boolean isCounterpart) {
        heatmap.setIsCounterpart(isCounterpart);
        counterpartHeatmap.setIsCounterpart(isCounterpart);
    }

    public void update() {
        if (visible) {
            heatmap.updateTwoStrategyHeatmap(currentPercent);
            counterpartHeatmap.updateTwoStrategyHeatmap(currentPercent);
            updateLabels();
        }
    }

    private void updateLabels() {
        float myAa, counterAa, myAb, counterAb, myBa, counterBa, myBb, counterBb;
        myAa = periodConfig.payoffFunction.getPayoff(currentPercent, new float[]{1}, new float[]{1});
        counterAa = periodConfig.counterpartPayoffFunction.getPayoff(currentPercent, new float[]{1}, new float[]{1});
        myAb = periodConfig.payoffFunction.getPayoff(currentPercent, new float[]{1}, new float[]{0});
        counterAb = periodConfig.counterpartPayoffFunction.getPayoff(currentPercent, new float[]{0}, new float[]{1});
        myBa = periodConfig.payoffFunction.getPayoff(currentPercent, new float[]{0}, new float[]{1});
        counterBa = periodConfig.counterpartPayoffFunction.getPayoff(currentPercent, new float[]{1}, new float[]{0});
        myBb = periodConfig.payoffFunction.getPayoff(currentPercent, new float[]{0}, new float[]{0});
        counterBb = periodConfig.counterpartPayoffFunction.getPayoff(currentPercent, new float[]{0}, new float[]{0});

        myHeatmapAa.setLabel(myAa);
        myHeatmapAb.setLabel(myAb);
        myHeatmapBa.setLabel(myBa);
        myHeatmapBb.setLabel(myBb);

        counterpartHeatmapAa.setLabel(counterAa);
        cuonterpartHeatmapAb.setLabel(counterAb);
        counterpartHeatmapBa.setLabel(counterBa);
        counterpartHeatmapBb.setLabel(counterBb);

        matrixAa.setLabel(myAa, counterAa);
        matrixAb.setLabel(myAb, counterAb);
        matrixBa.setLabel(myBa, counterBa);
        matrixBb.setLabel(myBb, counterBb);
    }

    private void setModeMatrix() {
        heatmap.setVisible(false);
        counterpartHeatmap.setVisible(false);
        myHeatmapAa.setVisible(false);
        myHeatmapAb.setVisible(false);
        myHeatmapBa.setVisible(false);
        myHeatmapBb.setVisible(false);
        counterpartHeatmapAa.setVisible(false);
        cuonterpartHeatmapAb.setVisible(false);
        counterpartHeatmapBa.setVisible(false);
        counterpartHeatmapBb.setVisible(false);
        matrixAa.setVisible(true);
        matrixAb.setVisible(true);
        matrixBa.setVisible(true);
        matrixBb.setVisible(true);
    }

    private void setModeHeatmapSingle() {
        heatmap.setVisible(true);
        counterpartHeatmap.setVisible(false);
        myHeatmapAa.setVisible(true);
        myHeatmapAb.setVisible(true);
        myHeatmapBa.setVisible(true);
        myHeatmapBb.setVisible(true);
        counterpartHeatmapAa.setVisible(false);
        cuonterpartHeatmapAb.setVisible(false);
        counterpartHeatmapBa.setVisible(false);
        counterpartHeatmapBb.setVisible(false);
        matrixAa.setVisible(false);
        matrixAb.setVisible(false);
        matrixBa.setVisible(false);
        matrixBb.setVisible(false);
    }

    private void setModeHeatmapBoth() {
        heatmap.setVisible(true);
        counterpartHeatmap.setVisible(true);
        myHeatmapAa.setVisible(true);
        myHeatmapAb.setVisible(true);
        myHeatmapBa.setVisible(true);
        myHeatmapBb.setVisible(true);
        counterpartHeatmapAa.setVisible(true);
        cuonterpartHeatmapAb.setVisible(true);
        counterpartHeatmapBa.setVisible(true);
        counterpartHeatmapBb.setVisible(true);
        matrixAa.setVisible(false);
        matrixAb.setVisible(false);
        matrixBa.setVisible(false);
        matrixBb.setVisible(false);
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
            dragged.setVisible(true);
            dragged.update((1 - percent_a) * width, applet.mouseY - origin.y);
            float hoverPercentA = 1 - ((applet.mouseY - origin.y) / height);
            dragged.setLabel(periodConfig.payoffFunction.getPayoff(currentPercent, new float[]{hoverPercentA}, new float[]{percent_a}));
        } else {
            dragged.setVisible(false);
            dragged.update((1 - percent_a) * width, dragged.origin.y);
        }

        if (targetPercent_A != percent_A) {
            planned.setVisible(true);
            planned.update((1 - percent_a) * width, (1 - targetPercent_A) * height);
            planned.setLabel(periodConfig.payoffFunction.getPayoff(currentPercent, new float[]{targetPercent_A}, new float[]{percent_a}));
        } else {
            planned.setVisible(false);
        }

        current.draw(applet);
        dragged.draw(applet);
        planned.draw(applet);
        if (System.currentTimeMillis() - hoverTimestamp >= hoverTimeMillis) {
            float hoverPercentA = 1 - ((applet.mouseY - origin.y) / height);
            float hoverPercenta = 1 - ((applet.mouseX - origin.x) / height);
            hover.setLabel(periodConfig.payoffFunction.getPayoff(currentPercent, new float[]{hoverPercentA}, new float[]{hoverPercenta}));
            hover.update((1 - hoverPercenta) * width, (1 - hoverPercentA) * height);
            hover.setVisible(true);
            hover.draw(applet);
        }
        if (periodConfig.twoStrategySelectionType == TwoStrategySelectionType.HeatmapBoth) {
            counterpart.setVisible(true);
            counterpart.setLabel(periodConfig.counterpartPayoffFunction.getPayoff(
                    currentPercent, new float[]{percent_a}, new float[]{percent_A}));
            counterpart.update(
                    counterpartHeatmap.origin.x + (1 - percent_A) * counterpartHeatmap.width,
                    counterpartHeatmap.origin.y + (1 - percent_a) * counterpartHeatmap.height);
            counterpart.draw(applet);
        }
    }

    private void drawMatrix() {
        applet.line(width / 2, 0, width / 2, height);
        applet.line(0, height / 2, width, height / 2);

        matrixAa.draw(applet);
        matrixAb.draw(applet);
        matrixBa.draw(applet);
        matrixBb.draw(applet);
    }

    private void drawHeatmap() {
        heatmap.draw(applet);

        if (periodConfig.twoStrategySelectionType == TwoStrategySelectionType.HeatmapBoth) {
            counterpartHeatmap.draw(applet);

            float x, y, w, h;
            x = counterpartHeatmap.origin.x;
            y = counterpartHeatmap.origin.y;
            w = counterpartHeatmap.width;
            h = counterpartHeatmap.height;

            applet.stroke(0);
            applet.strokeWeight(2);
            applet.line(x + w * (1 - percent_A), y, x + w * (1 - percent_A), y + h);
            applet.line(x, y + h * (1 - percent_a), x + w, y + h * (1 - percent_a));
        }

        myHeatmapAa.draw(applet);
        myHeatmapAb.draw(applet);
        myHeatmapBa.draw(applet);
        myHeatmapBb.draw(applet);

        if (periodConfig.twoStrategySelectionType == TwoStrategySelectionType.HeatmapBoth) {
            counterpartHeatmapAa.draw(applet);
            cuonterpartHeatmapAb.draw(applet);
            counterpartHeatmapBa.draw(applet);
            counterpartHeatmapBb.draw(applet);
        }
    }

    @Override
    public void draw(PEmbed applet) {
        if (!visible) {
            return;
        }
        if (!inRect(applet.mouseX, applet.mouseY) || applet.pmouseX != applet.mouseX || applet.pmouseY != applet.mouseY) {
            hoverTimestamp = System.currentTimeMillis();
            hover.setVisible(false);
        }
        applet.pushMatrix();
        applet.translate(origin.x, origin.y);

        switch (periodConfig.twoStrategySelectionType) {
            case HeatmapSingle:
            case HeatmapBoth:
                drawHeatmap();
                break;
            case Matrix:
                drawMatrix();
        }

        drawStrategyInfo();

        applet.popMatrix();

        if (Client.DEBUG) {
            String changesPerSecondEMA = String.format("%.2f", thread.changeNanosEMA / 1000000f);
            applet.text(changesPerSecondEMA, origin.x + width - 100, origin.y + height + 10);
        }
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
            heatmap.setPeriodConfig(periodConfig);
            counterpartHeatmap.setPeriodConfig(periodConfig);
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

    public void setTwoStrategyHeatmapBuffers(float[][][] payoff, float[][][] counterpartPayoff) {
        heatmap.setTwoStrategyHeatmapBuffers(payoff);
        counterpartHeatmap.setTwoStrategyHeatmapBuffers(counterpartPayoff);
    }

    private class StrategyChangeThread extends Thread {

        public volatile boolean running = true;
        private final static long sleepTimeMillis = Client.QUICK_TICK_TIME;
        public float changeNanosEMA = 0;

        @Override
        public void run() {
            while (running) {
                if (enabled && visible) {
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
                        client.setMyStrategy(new float[]{percent_A});
                        long startTime = System.nanoTime();
                        server.strategyChanged(client.getID());
                        changeNanosEMA += 0.01f * ((System.nanoTime() - startTime) - changeNanosEMA);
                    }
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
