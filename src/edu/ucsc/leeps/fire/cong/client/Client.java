package edu.ucsc.leeps.fire.cong.client;

import edu.ucsc.leeps.fire.FIREClientInterface;
import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.client.gui.TwoStrategySelector;
import edu.ucsc.leeps.fire.cong.client.gui.Countdown;
import edu.ucsc.leeps.fire.cong.client.gui.ChartLegend;
import edu.ucsc.leeps.fire.cong.client.gui.PointsDisplay;
import edu.ucsc.leeps.fire.cong.client.gui.ThreeStrategySelector;
import edu.ucsc.leeps.fire.cong.client.gui.Chart;
import edu.ucsc.leeps.fire.cong.server.ThreeStrategyPayoffFunction;
import edu.ucsc.leeps.fire.cong.server.TwoStrategyPayoffFunction;
import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import javax.swing.JFrame;
import javax.swing.JPanel;
import processing.core.PApplet;
import processing.core.PFont;

/**
 *
 * @author jpettit
 */
public class Client extends JPanel implements ClientInterface, FIREClientInterface {

    public static final boolean DEBUG = false;
    private int width, height;
    private PEmbed embed;
    private float percent;
    private Countdown countdown;
    private PointsDisplay pointsDisplay;
    private TwoStrategySelector bimatrix;
    private ThreeStrategySelector simplex;
    private Chart payoffChart, strategyChart;
    private Chart rChart, pChart, sChart;
    private ChartLegend legend;
    private StrategyChanger strategyChanger;

    public Client() {
        removeAll();
        width = 900;
        height = 500;
        embed = new PEmbed(width, height);
        embed.init();
        setSize(embed.getSize());
        add(embed);
        percent = -1;
        int leftMargin = 20;
        int topMargin = 20;
        float textHeight = embed.textAscent() + embed.textDescent();
        //int matrixSize = (int) (height - (4 * textHeight) - 120);
        int matrixSize = 320;
        int counterpartMatrixSize = 100;
        strategyChanger = new StrategyChanger();
        bimatrix = new TwoStrategySelector(
                leftMargin, topMargin + counterpartMatrixSize + 30,
                matrixSize, counterpartMatrixSize,
                embed, strategyChanger);
        simplex = new ThreeStrategySelector(
                20, 100, 250, 600,
                embed, strategyChanger);
        countdown = new Countdown(
                counterpartMatrixSize + 4 * leftMargin, 40 + topMargin, embed);
        pointsDisplay = new PointsDisplay(
                counterpartMatrixSize + 4 * leftMargin, (int) (40 + textHeight) + topMargin, embed);
        int chartWidth = (int) (width - bimatrix.width - 2 * leftMargin - 80);
        int chartMargin = 30;
        int strategyChartHeight = 100;
        int threeStrategyChartHeight = 30;
        int payoffChartHeight = (int) (height - strategyChartHeight - 2 * topMargin - chartMargin - 10);
        strategyChart = new Chart(
                bimatrix.width + 80 + leftMargin, topMargin,
                chartWidth, strategyChartHeight,
                simplex, Chart.Mode.TwoStrategy);
        payoffChart = new Chart(
                bimatrix.width + 80 + leftMargin, strategyChart.height + topMargin + chartMargin,
                chartWidth, payoffChartHeight,
                simplex, Chart.Mode.Payoff);
        rChart = new Chart(
                bimatrix.width + 80 + leftMargin, topMargin,
                chartWidth, threeStrategyChartHeight,
                simplex, Chart.Mode.RStrategy);
        pChart = new Chart(
                bimatrix.width + 80 + leftMargin, topMargin + threeStrategyChartHeight + 5,
                chartWidth, threeStrategyChartHeight,
                simplex, Chart.Mode.PStrategy);
        sChart = new Chart(
                bimatrix.width + 80 + leftMargin, topMargin + 2 * (threeStrategyChartHeight + 5),
                chartWidth, threeStrategyChartHeight,
                simplex, Chart.Mode.SStrategy);
        legend = new ChartLegend(
                (int) (strategyChart.origin.x + strategyChart.width), (int) strategyChart.origin.y + strategyChartHeight + 3,
                0, 0);
        embed.running = true;
        JFrame frame = new JFrame();
        frame.add(this);
        Dimension size = getPreferredSize();
        size.height += 20;
        frame.setSize(size);
        frame.setVisible(true);
    }

    public void startPeriod() {

        strategyChanger.setCurrentStrategy(FIRE.client.getConfig().initialStrategy);
        if (FIRE.client.getConfig().payoffFunction instanceof TwoStrategyPayoffFunction) {
            bimatrix.setMyStrategy(FIRE.client.getConfig().initialStrategy[0]);
        } else if (FIRE.client.getConfig().payoffFunction instanceof ThreeStrategyPayoffFunction) {
            simplex.setAllStrategies(FIRE.client.getConfig().initialStrategy);
        } else {
            assert false;
        }
        payoffChart.setMyStrategy(FIRE.client.getConfig().initialStrategy);
        strategyChart.setMyStrategy(FIRE.client.getConfig().initialStrategy);
        rChart.setMyStrategy(FIRE.client.getConfig().initialStrategy);
        pChart.setMyStrategy(FIRE.client.getConfig().initialStrategy);
        sChart.setMyStrategy(FIRE.client.getConfig().initialStrategy);

        this.percent = 0;
        strategyChanger.startPeriod();
        simplex.setEnabled(true);
        bimatrix.setEnabled(true);
        payoffChart.clearAll();
        strategyChart.clearAll();
        rChart.clearAll();
        pChart.clearAll();
        sChart.clearAll();
    }

    public void endPeriod() {
        strategyChanger.endPeriod();
        simplex.reset();
        bimatrix.setEnabled(false);
    }

    public void setIsPaused(boolean isPaused) {
        strategyChanger.setPause(isPaused);
        if (isPaused) {
            simplex.setEnabled(true);
        } else {
            simplex.pause();
        }
        bimatrix.setEnabled(!isPaused);
    }

    public void tick(int secondsLeft) {
        this.percent = embed.width * (1 - (secondsLeft / (float) FIRE.client.getConfig().length));
        countdown.setSecondsLeft(secondsLeft);
        bimatrix.update();
        simplex.update();
    }

    public void quickTick(int millisLeft) {
        if (millisLeft > 0) {
            this.percent = (1 - (millisLeft / ((float) FIRE.client.getConfig().length * 1000)));
            payoffChart.currentPercent = this.percent;
            strategyChart.currentPercent = this.percent;
            rChart.currentPercent = this.percent;
            pChart.currentPercent = this.percent;
            sChart.currentPercent = this.percent;
            bimatrix.setCurrentPercent(this.percent);
            simplex.currentPercent = this.percent;
            if (FIRE.client.getConfig().subperiods == 0) {
                payoffChart.updateLines();
                strategyChart.updateLines();
                rChart.updateLines();
                pChart.updateLines();
                sChart.updateLines();
            }
        }
    }

    public synchronized float[] getStrategy() {
        if (FIRE.client.getConfig().payoffFunction instanceof TwoStrategyPayoffFunction) {
            return bimatrix.getMyStrategy();
        } else if (FIRE.client.getConfig().payoffFunction instanceof ThreeStrategyPayoffFunction) {
            return simplex.getPlayerRPS();
        } else {
            assert false;
            return new float[]{};
        }
    }

    public synchronized void setMyStrategy(float[] s) {
        strategyChanger.setCurrentStrategy(s);
        if (FIRE.client.getConfig().payoffFunction instanceof TwoStrategyPayoffFunction) {
            bimatrix.setMyStrategy(s[0]);
        } else if (FIRE.client.getConfig().payoffFunction instanceof ThreeStrategyPayoffFunction) {
            simplex.setCurrentStrategies(s);
        } else {
            assert false;
        }
        payoffChart.setMyStrategy(s);
        strategyChart.setMyStrategy(s);
        rChart.setMyStrategy(s);
        pChart.setMyStrategy(s);
        sChart.setMyStrategy(s);
    }

    public synchronized void setCounterpartStrategy(float[] s) {
        if (FIRE.client.getConfig().payoffFunction instanceof TwoStrategyPayoffFunction) {
            bimatrix.setCounterpartStrategy(s[0]);
        } else if (FIRE.client.getConfig().payoffFunction instanceof ThreeStrategyPayoffFunction) {
            simplex.setCounterpartRPS(s[0], s[1], s[2]);
        } else {
            assert false;
        }
        payoffChart.setCounterpartStrategy(s);
        strategyChart.setCounterpartStrategy(s);
        rChart.setCounterpartStrategy(s);
        pChart.setCounterpartStrategy(s);
        sChart.setCounterpartStrategy(s);
    }

    public void endSubperiod(int subperiod, float[] subperiodStrategy, float[] counterpartSubperiodStrategy) {
        strategyChanger.setCurrentStrategy(subperiodStrategy);
        if (FIRE.client.getConfig().payoffFunction instanceof TwoStrategyPayoffFunction) {
            bimatrix.setMyStrategy(subperiodStrategy[0]);
            bimatrix.setCounterpartStrategy(counterpartSubperiodStrategy[0]);
        } else if (FIRE.client.getConfig().payoffFunction instanceof ThreeStrategyPayoffFunction) {
            simplex.setCurrentStrategies(subperiodStrategy);
            simplex.setCounterpartRPS(
                    counterpartSubperiodStrategy[0],
                    counterpartSubperiodStrategy[1],
                    counterpartSubperiodStrategy[2]);
        } else {
            assert false;
        }
        payoffChart.endSubperiod(subperiod, subperiodStrategy, counterpartSubperiodStrategy);
        strategyChart.endSubperiod(subperiod, subperiodStrategy, counterpartSubperiodStrategy);
        rChart.endSubperiod(subperiod, subperiodStrategy, counterpartSubperiodStrategy);
        pChart.endSubperiod(subperiod, subperiodStrategy, counterpartSubperiodStrategy);
        sChart.endSubperiod(subperiod, subperiodStrategy, counterpartSubperiodStrategy);
    }

    public boolean readyForNextPeriod() {
        return true;
    }

    public void disconnect() {
        System.exit(0);
    }

    public class PEmbed extends PApplet {

        private final String RENDERER = P2D;
        private int initWidth, initHeight;
        public PFont size14, size14Bold, size16, size16Bold, size18, size18Bold, size24, size24Bold;
        public boolean running = false;

        public PEmbed(int initWidth, int initHeight) {
            this.initWidth = initWidth;
            this.initHeight = initHeight;
            try {
                InputStream fontInputStream;
                fontInputStream = Client.class.getResourceAsStream("resources/DejaVuSans-14.vlw");
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
        }

        @Override
        public void setup() {
            size(initWidth, initHeight, RENDERER);
            smooth();
            textFont(size14);
            textMode(SCREEN);
        }

        @Override
        public void draw() {
            if (running) {
                background(255);
                bimatrix.draw(embed);
                simplex.draw(embed);
                if (FIRE.client.getConfig() != null) {
                    if (FIRE.client.getConfig().payoffFunction instanceof TwoStrategyPayoffFunction) {
                        strategyChart.draw(embed);
                    } else if (FIRE.client.getConfig().payoffFunction instanceof ThreeStrategyPayoffFunction) {
                        rChart.draw(embed);
                        pChart.draw(embed);
                        sChart.draw(embed);
                    }
                }
                payoffChart.draw(embed);
                legend.draw(embed);
                countdown.draw(embed);
                pointsDisplay.draw(embed);
                if (DEBUG) {
                    String frameRateString = String.format("FPS: %.2f", frameRate);
                    if (frameRate < 8) {
                        fill(255, 0, 0);
                    } else {
                        fill(0);
                    }
                    text(frameRateString, 330, 30);
                    float averageChangeTime = strategyChanger.getAverageChangeTime();
                    String changeTimeString = String.format("MPC: %.2f", averageChangeTime);
                    if (averageChangeTime > 10) {
                        fill(255, 0, 0);
                    } else {
                        fill(0);
                    }
                    text(changeTimeString, 330, 45);
                }
            }
        }
    }

    public static void main(String[] args) {
        FIRE.startClient();
    }
}
