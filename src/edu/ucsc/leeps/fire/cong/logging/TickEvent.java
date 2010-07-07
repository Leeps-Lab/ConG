package edu.ucsc.leeps.fire.cong.logging;

import edu.ucsc.leeps.fire.cong.server.PayoffFunction;
import edu.ucsc.leeps.fire.logging.LogEvent;

/**
 *
 * @author dev
 */
public class TickEvent implements LogEvent {

    public int subperiod;
    public int millisLeft;
    public int id;
    public int counterpartId;
    public float[] currentStrategy;
    public float[] targetStrategy;
    public float[] hoverStrategy_A, hoverStrategy_a;
    public float[] counterpartCurrentStrategy;
    public float[] counterpartTargetStrategy;
    public float[] counterpartHoverStrategy_A, counterpartHoverStrategy_a;
    public PayoffFunction payoffFunction;
    public PayoffFunction counterpartPayoffFunction;
}
