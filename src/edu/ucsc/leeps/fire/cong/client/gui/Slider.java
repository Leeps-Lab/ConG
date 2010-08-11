package edu.ucsc.leeps.fire.cong.client.gui;

import edu.ucsc.leeps.fire.cong.client.Client.PEmbed;
import java.awt.Color;
import processing.core.PImage;

public class Slider {
    public static enum Alignment { Horizontal, Vertical };

    private final int HANDLE_WIDTH = 10;
    private final int HANDLE_HEIGHT = 27;
    private final String FORMAT = "%4.2f";
    private final float ERROR_MARGIN = .01f;
    private Alignment align;
    private float sliderStart, sliderEnd, sliderLine;
    private float length;
    private float sliderPos;
    private float stratValue;
    private boolean ghosting;
    private float ghostPos;
    private float ghostValue;
    private float R, G, B;
    private PImage texture, ghostTexture;
    private String label, stratLabel, ghostLabel;
    private boolean grabbed;
    private boolean ghostGrabbed;
    private float maxValue;
    private Sprite parent;

    // Constructor ////////////////
    public Slider(PEmbed applet, Sprite parent, Alignment align, float start, float end, float line, Color C,
            String label, float maxValue) {
        if (end < start) {
            throw new RuntimeException("Invalid Slider coordinates " + "(end < start)");
        }

        this.parent = parent;
        this.align = align;

        this.sliderStart = start;
        this.sliderEnd = end;
        this.sliderLine = line;

        R = C.getRed();
        G = C.getGreen();
        B = C.getBlue();
        textureSetup(applet);
        length = end - start;
        if (align == Alignment.Horizontal) {
            sliderPos = start;
            ghostPos = start;
        } else {
            sliderPos = end;
            ghostPos = end;
        }
        stratValue = 0;

        ghosting = false;
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

    public Alignment getAlignment() {
        return align;
    }

    // Manipulation ///////
    public void setStratValue(float newStrat) {
        if (newStrat > maxValue + ERROR_MARGIN || newStrat < -ERROR_MARGIN) {
            throw new RuntimeException("Error: strategy value out of range.");
        }
        
        stratValue = newStrat;

        if (align == Alignment.Horizontal) {
            sliderPos = sliderStart + length * stratValue / maxValue;
        } else {
            sliderPos = sliderEnd - length * stratValue / maxValue;
        }
        stratLabel = String.format(FORMAT, stratValue);
    }

    public void moveSlider(float pos) {
        if (pos < sliderStart) {
            sliderPos = sliderStart;
        } else if (pos > sliderEnd) {
            sliderPos = sliderEnd;
        } else {
            sliderPos = pos;
        }

        if (align == Alignment.Horizontal) {
            stratValue = maxValue * (sliderPos - sliderStart) / length;
        } else {
            stratValue = maxValue * (sliderEnd - sliderPos) / length;
        }
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

        if (align == Alignment.Horizontal) {
            ghostPos = sliderStart + length * ghostValue / maxValue;
        } else {
            ghostPos = sliderEnd - length * ghostValue / maxValue;
        }
        ghostLabel = String.format(FORMAT, ghostValue);
    }

    public void moveGhost(float pos) {
        if (pos < sliderStart) {
            ghostPos = sliderStart;
        } else if (pos > sliderEnd) {
            ghostPos = sliderEnd;
        } else {
            ghostPos = pos;
        }

        if (align == Alignment.Horizontal) {
            ghostValue = maxValue * (ghostPos - sliderStart) / length;
        } else {
            ghostValue = maxValue * (sliderEnd - ghostPos) / length;
        }
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
        if (align == Alignment.Horizontal) {
            return (mouseX < sliderPos + HANDLE_WIDTH / 2 &&
                    mouseX > sliderPos - HANDLE_WIDTH / 2 &&
                    mouseY < sliderLine + HANDLE_HEIGHT / 2 &&
                    mouseY > sliderLine - HANDLE_HEIGHT / 2);
        } else {
            return (mouseY < sliderPos + HANDLE_WIDTH / 2 &&
                    mouseY > sliderPos - HANDLE_WIDTH / 2 &&
                    mouseX < sliderLine + HANDLE_HEIGHT / 2 &&
                    mouseX > sliderLine - HANDLE_HEIGHT / 2);
        }
    }

    public boolean mouseOnGhost(float mouseX, float mouseY) {
        if (align == Alignment.Horizontal) {
            return (mouseX < ghostPos + HANDLE_WIDTH / 2 &&
                    mouseX > ghostPos - HANDLE_WIDTH / 2 &&
                    mouseY < sliderLine + HANDLE_HEIGHT / 2 &&
                    mouseY > sliderLine - HANDLE_HEIGHT / 2);
        } else {
            return (mouseY < ghostPos + HANDLE_WIDTH / 2 &&
                    mouseY > ghostPos - HANDLE_WIDTH / 2 &&
                    mouseX < sliderLine + HANDLE_HEIGHT / 2 &&
                    mouseX > sliderLine - HANDLE_HEIGHT / 2);
        }
    }

    public void draw(PEmbed applet) {
        if (align == Alignment.Horizontal) {
            applet.stroke(0);
            applet.strokeWeight(3);
            applet.line(sliderStart, sliderLine, sliderEnd, sliderLine);

            applet.noStroke();
            applet.imageMode(PEmbed.CENTER);
            applet.image(texture, sliderPos, sliderLine);

            applet.fill(0);
            applet.textAlign(PEmbed.LEFT);
            float labelWidth = applet.textWidth(label);
            applet.text(label, sliderStart - labelWidth - 10, sliderLine + 2);
            applet.text(stratLabel, sliderEnd + 10, sliderLine + 2);

            if (ghosting) {
                applet.image(ghostTexture, ghostPos, sliderLine);

                applet.fill(120);
                applet.text(ghostLabel, sliderEnd + 10, sliderLine + 20);
            }
        } else {
            applet.stroke(0);
            applet.strokeWeight(3);
            applet.line(sliderLine, sliderStart, sliderLine, sliderEnd);

            applet.noStroke();
            applet.imageMode(PEmbed.CENTER);
            applet.image(texture, sliderLine, sliderPos);

            applet.fill(0);
            applet.textAlign(PEmbed.CENTER);
            float labelHeight = applet.textAscent() + applet.textDescent();
            applet.text(label, sliderLine,  sliderStart - labelHeight);
            applet.text(stratLabel, sliderLine, sliderEnd + labelHeight);

            if (ghosting) {
                applet.image(ghostTexture, sliderLine, ghostPos);

                applet.fill(120);
                applet.text(ghostLabel, sliderLine, sliderEnd + 2 * labelHeight);
            }
        }
    }

    private void textureSetup(PEmbed applet) {
        int width, height;
        if (align == Alignment.Horizontal) {
            texture = applet.createImage(HANDLE_WIDTH, HANDLE_HEIGHT, PEmbed.ARGB);
            ghostTexture = applet.createImage(HANDLE_WIDTH, HANDLE_HEIGHT, PEmbed.ARGB);
            width = HANDLE_WIDTH;
            height = HANDLE_HEIGHT;
        } else {
            texture = applet.createImage(HANDLE_HEIGHT, HANDLE_WIDTH, PEmbed.ARGB);
            ghostTexture = applet.createImage(HANDLE_HEIGHT, HANDLE_WIDTH, PEmbed.ARGB);
            width = HANDLE_HEIGHT;
            height = HANDLE_WIDTH;
        }
        texture.loadPixels();
        ghostTexture.loadPixels();

        int centerX = width / 2 - 1;
        int centerY = height / 2 - 1;

        float maxDist = PEmbed.sqrt(PEmbed.sq(width / 2) + PEmbed.sq(height / 2));

        for (int i = 0; i < texture.pixels.length; ++i) {
            int x = i % width;
            int y = i / width;

            float distance = PEmbed.dist(x, y, centerX, centerY);
            float percent = 1 - distance / maxDist;

            if (percent > .25f) {
                texture.pixels[i] = applet.color(R * percent, G * percent, B * percent, 255 * percent);
                ghostTexture.pixels[i] = applet.color(R * percent, G * percent, B * percent, 255 * (1 - percent));
            } else {
                texture.pixels[i] = applet.color(0, 0, 0, 0);
                ghostTexture.pixels[i] = applet.color(0, 0, 0, 0);
            }
        }

        texture.updatePixels();
        ghostTexture.updatePixels();
    }
}
