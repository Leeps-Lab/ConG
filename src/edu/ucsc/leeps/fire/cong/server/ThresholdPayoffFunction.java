/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.ucsc.leeps.fire.cong.server;

/**
 *
 * @author lakersparks
 */
public class ThresholdPayoffFunction extends TwoStrategyPayoffFunction {
    public float threshold;

    @Override
    public float getPayoff(
            float percent, float[] myStrategy, float[] opponentStrategy) {
        if (opponentStrategy[0] >= threshold)
            return ((myStrategy[0] * Aa) + ((1 - myStrategy[0]) * Ba));
        else
            return ((myStrategy[0] * Ab) + ((1 - myStrategy[0]) * Bb));
    }
}
