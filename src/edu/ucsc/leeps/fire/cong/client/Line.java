package edu.ucsc.leeps.fire.cong.client;

import edu.ucsc.leeps.fire.cong.client.Client.PEmbed;
import java.awt.Color;
import java.util.LinkedList;
import processing.core.PApplet;

public class Line extends Sprite {

    private float weight;
    private int r, g, b, alpha;
    private LinkedList<FPoint> points;

    public enum Mode {

        Solid, EndPoint, Dotted
    };
    private Mode mode;

    public Line(int x, int y, int width, int height, float weight, Color color, int alpha, Mode mode) {
        super(x, y, width, height);
        visible = true;
        this.r = color.getRed();
        this.g = color.getGreen();
        this.b = color.getBlue();
        this.weight = weight;
        this.alpha = alpha;
        this.mode = mode;
        points = new LinkedList<FPoint>();
    }

    public synchronized void addPoint(float x, float y) {
        if (points.size() == 0) {
            FPoint p = new FPoint(x, y);
            points.add(p);
        } else {
            FPoint p0 = points.get(points.size() - 1);
            FPoint p1 = new FPoint(x, y);
            for (int i = (int) p0.x; i < p1.x; i++) {
                FPoint p = new FPoint(x, y);
                points.add(p);
            }
            points.add(p1);
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
            for (int i = 0; i < points.size() - 1; i++) {
                FPoint p0 = points.get(i);
                FPoint p1 = points.get(i + 1);
                if (p0.visible && p1.visible) {
                    applet.line(
                            p0.x, p0.y,
                            p1.x, p1.y);
                }
            }
        }
    }

    private void drawDottedLine(PEmbed applet) {
        if (points.size() >= 2) {
            applet.stroke(r, g, b, alpha);
            applet.strokeWeight(weight);
            for (int i = 0; i < points.size() - 1; i++) {
                if (i % 2 == 0) {
                    FPoint p0 = points.get(i);
                    FPoint p1 = points.get(i + 1);
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
            FPoint last = points.getLast();
            applet.ellipse(last.x, last.y, 12, 12);
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
            case Dotted:
                drawDottedLine(applet);
                break;
            case EndPoint:
                drawLineEndPoint(applet);
                break;
        }
        applet.popMatrix();
    }

    public synchronized void clear() {
        points.clear();
    }

    public synchronized void removeFirst() {
        points.removeFirst();
    }
}
