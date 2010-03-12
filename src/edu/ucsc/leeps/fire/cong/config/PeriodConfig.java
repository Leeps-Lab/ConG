/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.config;

import edu.ucsc.leeps.fire.cong.server.PayoffFunction;
import edu.ucsc.leeps.fire.cong.server.Population;
import edu.ucsc.leeps.fire.cong.server.SinglePopulationExclude;
import edu.ucsc.leeps.fire.cong.server.SinglePopulationInclude;
import edu.ucsc.leeps.fire.cong.server.ThreeStrategyPayoffFunction;
import edu.ucsc.leeps.fire.cong.server.TwoPopulation;
import edu.ucsc.leeps.fire.cong.server.TwoStrategyPayoffFunction;

/**
 *
 * @author jpettit
 */
public class PeriodConfig extends edu.ucsc.leeps.fire.server.BasePeriodConfig {

    public float initialStrategy;
    public boolean pointsPerSecond;
    public PayoffFunction payoffFunction;
    public PayoffFunction counterpartPayoffFunction;
    public Population population;
    public TwoStrategySelectionType twoStrategySelectionType;
    public static final Class homotopy = TwoStrategyPayoffFunction.class;
    public static final Class rps = ThreeStrategyPayoffFunction.class;
    public static final Class singlePopulationInclude = SinglePopulationInclude.class;
    public static final Class singlePopulationExclude = SinglePopulationExclude.class;
    public static final Class twoPopulation = TwoPopulation.class;

    // Default options
    public PeriodConfig() {
        length = 60;
        paid = true;
        timeConstrained = true;
        initialStrategy = 0.0f;
        pointsPerSecond = false;
        TwoStrategyPayoffFunction u = new TwoStrategyPayoffFunction();
        float v = 5;
        float c = 7;
        u.AaStart = (v - c) / 2f;
        u.AaEnd = u.AaStart;
        u.Ab = v;
        u.Ba = 0;
        u.Bb = (v / 2f);
        this.payoffFunction = u;
        u = new TwoStrategyPayoffFunction();
        u.AaStart = (v - c) / 2f;
        u.AaEnd = u.AaStart;
        u.Ab = 0;
        u.Ba = v;
        u.Bb = (v / 2f);
        this.counterpartPayoffFunction = u;
        this.population = new TwoPopulation();
        this.twoStrategySelectionType = TwoStrategySelectionType.HeatmapSingle;
    }
}
