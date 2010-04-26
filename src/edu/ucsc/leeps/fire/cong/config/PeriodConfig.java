/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.config;

import edu.ucsc.leeps.fire.cong.client.Line;
import edu.ucsc.leeps.fire.cong.server.PairedPopulation;
import edu.ucsc.leeps.fire.cong.server.PayoffFunction;
import edu.ucsc.leeps.fire.cong.server.Population;
import edu.ucsc.leeps.fire.cong.server.SinglePopulationExclude;
import edu.ucsc.leeps.fire.cong.server.SinglePopulationInclude;
import edu.ucsc.leeps.fire.cong.server.ThreeStrategyPayoffFunction;
import edu.ucsc.leeps.fire.cong.server.TwoPopulation;
import edu.ucsc.leeps.fire.cong.server.TwoStrategyPayoffFunction;
import edu.ucsc.leeps.fire.cong.server.UltimatumPayoffFunction;
import edu.ucsc.leeps.fire.server.BasePeriodConfig;

/**
 *
 * @author jpettit
 */
public class PeriodConfig extends BasePeriodConfig {

    public float initialStrategy;
    public boolean pointsPerSecond;
    public float percentChangePerSecond;
    public PayoffFunction payoffFunction;
    public PayoffFunction counterpartPayoffFunction;
    public Population population;
    public TwoStrategySelectionType twoStrategySelectionType;
    public Line payoffAa, payoffAb, payoffBa, payoffBb, yourPayoff, otherPayoff;
    public Line yourStrategyOverTime, counterpartStrategyOverTime;
    public int heatmapColorLow, heatmapColorMid, heatmapColorHigh;
    public static final Class homotopy = TwoStrategyPayoffFunction.class;
    public static final Class rps = ThreeStrategyPayoffFunction.class;
    public static final Class ultimatum = UltimatumPayoffFunction.class;
    public static final Class singlePopulationInclude = SinglePopulationInclude.class;
    public static final Class singlePopulationExclude = SinglePopulationExclude.class;
    public static final Class twoPopulation = TwoPopulation.class;
    public static final Class paired = PairedPopulation.class;
    public static final Class line = Line.class;

    /*
     * Setup whatever defaults you like here.
     */
    public PeriodConfig() {
        initialStrategy = 0.0f;
        pointsPerSecond = false;
        percentChangePerSecond = 0.1f;
        payoffFunction = new TwoStrategyPayoffFunction();
        counterpartPayoffFunction = new TwoStrategyPayoffFunction();
        population = new PairedPopulation();
        twoStrategySelectionType = TwoStrategySelectionType.HeatmapBoth;
        yourPayoff = new Line();
        otherPayoff = new Line();
        heatmapColorLow = 0xFF0000;
        heatmapColorMid = 0x00FF00;
        heatmapColorHigh = 0x0000FF;
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
    }
}
