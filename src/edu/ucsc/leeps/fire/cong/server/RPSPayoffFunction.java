package edu.ucsc.leeps.fire.cong.server;

import java.io.Serializable;

/**
 *
 * @author jpettit
 */
public class RPSPayoffFunction implements Serializable {

    public float Rr, Rp, Rs,
            Pr, Pp, Ps,
            Sr, Sp, Ss;

    public RPSPayoffFunction() {
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
            float R, float P, float S,
            float r, float p, float s) {
        return R * (Rr * r + Rp * p + Rs * s)
                + P * (Pr * r + Pp * p + Ps * s)
                + S * (Sr * r + Sp * p + Ss * s);
    }
}
