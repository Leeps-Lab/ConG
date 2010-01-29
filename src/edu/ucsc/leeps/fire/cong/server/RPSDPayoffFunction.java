package edu.ucsc.leeps.fire.cong.server;

import java.io.Serializable;

/**
 *
 * @author jpettit
 */
public class RPSDPayoffFunction implements Serializable {

    public float Rr, Rp, Rs, Rd,
            Pr, Pp, Ps, Pd,
            Sr, Sp, Ss, Sd,
            Dr, Dp, Ds, Dd;

    public float getPayoff(
            float R, float P, float S, float D,
            float r, float p, float s, float d) {
        return R * (Rr * r + Rp * p + Rs * s + Rd * d)
                + P * (Rr * r + Rp * p + Rs * s + Rd * d)
                + S * (Rr * r + Rp * p + Rs * s + Rd * d)
                + D * (Rr * r + Rp * p + Rs * s + Rd * d);
    }
}
