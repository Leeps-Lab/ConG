/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.server;

import java.io.Serializable;

/**
 *
 * @author jpettit
 */
public class HomotopyPayoffFunction implements TwoStrategyPayoffFunction, Serializable {

    private float maxPercent;
    public float AaStart, AaEnd;
    public float Ab, Ba, Bb;

    @Override
    public float getMax() {
        return Math.max(AaStart, Math.max(AaEnd, Math.max(Ab, Math.max(Ba, Bb))));
    }

    @Override
    public float getMin() {
        return Math.min(AaStart, Math.max(AaEnd, Math.max(Ab, Math.max(Ba, Bb))));
    }

    @Override
    public float getPayoff(
            float percent,
            float A, float B,
            float a, float b) {
        float Aa;
        Aa = AaStart + (percent * (AaEnd - AaStart));
        return A * (a * Aa + b * Ab) + B * (a * Ba + b * Bb);
    }
}
