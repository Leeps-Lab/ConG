/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.ucsc.leeps.fire.cong.client.gui;

import edu.ucsc.leeps.fire.config.Configurable;
import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.client.Client;
import edu.ucsc.leeps.fire.cong.client.StrategyChanger;
import edu.ucsc.leeps.fire.cong.config.Config;
import edu.ucsc.leeps.fire.cong.config.StrategySelectionDisplayType;
import edu.ucsc.leeps.fire.cong.server.PayoffFunction;
import edu.ucsc.leeps.fire.cong.server.TwoStrategyPayoffFunction;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 *
 * @author swolpert
 */
public class OneStrategyStripSelector extends Sprite implements Configurable<Config>, MouseListener,
    KeyListener {

    private Client applet;
    private StrategyChanger strategyChanger;
    private float myStrat;
    private float opponentStrat;
    private boolean enabled;
    private HeatmapHelper heatmap;
    private Slider slider;
    private float currentPercent;
    private Config config;
    private PayoffFunction payoffFunction;
    private Marker currentPayoff;
    private Marker targetPayoff;
    private Marker BPayoff;
    private Marker APayoff;
    private Marker hover;

    public OneStrategyStripSelector(Sprite parent, int x, int y, int width, int height,
            Client applet, StrategyChanger strategyChanger) {
        super(parent, x, y, width, height);
        this.applet = applet;
        this.strategyChanger = strategyChanger;

        heatmap = new HeatmapHelper(this, 0, 0, width, height, true, applet);
        heatmap.setVisible(true);

        hover = new Marker(this, 0, 0, false, 7);

        if (width > height) {
            slider = new Slider(applet, this, Slider.Alignment.Horizontal, 0, width, height / 2f, Color.black, "A", 1f);
            currentPayoff = new Marker(this, 0, 0, true, 0);
            currentPayoff.setLabelMode(Marker.LabelMode.Top);
            targetPayoff = new Marker(this, 0, 0, true, 0);
            targetPayoff.setLabelMode(Marker.LabelMode.Top);
            BPayoff = new Marker(this, 0, 0, true, 0);
            BPayoff.setLabelMode(Marker.LabelMode.Top);
            APayoff = new Marker(this, width, 0, true, 0);
            APayoff.setLabelMode(Marker.LabelMode.Top);
            hover.setLabelMode(Marker.LabelMode.Top);
        } else {
            slider = new Slider(applet, this, Slider.Alignment.Vertical, 0, height, width / 2f, Color.black, "A", 1f);
            currentPayoff = new Marker(this, 0, 0, true, 0);
            currentPayoff.setLabelMode(Marker.LabelMode.Left);
            targetPayoff = new Marker(this, 0, 0, true, 0);
            targetPayoff.setLabelMode(Marker.LabelMode.Left);
            BPayoff = new Marker(this, 0, height, true, 0);
            BPayoff.setLabelMode(Marker.LabelMode.Left);
            APayoff = new Marker(this, 0, 0, true, 0);
            APayoff.setLabelMode(Marker.LabelMode.Left);
            hover.setLabelMode(Marker.LabelMode.Left);
        }
        slider.showGhost();

        applet.addMouseListener(this);
        FIRE.client.addConfigListener(this);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (!visible) {
            applet.removeMouseListener(this);
            applet.removeKeyListener(this);
        } else {
            applet.addMouseListener(this);
            applet.addKeyListener(this);
        }
    }

    public float[] getMyStrategy() {
        return new float[]{myStrat, 0};
    }

    public void setCurrentPercent(float percent) {
        currentPercent = percent;
    }

    public void setInitialStrategy(float A) {
        myStrat = A;
        slider.setStratValue(A);
        slider.setGhostValue(A);
    }

    public void setMyStrategy(float A) {
        myStrat = A;
        slider.setStratValue(A);
    }

    public void setCounterpartStrategy(float a) {
        opponentStrat = a;
    }

    public void update() {
        if (visible) {
            heatmap.updateStripHeatmap(currentPercent, opponentStrat);
        }
    }
    
    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        if (enabled && isHit(e.getX(), e.getY())) {
            slider.grabGhost();
            hover.setVisible(false);
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (enabled) {
            if (slider.isGhostGrabbed()) {
                slider.releaseGhost();
                strategyChanger.setTargetStrategy(new float[]{slider.getGhostValue(), 1 - slider.getGhostValue()});
            }
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        if (!enabled) {
            return;
        }

        if (e.isActionKey()) {
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                float targetStrat = strategyChanger.getTargetStrategy()[0];
                targetStrat += .01f;
                targetStrat = Client.constrain(targetStrat, 0, 1);
                slider.setGhostValue(targetStrat);
                strategyChanger.setTargetStrategy(new float[]{targetStrat, 1- targetStrat});
            } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                float targetStrat = strategyChanger.getTargetStrategy()[0];
                targetStrat -= .01f;
                targetStrat = Client.constrain(targetStrat, 0, 1);
                slider.setGhostValue(targetStrat);
                strategyChanger.setTargetStrategy(new float[]{targetStrat, 1 - targetStrat});
            }
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void draw(Client applet) {
        if (!visible) {
            return;
        }
        
        applet.pushMatrix();
        applet.translate(origin.x, origin.y);

        heatmap.draw(applet);
        
        if (enabled && slider.isGhostGrabbed()) {
            float mouseX = applet.mouseX - origin.x;
            float mouseY = applet.mouseY - origin.y;
            
            if (slider.getAlignment() == Slider.Alignment.Horizontal) {
                slider.moveGhost(mouseX);
            } else {
                slider.moveGhost(mouseY);
            }
        } else if (enabled && isHit(applet.mouseX, applet.mouseY) && heatmap.visible) {
            hover.setVisible(true);
            hover.update(applet.mouseX - origin.x, applet.mouseY - origin.y);
            float hoverA;
            if (width > height) {
                hoverA = (applet.mouseX - origin.x) / (float)width;
            } else {
                hoverA = 1 - (applet.mouseY - origin.y) / (float)height;
            }
            hover.setLabel(payoffFunction.getPayoff(currentPercent, new float[]{hoverA}, new float[]{opponentStrat}));
        } else {
            hover.setVisible(false);
        }

        updateLabels();
        APayoff.draw(applet);
        BPayoff.draw(applet);
        targetPayoff.draw(applet);
        currentPayoff.draw(applet);

        if (heatmap.visible) {
            applet.stroke(0);
            applet.strokeWeight(1);
            applet.line(0, 0, width, 0);
            applet.line(width, 0, width, height);
            applet.line(width, height, 0, height);
            applet.line(0, height, 0, 0);
        }

        slider.draw(applet);
        
        hover.draw(applet);

        applet.popMatrix();
    }

    public void configChanged(Config config) {
        this.config = config;

        if (config.mixedStrategySelection && config.stripStrategySelection &&
                config.payoffFunction instanceof TwoStrategyPayoffFunction) {
            payoffFunction = config.payoffFunction;
            setVisible(true);

            if (config.strategySelectionDisplayType == StrategySelectionDisplayType.HeatmapSingle) {
                setHeatmapMode(true);
            } else if (config.strategySelectionDisplayType == StrategySelectionDisplayType.Slider) {
                setHeatmapMode(false);
            }
        } else {
            setVisible(false);
        }
    }

    private void updateLabels() {
        float uA = payoffFunction.getPayoff(currentPercent, new float[]{1}, new float[]{opponentStrat});
        float uB = payoffFunction.getPayoff(currentPercent, new float[]{0}, new float[]{opponentStrat});
        float uCurrent = payoffFunction.getPayoff(currentPercent, new float[]{myStrat}, new float[]{opponentStrat});
        float uTarget = payoffFunction.getPayoff(currentPercent, new float[]{slider.getGhostValue()}, new float[]{opponentStrat});

        APayoff.setLabel(uA);
        BPayoff.setLabel(uB);
        currentPayoff.setLabel(uCurrent);
        targetPayoff.setLabel(uTarget);

        if (width > height) {
            currentPayoff.update(slider.getSliderPos(), 0);
            targetPayoff.update(slider.getGhostPos(), 0);
        } else {
            currentPayoff.update(0, slider.getSliderPos());
            targetPayoff.update(0, slider.getGhostPos());
        }
    }

    private void setHeatmapMode(boolean showHeatmap) {
        heatmap.setVisible(showHeatmap);
        currentPayoff.setVisible(showHeatmap);
        targetPayoff.setVisible(showHeatmap);
        APayoff.setVisible(showHeatmap);
        BPayoff.setVisible(showHeatmap);
    }
}
