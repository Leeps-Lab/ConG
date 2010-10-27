package edu.ucsc.leeps.fire.cong.client.gui;

import edu.ucsc.leeps.fire.config.Configurable;
import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.client.Client;
import edu.ucsc.leeps.fire.cong.client.StrategyChanger;
import edu.ucsc.leeps.fire.cong.client.StrategyChanger.Selector;
import edu.ucsc.leeps.fire.cong.config.Config;
import edu.ucsc.leeps.fire.cong.server.QWERTYPayoffFunction;

/**
 *
 * @author jpettit
 */
public class QWERTYStrategySelector extends Sprite implements Configurable<Config>, Selector {

    private RadioButtonGroup firmButtons;
    private float[] firm1 = new float[]{1};
    private float[] firm2 = new float[]{0};
    private Config config;
    private QWERTYPayoffFunction pf;

    public QWERTYStrategySelector(
            Sprite parent, int x, int y,
            int size,
            Client applet,
            StrategyChanger strategyChanger) {
        super(parent, x, y, size, size);
        firmButtons = new RadioButtonGroup(
                this, 200, 200, 200, 2,
                RadioButtonGroup.Alignment.Horizontal, 15, applet);
        firmButtons.setLabelMode(Marker.LabelMode.Bottom);
        firmButtons.setLabels(new String[]{"Firm 1", "Firm 2"});
        firmButtons.setEnabled(false);
        FIRE.client.addConfigListener(this);
    }

    public void setEnabled(boolean enabled) {
        firmButtons.setEnabled(enabled);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        firmButtons.setVisible(visible);
    }

    public float[] getTarget() {
        if (firmButtons.getSelection() == 0) {
            return firm1;
        }
        return firm2;
    }

    @Override
    public void draw(Client applet) {
        if (!visible) {
            return;
        }
        applet.pushMatrix();
        applet.translate(origin.x, origin.y);
        drawTable(applet);
        firmButtons.draw(applet);
        applet.popMatrix();
    }

    private void drawTable(Client applet) {
        float textWidth = applet.textWidth("00");
        float cellWidth = 11 + textWidth + 11;
        float cellHeight = 5 + textWidth + 5;
        applet.stroke(0);
        applet.strokeWeight(2);
        int cols = 3;
        int rows = 4;
        float tableWidth = cols * cellWidth;
        float tableHeight = rows * cellHeight;
        applet.pushMatrix();
        applet.translate(200 + tableWidth / 2f, 180 - tableHeight);
        for (int col = 1; col <= cols; col++) {
            applet.line(col * cellWidth, 0, col * cellWidth, cellHeight * rows);
        }
        for (int row = 1; row <= rows; row++) {
            applet.line(0, row * cellHeight, cellWidth * cols, row * cellHeight);
        }
        applet.textAlign(Client.CENTER, Client.CENTER);
        int numOwn = pf.getInSame(Client.state.id, Client.state.getMyStrategy(), Client.state.strategies);
        int numMatch = pf.getInSame(Client.state.id, Client.state.getMyStrategy(), Client.state.matchStrategies);
        for (int col = 0; col < cols; col++) {
            for (int row = 0; row < rows; row++) {
                if (col == 0 && row == 0) {
                    continue;
                }
                if (col == numOwn || row - 1 == numMatch) {
                    applet.fill(255, 169, 68, 100);
                    applet.rect(col * cellWidth, row * cellHeight, cellWidth, cellHeight);
                }
                applet.fill(0);
                if (col == 0) {
                    applet.text(row, Math.round(cellWidth / 2f), Math.round(row * cellHeight + cellHeight / 2f));
                } else if (row == 0) {
                    applet.text(col, Math.round(col * cellWidth + cellWidth / 2f), Math.round(cellHeight / 2f));
                } else {
                    float payoff = pf.payoffs[row - 1][col - 1];
                    String s = String.format("%.0f", payoff);
                    applet.text(s, Math.round(col * cellWidth + cellWidth / 2f), Math.round(row * cellHeight + cellHeight / 2f));
                }
            }
        }
        applet.popMatrix();
    }

    public void configChanged(Config config) {
        this.config = config;
        if (!config.mixedStrategySelection && !config.stripStrategySelection
                && config.payoffFunction instanceof QWERTYPayoffFunction) {
            this.pf = (QWERTYPayoffFunction) config.payoffFunction;
            setVisible(true);
        } else {
            setVisible(false);
        }
    }

    public void startPrePeriod() {
    }

    public void startPeriod() {
        if (Client.state.getMyStrategy()[0] == 1) {
            firmButtons.setSelection(0);
        } else {
            firmButtons.setSelection(1);
        }
    }

    public void update() {
    }
}
