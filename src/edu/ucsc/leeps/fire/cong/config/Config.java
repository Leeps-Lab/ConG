/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.config;

import edu.ucsc.leeps.fire.cong.client.gui.Line;
import edu.ucsc.leeps.fire.cong.server.PairedPopulation;
import edu.ucsc.leeps.fire.cong.server.PayoffFunction;
import edu.ucsc.leeps.fire.cong.server.Population;
import edu.ucsc.leeps.fire.cong.server.ThreeStrategyPayoffFunction;
import edu.ucsc.leeps.fire.cong.server.TwoStrategyPayoffFunction;
import edu.ucsc.leeps.fire.cong.server.UltimatumPayoffFunction;
import edu.ucsc.leeps.fire.config.BaseConfig;
import edu.ucsc.leeps.fire.cong.server.ThresholdPayoffFunction;
import java.awt.Color;

/**
 *
 * @author jpettit
 */
public class Config extends BaseConfig {

    public boolean pointsPerSecond;
    public float percentChangePerSecond;
    public PayoffFunction payoffFunction;
    public PayoffFunction counterpartPayoffFunction;
    public Population population;
    public boolean mixedStrategySelection;
    public boolean stripStrategySelection;
    public int subperiods;
    public StrategySelectionDisplayType strategySelectionDisplayType;
    public Line payoffAa, payoffAb, payoffBa, payoffBb, yourPayoff, otherPayoff;
    public Line yourStrategyOverTime, counterpartStrategyOverTime;
    public Line thresholdLine;
    public String rLabel, pLabel, sLabel, shortRLabel, shortPLabel, shortSLabel;
    public Color rColor, pColor, sColor;
    public boolean showRPSSliders;
    public ShockZone shock;
    public DecisionDelay initialDelay, delay;
    public float impulse;
    public float changeCost;
    public boolean showHeatmapLegend;
    public boolean chatroom;
    public boolean negativePayoffs;
    public static final Class homotopy = TwoStrategyPayoffFunction.class;
    public static final Class bimatrix = TwoStrategyPayoffFunction.class;
    public static final Class rps = ThreeStrategyPayoffFunction.class;
    public static final Class ultimatum = UltimatumPayoffFunction.class;
    public static final Class paired = PairedPopulation.class;
    public static final Class line = Line.class;
    public static final Class threshold = ThresholdPayoffFunction.class;
    public static final Class shockZone = ShockZone.class;
    public static final Class decisionDelay = DecisionDelay.class;
    // per-client
    public float[] initialStrategy;
    public int matchID;
    public boolean isCounterpart;

    public Config() {
        timeConstrained = true;
        paid = true;
        length = 120;
        pointsPerSecond = false;
        percentChangePerSecond = 0.1f;
        changeCost = 0;
        subperiods = 0;
        population = new PairedPopulation();
        strategySelectionDisplayType = StrategySelectionDisplayType.Matrix;
        mixedStrategySelection = true;
        stripStrategySelection = false;
        yourPayoff = new Line();
        yourPayoff.r = 0;
        yourPayoff.g = 255;
        yourPayoff.b = 0;
        yourPayoff.visible = true;
        yourPayoff.mode = Line.Mode.Shaded;
        otherPayoff = new Line();
        otherPayoff.r = 0;
        otherPayoff.g = 255;
        otherPayoff.b = 0;
        otherPayoff.visible = true;
        otherPayoff.mode = Line.Mode.Shaded;
        yourStrategyOverTime = new Line();
        yourStrategyOverTime.visible = true;
        yourStrategyOverTime.mode = Line.Mode.Solid;
        yourStrategyOverTime.r = 0;
        yourStrategyOverTime.g = 0;
        yourStrategyOverTime.b = 255;
        yourStrategyOverTime.alpha = 255;
        yourStrategyOverTime.weight = 2f;
        counterpartStrategyOverTime = new Line();
        counterpartStrategyOverTime.visible = true;
        counterpartStrategyOverTime.mode = Line.Mode.Solid;
        counterpartStrategyOverTime.r = 255;
        counterpartStrategyOverTime.g = 0;
        counterpartStrategyOverTime.b = 0;
        counterpartStrategyOverTime.alpha = 255;
        counterpartStrategyOverTime.weight = 2f;
        thresholdLine = new Line();
        thresholdLine.mode = Line.Mode.Dashed;
        thresholdLine.r = 255;
        thresholdLine.g = 170;
        thresholdLine.b = 0;
        thresholdLine.alpha = 255;
        thresholdLine.weight = 2f;
        rLabel = "Rock";
        pLabel = "Paper";
        sLabel = "Scissors";
        shortRLabel = "R";
        shortPLabel = "P";
        shortSLabel = "S";
        rColor = new Color(255, 255, 255);
        pColor = new Color(0, 0, 0);
        sColor = new Color(150, 150, 150);
        showRPSSliders = false;
        shock = new ShockZone();
        shock.start = 0f;
        shock.end = 0f;
        shock.backfill = false;
        impulse = 0f;
        showHeatmapLegend = true;
        chatroom = false;
        negativePayoffs = false;
    }
}
