/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.server.BaseLog;

/**
 *
 * @author jpettit
 */
public class TickLog extends BaseLog {

    public long secondsLeft;
    public Population population;

    public TickLog() {
        try {
            init("ticks");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
