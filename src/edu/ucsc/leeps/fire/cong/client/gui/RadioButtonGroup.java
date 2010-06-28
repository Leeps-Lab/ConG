package edu.ucsc.leeps.fire.cong.client.gui;

import edu.ucsc.leeps.fire.config.Configurable;
import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.client.Client.PEmbed;
import edu.ucsc.leeps.fire.cong.client.StrategyChanger;
import edu.ucsc.leeps.fire.cong.config.Config;
import edu.ucsc.leeps.fire.cong.config.TwoStrategySelectionType;
import edu.ucsc.leeps.fire.cong.server.PayoffFunction;
import edu.ucsc.leeps.fire.cong.server.TwoStrategyPayoffFunction;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 *
 * @author swolpert
 */
public class RadioButtonGroup extends Sprite implements MouseListener {

    public static enum Alignment {Horizontal, Vertical};

    private final int NO_BUTTON = -1;

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

    public RadioButtonGroup(float x, float y, int length,
            int numButtons, Alignment alignment, int buttonRadius) {
        super(x, y, 0, 0);
        this.numButtons = numButtons;
        this.alignment = alignment;
        // group has only a height or a width, depending on alignment
        if (alignment == Alignment.Horizontal) {
            width = length;
        } else {
            height = length;
        }

        buttons = new RadioButton[numButtons];

        selectedButton = NO_BUTTON;

        spacing = (float)length / (float)numButtons;

        // initialize buttons
        for (int i = 0; i < numButtons; ++i) {
            buttons[i] = new RadioButton(0, i * spacing, buttonRadius);
        }
    }

    @Override
    public void draw(PEmbed applet) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mouseClicked(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mouseEntered(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mouseExited(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mousePressed(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mouseReleased(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
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
                if (selected) {
                    applet.fill(0);
                } else {
                    applet.fill(255);
                    applet.stroke(0);
                }

                applet.ellipse(origin.x, origin.y, width, height);
            }
        }
    }
}
