package edu.ucsc.leeps.fire.cong.client;

import java.util.ArrayList;
import java.awt.Color;
import processing.core.PApplet;

public class Line extends Sprite {

    public boolean visible;
    public Color color;
    public float weight, scale;
    public int alpha;
    public ArrayList<FPoint> points;
    public int maxSize = 500;

    public Line(int x, int y, int width, int height, float weight, Color color, int alpha) {
        super(x, y, width, height);
        visible = true;
        scale = 1;
        this.color = color;
        this.weight = weight;
        this.alpha = alpha;
        points = new ArrayList<FPoint>();
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
        //while (points.size() > maxSize) {
        //    points.remove(0);
        //}
    }

    // TODO: error/range checking
    public synchronized void setVisible(int start, int stop, boolean vis) {
        for (int i = start; i < stop; i++) {
            points.get(i).visible = vis;
        }
    }

    public synchronized void draw(PApplet applet) {
        if (!visible) {
            return;
        }
        applet.pushMatrix();
        applet.translate(origin.x, origin.y);
        int hex = color.getRGB();
        applet.stroke(((hex & 0x00FF0000) >> 16),
                ((hex & 0x0000FF00) >> 8),
                (hex & 0x000000FF), alpha);
        applet.strokeWeight(weight);
        if (points.size() == 1) {
            FPoint p = points.get(0);
            if (p.visible) {
                applet.point(p.x, p.y);
            }
        } else if (points.size() > 1) {
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
        applet.popMatrix();
    }

    public synchronized void clear() {
        points.clear();
    }
}
