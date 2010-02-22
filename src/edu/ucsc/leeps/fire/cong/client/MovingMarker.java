
package edu.ucsc.leeps.fire.cong.client;

import java.awt.Color;
import processing.core.PApplet;

/**
 * MovingMarkers can be set to converge on a target
 * location at a given speed.
 * @author swolpert
 */
public class MovingMarker extends Marker {
    // speed of marker movement
    private float speed;
    private float xVel, yVel;

    // point marker moves toward
    private Marker target;

    public MovingMarker(float x, float y, boolean visible, float radius,
            float speed) {
        super(x, y, visible, radius);

        this.speed = speed;
        xVel = 0;
        yVel = 0;
        target = new Marker(x, y, true, radius - 2);
    }


    public void setSpeed(float speed) {
        this.speed = speed;
    }

    @Override
    public void setColor(float r, float g, float b) {
        super.setColor(r, g, b);
        target.setColor(r, g, b);
    }

    @Override
    public void setColor(Color C) {
        super.setColor(C);
        target.setColor(C);
    }

    @Override
    public void setAlpha(float alpha) {
        super.setAlpha(alpha);
        target.setAlpha(alpha);
    }

    public void setTargetLabel(String newLabel) {
        target.setLabel(newLabel);
    }

    public void setTargetLabel(float newLabel) {
        target.setLabel(newLabel);
    }

    public void setTargetLabelMode(int newMode) {
        target.setLabelMode(newMode);
    }

    public void setLocation(float x, float y) {
        this.x = x;
        this.y = y;
        target.x = x;
        target.y = y;
        xVel = 0;
        yVel = 0;
    }
    
    @Override
    public void update(float x, float y) {
        target.x = x;
        target.y = y;

        float deltaX = target.x - this.x;
        float deltaY = target.y - this.y;

        float dist = PApplet.sqrt(deltaX * deltaX + deltaY * deltaY);
        float scale = speed / dist;

        xVel = deltaX * scale;
        yVel = deltaY * scale;
    }

    public void update() {
        float deltaX = target.x - x;
        float deltaY = target.y - y;

        float distSquared = deltaX * deltaX + deltaY * deltaY;
        if (distSquared < speed * speed || distSquared == 0) {
            x = target.x;
            y = target.y;
            xVel = 0;
            yVel = 0;
        } else {
            x += xVel;
            y += yVel;
        }
        
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

    @Override
    public void draw(PApplet applet) {
        if (visible) {
            applet.stroke(0, 255, 255);
            applet.strokeWeight(1);
            applet.line(x, y, target.x, target.y);
            target.draw(applet);
        }
        super.draw(applet);
    }
}
