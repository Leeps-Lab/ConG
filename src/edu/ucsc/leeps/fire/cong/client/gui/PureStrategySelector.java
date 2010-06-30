package edu.ucsc.leeps.fire.cong.client.gui;

import edu.ucsc.leeps.fire.config.Configurable;
import edu.ucsc.leeps.fire.cong.client.Client.PEmbed;
import edu.ucsc.leeps.fire.cong.client.StrategyChanger;
import edu.ucsc.leeps.fire.cong.config.Config;
import edu.ucsc.leeps.fire.cong.server.PayoffFunction;
import edu.ucsc.leeps.fire.cong.server.ThreeStrategyPayoffFunction;
import edu.ucsc.leeps.fire.cong.server.TwoStrategyPayoffFunction;

/**
 *
 * @author swolpert
 */
public class PureStrategySelector extends Sprite implements Configurable<Config> {
    private PEmbed applet;
    private Config config;
    private float currentPercent;
    private boolean enabled;
    private Marker matrixTopLeft;
    private Marker matrixTopRight;
    private Marker matrixBotLeft;
    private Marker matrixBotRight;
    private float matrixSideLength;
    private Marker[][] cellMarker;
    private RadioButtonGroup buttons;
    private PayoffFunction payoffFunction, counterpartPayoffFunction;
    private StrategyChanger strategyChanger;

    public PureStrategySelector (int x, int y, int size,
            PEmbed applet, StrategyChanger strategyChanger) {
        super(x, y, size, size);
        visible = false;
        enabled = false;
        this.applet = applet;
        
        matrixTopLeft = new Marker(this, width / 4, width / 8, true, 0);
        matrixTopRight = new Marker(this, width, width / 8, true, 0);
        matrixBotLeft = new Marker(this, width / 4, 7 * (width / 8), true, 0);
        matrixBotRight = new Marker(this, width, 7 * (width / 8), true, 0);

        matrixSideLength = matrixTopRight.origin.x - matrixTopLeft.origin.x;

        this.strategyChanger = strategyChanger;
    }
    @Override
    public void draw(PEmbed applet) {
        if (visible) {
            applet.line(matrixTopLeft.origin.x, matrixTopLeft.origin.y, matrixTopRight.origin.x, matrixTopRight.origin.y);
            applet.line(matrixTopRight.origin.x, matrixTopRight.origin.y, matrixBotRight.origin.x, matrixBotRight.origin.y);
            applet.line(matrixBotLeft.origin.x, matrixBotLeft.origin.y, matrixBotRight.origin.x, matrixBotRight.origin.y);
            applet.line(matrixTopLeft.origin.x, matrixTopLeft.origin.y, matrixBotLeft.origin.x, matrixBotLeft.origin.y);

            float midpointX = (matrixTopRight.origin.x + matrixTopLeft.origin.x) / 2f;
            float midpointY = (matrixBotLeft.origin.y + matrixTopLeft.origin.y) / 2f;
            applet.line(midpointX, matrixTopLeft.origin.y, midpointX, matrixBotLeft.origin.y);
            applet.line(matrixTopLeft.origin.x, midpointY, matrixTopRight.origin.x, midpointY);

            buttons.draw(applet);
        }
    }

    @Override
    public void setVisible(boolean isVisible) {
        visible = isVisible;
        buttons.setVisible(isVisible);
    }

    public void configChanged(Config config) {
        this.config = config;
        int numStrategies = 0;
        if (config.payoffFunction instanceof TwoStrategyPayoffFunction) {
            numStrategies = 2;
        } else if (config.payoffFunction instanceof ThreeStrategyPayoffFunction) {
            numStrategies = 3;
        }

        if (numStrategies != 0) {
            this.payoffFunction = config.payoffFunction;
            this.counterpartPayoffFunction = config.counterpartPayoffFunction;
            cellMarker = new Marker[numStrategies][numStrategies];
            float interval = matrixSideLength / (numStrategies * 2f);
            int offsetY = 1;
            for (int i = 0; i < numStrategies; ++i) {
                int offsetX = 1;
                for (int j = 0; j < numStrategies; ++j) {
                    cellMarker[i][j] = new Marker(this, matrixTopLeft.origin.x + (j + offsetX) * interval,
                            matrixTopLeft.origin.y + (i + offsetY) * interval, true, 0);
                    cellMarker[i][j].setLabelMode(Marker.LabelMode.Bottom);
                    ++offsetX;
                }
                ++offsetY;
            }
        }
        
        if (config.mixedStrategySelection) {
            setVisible(false);
        } else {
            setVisible(true);
        }
    }

    private void updateLabels() {
        
    }
}
