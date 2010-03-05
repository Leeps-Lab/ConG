/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.server;

/**
 *
 * @author jpettit
 */
public class PeriodConfig extends edu.ucsc.leeps.fire.server.BasePeriodConfig {

    public float initialStrategy;
    public boolean pointsPerSecond;
    public PayoffFunction payoffFunction;
    public Population population;
    public static final Class homotopy = TwoStrategyPayoffFunction.class;
    public static final Class rps = ThreeStrategyPayoffFunction.class;
    public static final Class pair = Pair.class;
    public static final Class singlePopulationInclude = SinglePopulationInclude.class;
}
