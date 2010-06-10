package edu.ucsc.leeps.fire.cong.server;

/**
 *
 * @author jpettit
 */
public interface ServerInterface {

    public void strategyChanged(
            float[] newStrategy,
            float[] targetStrategy,
            float[] hoverStrategy_A,
            float[] hoverStrategy_a,
            Integer id);
}
