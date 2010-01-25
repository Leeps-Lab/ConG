package edu.ucsc.leeps.fire.cong.client;

import processing.core.PApplet;
import processing.core.PMatrix2D;

public abstract class Sprite {

        FPoint origin;
        FPoint screenLocation;
        int width, height;

        public Sprite(float x, float y, int width, int height) {
            origin = new FPoint(x, y);
            this.width = width;
            this.height = height;
            screenLocation = new FPoint();
        }

        public abstract void draw(PApplet applet);

        public boolean isHit(float x, float y) {
            return x >= screenLocation.x && x <= screenLocation.x + width
                    && y >= screenLocation.y && y <= screenLocation.y + height;
        }

        public boolean circularIsHit(float x, float y) {
            return PApplet.dist(x, y, screenLocation.x, screenLocation.y) <= width;
        }

        public void setScreenLocation(PApplet applet) {
            PMatrix2D matrix = new PMatrix2D();
            applet.g.getMatrix(matrix);
            screenLocation.x = matrix.m02;
            screenLocation.y = matrix.m12;
        }
    }
