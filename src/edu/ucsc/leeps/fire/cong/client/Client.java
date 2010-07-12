package edu.ucsc.leeps.fire.cong.client;

import edu.ucsc.leeps.fire.FIREClientInterface;
import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.client.gui.TwoStrategySelector;
import edu.ucsc.leeps.fire.cong.client.gui.Countdown;
import edu.ucsc.leeps.fire.cong.client.gui.ChartLegend;
import edu.ucsc.leeps.fire.cong.client.gui.PointsDisplay;
import edu.ucsc.leeps.fire.cong.client.gui.ThreeStrategySelector;
import edu.ucsc.leeps.fire.cong.client.gui.Chart;
import edu.ucsc.leeps.fire.cong.client.gui.PureStrategySelector;
import edu.ucsc.leeps.fire.cong.client.gui.HeatmapLegend;
import edu.ucsc.leeps.fire.cong.client.gui.OneStrategyStripSelector;
import edu.ucsc.leeps.fire.cong.client.gui.Chatroom;
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
    private PureStrategySelector pureMatrix;
    private OneStrategyStripSelector strip;
    private Chart payoffChart, strategyChart;
    private Chart rChart, pChart, sChart;
    private ChartLegend legend;
    private HeatmapLegend heatmapLegend;
    private StrategyChanger strategyChanger;
    private Chatroom chatroom;
    private boolean chatroomEnabled = false;
    private JFrame frame = new JFrame();

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
                null, leftMargin, topMargin + counterpartMatrixSize + 30,
                matrixSize, counterpartMatrixSize,
                embed, strategyChanger);
        simplex = new ThreeStrategySelector(
                null, 20, 100, 250, 600,
                embed, strategyChanger);
        pureMatrix = new PureStrategySelector(
                null, leftMargin, topMargin + counterpartMatrixSize + 30,
                matrixSize, embed, strategyChanger);
        strip = new OneStrategyStripSelector(leftMargin + 7 * matrixSize / 8,
                topMargin + counterpartMatrixSize + 30,
                matrixSize / 8, matrixSize, embed, strategyChanger);
        countdown = new Countdown(
                null, counterpartMatrixSize + 4 * leftMargin, 40 + topMargin, embed);
        pointsDisplay = new PointsDisplay(
                null, counterpartMatrixSize + 4 * leftMargin, (int) (40 + textHeight) + topMargin, embed);
        int chartWidth = (int) (width - bimatrix.width - 2 * leftMargin - 80);
        int chartMargin = 30;
        int strategyChartHeight = 100;
        int threeStrategyChartHeight = 30;
        int payoffChartHeight = (int) (height - strategyChartHeight - 2 * topMargin - chartMargin - 10);
        strategyChart = new Chart(
                null, bimatrix.width + 80 + leftMargin, topMargin,
                chartWidth, strategyChartHeight,
                simplex, Chart.Mode.TwoStrategy, strategyChanger);
        payoffChart = new Chart(
                null, bimatrix.width + 80 + leftMargin, strategyChart.height + topMargin + chartMargin,
                chartWidth, payoffChartHeight,
                simplex, Chart.Mode.Payoff, strategyChanger);
        rChart = new Chart(
                null, bimatrix.width + 80 + leftMargin, topMargin,
                chartWidth, threeStrategyChartHeight,
                simplex, Chart.Mode.RStrategy, strategyChanger);
        pChart = new Chart(
                null, bimatrix.width + 80 + leftMargin, topMargin + threeStrategyChartHeight + 5,
                chartWidth, threeStrategyChartHeight,
                simplex, Chart.Mode.PStrategy, strategyChanger);
        sChart = new Chart(
                null, bimatrix.width + 80 + leftMargin, topMargin + 2 * (threeStrategyChartHeight + 5),
                chartWidth, threeStrategyChartHeight,
                simplex, Chart.Mode.SStrategy, strategyChanger);
        legend = new ChartLegend(
                null, (int) (strategyChart.origin.x + strategyChart.width), (int) strategyChart.origin.y + strategyChartHeight + 3,
                0, 0);
        heatmapLegend = new HeatmapLegend(
                null, bimatrix.width + 10 + leftMargin, strategyChart.height + topMargin + chartMargin, 20, payoffChartHeight);
        embed.running = true;
        frame.add(this);
        frame.pack();
        frame.setVisible(true);
    }

    public void startPeriod() {

        strategyChanger.setCurrentStrategy(FIRE.client.getConfig().initialStrategy);
        if (FIRE.client.getConfig().mixedStrategySelection) {
            if (FIRE.client.getConfig().payoffFunction instanceof TwoStrategyPayoffFunction) {
                if (FIRE.client.getConfig().stripStrategySelection) {
                    strip.setInitialStrategy(FIRE.client.getConfig().initialStrategy[0]);
                } else {
                    bimatrix.setMyStrategy(FIRE.client.getConfig().initialStrategy[0]);
                }
            } else if (FIRE.client.getConfig().payoffFunction instanceof ThreeStrategyPayoffFunction) {
                simplex.setAllStrategies(FIRE.client.getConfig().initialStrategy);
            } else {
                assert false;
            }
        } else {
            pureMatrix.setMyStrategy(FIRE.client.getConfig().initialStrategy);
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
        pureMatrix.setEnabled(true);
        strip.setEnabled(true);
        payoffChart.clearAll();
        strategyChart.clearAll();
        rChart.clearAll();
        pChart.clearAll();
        sChart.clearAll();
        if (FIRE.client.getConfig().chatroom && !chatroomEnabled) {
            chatroomEnabled = true;
            chatroom = new Chatroom(frame);
        }
    }

    public void endPeriod() {
        strategyChanger.endPeriod();
        simplex.reset();
        bimatrix.setEnabled(false);
        pureMatrix.setEnabled(false);
        strip.setEnabled(false);
    }

    public void setIsPaused(boolean isPaused) {
        strategyChanger.setPause(isPaused);
        if (isPaused) {
            simplex.setEnabled(true);
        } else {
            simplex.pause();
        }
        bimatrix.setEnabled(!isPaused);
        pureMatrix.setEnabled(!isPaused);
        strip.setEnabled(!isPaused);
    }

    public void tick(int secondsLeft) {
        this.percent = embed.width * (1 - (secondsLeft / (float) FIRE.client.getConfig().length));
        countdown.setSecondsLeft(secondsLeft);
        bimatrix.update();
        simplex.update();
        pureMatrix.update();
        strip.update();
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
            pureMatrix.setCurrentPercent(percent);
            strip.setCurrentPercent(percent);
            if (FIRE.client.getConfig().subperiods == 0) {
                payoffChart.updateLines();
                strategyChart.updateLines();
                rChart.updateLines();
                pChart.updateLines();
                sChart.updateLines();
            }
            pointsDisplay.update();
        }
    }

    public synchronized float[] getStrategy() {
        if (FIRE.client.getConfig().mixedStrategySelection) {
            if (FIRE.client.getConfig().payoffFunction instanceof TwoStrategyPayoffFunction) {
                if (FIRE.client.getConfig().stripStrategySelection) {
                    return strip.getMyStrategy();
                } else {
                    return bimatrix.getMyStrategy();
                }
            } else if (FIRE.client.getConfig().payoffFunction instanceof ThreeStrategyPayoffFunction) {
                return simplex.getPlayerRPS();
            } else {
                assert false;
                return new float[]{};
            }
        } else {
            return pureMatrix.getMyStrategy();
        }
    }

    public synchronized void setMyStrategy(float[] s) {
        strategyChanger.setCurrentStrategy(s);
        if (FIRE.client.getConfig().mixedStrategySelection) {
            if (FIRE.client.getConfig().payoffFunction instanceof TwoStrategyPayoffFunction) {
                if (FIRE.client.getConfig().stripStrategySelection) {
                    strip.setMyStrategy(s[0]);
                } else {
                    bimatrix.setMyStrategy(s[0]);
                }
            } else if (FIRE.client.getConfig().payoffFunction instanceof ThreeStrategyPayoffFunction) {
                simplex.setCurrentStrategies(s);
            } else {
                assert false;
            }
        } else {
            pureMatrix.setMyStrategy(s);
        }
        payoffChart.setMyStrategy(s);
        strategyChart.setMyStrategy(s);
        rChart.setMyStrategy(s);
        pChart.setMyStrategy(s);
        sChart.setMyStrategy(s);
    }

    public synchronized void setCounterpartStrategy(float[] s) {
        if (FIRE.client.getConfig().mixedStrategySelection) {
            if (FIRE.client.getConfig().payoffFunction instanceof TwoStrategyPayoffFunction) {
                if (FIRE.client.getConfig().stripStrategySelection) {
                    strip.setCounterpartStrategy(s[0]);
                } else {
                    bimatrix.setCounterpartStrategy(s[0]);
                }
            } else if (FIRE.client.getConfig().payoffFunction instanceof ThreeStrategyPayoffFunction) {
                simplex.setCounterpartRPS(s[0], s[1], s[2]);
            } else {
                assert false;
            }
        } else {
            pureMatrix.setCounterpartStrategy(s);
        }
        payoffChart.setCounterpartStrategy(s);
        strategyChart.setCounterpartStrategy(s);
        rChart.setCounterpartStrategy(s);
        pChart.setCounterpartStrategy(s);
        sChart.setCounterpartStrategy(s);
    }

    public void endSubperiod(int subperiod, float[] subperiodStrategy, float[] counterpartSubperiodStrategy) {
        strategyChanger.setCurrentStrategy(subperiodStrategy);
        if (FIRE.client.getConfig().mixedStrategySelection) {
            if (FIRE.client.getConfig().payoffFunction instanceof TwoStrategyPayoffFunction) {
                if (FIRE.client.getConfig().stripStrategySelection) {
                    strip.setMyStrategy(subperiodStrategy[0]);
                    strip.setCounterpartStrategy(subperiodStrategy[0]);
                } else {
                    bimatrix.setMyStrategy(subperiodStrategy[0]);
                    bimatrix.setCounterpartStrategy(counterpartSubperiodStrategy[0]);
                }
            } else if (FIRE.client.getConfig().payoffFunction instanceof ThreeStrategyPayoffFunction) {
                simplex.setCurrentStrategies(subperiodStrategy);
                simplex.setCounterpartRPS(
                        counterpartSubperiodStrategy[0],
                        counterpartSubperiodStrategy[1],
                        counterpartSubperiodStrategy[2]);
            } else {
                assert false;
            }
        } else {
            pureMatrix.setMyStrategy(subperiodStrategy);
            pureMatrix.setCounterpartStrategy(counterpartSubperiodStrategy);
        }
        payoffChart.endSubperiod(subperiod, subperiodStrategy, counterpartSubperiodStrategy);
        strategyChart.endSubperiod(subperiod, subperiodStrategy, counterpartSubperiodStrategy);
        rChart.endSubperiod(subperiod, subperiodStrategy, counterpartSubperiodStrategy);
        pChart.endSubperiod(subperiod, subperiodStrategy, counterpartSubperiodStrategy);
        sChart.endSubperiod(subperiod, subperiodStrategy, counterpartSubperiodStrategy);
    }

    public void newMessage(String message, int senderID) {
        chatroom.newMessage(message, senderID);
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
                heatmapLegend.draw(embed);
                bimatrix.draw(embed);
                simplex.draw(embed);
                pureMatrix.draw(embed);
                strip.draw(embed);
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
