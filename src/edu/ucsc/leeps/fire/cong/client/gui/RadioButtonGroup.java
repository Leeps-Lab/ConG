package edu.ucsc.leeps.fire.cong.client.gui;

import edu.ucsc.leeps.fire.cong.client.Client.PEmbed;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import processing.core.PImage;

/**
 *
 * @author swolpert
 */
public class RadioButtonGroup extends Sprite implements MouseListener {

    public static enum Alignment {Horizontal, Vertical};

    public static final int NO_BUTTON = -1;

    // parent Sprite - used to adjust mouse position on clicks
    private Sprite parent;
    // applet in which button group exists - used to add and remove
    // group as a MouseListener
    private PEmbed applet;

    // number of buttons in group
    private int numButtons;
    // how the buttons are aligned
    private Alignment alignment;
    // array storing buttons themselves
    private RadioButton[] buttons;

    // button currently pressed
    private int selectedButton;

    // distance between buttons - buttons are evenly spaced
    private float spacing;

    // whether or not group is enabled
    private boolean enabled;

    public RadioButtonGroup(Sprite parent, float x, float y, int length,
            int numButtons, Alignment alignment, int buttonRadius, PEmbed applet) {
        super(parent, x, y, 0, 0);
        this.numButtons = numButtons;
        this.alignment = alignment;
        this.applet = applet;
        // group has only a height or a width, depending on alignment
        if (alignment == Alignment.Horizontal) {
            width = length;
        } else {
            height = length;
        }

        buttons = new RadioButton[numButtons];

        selectedButton = NO_BUTTON;

        spacing = (float)length / (float)numButtons;

        // buffer space at beginning and end of row of buttons
        float buffer = spacing / 2f;

        // initialize buttons
        if (alignment == Alignment.Vertical) {
            for (int i = 0; i < numButtons; ++i) {
                buttons[i] = new RadioButton(parent, 0, buffer + i * spacing, buttonRadius, this);
            }
        } else {
            for (int i = 0; i < numButtons; ++i) {
                buttons[i] = new RadioButton(parent, buffer + i * spacing, 0, buttonRadius, this);
            }
        }

        enabled = false;
    }

    @Override
    public void draw(PEmbed applet) {
        if (visible) {
            applet.pushMatrix();
            applet.translate(origin.x, origin.y);

            for(int i = 0; i < numButtons; ++i) {
                buttons[i].draw(applet);
            }

            applet.popMatrix();
        }
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        if (enabled) {
            // adjust mouse position
            float mouseX = e.getX() - origin.x;
            float mouseY = e.getY() - origin.y;
            if (parent != null) {
                mouseX -= parent.origin.x;
                mouseY -= parent.origin.y;
            }
            // check to see if any of the buttons in the group were clicked on
            for (int i = 0; i < numButtons; ++i) {
                if (buttons[i].circularIsHit(mouseX, mouseY)) {
                    if (selectedButton != NO_BUTTON) {
                        buttons[selectedButton].setSelected(false);
                    }
                    buttons[i].setSelected(true);
                    selectedButton = i;
                }
            }
        }
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setVisible(boolean isVisible) {
        visible = isVisible;
        for (int i = 0; i < numButtons; ++i) {
            buttons[i].setVisible(isVisible);
        }

        if (isVisible) {
            applet.addMouseListener(this);
        } else {
            applet.removeMouseListener(this);
        }
    }

    public void setSelection(int selection) {
        if (selectedButton != NO_BUTTON) {
            buttons[selectedButton].setSelected(false);
        }
        selectedButton = selection;
        buttons[selectedButton].setSelected(true);
    }

    public int getSelection() {
        return selectedButton;
    }

    public int getNumButtons() {
        return numButtons;
    }

    public Alignment getAlignment() {
        return alignment;
    }

    public void clearSelections() {
        if (selectedButton == NO_BUTTON) {
            return;
        }

        buttons[selectedButton].setSelected(false);
        selectedButton = NO_BUTTON;
    }

    public void setLabelMode(Marker.LabelMode labelMode) {
        for (RadioButton button : buttons) {
            button.setLabelMode(labelMode);
        }
    }
    
    public void setLabels(String[] labels) {
        for (int i = 0; i < numButtons; ++i) {
            buttons[i].setLabel(labels[i]);
        }
    }
    
    private class RadioButton extends Marker {
        private RadioButtonGroup group;
        private boolean selected;
        private PImage idleTexture;
        private PImage selectedTexture;

        public RadioButton(Sprite parent, float x, float y, int radius,
                RadioButtonGroup group) {
            super(parent, x, y, true, radius);
            this.group = group;
            selected = false;
            textureSetup();
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        @Override
        public void draw(PEmbed applet) {
            if (visible) {
                if (label1 != null) {
                    drawLabels(applet);
                }

                applet.ellipseMode(PEmbed.CENTER);
                applet.imageMode(PEmbed.CENTER);

                applet.noStroke();
                applet.fill(245, 245, 245);
                applet.ellipse(origin.x, origin.y, width + 3, height + 3);
                
                applet.fill(0, 0, 0);
                applet.ellipse(origin.x, origin.y, width + 1, height + 1);
                
                if (selected) {
                    applet.image(selectedTexture, origin.x, origin.y);
                } else {
                    applet.image(idleTexture, origin.x, origin.y);
                }

            }
        }

        private void textureSetup() {
            idleTexture = applet.createImage(width, height, PEmbed.ARGB);
            idleTexture.loadPixels();
            selectedTexture = applet.createImage(width, height, PEmbed.ARGB);
            selectedTexture.loadPixels();
            float centerX = width / 2 - 1;
            float centerY = width / 2 - 1;
            for(int i = 0; i < idleTexture.pixels.length; ++i) {
                float x = i % width;
                float y = i / width;
                float distance = PEmbed.dist(x, y, centerX, centerY);
                if (distance < width / 2) {
                    idleTexture.pixels[i] = applet.color(175, 175, 175, 255);
                    selectedTexture.pixels[i] = applet.color(0, 32, 113, 255);
                } else {
                    idleTexture.pixels[i] = applet.color(255, 255, 255, 0);
                    selectedTexture.pixels[i] = applet.color(255, 255, 255, 0);
                }
            }

            centerX = width / 2 - 1;
            centerY = 0;
            for(int i = 0; i < idleTexture.pixels.length; ++i) {
                float x = i % width;
                float y = i / width;
                float distance = PEmbed.dist(x, y, centerX, centerY);
                if (distance < width / 2 &&
                    idleTexture.pixels[i] != applet.color(255, 255, 255, 0)) {
                    float adjustment = distance * 10;
                    idleTexture.pixels[i] = applet.color(237 - adjustment, 237 - adjustment, 237 - adjustment, 255);
                    selectedTexture.pixels[i] = applet.color(50 - adjustment, 140 - adjustment, 250 - adjustment, 255);
                }
            }

            centerX = width / 2 - 1;
            centerY = height - 1;
            for(int i = 0; i < idleTexture.pixels.length; ++i) {
                float x = i % width;
                float y = i / width;
                float distance = PEmbed.dist(x, y, centerX, centerY);
                if (distance < width / 2 &&
                    idleTexture.pixels[i] != applet.color(255, 255, 255, 0)) {
                    float adjustment = distance * 10;
                    idleTexture.pixels[i] = applet.color(237 - adjustment, 237 - adjustment, 237 - adjustment, 255);
                    selectedTexture.pixels[i] = applet.color(50 - adjustment, 140 - adjustment, 250 - adjustment, 255);
                }
            }

            centerX = width / 2 - 1;
            centerY = width / 2 - 1;
            for(int i = 0; i < idleTexture.pixels.length; ++i) {
                float x = i % width;
                float y = i / width;
                if (PEmbed.dist(x, y, centerX, centerY) < width / 4) {
                    idleTexture.pixels[i] = applet.color(175, 175, 175, 255);
                    selectedTexture.pixels[i] = applet.color(0, 0, 0, 255);
                }
            }

            idleTexture.updatePixels();
            selectedTexture.updatePixels();
        }

        @Override
        protected void drawLabels(PEmbed applet) {
            applet.textFont(applet.size14);
            float textWidth = applet.textWidth(label1);
            if (label2 != null) {
                applet.textFont(applet.size14Bold);
                textWidth += applet.textWidth(label2);
            }
            if (textWidth > 16 && mode == LabelMode.Left) {
                labelOrigin.x = origin.x - radius - textWidth / 2;
            } else if (textWidth > 16 && mode == LabelMode.Right) {
                labelOrigin.x = origin.x + radius + textWidth / 2;
            }
            float textHeight = applet.textAscent() + applet.textDescent();
            applet.rectMode(PEmbed.CENTER);
            applet.fill(255);
            applet.noStroke();
            applet.rect(labelOrigin.x, labelOrigin.y, textWidth, textHeight);
            applet.textAlign(PEmbed.CENTER, PEmbed.CENTER);
            applet.fill(0);
            if (label1 != null && label2 != null) {
                float label1Width = applet.textWidth(label1);
                applet.textFont(applet.size14Bold);
                applet.text(label1, parent.origin.x + group.origin.x + labelOrigin.x - label1Width / 2, parent.origin.y + group.origin.y + labelOrigin.y);
                applet.textFont(applet.size14);
                applet.text("," + label2, parent.origin.x + group.origin.x + labelOrigin.x + label1Width / 2, parent.origin.y + group.origin.y + labelOrigin.y);
            } else if (label1 != null) {
                applet.textFont(applet.size14);
                applet.text(label1, parent.origin.x + group.origin.x + labelOrigin.x, parent.origin.y + group.origin.y + labelOrigin.y);
            }
        }
    }
}
