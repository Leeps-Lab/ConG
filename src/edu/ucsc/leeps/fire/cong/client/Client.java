package edu.ucsc.leeps.fire.cong.client;

import edu.ucsc.leeps.fire.cong.server.ClientConfig;
import edu.ucsc.leeps.fire.cong.server.ServerInterface;
import edu.ucsc.leeps.fire.cong.server.PeriodConfig;
import processing.core.PApplet;
import processing.core.PFont;

/**
 *
 * @author jpettit
 */
public class Client extends edu.ucsc.leeps.fire.client.Client implements ClientInterface {

    private int width, height;
    private PEmbed embed;
    private ServerInterface server;
    private float percent;
    private PeriodConfig periodConfig;
    private ClientConfig clientConfig;
    private Countdown countdown;
    private PointsDisplay pointsDisplay;
    private BiMatrixDisplay bimatrix;
    private RPSDisplay rps;
    private Chart chart;

    @Override
    public void init(edu.ucsc.leeps.fire.server.ServerInterface server) {
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
        bimatrix = new BiMatrixDisplay(
                10, 100, embed,
                this.server, this);
        rps = new RPSDisplay(
                10, 100, 200, 600,
                embed,
                this.server,
                this);
        chart = new Chart(250, 100, 500, 400);
    }

    @Override
    public void startPeriod() {
        this.percent = 0;
        rps.setEnabled(true);
        super.startPeriod();
    }

    @Override
    public void endPeriod() {
        rps.reset();
        super.endPeriod();
    }

    @Override
    public void pause() {
        if (rps.isEnabled()) {
            rps.pause();
        } else {
            rps.setEnabled(true);
        }
        super.pause();
    }

    @Override
    public void setPeriodConfig(edu.ucsc.leeps.fire.server.PeriodConfig superPeriodConfig) {
        super.setPeriodConfig(superPeriodConfig);
        this.periodConfig = (PeriodConfig) superPeriodConfig;
        //this.clientConfig = (ClientConfig) superPeriodConfig.clientConfigs.get(getID());
        bimatrix.setPayoffFunction(periodConfig.twoStrategyPayoffFunction);
        rps.setPayoffFunction(periodConfig.RPSPayoffFunction);
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

    @Override
    public void tick(int secondsLeft) {
        this.percent = embed.width * (1 - (secondsLeft / (float) periodConfig.length));
        countdown.setSecondsLeft(secondsLeft);
        bimatrix.update();
        rps.update();
    }

    @Override
    public void quickTick(int millisLeft) {
        this.percent = (1 - (millisLeft / ((float) periodConfig.length * 1000)));
        chart.currentPercent = this.percent;
        chart.updateLines();
        bimatrix.currentPercent = this.percent;
        rps.currentPercent = this.percent;
    }

    @Override
    public void setActionsEnabled(boolean enabled) {
        rps.setEnabled(enabled);
    }

    public float[] getStrategy() {
        if (periodConfig.twoStrategyPayoffFunction != null) {
            return new float[]{bimatrix.percent_A, 1 - bimatrix.percent_A};
        } else if (periodConfig.RPSPayoffFunction != null) {
            return rps.getPlayerRPS();
        } else {
            assert false;
            return new float[]{};
        }
    }

    public void setMyStrategy(float[] s) {
        if (periodConfig.twoStrategyPayoffFunction != null) {
            bimatrix.percent_A = s[0];
        } else if (periodConfig.RPSPayoffFunction != null) {
            rps.setPlayerRPS(s[0], s[1], s[2]);
        } else {
            assert false;
        }
        chart.setMyStrategy(s);
    }

    public void setOpponentStrategy(float[] s) {
        if (periodConfig.twoStrategyPayoffFunction != null) {
            bimatrix.percent_a = s[0];
        } else if (periodConfig.RPSPayoffFunction != null) {
            rps.setOpponentRPS(s[0], s[1], s[2]);
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
            rps.draw(embed);
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
