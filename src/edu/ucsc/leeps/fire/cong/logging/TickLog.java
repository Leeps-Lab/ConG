/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.logging;

import edu.ucsc.leeps.fire.server.BaseLog;

/**
 *
 * @author jpettit
 */
public class TickLog extends BaseLog {

    public long secondsLeft;

    public TickLog() {
        try {
            init("ticks");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
