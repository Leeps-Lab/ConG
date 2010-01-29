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
    public RPSDPayoffFunction RPSDPayoffFunction;
    public Class homotopy = HomotopyPayoffFunction.class;
    public Class rpsd = RPSDPayoffFunction.class;
}
