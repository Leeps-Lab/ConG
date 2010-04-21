package edu.ucsc.leeps.fire.cong.client;

import edu.ucsc.leeps.fire.client.BaseClient;
import edu.ucsc.leeps.fire.cong.config.ClientConfig;
import edu.ucsc.leeps.fire.cong.server.ServerInterface;
import edu.ucsc.leeps.fire.cong.config.PeriodConfig;
import edu.ucsc.leeps.fire.cong.server.PayoffFunction;
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
    private Chart payoffChart, strategyChart;
    private float[] myStrategy;
    private float[] counterpartStrategy;
    private boolean isCounterpart = false;
    public final static int SLEEP_TIME_MILLIS = 100;

    //@Override
    public void init(edu.ucsc.leeps.fire.server.BaseServerInterface server) {
        this.server = (ServerInterface) server;
        removeAll();
        width = 1200;
        height = 650;
        embed = new PEmbed(width, height);
        embed.init();
        setSize(embed.getSize());
        add(embed);
        percent = -1;
        countdown = new Countdown(230, 55);
        pointsDisplay = new PointsDisplay(230, (int) (55 + embed.textAscent() + embed.textDescent()));
        bimatrix = new TwoStrategySelector(
                20, 150, 400, 400, embed,
                this.server, this);
        simplex = new ThreeStrategySelector(
                20, 150, 200, 600,
                embed,
                this.server,
                this);
        payoffChart = new Chart(480, 150, 700, 400, simplex, Chart.Mode.Payoff);
        strategyChart = new Chart(480, 20, 700, 100, simplex, Chart.Mode.Strategy);
    }

    @Override
    public void startPeriod() {
        this.percent = 0;
        simplex.setEnabled(true);
        bimatrix.setEnabled(true);
        payoffChart.clearAll();
        strategyChart.clearAll();
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
        if (isCounterpart) {
            PayoffFunction tmp = periodConfig.payoffFunction;
            periodConfig.payoffFunction = periodConfig.counterpartPayoffFunction;
            periodConfig.counterpartPayoffFunction = tmp;
        }
        //this.clientConfig = (ClientConfig) superPeriodConfig.clientConfigs.get(getID());
        bimatrix.setPeriodConfig(periodConfig);
        simplex.setPeriodConfig(periodConfig);
        payoffChart.setPeriodConfig(periodConfig);
        strategyChart.setPeriodConfig(periodConfig);
    }

    @Override
    public void setPeriodPoints(float periodPoints) {
        super.setPeriodPoints(periodPoints);
        pointsDisplay.setPoints(periodPoints, totalPoints);
    }

    @Override
    public void addToPeriodPoints(float points) {
        super.addToPeriodPoints(points);
        pointsDisplay.setPoints(periodPoints, totalPoints);
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
            payoffChart.currentPercent = this.percent;
            payoffChart.updateLines();
            strategyChart.currentPercent = this.percent;
            strategyChart.updateLines();
            bimatrix.setCurrentPercent(this.percent);
            simplex.currentPercent = this.percent;
        }
    }

    public void setActionsEnabled(boolean enabled) {
        simplex.setEnabled(enabled);
    }

    public synchronized float[] getStrategy() {
        if (periodConfig.payoffFunction instanceof TwoStrategyPayoffFunction) {
            myStrategy = bimatrix.getMyStrategy();
            return myStrategy;
        } else if (periodConfig.payoffFunction instanceof ThreeStrategyPayoffFunction) {
            myStrategy = simplex.getPlayerRPS();
            return myStrategy;
        } else {
            assert false;
            return new float[]{};
        }
    }

    public synchronized void setMyStrategy(float[] s) {
        myStrategy = s;
        if (periodConfig.payoffFunction instanceof TwoStrategyPayoffFunction) {
            bimatrix.setMyStrategy(s[0]);
        } else if (periodConfig.payoffFunction instanceof ThreeStrategyPayoffFunction) {
            simplex.setPlayerRPS(s[0], s[1], s[2]);
        } else {
            assert false;
        }
        payoffChart.setMyStrategy(s);
        strategyChart.setMyStrategy(s);
    }

    public synchronized void setCounterpartStrategy(float[] s) {
        counterpartStrategy = s;
        if (periodConfig.payoffFunction instanceof TwoStrategyPayoffFunction) {
            bimatrix.setCounterpartStrategy(s[0]);
        } else if (periodConfig.payoffFunction instanceof ThreeStrategyPayoffFunction) {
            simplex.setCounterpartRPS(s[0], s[1], s[2]);
        } else {
            assert false;
        }
        payoffChart.setCounterpartStrategy(s);
        strategyChart.setCounterpartStrategy(s);
    }

    @Override
    public int getQuickTickInterval() {
        return SLEEP_TIME_MILLIS;
    }

    public void setIsCounterpart(boolean isCounterpart) {
        this.isCounterpart = isCounterpart;
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
            frameRate(30);
        }

        @Override
        public void draw() {
            background(255);
            fill(0);
            stroke(0);
            try {
                bimatrix.draw(embed);
                simplex.draw(embed);
                strategyChart.draw(embed);
                payoffChart.draw(embed);
                countdown.draw(embed);
                pointsDisplay.draw(embed);
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }
            //System.err.println(frame)
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
