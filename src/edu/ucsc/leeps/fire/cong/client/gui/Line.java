package edu.ucsc.leeps.fire.cong.client.gui;

import edu.ucsc.leeps.fire.config.Configurable;
import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.client.Client;
import edu.ucsc.leeps.fire.cong.config.Config;
import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;

public class Line extends Sprite implements Serializable, Configurable<Config> {

    public enum Mode {

        Solid, EndPoint, Dashed, Shaded,
    };
    public float weight;
    public int r, g, b, alpha;
    public Mode mode;
    public boolean stepFunction;
    private transient int color;
    private transient ArrayList<Integer> ypoints;
    private transient int xMax;
    private transient int maxWidth;
    private transient float minPayoff;
    private transient float maxPayoff;

    public Line() {
        super(null, 0, 0, 0, 0);
        r = g = b = 0;
        alpha = 255;
        weight = 1.0f;
        mode = Mode.Solid;
    }

    public Line(Sprite parent, int x, int y, int width, int height) {
        super(parent, x, y, width, height);
        visible = false;
        ypoints = new ArrayList<Integer>();
        xMax = 0;
        FIRE.client.addConfigListener(this);
    }

    public void configure(Line config) {
        this.visible = true;
        this.color = new Color(config.r, config.g, config.b, config.alpha).getRGB();
        this.weight = config.weight;
        this.mode = config.mode;
        stepFunction = FIRE.client.getConfig().subperiods != 0;
    }

    public void configChanged(Config config) {
        maxPayoff = config.payoffFunction.getMax();
        minPayoff = config.payoffFunction.getMin();
        if (config.indefiniteEnd != null) {
            maxWidth = (int) (0.75f * width);
        } else {
            maxWidth = Integer.MAX_VALUE;
        }
    }

    public void setPoint(int x, int y) {
        while (xMax < x) {
            ypoints.add(y);
            if (ypoints.size() > maxWidth) {
                ypoints.remove(0);
            }
            xMax++;
        }
    }

    private void drawSolidLine(Client applet) {
        applet.stroke(color);
        applet.strokeWeight(weight);
        applet.fill(color);
        for (int x = 0; x < ypoints.size(); x++) {
            applet.point(x, ypoints.get(x));
            if (x > 1 && Math.abs(ypoints.get(x) - ypoints.get(x - 1)) > 1) {
                if (stepFunction) {
                    applet.line(x, ypoints.get(x), x, ypoints.get(x - 1));
                } else {
                    applet.line(x, ypoints.get(x), x - 1, ypoints.get(x - 1));
                }
            }
        }
    }

    private void drawDashedLine(Client applet) {
        throw new UnsupportedOperationException();
    }

    private void drawLineEndPoint(Client applet) {
        throw new UnsupportedOperationException();
    }

    private void drawShadedArea(Client applet) {
        applet.stroke(color);
        applet.strokeWeight(weight);
        applet.fill(color);
        applet.beginShape();
        for (int x = 0; x < ypoints.size(); x++) {
            if (x > 0 && stepFunction && Math.abs(ypoints.get(x) - ypoints.get(x - 1)) > 1) {
                applet.vertex(x, ypoints.get(x - 1));
            }
            applet.vertex(x, ypoints.get(x));
        }
        if (ypoints.size() > 0) {
            applet.vertex(ypoints.size(), ypoints.get(ypoints.size() - 1));
        }
        applet.vertex(ypoints.size(), height);
        applet.vertex(0, height);
        applet.endShape(Client.CLOSE);
    }

    public synchronized void drawCostArea(Client applet, float cost) {
        if (cost == 0) {
            return;
        }
        throw new UnsupportedOperationException();
    }

    public synchronized void draw(Client applet) {
        if (!visible) {
            return;
        }
        applet.pushMatrix();
        applet.translate(origin.x, origin.y);
        switch (mode) {
            case Solid:
                drawSolidLine(applet);
                break;
            case Dashed:
                drawDashedLine(applet);
                break;
            case EndPoint:
                drawLineEndPoint(applet);
                break;
            case Shaded:
                drawShadedArea(applet);
                break;
        }
        applet.popMatrix();
    }

    public void clear() {
        xMax = 0;
        ypoints.clear();
    }

    public void addPayoffPoint(float x, float y) {
        setPoint(
                Math.round(width * x),
                Math.round(height * (1 - ((y - minPayoff) / (maxPayoff - minPayoff)))));
    }

    public void addStrategyPoint(float x, float y) {
        setPoint(
                Math.round(width * x),
                Math.round(height * (1 - y)));
    }
}
