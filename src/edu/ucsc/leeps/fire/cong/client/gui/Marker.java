package edu.ucsc.leeps.fire.cong.client.gui;

import edu.ucsc.leeps.fire.cong.client.Client;
import java.awt.Color;

public class Marker extends Sprite {

    protected float R, G, B, alpha;
    protected float outline;
    protected float diameter;
    protected float largeDiameter;
    protected String label1;
    protected String label2;
    protected FPoint labelOrigin;
    protected boolean grabbed;
    protected boolean enlarged;
    protected LabelMode labelMode;
    protected DrawMode drawMode;

    public enum LabelMode {

        Center, Top, Right, Bottom, Left
    };

    public enum DrawMode {
        Filled, Outline, FillOutline, Target
    }

    public Marker(Sprite parent, float x, float y, boolean visible, float diameter) {
        super(parent, x, y, (int)diameter, (int)diameter);
        this.visible = visible;
        this.diameter = diameter;
        largeDiameter = diameter * 1.5f;

        R = 0;
        G = 0;
        B = 0;
        outline = 0;
        alpha = 255;

        labelMode = LabelMode.Center;
        drawMode = DrawMode.Filled;

        label1 = null;
        label2 = null;

        grabbed = false;
        enlarged = false;

        labelOrigin = getTranslation(origin);
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public void setColor(float r, float g, float b) {
        R = r;
        G = g;
        B = b;
        if (drawMode == DrawMode.FillOutline) {
            if (R > 200 || G > 200 || B > 200 ||
                    R + G + B > 300) {
                outline = 0;
            } else {
                outline = 255;
            }
        }
    }

    public void setColor(float r, float g, float b, int a) {
        R = r;
        G = g;
        B = b;
        alpha = a;
        if (drawMode == DrawMode.FillOutline) {
            if (R > 200 || G > 200 || B > 200 ||
                    R + G + B > 300) {
                outline = 0;
            } else {
                outline = 255;
            }
        }
    }

    public void setColor(Color C) {
        R = C.getRed();
        G = C.getGreen();
        B = C.getBlue();
        alpha = C.getAlpha();
        if (drawMode == DrawMode.FillOutline) {
            if (R > 200 || G > 200 || B > 200 ||
                    R + G + B > 300) {
                outline = 0;
            } else {
                outline = 255;
            }
        }
    }

    public void setLabel(String newLabel) {
        label1 = newLabel;
    }

    public void setLabel(float newLabel) {
        label1 = String.format("%3.2f", newLabel);
    }

    public void setLabelPercent(float newLabel) {
        label1 = String.format("%3.0f%%", newLabel * 100);
    }

    public void setLabel(float newLabel1, float newLabel2) {
        label1 = String.format("%3.2f", newLabel1);
        label2 = String.format("%3.2f", newLabel2);
    }

    public void setLabelMode(LabelMode position) {
        this.labelMode = position;
        switch (position) {
            case Center:
                labelOrigin.x = origin.x;
                labelOrigin.y = origin.y;
                break;
            case Top:
                labelOrigin.x = origin.x;
                labelOrigin.y = origin.y - diameter - 8;
                break;
            case Right:
                labelOrigin.x = origin.x + diameter + 8;
                labelOrigin.y = origin.y;
                break;
            case Bottom:
                labelOrigin.x = origin.x;
                labelOrigin.y = origin.y + diameter + 8;
                break;
            case Left:
                labelOrigin.x = origin.x - diameter - 8;
                labelOrigin.y = origin.y;
                break;
        }
    }

    public void setDrawMode(DrawMode mode) {
        drawMode = mode;
        if (drawMode == DrawMode.FillOutline) {
            if (R > 200 || G > 200 || B > 200 ||
                    R + G + B > 300) {
                outline = 0;
            } else {
                outline = 255;
            }
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
        width = (int)largeDiameter;
        height = (int)largeDiameter;
        enlarged = true;
    }

    public void shrink() {
        width = (int)diameter;
        height = (int)diameter;
        enlarged = false;
    }

    public boolean isEnlarged() {
        return enlarged;
    }

    public void update(float x, float y) {
        origin.x = x;
        origin.y = y;
        setLabelMode(labelMode);
    }

    public void draw(Client applet) {
        if (!visible) {
            return;
        }
        if (label1 != null) {
            drawLabels(applet);
        }

        if (drawMode == DrawMode.Filled) {
            applet.noStroke();
        } else {
            applet.stroke(outline);
            applet.strokeWeight(1);
        }

        if (drawMode == DrawMode.Outline ||
                drawMode == DrawMode.Target) {
            applet.noFill();
        } else {
            applet.fill(R, G, B, alpha);
        }
        
        applet.ellipseMode(Client.CENTER);
        if (!enlarged) {
            applet.ellipse(origin.x, origin.y, diameter, diameter);
            if (drawMode == DrawMode.Target) {
                applet.strokeWeight(2);
                applet.line(origin.x - diameter, origin.y, origin.x + diameter, origin.y);
                applet.line(origin.x + .5f, origin.y - diameter, origin.x + .5f, origin.y + diameter);
            }
        } else {
            applet.ellipse(origin.x, origin.y, largeDiameter, largeDiameter);
            if (drawMode == DrawMode.Target) {
                applet.strokeWeight(2);
                applet.line(origin.x - diameter, origin.y, origin.x + diameter, origin.y);
                applet.line(origin.x + .5f, origin.y - diameter, origin.x + .5f, origin.y + diameter);
            }
        }
    }

    protected void drawLabels(Client applet) {
        applet.textFont(applet.size14);
        float textWidth = applet.textWidth(label1);
        if (label2 != null) {
            applet.textFont(applet.size14Bold);
            textWidth += applet.textWidth(label2);
        }
        if (textWidth > 16 && labelMode == LabelMode.Left) {
            labelOrigin.x = origin.x - diameter - textWidth / 2;
        } else if (textWidth > 16 && labelMode == LabelMode.Right) {
            labelOrigin.x = origin.x + diameter + textWidth / 2;
        }
        float textHeight = applet.textAscent() + applet.textDescent();
        applet.rectMode(Client.CENTER);
        applet.fill(255);
        applet.noStroke();
        applet.rect(labelOrigin.x, labelOrigin.y, textWidth, textHeight);
        applet.textAlign(Client.CENTER, Client.CENTER);
        applet.fill(0);
        if (label1 != null && label2 != null) {
            float label1Width = applet.textWidth(label1);
            applet.textFont(applet.size14Bold);
            applet.text(label1, labelOrigin.x - label1Width / 2, labelOrigin.y);
            applet.textFont(applet.size14);
            applet.text("," + label2, labelOrigin.x + label1Width / 2, labelOrigin.y);
        } else if (label1 != null) {
            applet.textFont(applet.size14);
            applet.text(label1, labelOrigin.x, labelOrigin.y);
        }
    }
}
