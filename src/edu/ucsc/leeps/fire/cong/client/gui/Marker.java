package edu.ucsc.leeps.fire.cong.client.gui;

import edu.ucsc.leeps.fire.cong.client.Client.PEmbed;
import java.awt.Color;

public class Marker extends Sprite {

    protected float R, G, B, alpha;
    protected float radius;
    protected float largeRadius;
    protected String label1;
    protected String label2;
    protected FPoint labelOrigin;
    protected boolean grabbed;
    protected boolean enlarged;
    protected Sprite parent;
    protected LabelMode mode;

    public enum LabelMode {

        Center, Top, Right, Bottom, Left
    };

    public Marker(Sprite parent, float x, float y, boolean visible, float radius) {
        super(x, y, (int)radius, (int)radius);
        this.parent = parent;
        this.visible = visible;
        this.radius = radius;
        largeRadius = radius * 1.5f;

        R = 0;
        G = 0;
        B = 0;
        alpha = 255;

        mode = LabelMode.Center;

        label1 = null;
        label2 = null;

        grabbed = false;
        enlarged = false;

        labelOrigin = new FPoint(x, y);
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public void setColor(float r, float g, float b) {
        R = r;
        G = g;
        B = b;
    }

    public void setColor(float r, float g, float b, int a) {
        R = r;
        G = g;
        B = b;
        alpha = a;
    }

    public void setColor(Color C) {
        R = C.getRed();
        G = C.getGreen();
        B = C.getBlue();
        alpha = C.getAlpha();
    }

    public void setLabel(String newLabel) {
        label1 = newLabel;
    }

    public void setLabel(float newLabel) {
        label1 = String.format("%3.2f", newLabel);
    }

    public void setLabel(float newLabel1, float newLabel2) {
        label1 = String.format("%3.2f", newLabel1);
        label2 = String.format("%3.2f", newLabel2);
    }

    public void setLabelMode(LabelMode position) {
        this.mode = position;
        switch (position) {
            case Center:
                labelOrigin.x = origin.x;
                labelOrigin.y = origin.y;
                break;
            case Top:
                labelOrigin.x = origin.x;
                labelOrigin.y = origin.y - radius - 8;
                break;
            case Right:
                labelOrigin.x = origin.x + radius + 8;
                labelOrigin.y = origin.y;
                break;
            case Bottom:
                labelOrigin.x = origin.x;
                labelOrigin.y = origin.y + radius + 8;
                break;
            case Left:
                labelOrigin.x = origin.x - radius - 8;
                labelOrigin.y = origin.y;
                break;
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

    public void enlarge() {
        width = (int)largeRadius;
        height = (int)largeRadius;
        enlarged = true;
    }

    public void shrink() {
        width = (int)radius;
        height = (int)radius;
        enlarged = false;
    }

    public boolean isEnlarged() {
        return enlarged;
    }

    public void update(float x, float y) {
        origin.x = x;
        origin.y = y;
        setLabelMode(mode);
    }

    public void draw(PEmbed applet) {
        if (!visible) {
            return;
        }
        if (label1 != null) {
            drawLabels(applet);
        }
        applet.noStroke();
        applet.fill(R, G, B, alpha);
        applet.ellipseMode(PEmbed.CENTER);
        if (!enlarged) {
            applet.ellipse(origin.x, origin.y, radius, radius);
        } else {
            applet.ellipse(origin.x, origin.y, largeRadius, largeRadius);
        }
    }

    protected void drawLabels(PEmbed applet) {
        applet.textFont(applet.size14);
        float textWidth = applet.textWidth(label1);
        if (label2 != null) {
            applet.textFont(applet.size14Bold);
            textWidth += applet.textWidth(label2);
        }
        if (textWidth > 16 && mode == LabelMode.Left) {
            labelOrigin.x = origin.x - radius - textWidth / 2;
        } else if (textWidth > 16 && mode == LabelMode.Right) {
            labelOrigin.x = origin.x + radius + textWidth / 2;
        }
        float textHeight = applet.textAscent() + applet.textDescent();
        applet.rectMode(PEmbed.CENTER);
        applet.fill(255);
        applet.noStroke();
        applet.rect(labelOrigin.x, labelOrigin.y, textWidth, textHeight);
        applet.textAlign(PEmbed.CENTER, PEmbed.CENTER);
        applet.fill(0);
        if (label1 != null && label2 != null) {
            float label1Width = applet.textWidth(label1);
            applet.textFont(applet.size14Bold);
            applet.text(label1, parent.origin.x + labelOrigin.x - label1Width / 2, parent.origin.y + labelOrigin.y);
            applet.textFont(applet.size14);
            applet.text("," + label2, parent.origin.x + labelOrigin.x + label1Width / 2, parent.origin.y + labelOrigin.y);
        } else if (label1 != null) {
            applet.textFont(applet.size14);
            applet.text(label1, parent.origin.x + labelOrigin.x, parent.origin.y + labelOrigin.y);
        }
    }
}
