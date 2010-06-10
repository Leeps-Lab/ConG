package edu.ucsc.leeps.fire.cong.logging;

import edu.ucsc.leeps.fire.cong.server.PayoffFunction;

public class EventLog {

    public int period;
    public long timestamp;
    public long periodStartTime;
    public int changedId;
    public int counterpartId;
    public boolean isCounterpart;
    public float[] currentStrategy;
    public float[] targetStrategy;
    public float[] hoverStrategy_A, hoverStrategy_a;
    public float[] counterpartCurrentStrategy;
    public float[] counterpartTargetStrategy;
    public float[] counterpartHoverStrategy_A, counterpartHoverStrategy_a;
    public PayoffFunction payoffFunction;
    public PayoffFunction counterpartPayoffFunction;
}
