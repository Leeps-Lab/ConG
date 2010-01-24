/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.server;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author jpettit
 */
public class HomotopyPayoffFunction implements TwoStrategyPayoffFunction, Serializable {

    private float maxPercent;
    public float AaStart, AaEnd;
    public float Ab, Ba, Bb;
    private int numParameters = 6;

    @Override
    public int getPayoff(float percent, float A, float a) {
        float Aa;
        if (percent <= maxPercent) {
            Aa = AaStart + (percent * (AaEnd - AaStart));
        } else {
            Aa = AaStart + ((1 - percent) * (AaEnd - AaStart));
        }
        float B, b;
        B = 1 - A;
        b = 1 - a;
        return Math.round(A * (a * Aa + b * Ab) + B * (a * Ba + b * Bb));
    }

    public int getNumParameters() {
        return numParameters;
    }
}
