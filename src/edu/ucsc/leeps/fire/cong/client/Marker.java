package edu.ucsc.leeps.fire.cong.client;

import java.awt.Color;
import processing.core.PApplet;

public class Marker extends FPoint {
    private float R, G, B, alpha;
    private float radius;
    private String label;
    private FPoint labelCoords;
    private int labelMode;
    private boolean grabbed;

    public static final int NONE = 0;
    public static final int TOP = 1;
    public static final int RIGHT = 2;
    public static final int BOTTOM = 3;
    public static final int LEFT = 4;

    public Marker(float x, float y, boolean visible, float radius) {
        super(x, y);
        this.visible = visible;
        this.radius = radius;

        this.R = 0;
        this.G = 0;
        this.B = 0;
        this.alpha = 255;

        labelMode = NONE;
        labelCoords = new FPoint(x, y);

        grabbed = false;
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

    public void setLabel(String newLabel) {
        label = newLabel;
        if(labelMode == NONE) {
            labelMode = BOTTOM;
        }
    }

    public void setLabel(float newLabel) {
        label = String.format("%3.2f", newLabel);
        if(labelMode == NONE) {
            labelMode = BOTTOM;
        }
    }

    public void setLabelMode(int newMode) {
        labelMode = newMode;
        switch(labelMode) {
            case NONE:
                break;
            case TOP:
                labelCoords.x = this.x;
                labelCoords.y = this.y - radius - 8;
                break;
            case RIGHT:
                labelCoords.x = this.x + radius + 8;
                labelCoords.y = this.y;
                break;
            case BOTTOM:
                labelCoords.x = this.x;
                labelCoords.y = this.y + radius + 8;
                break;
            case LEFT:
                labelCoords.x = this.x - radius - 8;
                labelCoords.y = this.y;
                break;
            default:
                throw new RuntimeException("Label mode out of range.");
        }
    }

    public void grab() {
        grabbed = true;
    }

    public void release() {
        grabbed = false;
    }

    public boolean isGrabbed() {
        return grabbed;
    }

    public void update(float x, float y) {
        this.x = x;
        this.y = y;
        switch(labelMode) {
            case NONE:
                break;
            case TOP:
                labelCoords.x = this.x;
                labelCoords.y = this.y - radius - 5;
                break;
            case RIGHT:
                labelCoords.x = this.x + radius + 8;
                labelCoords.y = this.y;
                break;
            case BOTTOM:
                labelCoords.x = this.x;
                labelCoords.y = this.y + radius + 5;
                break;
            case LEFT:
                labelCoords.x = this.x - radius - 8;
                labelCoords.y = this.y;
                break;
            default:
                throw new RuntimeException("Label mode out of range.");
        }
    }

    public void draw(PApplet applet) {
        if (visible) {
            if(labelMode != NONE) {
                applet.rectMode(PApplet.CENTER);
                applet.fill(255);
                float textWidth = applet.textWidth(label);
                if (textWidth > 16 && labelMode == LEFT) {
                    labelCoords.x = this.x - radius - textWidth / 2;
                } else if (textWidth > 16 && labelMode == RIGHT) {
                    labelCoords.x = this.x + radius + textWidth / 2;
                }
                float textHeight = applet.textAscent() + applet.textDescent();
                applet.rect(labelCoords.x, labelCoords.y, textWidth, textHeight);
                applet.textAlign(PApplet.CENTER, PApplet.CENTER);
                applet.fill(0);
                applet.text(label, labelCoords.x, labelCoords.y);
            }
            applet.noStroke();
            applet.fill(R, G, B, alpha);
            applet.ellipseMode(PApplet.CENTER);
            applet.ellipse(x, y, radius, radius);
        }
    }
}
