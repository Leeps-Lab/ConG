/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.logging;

import edu.ucsc.leeps.fire.server.BaseLog;

/**
 *
 * @author dev
 */
public class EventLog extends BaseLog {

    public long timestamp;
    public int subjectId;
    public float[] strategy;

    public EventLog() {
        try {
            init("events");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
