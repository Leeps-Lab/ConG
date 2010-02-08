/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.server;

/**
 *
 * @author jpettit
 */
public class PeriodConfig extends edu.ucsc.leeps.fire.server.PeriodConfig {

    public float initialStrategy;
    public boolean pointsPerSecond;
    public TwoStrategyPayoffFunction twoStrategyPayoffFunction;
    public RPSPayoffFunction RPSPayoffFunction;
    public static final Class homotopy = HomotopyPayoffFunction.class;
    public static final Class rps = RPSPayoffFunction.class;
}
