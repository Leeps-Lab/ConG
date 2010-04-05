package edu.ucsc.leeps.fire.cong.client;

import edu.ucsc.leeps.fire.cong.client.Client.PEmbed;
import processing.core.PMatrix2D;

public abstract class Sprite {

    FPoint origin;
    FPoint screenLocation;
    int width, height;
    boolean visible;

    public Sprite(float x, float y, int width, int height) {
        origin = new FPoint(x, y);
        this.width = width;
        this.height = height;
        screenLocation = new FPoint();
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
}
