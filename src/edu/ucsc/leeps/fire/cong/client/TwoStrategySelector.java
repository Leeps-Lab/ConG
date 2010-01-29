package edu.ucsc.leeps.fire.cong.client;

import processing.core.PApplet;

public class TwoStrategySelector extends Sprite {

    private Button option1, option2;
    private int selection;

    private class Button extends Sprite {
        // Fields ////////

        private boolean pressed;
        private boolean mouseOver;
        private String label;

        // Constructor //////////////
        public Button(float x, float y, int radius, String label) {
            super(x, y, radius, radius);

            this.label = label;

            pressed = false;
            mouseOver = false;
        }

        // Methods /////////////////
        // Access /////////
        public boolean isPressed() {
            return pressed;
        }

        public boolean mouseOnButton() {
            return mouseOver;
        }

        // Manipulation /////////
        public void press() {
            pressed = true;
        }

        public void release() {
            pressed = false;
        }

        public void determineMouseOver(float mouseX, float mouseY) {
            mouseOver = circularIsHit(mouseX, mouseY);
        }

        @Override
        public void draw(PApplet applet) {
            applet.pushMatrix();
            applet.translate(origin.x, origin.y);

            setScreenLocation(applet);

            applet.ellipseMode(PApplet.CENTER);

            // draw button
            if (pressed) {
                applet.stroke(0, 0, 255);
                applet.fill(0, 255, 0);
            } else {
                applet.stroke(0);
                applet.fill(255, 0, 0);
            }
            if (mouseOver) {
                applet.stroke(255, 255, 0);
            }

            applet.strokeWeight(3);
            applet.ellipse(0, 0, width, width);

            applet.noStroke();

            // draw label
            applet.fill(0);
            applet.textAlign(PApplet.CENTER);
            applet.text(label, 0, 4 + height);
            applet.popMatrix();
        }
    }

    public TwoStrategySelector(float x, float y, int width, int height,
            String label1, String label2) {
        super(x, y, width, height);

        option1 = new Button(width / 4, height / 2, (3 * height) / 8,
                label1);
        option2 = new Button(3 * (width / 4), height / 2, (3 * height) / 8,
                label2);

        selection = -1;
    }

    public int getSelection() {
        return selection;
    }

    public boolean mouseOnAButton(float mouseX, float mouseY) {
        option1.determineMouseOver(mouseX, mouseY);
        if (!option1.mouseOnButton()) {
            option2.determineMouseOver(mouseX, mouseY);
        }
        return option1.mouseOnButton() || option2.mouseOnButton();
    }

    public void pressButton() {
        if (option1.mouseOnButton() && !option1.isPressed()) {
            option1.press();
            selection = 1;
            if (option2.isPressed()) {
                option2.release();
            }
        } else if (option2.mouseOnButton() && !option2.isPressed()) {
            option2.press();
            selection = 2;
            if (option1.isPressed()) {
                option1.release();
            }
        }
    }

    public void chooseStrategyOne() {
        option1.press();
        selection = 1;
        option2.release();
    }

    public void chooseStrategyTwo() {
        option2.press();
        selection = 2;
        option1.release();
    }

    @Override
    public void draw(PApplet applet) {
        applet.pushMatrix();
        applet.translate(origin.x, origin.y);

        applet.rectMode(PApplet.CORNER);
        applet.noFill();
        applet.stroke(0);
        applet.strokeWeight(3);
        applet.rect(0, 0, width, height);

        option1.determineMouseOver(applet.mouseX, applet.mouseY);
        option2.determineMouseOver(applet.mouseX, applet.mouseY);
        option1.draw(applet);
        option2.draw(applet);

        applet.popMatrix();
    }
}
