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
    private PayoffFunction payoffFucntion, counterpartPayoffFunction;

    public OneStrategyStripSelector(int x, int y, int width, int height,
            PEmbed applet, StrategyChanger strategyChanger) {
        super(x, y, width, height);
        this.applet = applet;
        this.strategyChanger = strategyChanger;

        heatmap = new HeatmapHelper(0, 0, width, height, true, applet);

        if (width > height) {
            slider = new Slider(applet, this, Slider.Alignment.Horizontal, 0, width, height / 2f, Color.black, "A", 1f);
        } else {
            slider = new Slider(applet, this, Slider.Alignment.Vertical, 0, height, width / 2f, Color.black, "A", 1f);
        }
        slider.showGhost();

        applet.addMouseListener(this);
        FIRE.client.addConfigListener(this);
    }
    
    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        float mouseX = e.getX() - origin.x;
        float mouseY = e.getY() - origin.y;

        if (slider.mouseOnGhost(mouseX, mouseY)) {
            slider.grabGhost();
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (slider.isGhostGrabbed()) {
            slider.releaseGhost();
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

        float mouseX = applet.mouseX - origin.x;
        float mouseY = applet.mouseY - origin.y;
        
        if (slider.isGhostGrabbed()) {
            if (slider.getAlignment() == Slider.Alignment.Horizontal) {
                slider.moveGhost(mouseX);
            } else {
                slider.moveGhost(mouseY);
            }
        }

        slider.draw(applet);

        applet.popMatrix();
    }

    public void configChanged(Config config) {
        this.config = config;

        if (config.mixedStrategySelection && config.stripStrategySelection &&
                config.payoffFunction instanceof TwoStrategyPayoffFunction) {
            setVisible(true);
        } else {
            setVisible(false);
        }
    }

}
