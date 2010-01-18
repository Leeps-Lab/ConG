/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.client;

/**
 *
 * @author jpettit
 */
public class PeriodThread extends Thread {

    private Client client;
    private float length, currLength;

    public PeriodThread(Client client, int length) {
        this.client = client;
        this.currLength = this.length = length;
    }

    @Override
    public void run() {
        while (currLength > 0) {
            client.tick(currLength / (float)length);
            try {
                sleep(100);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            currLength -= 0.1;
        }
    }
}
