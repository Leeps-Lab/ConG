package edu.ucsc.leeps.fire.cong.client.gui;

import edu.ucsc.leeps.fire.cong.client.Client.PEmbed;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import processing.core.PApplet;

public class Line extends Sprite implements Serializable {

    public enum Mode {

        Solid, EndPoint, Dashed, Shaded,
    };
    public float weight;
    public int r, g, b, alpha;
    public Mode mode;
    public int SAMPLE_RATE = 2;
    public boolean showShock = true;
    private transient HashMap<Integer, FPoint> definedPoints;
    private transient LinkedList<FPoint> points;

    public Line() {
        super(0, 0, 0, 0);
        r = g = b = 0;
        alpha = 255;
        weight = 1.0f;
        mode = Mode.Solid;
    }

    public Line(int x, int y, int width, int height) {
        super(x, y, width, height);
        visible = false;
        definedPoints = new HashMap<Integer, FPoint>();
        points = new LinkedList<FPoint>();
    }

    public void configure(Line config) {
        this.visible = true;
        this.r = config.r;
        this.g = config.g;
        this.b = config.b;
        this.alpha = config.alpha;
        this.weight = config.weight;
        this.mode = config.mode;
        this.showShock = config.showShock;
    }

    public synchronized void setPoint(int x, int y, boolean visible) {
        if (!definedPoints.containsKey(x)) {
            FPoint point = new FPoint(x, y);
            point.visible = visible;
            definedPoints.put(x, point);
            points.add(point);
        }
    }

    public synchronized void setVisible(int start, int stop, boolean vis) {
        if (start <= stop && start >= 0 && stop < points.size() - 1) {
            for (int i = start; i < stop; i++) {
                points.get(i).visible = vis;
            }
        }
    }

    private void drawSolidLine(PEmbed applet) {
        if (points.size() >= 2) {
            applet.stroke(r, g, b, alpha);
            applet.strokeWeight(weight);
            FPoint last = null;
            int i = 0;
            for (FPoint p : points) {
                if (!p.visible) {
                    last = p;
                    continue;
                }
                if (i % SAMPLE_RATE == 0 || i == points.size() - 1) {
                    if (last != null) {
                        applet.stroke(r, g, b, alpha);
                        applet.line(last.x, last.y, p.x, p.y);
                    }
                    last = p;
                }
                i++;
            }
        }
    }

    private void drawDashedLine(PEmbed applet) {
        if (points.size() >= 2) {
            applet.stroke(r, g, b, alpha);
            applet.strokeWeight(weight);
            for (int i = 0; i < points.size() - 1; i++) {
                if (i % 2 == 0) {
                    FPoint p0 = points.get(i);
                    FPoint p1 = points.get(i + 1);
                    if (!p0.visible || !p1.visible) {
                        continue;
                    }
                    if (p0.visible && p1.visible) {
                        applet.line(
                                p0.x, p0.y,
                                p1.x, p1.y);
                    }
                }
            }
        }
    }

    private void drawLineEndPoint(PEmbed applet) {
        if (points.size() >= 1) {
            applet.fill(r, g, b, alpha);
            applet.stroke(r, g, b, alpha);
            applet.strokeWeight(weight);
            applet.ellipseMode(PApplet.CENTER);
            FPoint last = null;
            for (FPoint p : points) {
                if (p != null) {
                    last = p;
                }
            }
            if (last.visible) {
                applet.ellipse(last.x, last.y, 12, 12);
            }
        }
    }

    private void drawShadedArea(PEmbed applet) {
        if (points.size() >= 1) {
            applet.fill(r, g, b, alpha);
            applet.stroke(r, g, b, alpha);
            applet.strokeWeight(weight);

            applet.beginShape();
            FPoint last = null;
            int i = 0;
            for (FPoint p : points) {
                if (last != null && last.visible && !p.visible) {
                    // begin shock zone
                    applet.vertex(last.x, height);
                    applet.endShape(PApplet.CLOSE);
                } else if (last != null && !last.visible && p.visible) {
                    // end shock zone
                    applet.beginShape();
                    applet.vertex(p.x, height);
                }
                if (!p.visible) {
                    last = p;
                    continue;
                }
                if (i % SAMPLE_RATE == 0 || i == points.size() - 1) {
                    if (p != null) {
                        if (last == null) {
                            applet.vertex(p.x, height);
                            // for some reason, the polygon render fails if the
                            // next point has y value equal to the last point,
                            // we eliminate that vertex in this special case
                            if (Math.abs(p.y - height) > Float.MIN_NORMAL) {
                                applet.vertex(p.x, p.y);
                            }
                        } else {
                            applet.vertex(p.x, p.y);
                        }
                        last = p;
                    }
                }
                i++;
            }
            applet.vertex(last.x, height);
            applet.endShape(PApplet.CLOSE);
        }
    }

    public synchronized void draw(PEmbed applet) {
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

    public synchronized void clear() {
        definedPoints.clear();
        points.clear();
    }

    public synchronized void removeFirst() {
        FPoint first = points.removeFirst();
        definedPoints.remove(Math.round(first.x));
    }

    public synchronized void clearShocks() {
        for (FPoint point : points) {
            point.visible = true;
        }
    }
}
