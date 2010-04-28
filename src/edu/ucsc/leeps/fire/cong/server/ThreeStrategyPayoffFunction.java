package edu.ucsc.leeps.fire.cong.server;

import java.io.Serializable;
import java.util.Collections;

/**
 *
 * @author jpettit
 */
public class ThreeStrategyPayoffFunction implements PayoffFunction, Serializable {

    public float Rr, Rp, Rs,
            Pr, Pp, Ps,
            Sr, Sp, Ss;

    public ThreeStrategyPayoffFunction() {
        Rr = 0;
        Rp = 0;
        Rs = 100;
        Pr = 100;
        Pp = 0;
        Ps = 0;
        Sr = 0;
        Sp = 100;
        Ss = 0;
    }

    public float getMin() {
        return 0;
    }

    public float getMax() {
        return 100;
    }

    public float getPayoff(
            float percent,
            float[] myStrategy, float[] opponentStrategy) {
        float A, B, C, a, b, c;
        A = myStrategy[0];
        B = myStrategy[1];
        C = myStrategy[2];
        a = opponentStrategy[0];
        b = opponentStrategy[1];
        c = opponentStrategy[2];
        return A * (Rr * a + Rp * b + Rs * c)
                + B * (Pr * a + Pp * b + Ps * c)
                + C * (Sr * a + Sp * b + Ss * c);
    }
}
