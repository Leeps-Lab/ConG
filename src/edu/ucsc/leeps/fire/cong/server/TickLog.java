/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.server;

/**
 *
 * @author jpettit
 */
public class TickLog extends edu.ucsc.leeps.fire.server.Log {

    public int population;
    public float avgA, avgB;
    public float avgR, avgP, avgS;
    public PeriodConfig periodConfig;
    public float tick;

    public TickLog() {
        try {
            init("ticks");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
