package edu.ucsc.leeps.fire.cong.client.gui;

import edu.ucsc.leeps.fire.cong.client.Client.PEmbed;
import java.io.Serializable;
import processing.core.PMatrix2D;

public abstract class Sprite implements Serializable {

    public transient FPoint origin;
    public transient FPoint screenLocation;
    public transient int width, height;
    public transient Sprite parent;
    public boolean visible;

    public Sprite(Sprite parent, float x, float y, int width, int height) {
        origin = new FPoint(x, y);
        this.parent = parent;
        this.width = width;
        this.height = height;
        screenLocation = new FPoint(x, y);
    }

    public abstract void draw(PEmbed applet);

    public boolean isHit(float x, float y) {
        return x >= screenLocation.x && x <= screenLocation.x + width
                && y >= screenLocation.y && y <= screenLocation.y + height;
    }

    public boolean circularIsHit(float x, float y) {
        return PEmbed.dist(x, y, screenLocation.x, screenLocation.y) <= width;
    }

    public void setScreenLocation(PEmbed applet) {
        PMatrix2D matrix = new PMatrix2D();
        applet.getMatrix(matrix);
        screenLocation.x = matrix.m02;
        screenLocation.y = matrix.m12;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public FPoint getTranslation(FPoint newOrigin) {
        if (parent == null) {
            return newOrigin;
        } else {
            FPoint translation = new FPoint(parent.origin.x + newOrigin.x, parent.origin.y + newOrigin.y);
            return parent.getTranslation(translation);
        }
    }
}
