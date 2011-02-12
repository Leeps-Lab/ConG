/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.ucsc.leeps.fire.cong.logging;

import edu.ucsc.leeps.fire.logging.LogEvent;

/**
 *
 * @author subjects
 */
public class MessageEvent implements LogEvent {
    public String period;
    public int timestamp;
    public int subject;
    public int tuple;
    public String text;
}
