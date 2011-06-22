package edu.ucsc.leeps.fire.cong.client.gui;

import edu.ucsc.leeps.fire.config.Configurable;
import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.client.Client;
import edu.ucsc.leeps.fire.cong.client.StrategyChanger.Selector;
import edu.ucsc.leeps.fire.cong.config.Config;
import edu.ucsc.leeps.fire.cong.server.QWERTYPayoffFunction;

/**
 *
 * @author jpettit
 */
public class QWERTYStrategySelector extends Sprite implements Configurable<Config>, Selector {

    private RadioButtonGroup firmButtons;
    private QWERTYPayoffFunction pf;

    public QWERTYStrategySelector(
            Sprite parent, int x, int y,
            int size,
            Client applet) {
        super(parent, x, y, size, size);
        firmButtons = new RadioButtonGroup(
                this, 100, 300, 200, 2,
                RadioButtonGroup.Alignment.Horizontal, 15, applet);
        firmButtons.setLabelMode(Marker.LabelMode.Bottom);
        firmButtons.setLabels(new String[]{"Firm A", "Firm B"});
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

        if (firmButtons.getSelection() == 0) {
            Client.state.target[0] = 1;
        } else {
            Client.state.target[0] = 0;
        }
    }

    private void drawTable(Client applet) {
        float textWidth = applet.textWidth("00, 00");
        float cellWidth = 11 + textWidth + 11;
        float cellHeight = 5 + textWidth + 5;
        applet.stroke(0);
        applet.strokeWeight(2);
        int cols = 3;
        int rows = 4;
        float tableWidth = cols * cellWidth;
        float tableHeight = rows * cellHeight;
        applet.pushMatrix();
        applet.translate(50 + tableWidth / 2f, 240 - tableHeight);
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
                    applet.text(row - 1, Math.round(cellWidth / 2f), Math.round(row * cellHeight + cellHeight / 2f));
                } else if (row == 0) {
                    applet.text(col, Math.round(col * cellWidth + cellWidth / 2f), Math.round(cellHeight / 2f));
                } else {
                    float[] payoffs = {pf.payoffs[0][row - 1][col - 1], pf.payoffs[1][row - 1][col - 1]};
                    String s = String.format("%.0f, %.0f", payoffs[0], payoffs[1]);
                    applet.text(s, Math.round(col * cellWidth + cellWidth / 2f), Math.round(row * cellHeight + cellHeight / 2f));
                }
            }
        }
        textWidth = applet.textWidth("AAAAA");
        float textHeight = applet.textAscent() + applet.textDescent() + 4;
        applet.text("Number of players", Math.round(2 * cellWidth), -2 * textHeight + 5);
        applet.text("in your firm", Math.round(2 * cellWidth), -1 * textHeight + 5);
        applet.text("Number of", -1 * textWidth - 5, Math.round(2.5 * cellHeight - 1.5 * textHeight));
        applet.text("players in", -1 * textWidth - 5, Math.round(2.5 * cellHeight - .5 * textHeight));
        applet.text("the other", -1 * textWidth - 5, Math.round(2.5 * cellHeight + .5 * textHeight));
        applet.text("firm", -1 * textWidth - 5, Math.round(2.5 * cellHeight + 1.5 * textHeight));
        applet.popMatrix();
    }

    public void configChanged(Config config) {
        if (config.selector == Config.StrategySelector.qwerty) {
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

    public void endSubperiod(int subperiod) {
    }
}
