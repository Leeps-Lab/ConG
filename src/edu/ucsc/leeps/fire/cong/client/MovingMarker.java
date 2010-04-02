package edu.ucsc.leeps.fire.cong.client;

import edu.ucsc.leeps.fire.cong.client.Client.PEmbed;
import java.awt.Color;

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

    public MovingMarker(Sprite parent, float x, float y, boolean visible, float radius,
            float speed) {
        super(parent, x, y, visible, radius);

        this.speed = speed;
        xVel = 0;
        yVel = 0;
        target = new Marker(parent, x, y, true, radius - 2);
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

    public void setTargetLabel(String newLabel) {
        target.setLabel(newLabel);
    }

    public void setTargetLabel(float newLabel) {
        target.setLabel(newLabel);
    }

    public void setTargetLabelMode(LabelMode newMode) {
        target.setLabelMode(mode);
    }

    public void setLocation(float x, float y) {
        origin.x = x;
        origin.y = y;
        target.origin.x = x;
        target.origin.y = y;
        xVel = 0;
        yVel = 0;
    }

    @Override
    public void update(float x, float y) {
        target.origin.x = x;
        target.origin.y = y;

        float deltaX = target.origin.x - origin.x;
        float deltaY = target.origin.y - origin.y;

        float dist = PEmbed.sqrt(deltaX * deltaX + deltaY * deltaY);
        float scale = speed / dist;

        xVel = deltaX * scale;
        yVel = deltaY * scale;
    }

    public void update() {
        float deltaX = target.origin.x - origin.x;
        float deltaY = target.origin.y - origin.y;

        float distSquared = deltaX * deltaX + deltaY * deltaY;
        if (distSquared < speed * speed || distSquared == 0) {
            origin.x = target.origin.x;
            origin.y = target.origin.y;
            xVel = 0;
            yVel = 0;
        } else {
            origin.x += xVel;
            origin.y += yVel;
        }
        setLabelMode(mode);
    }

    @Override
    public void draw(PEmbed applet) {
        if (visible) {
            applet.stroke(0, 255, 255);
            applet.strokeWeight(1);
            applet.line(origin.x, origin.y, target.origin.x, target.origin.y);
            target.draw(applet);
        }
        super.draw(applet);
    }
}
