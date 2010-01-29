package edu.ucsc.leeps.fire.cong.client;

import edu.ucsc.leeps.fire.cong.server.ClientConfig;
import edu.ucsc.leeps.fire.cong.server.ServerInterface;
import edu.ucsc.leeps.fire.cong.server.PeriodConfig;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import processing.core.PApplet;
import processing.core.PFont;

/**
 *
 * @author jpettit
 */
public class Client extends edu.ucsc.leeps.fire.client.Client implements ClientInterface, KeyListener {

    private int width, height;
    private PEmbed embed;
    private ServerInterface server;
    private float percent;
    private float percent_A, percent_a;
    private PeriodConfig periodConfig;
    private ClientConfig clientConfig;
    private Countdown countdown;
    private PointsDisplay pointsDisplay;

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
        percent_A = percent_a = 0;
        embed.addKeyListener(this);
        countdown = new Countdown(10, 20);
        pointsDisplay = new PointsDisplay(750, 20);
    }

    @Override
    public void startPeriod() {
        this.percent = 0;
        super.startPeriod();
    }

    @Override
    public void setPeriodConfig(edu.ucsc.leeps.fire.server.PeriodConfig superPeriodConfig) {
        super.setPeriodConfig(superPeriodConfig);
        this.periodConfig = (PeriodConfig) superPeriodConfig;
        //this.clientConfig = (ClientConfig) superPeriodConfig.clientConfigs.get(getID());
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
    }

    @Override
    public void quickTick(int millisLeft) {
        this.percent = embed.width * (1 - (millisLeft / ((float) periodConfig.length * 1000)));
    }

    @Override
    public void setActionsEnabled(boolean enabled) {
    }

    @Override
    public void keyTyped(KeyEvent ke) {
    }

    @Override
    public void keyPressed(KeyEvent ke) {
        if (periodIsRunning()) {
            if (ke.isActionKey()) {
                if (ke.getKeyCode() == KeyEvent.VK_UP) {
                    percent_A += 0.01f;
                    server.strategyChanged(getFullName());
                } else if (ke.getKeyCode() == KeyEvent.VK_DOWN) {
                    percent_A -= 0.01f;
                    server.strategyChanged(getFullName());
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent ke) {
    }

    @Override
    public void setStrategyAB(float A, float B, float a, float b) {
        this.percent_A = A;
        this.percent_a = a;
    }

    @Override
    public float[] getStrategyAB() {
        return new float[]{percent_A, 1 - percent_A};
    }

    @Override
    public void setStrategyRPSD(
            float R, float P, float S, float D,
            float r, float p, float s, float d) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float[] getStrategyRPSD() {
        throw new UnsupportedOperationException("Not supported yet.");
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
            if (percent >= 0) {
                ellipse(percent, 20 + ((1 - percent_A) * (height - 40)), 10, 10);
                ellipse(percent, 20 + ((1 - percent_a) * (height - 40)), 10, 10);
            }
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
