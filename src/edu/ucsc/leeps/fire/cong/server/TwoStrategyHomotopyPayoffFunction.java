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
public class TwoStrategyHomotopyPayoffFunction implements PayoffFunction, Serializable {

    private float maxPercent;
    public float AaStart, AaEnd;
    public float Ab, Ba, Bb;

    public float getMax() {
        return Math.max(AaStart, Math.max(AaEnd, Math.max(Ab, Math.max(Ba, Bb))));
    }

    public float getMin() {
        return Math.min(AaStart, Math.max(AaEnd, Math.max(Ab, Math.max(Ba, Bb))));
    }

    public float getPayoff(
            float percent, float[] myStrategy, float[] opponentStrategy) {
        float Aa;
        Aa = AaStart + (percent * (AaEnd - AaStart));
        float A, B, a, b;
        A = myStrategy[0];
        B = 1 - A;
        a = opponentStrategy[0];
        b = 1 - a;
        return A * (a * Aa + b * Ab) + B * (a * Ba + b * Bb);
    }
}
