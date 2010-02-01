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

    public RPSDPayoffFunction() {
        Rr = 0;
        Rp = 0;
        Rs = 100;
        Rd = 10;
        Pr = 100;
        Pp = 0;
        Ps = 0;
        Pd = 10;
        Sr = 0;
        Sp = 100;
        Ss = 0;
        Sd = 10;
        Dr = 10;
        Dp = 10;
        Ds = 10;
        Dd = 10;
    }

    public float getPayoff(
            float R, float P, float S, float D,
            float r, float p, float s, float d) {
        return R * (Rr * r + Rp * p + Rs * s + Rd * d)
                + P * (Rr * r + Rp * p + Rs * s + Rd * d)
                + S * (Rr * r + Rp * p + Rs * s + Rd * d)
                + D * (Rr * r + Rp * p + Rs * s + Rd * d);
    }
}
