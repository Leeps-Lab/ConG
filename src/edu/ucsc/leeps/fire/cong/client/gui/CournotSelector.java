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
        slider = new Slider(applet, Slider.Alignment.Horizontal,
                0, this.width, this.height, Color.black, "", 1f);
        slider.setShowStrategyLabel(false);
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
        slider.sliderStart = 0;
        slider.sliderEnd = width;
        slider.length = width;

        slider.setStratValue(Client.state.getMyStrategy()[0]);
        if (Client.state.target != null) {
            slider.setGhostValue(Client.state.target[0]);
        }

        if (enabled && slider.isGhostGrabbed()) {
            float mouseX = applet.mouseX - origin.x;
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

        drawAxis(applet);

        slider.draw(applet);

        int i = 1;
        for (int id : Client.state.strategies.keySet()) {
            Color color;
            if (id == FIRE.client.getID()) {
                color = Config.colors[0];
            } else {
                color = Config.colors[i];
                i++;
            }
            if (FIRE.client.getID() != id) {
                drawTickMark(applet, color, id);
            }
        }
        drawTickMark(applet, Color.BLACK, FIRE.client.getID());
        applet.popMatrix();

    }

    private void drawTickMark(Client applet, Color color, int id) {
        float x, y, min, max;
        min = config.payoffFunction.getMin();
        max = config.payoffFunction.getMax();
        x = width * Client.state.strategies.get(id)[0];
        y = height * (1
                - (PayoffFunction.Utilities.getPayoff(id, Client.state.strategies.get(id))
                - min) / (max - min));
        if (id == FIRE.client.getID()) {
            applet.stroke(-40, 160, 60);
            applet.fill(40, 240, 140);
        } else {
            applet.stroke(color.getRed() - 40, color.getGreen() - 40, color.getBlue() - 40);
            applet.fill(color.getRed() + 40, color.getGreen() + 40, color.getBlue() + 40);
            applet.strokeWeight(3);
            applet.line(x, height - 5, x, height + 5);
        }
        applet.strokeWeight(1);
        applet.ellipse(x, y, 11, 11);
    }

    private void drawAxis(Client applet) {
        float min, max;
        min = config.payoffFunction.getMin();
        max = config.payoffFunction.getMax();
        applet.rectMode(Client.CORNER);
        applet.noFill();
        applet.stroke(0);
        applet.strokeWeight(2);
        applet.rect(0, 0, width, height);

        applet.textAlign(Client.CENTER, Client.CENTER);
        applet.fill(255);
        applet.noStroke();
        applet.rect(-40, 0, 38, height);
        applet.rect(0, height + 2, width, 40);
        /*for (float x = 0.0f; x <= 1.01f; x += 0.1f) {
            applet.noFill();
            applet.stroke(100, 100, 100);
            applet.strokeWeight(2);
            float x0, y0, x1, y1;
            x0 = x * width;
            y0 = height;
            x1 = x * width;
            y1 = height + 10;
            applet.line(x0, y0, x1, y1);
            applet.fill(0);
        }*/
        String maxPayoffLabel = String.format("%.1f", max);
        float labelX = 10 + width + 1.1f * applet.textWidth(maxPayoffLabel) / 2f;
        for (float y = 0.0f; y <= 1.01f; y += 0.1f) {
            applet.noFill();
            applet.stroke(100, 100, 100);
            applet.strokeWeight(2);
            float x0, y0, x1, y1;
            x0 = 0;
            y0 = y * height;
            x1 = width + 10;
            y1 = y * height;
            applet.line(x0, y0, x1, y1);
            applet.stroke(100, 100, 100, 50);
            applet.line(x0, y0, x1, y1);
            applet.fill(0);
            float payoff = (1 - y) * (max - min) + min;
            if (payoff < 0) {
                payoff = 0f;
            }
            String label = String.format("%.1f", payoff);
            applet.text(label, Math.round(labelX), Math.round(y0));
        }
    }

    public void configChanged(Config config) {
        this.config = config;
        if (config.mixedStrategySelection && !config.stripStrategySelection
                && config.payoffFunction instanceof CournotPayoffFunction) {
            setVisible(true);
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
            Client.state.target[0] = newTarget;
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void endSubperiod(int subperiod) {
    }
}
