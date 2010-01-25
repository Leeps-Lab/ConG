package edu.ucsc.leeps.fire.cong.client;

import java.awt.Color;
import processing.core.PApplet;

public class Marker extends FPoint {

    private float R, G, B, alpha;
    private float radius;

    public Marker(float x, float y, boolean visible, float radius) {
        super(x, y);
        this.visible = visible;
        this.radius = radius;

        this.R = 0;
        this.G = 0;
        this.B = 0;
        this.alpha = 255;
    }

    public void show() {
        visible = true;
    }

    public void hide() {
        visible = false;
    }

    public void setColor(float r, float g, float b) {
        R = r;
        G = g;
        B = b;
    }

    public void setColor(Color C) {
        R = C.getRed();
        G = C.getGreen();
        B = C.getBlue();
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public void update(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void draw(PApplet applet) {
        if (visible) {
            applet.noStroke();
            applet.fill(R, G, B, alpha);
            applet.ellipseMode(PApplet.CENTER);
            applet.ellipse(x, y, radius, radius);
        }
    }
}
