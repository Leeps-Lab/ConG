package edu.ucsc.leeps.fire.cong;

import edu.ucsc.leeps.fire.FIRE;
import edu.ucsc.leeps.fire.cong.client.ClientState;
import edu.ucsc.leeps.fire.cong.client.Client;
import edu.ucsc.leeps.fire.cong.config.PeriodConfig;
import edu.ucsc.leeps.fire.cong.server.Server;

/**
 *
 * @author jpettit
 */
public class Main {

    public static void main(String[] args) throws Exception {
        FIRE.startServer(Server.class, PeriodConfig.class, ClientState.class, "configs/rps.csv");
        FIRE.startClient(Client.class, "Player", "One");
        FIRE.startClient(Client.class, "Player", "Two");
    }
}
