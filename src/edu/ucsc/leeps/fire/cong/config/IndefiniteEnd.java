package edu.ucsc.leeps.fire.cong.config;

import java.io.Serializable;
import java.util.Random;

/**
 *
 * @author jpettit
 */
public interface IndefiniteEnd extends Serializable {

    public int length(Random random);

    public class Uniform implements IndefiniteEnd {

        public int max;

        public int length(Random random) {
            return random.nextInt(max);
        }
    }
}