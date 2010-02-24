/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.server;

/**
 *
 * @author dev
 */
public interface PayoffFunction {

    public float getMin();

    public float getMax();

    public float getPayoff(float percent, float[] myStrategy, float[] opponentStrategy);
}
