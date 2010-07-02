package edu.ucsc.leeps.fire.cong.client.gui;

import edu.ucsc.leeps.fire.cong.client.Client.PEmbed;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

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
        super(x, y, 0, 0);
        this.parent = parent;
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
                buttons[i] = new RadioButton(0, buffer + i * spacing, buttonRadius);
            }
        } else {
            for (int i = 0; i < numButtons; ++i) {
                buttons[i] = new RadioButton(buffer + i * spacing, 0, buttonRadius);
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

    public void clearSelections() {
        if (selectedButton == NO_BUTTON) {
            return;
        }

        buttons[selectedButton].setSelected(false);
        selectedButton = NO_BUTTON;
    }
    
    private class RadioButton extends Sprite {
        private boolean selected;

        public RadioButton(float x, float y, int radius) {
            super(x, y, radius, radius);
            selected = false;
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
                applet.ellipseMode(PEmbed.CENTER);
                applet.strokeWeight(1);
                applet.stroke(0);
                if (selected) {
                    applet.fill(0);
                } else {
                    applet.fill(255);
                }

                applet.ellipse(origin.x, origin.y, width, height);
            }
        }
    }
}
