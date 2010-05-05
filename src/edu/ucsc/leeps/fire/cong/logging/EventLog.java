/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.logging;

import edu.ucsc.leeps.fire.cong.server.PayoffFunction;
import edu.ucsc.leeps.fire.server.BaseLog;

/**
 *
 * @author dev
 */
public class EventLog extends BaseLog {

    public int period;
    public long timestamp;
    public int changedId;
    public int counterpartId;
    public boolean isCounterpart;
    public float[] currentStrategy;
    public float[] targetStrategy;
    public float[][] hoverStrategy;
    public float[] counterpartCurrentStrategy;
    public float[] counterpartTargetStrategy;
    public float[][] counterpartHoverStrategy;
    public PayoffFunction payoffFunction;
    public PayoffFunction counterpartPayoffFunction;

    public EventLog() {
        try {
            init("events");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
