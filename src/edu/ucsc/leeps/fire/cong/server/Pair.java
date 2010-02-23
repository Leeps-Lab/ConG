/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.client.ClientInterface;

/**
 *
 * @author dev
 */
public class Pair {
    public ClientInterface player1, player2;
    public long lastEvalTime;
    public float player1_A;
    public float player2_A;
    public float player1_R, player1_P, player1_S;
    public float player2_R, player2_P, player2_S;
}
