package edu.ucsc.leeps.fire.cong.config;

import java.io.Serializable;

/**
 *
 * @author jpettit
 */
public class DecisionDelay implements Serializable {

    public static enum Distribution {
        uniform, poisson, gaussian;
    };
    public Distribution distribution;
    public float lambda;
    public boolean initialLock;

    public DecisionDelay() {
        distribution = Distribution.uniform;
        lambda = 5;
        initialLock = true;
    }
}
