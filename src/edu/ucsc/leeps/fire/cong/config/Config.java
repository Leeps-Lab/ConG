/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.config;

import edu.ucsc.leeps.fire.cong.client.gui.Line;
import edu.ucsc.leeps.fire.cong.server.PairedPopulation;
import edu.ucsc.leeps.fire.cong.server.PayoffFunction;
import edu.ucsc.leeps.fire.cong.server.Population;
import edu.ucsc.leeps.fire.cong.server.SinglePopulationExclude;
import edu.ucsc.leeps.fire.cong.server.SinglePopulationInclude;
import edu.ucsc.leeps.fire.cong.server.ThreeStrategyPayoffFunction;
import edu.ucsc.leeps.fire.cong.server.TwoPopulation;
import edu.ucsc.leeps.fire.cong.server.TwoStrategyPayoffFunction;
import edu.ucsc.leeps.fire.cong.server.UltimatumPayoffFunction;
import edu.ucsc.leeps.fire.config.BaseConfig;
import edu.ucsc.leeps.fire.cong.server.ThresholdPayoffFunction;

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
    public int subperiods;
    public TwoStrategySelectionType twoStrategySelectionType;
    public Line payoffAa, payoffAb, payoffBa, payoffBb, yourPayoff, otherPayoff;
    public Line yourStrategyOverTime, counterpartStrategyOverTime;
    public int heatmapColorLow, heatmapColorMid, heatmapColorHigh;
    public String rLabel, pLabel, sLabel, shortRLabel, shortPLabel, shortSLabel;
    public ShockZone shock;
    public float impulse;
    public static final Class homotopy = TwoStrategyPayoffFunction.class;
    public static final Class bimatrix = TwoStrategyPayoffFunction.class;
    public static final Class rps = ThreeStrategyPayoffFunction.class;
    public static final Class ultimatum = UltimatumPayoffFunction.class;
    public static final Class singlePopulationInclude = SinglePopulationInclude.class;
    public static final Class singlePopulationExclude = SinglePopulationExclude.class;
    public static final Class twoPopulation = TwoPopulation.class;
    public static final Class paired = PairedPopulation.class;
    public static final Class line = Line.class;
    public static final Class threshold = ThresholdPayoffFunction.class;
    public static final Class shockZone = ShockZone.class;
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
        subperiods = 0;
        population = new SinglePopulationInclude();
        twoStrategySelectionType = TwoStrategySelectionType.HeatmapBoth;
        mixedStrategySelection = true;
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
        heatmapColorLow = 0xff0000;
        heatmapColorMid = 0x0000ff;
        heatmapColorHigh = 0x00ff00;
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
        rLabel = "Rock";
        pLabel = "Paper";
        sLabel = "Scissors";
        shortRLabel = "R";
        shortPLabel = "P";
        shortSLabel = "S";
        shock = new ShockZone();
        shock.start = 0f;
        shock.end = 0f;
        impulse = 0f;
    }
}
