/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.ucsc.leeps.fire.cong.client.gui;

import edu.ucsc.leeps.fire.config.Configurable;
import edu.ucsc.leeps.fire.cong.client.Client.PEmbed;
import edu.ucsc.leeps.fire.cong.client.StrategyChanger;
import edu.ucsc.leeps.fire.cong.config.Config;
import edu.ucsc.leeps.fire.cong.server.PayoffFunction;
import java.awt.Color;

/**
 *
 * @author swolpert
 */
public class OneStrategyStripSelector extends Sprite implements Configurable<Config> {
    public static enum Alignment { Horizontal, Vertical };

    private Alignment align;
    private PEmbed applet;
    private StrategyChanger strategyChanger;
    private float myStrat;
    private float opponentStrat;
    private Slider slider;
    private float currentPercent;
    private PayoffFunction payoffFucntion, counterpartPayoffFunction;

    public OneStrategyStripSelector(Alignment align, int x, int y, int width, int height,
            PEmbed applet, StrategyChanger strategyChanger) {
        super(x, y, width, height);
        this.align = align;
        this.applet = applet;
        this.strategyChanger = strategyChanger;

        if (align == Alignment.Horizontal) {
            slider = new Slider(applet, this, Slider.Alignment.Horizontal, 0, width, height / 2f, Color.black, "A", 1f);
        } else {
            slider = new Slider(applet, this, Slider.Alignment.Vertical, 0, height, width / 2f, Color.black, "A", 1f);
        }
    }
    @Override
    public void draw(PEmbed applet) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void configChanged(Config config) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
