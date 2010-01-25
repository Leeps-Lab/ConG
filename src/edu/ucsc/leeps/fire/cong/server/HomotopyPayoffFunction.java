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
    public int getPayoff(
            float percent,
            float A, float B,
            float a, float b) {
        float Aa;
        if (percent <= maxPercent) {
            Aa = AaStart + (percent * (AaEnd - AaStart));
        } else {
            Aa = AaStart + ((1 - percent) * (AaEnd - AaStart));
        }
        return Math.round(A * (a * Aa + b * Ab) + B * (a * Ba + b * Bb));
    }
}
