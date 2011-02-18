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

    private boolean enabled;
    private Slider slider;
    private Config config;
    private float[] subperiodStrategy;

    public CournotSelector(Sprite parent, int x, int y, int width, int height,
            Client applet) {
        super(parent, x, y, width, height);
        slider = new Slider(applet, Slider.Alignment.Horizontal,
                0, this.width, this.height, Color.black, "", 1f);
        slider.setShowStrategyLabel(false);
        slider.hideGhost();
        slider.setOutline(true);
        FIRE.client.addConfigListener(this);
        applet.addMouseListener(this);
        applet.addKeyListener(this);
        subperiodStrategy = new float[1];
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

        if (enabled && !config.trajectory && slider.isGhostGrabbed()) {
            float mouseX = applet.mouseX - origin.x;
            slider.moveGhost(mouseX);
            setTarget(slider.getGhostValue());
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
                drawStrategy(applet, color, id);
            }
        }
        drawStrategy(applet, Color.BLACK, FIRE.client.getID());
        if (config.subperiods != 0 && FIRE.client.isRunningPeriod() && !FIRE.client.isPaused()) {
            drawPlannedStrategy(applet);
        }
        applet.popMatrix();
    }

    private void drawPlannedStrategy(Client applet) {
        applet.stroke(0, 0, 0, 20);
        applet.line(width * Client.state.getMyStrategy()[0], 0, width * Client.state.getMyStrategy()[0], height);
    }

    private void drawStrategy(Client applet, Color color, int id) {
        float x, y, min, max;
        min = config.payoffFunction.getMin();
        max = config.payoffFunction.getMax();
        float payoff, strategy;
        if (config.subperiods != 0) {
            if (id == FIRE.client.getID()) {
                payoff = Client.state.subperiodPayoff;
                strategy = subperiodStrategy[0];
            } else {
                payoff = config.payoffFunction.getPayoff(
                        id, 0, Client.state.getFictitiousStrategies(FIRE.client.getID(), subperiodStrategy), null, config);
                strategy = Client.state.strategies.get(id)[0];
            }
        } else {
            payoff = PayoffFunction.Utilities.getPayoff(id, Client.state.strategies.get(id));
            strategy = Client.state.strategies.get(id)[0];
        }
        x = width * strategy;
        y = height * (1 - (payoff - min) / (max - min));
        if (y > height) {
            y = height;
        } else if (y < 0) {
            y = 0;
        }
        if (id == FIRE.client.getID()) {
            applet.strokeWeight(3);
            applet.stroke(-40, 160, 60);
            applet.fill(40, 240, 140);
        } else if (config.subperiods == 0 || Client.state.subperiod != 0) {
            applet.stroke(color.getRed() - 40, color.getGreen() - 40, color.getBlue() - 40);
            applet.fill(color.getRed() + 40, color.getGreen() + 40, color.getBlue() + 40);
            applet.strokeWeight(3);
            applet.line(x, height - 5, x, height + 5);
        }
        applet.strokeWeight(1);
        if (id == FIRE.client.getID() && (config.subperiods == 0 || Client.state.subperiod != 0)) {
            applet.ellipse(x, y, 11, 11);
            applet.fill(40 - 30, 240 - 30, 140 - 30);
            applet.stroke(40 - 30, 240 - 30, 140 - 30);
        } else if (config.subperiods == 0 || Client.state.subperiod != 0) {
            applet.ellipse(x, y, 8, 8);
            applet.fill(color.getRed() - 70, color.getGreen() - 70, color.getBlue() - 70);
            applet.stroke(color.getRed() - 70, color.getGreen() - 70, color.getBlue() - 70);
        }
        if (config.subperiods == 0 || Client.state.subperiod != 0) {
            String label = String.format("%.0f", payoff);
            applet.text(label, Math.round(x + 5), Math.round(y - 3));
        }
        if (payoff > max && (config.subperiods == 0 || Client.state.subperiod != 0)) {
            drawUpArrow(applet, color, x);
        } else if (payoff < min && (config.subperiods == 0 || Client.state.subperiod != 0)) {
            drawDownArrow(applet, color, x);
        }
    }

    private void drawUpArrow(Client applet, Color color, float x) {
        applet.strokeWeight(3f);
        applet.line(x, -22, x, -10);
        applet.noStroke();
        applet.triangle(x - 5, -20, x, -30, x + 5, -20);
    }

    private void drawDownArrow(Client applet, Color color, float x) {
        applet.strokeWeight(3f);
        applet.line(x, height + 10, x, height + 22);
        applet.noStroke();
        applet.triangle(x - 5, height + 20, x, height + 30, x + 5, height + 20);
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
            applet.stroke(100, 100, 100, 50);
            applet.line(x0, y0, x1, y1);
            float payoff = (1 - y) * (max - min) + min;
            if (payoff < 0) {
                payoff = 0f;
            }
            applet.fill(0);
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

    private void setTarget(float newTarget) {
        if (config.trajectory) {
            float current = Client.state.getMyStrategy()[0];
            if (newTarget == current) {
                Client.state.target[0] = newTarget;
            } else if (newTarget > current) {
                Client.state.target[0] = 1f;
            } else if (newTarget < current) {
                Client.state.target[0] = 0f;
            }
        } else {
            Client.state.target[0] = newTarget;
        }
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
                if (config.trajectory) {
                    newTarget = 1f;
                } else {
                    newTarget += .01f;
                }
            } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                if (config.trajectory) {
                    newTarget = 0f;
                } else {
                    newTarget -= .01f;
                }
            }
            newTarget = Client.constrain(newTarget, 0, 1);
            setTarget(newTarget);
        }
    }

    public void keyReleased(KeyEvent e) {
        if (!visible || !enabled) {
            return;
        }
        if (e.isActionKey() && (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_LEFT)) {
            setTarget(Client.state.getMyStrategy()[0]);
        }
    }

    public void endSubperiod(int subperiod) {
        subperiodStrategy[0] = Client.state.getMyStrategy()[0];
    }
}
