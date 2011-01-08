/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.ucsc.leeps.fire.cong.client.gui;

import edu.ucsc.leeps.fire.config.Configurable;
import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.client.Client;
import edu.ucsc.leeps.fire.cong.client.StrategyChanger.Selector;
import edu.ucsc.leeps.fire.cong.config.Config;
import edu.ucsc.leeps.fire.cong.server.CournotPayoffFunction;
import edu.ucsc.leeps.fire.cong.server.PayoffFunction;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 *
 * @author rlou
 */
public class CournotSelector extends Sprite implements Configurable<Config>, Selector, MouseListener, KeyListener {

    private Client applet;
    private boolean enabled;
    private Slider slider;
    Config config;

    public CournotSelector(Sprite parent, int x, int y, int width, int height,
            Client applet) {
        super(parent, x, y, width, height);
        x += (int)(.05 * width);
        width = (int)(0.89 * width);
        slider = new Slider(applet, Slider.Alignment.Horizontal, 0, width, height / 2f, Color.black, "A", 1f);
        slider.showGhost();
        this.applet = applet;
        FIRE.client.addConfigListener(this);
        applet.addMouseListener(this);
        applet.addKeyListener(this);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        slider.setVisible(visible);
    }

    @Override
    public void draw(Client applet) {
        if (!visible) {
            return;
        }

        slider.setStratValue(Client.state.getMyStrategy()[0]);
        if (Client.state.target != null)
            slider.setGhostValue(Client.state.target[0]);

        if (enabled && slider.isGhostGrabbed()) {
            float mouseX = applet.mouseX - origin.x;
            float mouseY = applet.mouseY - origin.y;
            slider.moveGhost(mouseX);
            float newTarget = slider.getGhostValue();
            float newPrice =
                    (newTarget * FIRE.client.getConfig().payoffFunction.getMax()) - FIRE.client.getConfig().payoffFunction.getMin();
            if (newPrice < FIRE.client.getConfig().marginalCost) {
                float marginalCostTarget = FIRE.client.getConfig().marginalCost / (FIRE.client.getConfig().payoffFunction.getMax() - FIRE.client.getConfig().payoffFunction.getMin());
                slider.setGhostValue(marginalCostTarget);
                Client.state.target[0] = marginalCostTarget;
            } else {
                Client.state.target[0] = newTarget;
            }
        }

        applet.pushMatrix();
        applet.translate(origin.x, origin.y);

        slider.draw(applet);

        int i = 1;
        for (int id : Client.state.strategies.keySet()) {
            Color color;
            if (id == FIRE.client.getID())
               color = Config.colors[0];
            else {
                color = Config.colors[i];
                i++;
            }
            drawTickMark(applet, color, id);
        }

        applet.popMatrix();

    }

    private void drawTickMark(Client applet, Color color, int id) {
        float x, y, min, max;
        min = config.payoffFunction.getMin();
        max = config.payoffFunction.getMax();
        x = width * Client.state.strategies.get(id)[0];
        y = height * (1 -
                (PayoffFunction.Utilities.getPayoff(Client.state.strategies.get(id))
                - min) / (max - min));
        applet.stroke(color.getRed(), color.getGreen(), color.getBlue());
        applet.strokeWeight(3);
        if (id != FIRE.client.getID())
           applet.line(x, height - 5, x, height + 5);
        
    }

    public void configChanged(Config config) {
        this.config = config;
        if (config.mixedStrategySelection && !config.stripStrategySelection
                && config.payoffFunction instanceof CournotPayoffFunction) {
            setVisible(true);
            slider = new Slider(applet, Slider.Alignment.Horizontal,
                    0, width, height, Color.black, "", 1f);
            slider.showGhost();
            slider.setVisible(true);
        } else {
            setVisible(false);
        }
    }

    public void startPrePeriod() {
    }

    public void startPeriod() {
        slider.setStratValue(Client.state.getMyStrategy()[0]);
        slider.setGhostValue(slider.getStratValue());
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        if (enabled) {
            slider.grabGhost();
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (enabled) {
            if (slider.isGhostGrabbed()) {
                slider.releaseGhost();
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
        if (!visible || !enabled) {
            return;
        }
        if (e.isActionKey()) {
            float newTarget = slider.getGhostValue();
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                newTarget += .01f;
            } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                newTarget -= .01f;
            }
            newTarget = Client.constrain(newTarget, 0, 1);
       }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void endSubperiod(int subperiod) {
    }

}
