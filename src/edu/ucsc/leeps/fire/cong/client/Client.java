package edu.ucsc.leeps.fire.cong.client;

import edu.ucsc.leeps.fire.client.BaseClient;
import edu.ucsc.leeps.fire.cong.server.ClientConfig;
import edu.ucsc.leeps.fire.cong.server.ServerInterface;
import edu.ucsc.leeps.fire.cong.server.PeriodConfig;
import edu.ucsc.leeps.fire.cong.server.ThreeStrategyPayoffFunction;
import edu.ucsc.leeps.fire.cong.server.TwoStrategyPayoffFunction;
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
        width = 800;
        height = 600;
        embed = new PEmbed(width, height);
        embed.init();
        setSize(embed.getSize());
        add(embed);
        percent = -1;
        countdown = new Countdown(10, 20);
        pointsDisplay = new PointsDisplay(750, 20);
        bimatrix = new TwoStrategySelector(
                10, 100, embed,
                this.server, this);
        simplex = new ThreeStrategySelector(
                10, 100, 200, 600,
                embed,
                this.server,
                this);
        chart = new Chart(250, 100, 500, 400, simplex);
    }

    @Override
    public void startPeriod() {
        this.percent = 0;
        simplex.setEnabled(true);
        super.startPeriod();
    }

    @Override
    public void endPeriod() {
        simplex.reset();
        super.endPeriod();
    }

    @Override
    public void pause() {
        if (simplex.isEnabled()) {
            simplex.pause();
        } else {
            simplex.setEnabled(true);
        }
        super.pause();
    }

    @Override
    public void setPeriodConfig(edu.ucsc.leeps.fire.server.BasePeriodConfig superPeriodConfig) {
        super.setPeriodConfig(superPeriodConfig);
        this.periodConfig = (PeriodConfig) superPeriodConfig;
        //this.clientConfig = (ClientConfig) superPeriodConfig.clientConfigs.get(getID());
        if (periodConfig.payoffFunction instanceof TwoStrategyPayoffFunction) {
            bimatrix.setPayoffFunction(periodConfig.payoffFunction);
            bimatrix.setVisible(true);
            simplex.setVisible(false);
        } else if (periodConfig.payoffFunction instanceof ThreeStrategyPayoffFunction) {
            simplex.setPayoffFunction(periodConfig.payoffFunction);
            bimatrix.setVisible(false);
            simplex.setVisible(true);
        }
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

    //@Override
    public void tick(int secondsLeft) {
        this.percent = embed.width * (1 - (secondsLeft / (float) periodConfig.length));
        countdown.setSecondsLeft(secondsLeft);
        bimatrix.update();
        simplex.update();
    }

    //@Override
    public void quickTick(int millisLeft) {
        this.percent = (1 - (millisLeft / ((float) periodConfig.length * 1000)));
        chart.currentPercent = this.percent;
        chart.updateLines();
        bimatrix.setCurrentPercent(this.percent);
        simplex.currentPercent = this.percent;
    }

    //@Override
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
        private PFont font;

        public PEmbed(int initWidth, int initHeight) {
            this.initWidth = initWidth;
            this.initHeight = initHeight;
        }

        @Override
        public void setup() {
            size(initWidth, initHeight, PApplet.P2D);
            smooth();
            font = createFont("Mono", 12);
            textFont(font);
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
