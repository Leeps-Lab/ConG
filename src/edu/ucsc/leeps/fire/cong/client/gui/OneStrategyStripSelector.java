/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.ucsc.leeps.fire.cong.client.gui;

import edu.ucsc.leeps.fire.config.Configurable;
import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.client.Client.PEmbed;
import edu.ucsc.leeps.fire.cong.client.StrategyChanger;
import edu.ucsc.leeps.fire.cong.config.Config;
import edu.ucsc.leeps.fire.cong.server.PayoffFunction;
import edu.ucsc.leeps.fire.cong.server.TwoStrategyPayoffFunction;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 *
 * @author swolpert
 */
public class OneStrategyStripSelector extends Sprite implements Configurable<Config>, MouseListener{

    private PEmbed applet;
    private StrategyChanger strategyChanger;
    private float myStrat;
    private float opponentStrat;
    private boolean enabled;
    private HeatmapHelper heatmap;
    private Slider slider;
    private float currentPercent;
    private Config config;
    private PayoffFunction payoffFunction, counterpartPayoffFunction;
    private Marker currentPayoff;
    private Marker targetPayoff;
    private Marker BPayoff;
    private Marker APayoff;

    public OneStrategyStripSelector(Sprite parent, int x, int y, int width, int height,
            PEmbed applet, StrategyChanger strategyChanger) {
        super(parent, x, y, width, height);
        this.applet = applet;
        this.strategyChanger = strategyChanger;

        heatmap = new HeatmapHelper(this, 0, 0, width, height, true, applet);
        heatmap.setVisible(true);

        if (width > height) {
            slider = new Slider(applet, this, Slider.Alignment.Horizontal, 0, width, height / 2f, Color.black, "A", 1f);
            currentPayoff = new Marker(this, 0, height, true, 0);
            currentPayoff.setLabelMode(Marker.LabelMode.Bottom);
            targetPayoff = new Marker(this, 0, height, true, 0);
            targetPayoff.setLabelMode(Marker.LabelMode.Bottom);
            BPayoff = new Marker(this, 0, 0, true, 0);
            BPayoff.setLabelMode(Marker.LabelMode.Top);
            APayoff = new Marker(this, width, 0, true, 0);
            APayoff.setLabelMode(Marker.LabelMode.Top);
        } else {
            slider = new Slider(applet, this, Slider.Alignment.Vertical, 0, height, width / 2f, Color.black, "A", 1f);
            currentPayoff = new Marker(this, width, 0, true, 0);
            currentPayoff.setLabelMode(Marker.LabelMode.Right);
            targetPayoff = new Marker(this, width, 0, true, 0);
            targetPayoff.setLabelMode(Marker.LabelMode.Right);
            BPayoff = new Marker(this, 0, height, true, 0);
            BPayoff.setLabelMode(Marker.LabelMode.Left);
            APayoff = new Marker(this, 0, 0, true, 0);
            APayoff.setLabelMode(Marker.LabelMode.Left);
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
        } else {
            applet.addMouseListener(this);
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
        if (enabled) {
            float mouseX = e.getX() - origin.x;
            float mouseY = e.getY() - origin.y;

            if (slider.mouseOnGhost(mouseX, mouseY)) {
                slider.grabGhost();
            }
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

    @Override
    public void draw(PEmbed applet) {
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
        }

        updateLabels();
        APayoff.draw(applet);
        BPayoff.draw(applet);
        targetPayoff.draw(applet);
        currentPayoff.draw(applet);

        applet.stroke(0);
        applet.strokeWeight(1);
        applet.line(0, 0, width, 0);
        applet.line(width, 0, width, height);
        applet.line(width, height, 0, height);
        applet.line(0, height, 0, 0);

        slider.draw(applet);

        applet.popMatrix();
    }

    public void configChanged(Config config) {
        this.config = config;

        if (config.mixedStrategySelection && config.stripStrategySelection &&
                config.payoffFunction instanceof TwoStrategyPayoffFunction) {
            payoffFunction = config.payoffFunction;
            counterpartPayoffFunction = config.counterpartPayoffFunction;
            setVisible(true);
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
            currentPayoff.update(slider.getSliderPos(), height);
            targetPayoff.update(slider.getGhostPos(), height);
        } else {
            currentPayoff.update(width, slider.getSliderPos());
            targetPayoff.update(width, slider.getGhostPos());
        }
    }
}
