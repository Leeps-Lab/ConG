package edu.ucsc.leeps.fire.cong.client;

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

    @Override
    public void init(edu.ucsc.leeps.fire.server.ServerInterface server) {
        this.server = (ServerInterface) server;
        removeAll();
        width = 400;
        height = 400;
        embed = new PEmbed(width, height);
        embed.init();
        setSize(embed.getSize());
        add(embed);
        percent = -1;
        percent_A = percent_a = 0;
        embed.addKeyListener(this);
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
    }

    @Override
    public void setPercent_A(float percent_A) {
        this.percent_A = percent_A;
    }

    @Override
    public void setPercent_a(float percent_a) {
        this.percent_a = percent_a;
    }

    @Override
    public float getPercent_A() {
        return percent_A;
    }

    @Override
    public void tick(int secondsLeft) {
        this.percent = embed.width * (1 - (secondsLeft / (float) periodConfig.length));
    }

    @Override
    public void quickTick(int millisLeft) {
        this.percent = embed.width * (1 - (millisLeft / ((float) periodConfig.length * 1000)));
    }

    @Override
    public void keyTyped(KeyEvent ke) {
    }

    @Override
    public void keyPressed(KeyEvent ke) {
        if (ke.isActionKey()) {
            if (ke.getKeyCode() == KeyEvent.VK_UP) {
                percent_A += 0.01f;
                server.strategyChanged(getClientName());
            } else if (ke.getKeyCode() == KeyEvent.VK_DOWN) {
                percent_A -= 0.01f;
                server.strategyChanged(getClientName());
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent ke) {
    }

    public class PEmbed extends PApplet {

        private int initWidth, initHeight;
        private PFont font;

        public PEmbed(int initWidth, int initHeight) {
            this.initWidth = initWidth;
            this.initHeight = initHeight;
            font = createFont("Mono", 12);
        }

        @Override
        public void setup() {
            size(initWidth, initHeight, PApplet.P2D);
            smooth();
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
        }
    }

    public static void main(String[] args) throws Exception {
        Client client = new Client();
        if (args.length == 2) {
            Client.start(args[0], args[1], client, ServerInterface.class, ClientInterface.class);
        } else {
            Client.start(client, ServerInterface.class, ClientInterface.class);
        }
    }
}
