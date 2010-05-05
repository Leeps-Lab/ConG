/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.server;

/**
 *
 * @author jpettit
 */
public interface ServerInterface {

    public void strategyChanged(float[] newStrategy, float[] targetStrategy, float[][] hoverStrategy, Integer id);
}
