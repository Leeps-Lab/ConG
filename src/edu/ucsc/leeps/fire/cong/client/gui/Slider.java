package edu.ucsc.leeps.fire.cong.client.gui;

import edu.ucsc.leeps.fire.cong.client.Client.PEmbed;
import java.awt.Color;

public class Slider {

    private final int HANDLE_WIDTH = 10;
    private final int HANDLE_HEIGHT = 25;
    private final String FORMAT = "%4.2f";
    private final float ERROR_MARGIN = .01f;
    private float sliderStart, sliderEnd, sliderY;
    private float length;
    private float sliderPos;
    private float stratValue;
    private boolean ghosting;
    private float ghostPos;
    private float ghostValue;
    private float R, G, B;
    private String label, stratLabel, ghostLabel;
    private boolean grabbed;
    private boolean ghostGrabbed;
    private float maxValue;
    private Sprite parent;

    // Constructor ////////////////
    public Slider(Sprite parent, float x1, float x2, float y, Color C,
            String label, float maxValue) {
        if (x2 < x1) {
            throw new RuntimeException("Invalid Slider coordinates " + "(x2 < x1)");
        }

        this.parent = parent;

        this.sliderStart = x1;
        this.sliderEnd = x2;
        this.sliderY = y;

        R = C.getRed();
        G = C.getGreen();
        B = C.getBlue();
        length = x2 - x1;
        sliderPos = x1;
        stratValue = 0;

        ghosting = false;
        ghostPos = x1;
        ghostValue = 0;

        this.label = label;
        stratLabel = String.format(FORMAT, stratValue);
        ghostLabel = String.format(FORMAT, ghostValue);

        grabbed = false;
        ghostGrabbed = false;
        this.maxValue = maxValue;
    }

    // Methods ///////////////////
    // Access ///////
    public float getStratValue() {
        return stratValue;
    }

    public float getGhostValue() {
        return ghostValue;
    }

    public float getSliderPos() {
        return sliderPos;
    }

    public float getGhostPos() {
        return ghostPos;
    }

    public float getLength() {
        return length;
    }

    public boolean isGrabbed() {
        return grabbed;
    }

    public boolean isGhostGrabbed() {
        return ghostGrabbed;
    }

    // Manipulation ///////
    public void setStratValue(float newStrat) {
        if (newStrat > maxValue + ERROR_MARGIN || newStrat < -ERROR_MARGIN) {
            throw new RuntimeException("Error: strategy value out of range.");
        }
        
        stratValue = newStrat;

        sliderPos = sliderStart + length * stratValue / maxValue;
        stratLabel = String.format(FORMAT, stratValue);
    }

    public void moveSlider(float x) {
        if (x < sliderStart) {
            sliderPos = sliderStart;
        } else if (x > sliderEnd) {
            sliderPos = sliderEnd;
        } else {
            sliderPos = x;
        }

        stratValue = maxValue * (sliderPos - sliderStart) / length;
        stratLabel = String.format(FORMAT, stratValue);
    }

    public void showGhost() {
        ghosting = true;
    }

    public void hideGhost() {
        ghosting = false;
    }

    public void setGhostValue(float ghostVal) {
        ghostValue = ghostVal;

        ghostPos = sliderStart + length * ghostValue / maxValue;
        ghostLabel = String.format(FORMAT, ghostValue);
    }

    public void moveGhost(float x) {
        if (x < sliderStart) {
            ghostPos = sliderStart;
        } else if (x > sliderEnd) {
            ghostPos = sliderEnd;
        } else {
            ghostPos = x;
        }

        ghostValue = maxValue * (ghostPos - sliderStart) / length;
        ghostLabel = String.format(FORMAT, ghostValue);
    }

    public void grab() {
        grabbed = true;
    }

    public void release() {
        grabbed = false;
    }

    public void grabGhost() {
        ghostGrabbed = true;
    }

    public void releaseGhost() {
        ghostGrabbed = false;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean mouseOnHandle(float mouseX, float mouseY) {
        return (mouseX < sliderPos + HANDLE_WIDTH / 2 && mouseX > sliderPos - HANDLE_WIDTH / 2 && mouseY < sliderY + HANDLE_HEIGHT / 2 && mouseY > sliderY - HANDLE_HEIGHT / 2);
    }

    public boolean mouseOnGhost(float mouseX, float mouseY) {
        return (mouseX < ghostPos + HANDLE_WIDTH / 2 && mouseX > ghostPos - HANDLE_WIDTH / 2 && mouseY < sliderY + HANDLE_HEIGHT / 2 && mouseY > sliderY - HANDLE_HEIGHT / 2);
    }

    public void draw(PEmbed applet) {
        applet.stroke(0);
        applet.strokeWeight(3);
        applet.line(sliderStart, sliderY, sliderEnd, sliderY);

        applet.noStroke();
        applet.fill(R, G, B);
        applet.rectMode(PEmbed.CENTER);
        applet.rect(sliderPos, sliderY, HANDLE_WIDTH, HANDLE_HEIGHT);

        applet.fill(0);
        applet.textAlign(PEmbed.LEFT);
        float labelWidth = applet.textWidth(label);
        applet.text(label, parent.origin.x + sliderStart - labelWidth - 10, parent.origin.y + sliderY + 2);
        applet.text(stratLabel, parent.origin.x + sliderEnd + 10, parent.origin.y + sliderY + 2);

        if (ghosting) {
            applet.fill(R, G, B, 125);
            applet.rect(ghostPos, sliderY, HANDLE_WIDTH, HANDLE_HEIGHT);

            applet.fill(120);
            applet.text(ghostLabel, parent.origin.x + sliderEnd + 10, parent.origin.y + sliderY + 20);
        }
    }
}