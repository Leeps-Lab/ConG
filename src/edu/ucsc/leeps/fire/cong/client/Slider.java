package edu.ucsc.leeps.fire.cong.client;

import java.awt.Color;
import processing.core.PApplet;

public class Slider {

    private final int HANDLE_WIDTH = 10;
    private final int HANDLE_HEIGHT = 25;
    private final String FORMAT = "%4.2f";
    private float sliderStart, sliderEnd, sliderY;
    private float length;
    private float sliderPos;
    private float stratValue;
    private boolean planning;
    private float plannedPos;
    private float plannedValue;
    private float R, G, B;
    private String label, stratLabel, plannedLabel;
    private boolean grabbed;
    private float maxValue;

    // Constructor ////////////////
    public Slider(float x1, float x2, float y, Color C, String label, float maxValue) {
        if (x2 < x1) {
            throw new RuntimeException("Invalid Slider coordinates " + "(x2 < x1)");
        }

        this.sliderStart = x1;
        this.sliderEnd = x2;
        this.sliderY = y;

        R = C.getRed();
        G = C.getGreen();
        B = C.getBlue();
        length = x2 - x1;
        sliderPos = x1;
        stratValue = 0;

        planning = false;
        plannedPos = x1;
        plannedValue = 0;

        this.label = label;
        stratLabel = String.format(FORMAT, stratValue);
        plannedLabel = String.format(FORMAT, plannedValue);

        grabbed = false;
        this.maxValue = maxValue;
    }

    // Methods ///////////////////
    // Access ///////
    public float getStratValue() {
        return stratValue;
    }

    public float getSliderPos() {
        return sliderPos;
    }

    public float getLength() {
        return length;
    }

    public boolean isGrabbed() {
        return grabbed;
    }

    // Manipulation ///////
    public void setStratValue(float newStrat) {
        if (newStrat > maxValue || newStrat < 0) {
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

    public void showPlan() {
        planning = true;
    }

    public void hidePlan() {
        planning = false;
    }

    public void setPlan(float plannedStrat) {
        plannedValue = plannedStrat;

        plannedPos = sliderStart + length * plannedValue / maxValue;
        plannedLabel = String.format(FORMAT, plannedValue);
    }

    public void grab() {
        grabbed = true;
    }

    public void release() {
        grabbed = false;
    }

    public boolean mouseOnHandle(float mouseX, float mouseY) {
        return (mouseX < sliderPos + HANDLE_WIDTH / 2 && mouseX > sliderPos - HANDLE_WIDTH / 2 && mouseY < sliderY + HANDLE_HEIGHT / 2 && mouseY > sliderY - HANDLE_HEIGHT / 2);
    }

    public void draw(PApplet applet) {
        applet.stroke(0);
        applet.strokeWeight(3);
        applet.line(sliderStart, sliderY, sliderEnd, sliderY);

        applet.noStroke();
        applet.fill(R, G, B);
        applet.rectMode(PApplet.CENTER);
        applet.rect(sliderPos, sliderY, HANDLE_WIDTH, HANDLE_HEIGHT);

        applet.fill(0);
        applet.textAlign(PApplet.LEFT);
        applet.text(label, sliderStart - 50, sliderY + 2);
        applet.text(stratLabel, sliderEnd + 10, sliderY + 2);

        if (planning) {
            applet.fill(R, G, B, 125);
            applet.rect(plannedPos, sliderY, HANDLE_WIDTH, HANDLE_HEIGHT);

            applet.fill(120);
            applet.text(plannedLabel, sliderEnd + 10, sliderY + 20);
        }
    }
}
