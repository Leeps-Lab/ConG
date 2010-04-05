package edu.ucsc.leeps.fire.cong.client;

import edu.ucsc.leeps.fire.client.BaseClient;
import edu.ucsc.leeps.fire.cong.config.ClientConfig;
import edu.ucsc.leeps.fire.cong.server.ServerInterface;
import edu.ucsc.leeps.fire.cong.config.PeriodConfig;
import edu.ucsc.leeps.fire.cong.server.ThreeStrategyPayoffFunction;
import edu.ucsc.leeps.fire.cong.server.TwoStrategyPayoffFunction;
import edu.ucsc.leeps.fire.server.BasePeriodConfig;
import java.io.IOException;
import java.io.InputStream;
import processing.core.PApplet;
import processing.core.PFont;

/**
 *
 * @author jpettit
 */
public class Client extends BaseClient implements ClientInterface {

    private int width, height;
    private PEmbed embed;
    private ServerInterface server;
    private float percent;
    private PeriodConfig periodConfig;
    private ClientConfig clientConfig;
    private Countdown countdown;
    private PointsDisplay pointsDisplay;
    private TwoStrategySelector bimatrix;
    private ThreeStrategySelector simplex;
    private Chart chart;

    //@Override
    public void init(edu.ucsc.leeps.fire.server.BaseServerInterface server) {
        this.server = (ServerInterface) server;
        removeAll();
        width = 1000;
        height = 600;
        embed = new PEmbed(width, height);
        embed.init();
        setSize(embed.getSize());
        add(embed);
        percent = -1;
        countdown = new Countdown(10, 20);
        pointsDisplay = new PointsDisplay(750, 20);
        bimatrix = new TwoStrategySelector(
                10, 100, 400, 400, embed,
                this.server, this);
        simplex = new ThreeStrategySelector(
                10, 100, 200, 600,
                embed,
                this.server,
                this);
        chart = new Chart(475, 100, 500, 400, simplex);
    }

    @Override
    public void startPeriod() {
        this.percent = 0;
        simplex.setEnabled(true);
        bimatrix.setEnabled(true);
        chart.clearAll();
        super.startPeriod();
    }

    @Override
    public void endPeriod() {
        simplex.reset();
        bimatrix.reset();
        bimatrix.setEnabled(false);
        super.endPeriod();
    }

    @Override
    public void setPause(boolean paused) {
        if (paused) {
            simplex.setEnabled(true);
        } else {
            simplex.pause();
        }
        bimatrix.setEnabled(!paused);
        super.setPause(paused);
    }

    @Override
    public void setPeriodConfig(BasePeriodConfig basePeriodConfig) {
        super.setPeriodConfig(basePeriodConfig);
        periodConfig = (PeriodConfig) basePeriodConfig;
        //this.clientConfig = (ClientConfig) superPeriodConfig.clientConfigs.get(getID());
        bimatrix.setPeriodConfig(periodConfig);
        simplex.setPeriodConfig(periodConfig);
        chart.setPeriodConfig(periodConfig);
    }

    @Override
    public void setPeriodPoints(float periodPoints) {
        super.setPeriodPoints(periodPoints);
        pointsDisplay.setPeriodPoints(periodPoints);
    }

    @Override
    public void addToPeriodPoints(float points) {
        super.addToPeriodPoints(points);
        pointsDisplay.setPeriodPoints(periodPoints);
    }

    public void localTick(int secondsLeft) {
        this.percent = embed.width * (1 - (secondsLeft / (float) periodConfig.length));
        countdown.setSecondsLeft(secondsLeft);
        bimatrix.update();
        simplex.update();
    }

    public void quickTick(int millisLeft) {
        if (millisLeft > 0) {
            this.percent = (1 - (millisLeft / ((float) periodConfig.length * 1000)));
            chart.currentPercent = this.percent;
            chart.updateLines();
            bimatrix.setCurrentPercent(this.percent);
            simplex.currentPercent = this.percent;
        }
    }

    public void setActionsEnabled(boolean enabled) {
        simplex.setEnabled(enabled);
    }

    public synchronized float[] getStrategy() {
        if (periodConfig.payoffFunction instanceof TwoStrategyPayoffFunction) {
            return bimatrix.getMyStrategy();
        } else if (periodConfig.payoffFunction instanceof ThreeStrategyPayoffFunction) {
            return simplex.getPlayerRPS();
        } else {
            assert false;
            return new float[]{};
        }
    }

    public synchronized void setMyStrategy(float[] s) {
        if (periodConfig.payoffFunction instanceof TwoStrategyPayoffFunction) {
            bimatrix.setMyStrategy(s[0]);
        } else if (periodConfig.payoffFunction instanceof ThreeStrategyPayoffFunction) {
            simplex.setPlayerRPS(s[0], s[1], s[2]);
        } else {
            assert false;
        }
        chart.setMyStrategy(s);
    }

    public synchronized void setOpponentStrategy(float[] s) {
        if (periodConfig.payoffFunction instanceof TwoStrategyPayoffFunction) {
            bimatrix.setOpponentStrategy(s[0]);
        } else if (periodConfig.payoffFunction instanceof ThreeStrategyPayoffFunction) {
            simplex.setOpponentRPS(s[0], s[1], s[2]);
        } else {
            assert false;
        }
        chart.setOpponentStrategy(s);
    }

    @Override
    public int getQuickTickInterval() {
        return 100;
    }

    public class PEmbed extends PApplet {

        private int initWidth, initHeight;
        public PFont size14, size14Bold, size16, size16Bold, size18, size18Bold, size24, size24Bold;

        public PEmbed(int initWidth, int initHeight) {
            this.initWidth = initWidth;
            this.initHeight = initHeight;
        }

        @Override
        public void setup() {
            size(initWidth, initHeight, PEmbed.P2D);
            smooth();
            try {
                InputStream fontInputStream = Client.class.getResourceAsStream("resources/DejaVuSans-14.vlw");
                size14 = new PFont(fontInputStream);
                fontInputStream = Client.class.getResourceAsStream("resources/DejaVuSans-Bold-14.vlw");
                size14Bold = new PFont(fontInputStream);
                fontInputStream = Client.class.getResourceAsStream("resources/DejaVuSans-16.vlw");
                size16 = new PFont(fontInputStream);
                fontInputStream = Client.class.getResourceAsStream("resources/DejaVuSans-Bold-16.vlw");
                size16Bold = new PFont(fontInputStream);
                fontInputStream = Client.class.getResourceAsStream("resources/DejaVuSans-18.vlw");
                size18 = new PFont(fontInputStream);
                fontInputStream = Client.class.getResourceAsStream("resources/DejaVuSans-Bold-18.vlw");
                size18Bold = new PFont(fontInputStream);
                fontInputStream = Client.class.getResourceAsStream("resources/DejaVuSans-24.vlw");
                size24 = new PFont(fontInputStream);
                fontInputStream = Client.class.getResourceAsStream("resources/DejaVuSans-Bold-24.vlw");
                size24Bold = new PFont(fontInputStream);
            } catch (IOException ex) {
                ex.printStackTrace();
                System.exit(1);
            }
            textFont(size16);
            textMode(SCREEN);
        }

        @Override
        public void draw() {
            background(255);
            fill(0);
            stroke(0);
            bimatrix.draw(embed);
            simplex.draw(embed);
            chart.draw(embed);
            countdown.draw(embed);
            pointsDisplay.draw(embed);
        }
    }

    public static void main(String[] args) throws Exception {
        Client client = new Client();
        if (args.length == 3) {
            Client.start(
                    args[0], args[1], args[2],
                    client, ServerInterface.class, ClientInterface.class);
        } else {
            Client.start(client, ServerInterface.class, ClientInterface.class);
        }
    }
}
