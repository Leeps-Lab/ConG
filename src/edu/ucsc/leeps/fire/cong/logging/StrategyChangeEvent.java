package edu.ucsc.leeps.fire.cong.logging;

import edu.ucsc.leeps.fire.cong.server.PayoffFunction;
import edu.ucsc.leeps.fire.logging.LogEvent;

public class StrategyChangeEvent implements LogEvent {

    public int changedId;
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
