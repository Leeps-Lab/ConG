/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.server.Log;

/**
 *
 * @author dev
 */
public class EventLog extends Log {

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
