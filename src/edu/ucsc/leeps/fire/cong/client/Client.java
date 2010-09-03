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
import edu.ucsc.leeps.fire.cong.client.gui.OneStrategyStripSelector;
import edu.ucsc.leeps.fire.cong.client.gui.Chatroom;
import edu.ucsc.leeps.fire.cong.client.gui.Sprite;
import edu.ucsc.leeps.fire.cong.server.ThreeStrategyPayoffFunction;
import edu.ucsc.leeps.fire.cong.server.TwoStrategyPayoffFunction;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import javax.swing.JFrame;
import processing.core.PApplet;
import processing.core.PFont;

/**
 *
 * @author jpettit
 */
public class Client extends PApplet implements ClientInterface, FIREClientInterface {

    public static final boolean DEBUG = false;
    private float percent;
    private Countdown countdown;
    private PointsDisplay pointsDisplay;
    private TwoStrategySelector bimatrix;
    private ThreeStrategySelector simplex;
    private PureStrategySelector pureMatrix;
    private OneStrategyStripSelector strip;
    private Sprite selector;
    private Chart payoffChart, strategyChart;
    private Chart rChart, pChart, sChart;
    private ChartLegend legend;
    private StrategyChanger strategyChanger;
    private Chatroom chatroom;
    private boolean chatroomEnabled = false;
    private boolean initialStrategyChosen;
    public PFont size14, size14Bold, size16, size16Bold, size18, size18Bold, size24, size24Bold;

    public Client() {
        loadLibraries();
        noLoop();
        width = 900;
        height = 550;
        frame = new JFrame();
        ((JFrame) frame).setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setResizable(false);
        frame.add(Client.this);
        frame.setSize(width, height);
        init();
        loop();
        frame.setVisible(true);
    }

    public boolean isInitialStrategyChosen() {
        return initialStrategyChosen;
    }

    public void setInitialStrategyChosen(boolean isChosen) {
        initialStrategyChosen = isChosen;
    }

    public void startPrePeriod() {
        initialStrategyChosen = false;
        this.percent = 0;
        if (simplex.visible) {
            selector = simplex;
            strategyChanger.selector = simplex;
        } else if (bimatrix.visible) {
            selector = bimatrix;
            strategyChanger.selector = bimatrix;
        } else if (pureMatrix.visible) {
            selector = pureMatrix;
            strategyChanger.selector = pureMatrix;
        } else if (strip.visible) {
            selector = strip;
            strategyChanger.selector = strip;
        }
        strategyChanger.selector.startPrePeriod();
        payoffChart.clearAll();
        strategyChart.clearAll();
        rChart.clearAll();
        pChart.clearAll();
        sChart.clearAll();
        // TODO: HACK to get startPeriod to be called
        if(FIRE.client.getConfig().preLength == 0) {
            initialStrategyChosen = true;
        }
    }

    public void startPeriod() {
        if (FIRE.client.getConfig().preLength == 0) {
            initialStrategyChosen = true;
            strategyChanger.setCurrentStrategy(FIRE.client.getConfig().initialStrategy);
            payoffChart.setMyStrategy(FIRE.client.getConfig().initialStrategy);
            strategyChart.setMyStrategy(FIRE.client.getConfig().initialStrategy);
            rChart.setMyStrategy(FIRE.client.getConfig().initialStrategy);
            pChart.setMyStrategy(FIRE.client.getConfig().initialStrategy);
            sChart.setMyStrategy(FIRE.client.getConfig().initialStrategy);
            this.percent = 0;
            bimatrix.setEnabled(true);
            pureMatrix.setEnabled(true);
            strip.setEnabled(true);
            payoffChart.clearAll();
            strategyChart.clearAll();
            rChart.clearAll();
            pChart.clearAll();
            sChart.clearAll();
        }
        simplex.setEnabled(true);

        strategyChanger.startPeriod();
        
        if (FIRE.client.getConfig().chatroom && !chatroomEnabled) {
            chatroomEnabled = true;
            chatroom = new Chatroom(frame);
        }
    }

    // TODO: add reset to selector
    public void endPeriod() {
        strategyChanger.endPeriod();
        //simplex.reset();
        //bimatrix.setEnabled(false);
        //pureMatrix.setEnabled(false);
        //strip.setEnabled(false);
    }

    public float getCost() {
        return strategyChanger.getCost();
    }

    public void setIsPaused(boolean isPaused) {
        strategyChanger.setPause(isPaused);
    }

    public void tick(int secondsLeft) {
        this.percent = width * (1 - (secondsLeft / (float) FIRE.client.getConfig().length));
        countdown.setSecondsLeft(secondsLeft);
        strategyChanger.selector.update();
    }

    public void quickTick(int millisLeft) {
        if (millisLeft > 0) {
            this.percent = (1 - (millisLeft / ((float) FIRE.client.getConfig().length * 1000)));
            payoffChart.currentPercent = this.percent;
            strategyChart.currentPercent = this.percent;
            rChart.currentPercent = this.percent;
            pChart.currentPercent = this.percent;
            sChart.currentPercent = this.percent;
            strategyChanger.selector.setCurrentPercent(this.percent);
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
//        if (FIRE.client.getConfig().mixedStrategySelection) {
//            if (FIRE.client.getConfig().payoffFunction instanceof TwoStrategyPayoffFunction) {
//                if (FIRE.client.getConfig().stripStrategySelection) {
//                    return strip.getMyStrategy();
//                } else {
//                    return bimatrix.getMyStrategy();
//                }
//            } else if (FIRE.client.getConfig().payoffFunction instanceof ThreeStrategyPayoffFunction) {
//                return simplex.getPlayerRPS();
//            } else {
//                assert false;
//                return new float[]{};
//            }
//        } else {
//            return pureMatrix.getMyStrategy();
//        }

        return strategyChanger.getCurrentStrategy();
    }

    public synchronized void setMyStrategy(float[] s) {
        strategyChanger.setCurrentStrategy(s);
//        if (FIRE.client.getConfig().mixedStrategySelection) {
//            if (FIRE.client.getConfig().payoffFunction instanceof TwoStrategyPayoffFunction) {
//                if (FIRE.client.getConfig().stripStrategySelection) {
//                    strip.setMyStrategy(s[0]);
//                } else {
//                    bimatrix.setCurrent(s);
//                }
//            } else if (FIRE.client.getConfig().payoffFunction instanceof ThreeStrategyPayoffFunction) {
//                simplex.setCurrentStrategies(s);
//            } else {
//                assert false;
//            }
//        } else {
//            pureMatrix.setMyStrategy(s);
//        }
        payoffChart.setMyStrategy(s);
        strategyChart.setMyStrategy(s);
        rChart.setMyStrategy(s);
        pChart.setMyStrategy(s);
        sChart.setMyStrategy(s);
    }

    public synchronized void setCounterpartStrategy(float[] s) {
        strategyChanger.selector.setCounterpart(s);
//        if (FIRE.client.getConfig().mixedStrategySelection) {
//            if (FIRE.client.getConfig().payoffFunction instanceof TwoStrategyPayoffFunction) {
//                if (FIRE.client.getConfig().stripStrategySelection) {
//                    strip.setCounterpartStrategy(s[0]);
//                } else {
//                    bimatrix.setCounterpart(s);
//                }
//            } else if (FIRE.client.getConfig().payoffFunction instanceof ThreeStrategyPayoffFunction) {
//                simplex.setCounterpartRPS(s[0], s[1], s[2]);
//            } else {
//                assert false;
//            }
//        } else {
//            pureMatrix.setCounterpartStrategy(s);
//        }  
        payoffChart.setCounterpartStrategy(s);
        strategyChart.setCounterpartStrategy(s);
        rChart.setCounterpartStrategy(s);
        pChart.setCounterpartStrategy(s);
        sChart.setCounterpartStrategy(s);
    }

    public void endSubperiod(int subperiod, float[] subperiodStrategy, float[] counterpartSubperiodStrategy) {
        strategyChanger.setCurrentStrategy(subperiodStrategy);
        strategyChanger.selector.setCounterpart(counterpartSubperiodStrategy);
//        if (FIRE.client.getConfig().mixedStrategySelection) {
//            if (FIRE.client.getConfig().payoffFunction instanceof TwoStrategyPayoffFunction) {
//                if (FIRE.client.getConfig().stripStrategySelection) {
//                    strip.setMyStrategy(subperiodStrategy[0]);
//                    strip.setCounterpartStrategy(subperiodStrategy[0]);
//                } else {
//                    bimatrix.setCurrent(subperiodStrategy);
//                    bimatrix.setCounterpart(counterpartSubperiodStrategy);
//                }
//            } else if (FIRE.client.getConfig().payoffFunction instanceof ThreeStrategyPayoffFunction) {
//                simplex.setCurrentStrategies(subperiodStrategy);
//                simplex.setCounterpartRPS(
//                        counterpartSubperiodStrategy[0],
//                        counterpartSubperiodStrategy[1],
//                        counterpartSubperiodStrategy[2]);
//            } else {
//                assert false;
//            }
//        } else {
//            pureMatrix.setMyStrategy(subperiodStrategy);
//            pureMatrix.setCounterpartStrategy(counterpartSubperiodStrategy);
//        }
        payoffChart.endSubperiod(subperiod, subperiodStrategy, counterpartSubperiodStrategy);
        strategyChart.endSubperiod(subperiod, subperiodStrategy, counterpartSubperiodStrategy);
        rChart.endSubperiod(subperiod, subperiodStrategy, counterpartSubperiodStrategy);
        pChart.endSubperiod(subperiod, subperiodStrategy, counterpartSubperiodStrategy);
        sChart.endSubperiod(subperiod, subperiodStrategy, counterpartSubperiodStrategy);
        strategyChanger.endSubperiod(subperiod, subperiodStrategy, counterpartSubperiodStrategy);
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

    @Override
    public void setup() {
        size(getWidth(), getHeight(), OPENGL);
        hint(DISABLE_OPENGL_2X_SMOOTH);
        hint(DISABLE_OPENGL_ERROR_REPORT);
        hint(DISABLE_DEPTH_TEST);
        setupFonts();
        textFont(size14);
        textMode(MODEL);
        //smooth();

        percent = -1;
        int leftMargin = 20;
        int topMargin = 20;
        float textHeight = textAscent() + textDescent();
        //int matrixSize = (int) (height - (4 * textHeight) - 120);
        int matrixSize = 320;
        int counterpartMatrixSize = 100;
        strategyChanger = new StrategyChanger();
        bimatrix = new TwoStrategySelector(
                null, leftMargin, topMargin + counterpartMatrixSize + 30,
                matrixSize, counterpartMatrixSize,
                this, strategyChanger);
        simplex = new ThreeStrategySelector(
                null, 35, 150, 300, 600,
                this, strategyChanger);
        pureMatrix = new PureStrategySelector(
                null, leftMargin, topMargin + counterpartMatrixSize + 30,
                matrixSize, this, strategyChanger);
        strip = new OneStrategyStripSelector(null, leftMargin + 7 * matrixSize / 8,
                topMargin + counterpartMatrixSize + 30,
                matrixSize / 8, matrixSize, this, strategyChanger);
        countdown = new Countdown(
                null, counterpartMatrixSize + 4 * leftMargin, 20 + topMargin, this);
        pointsDisplay = new PointsDisplay(
                null, counterpartMatrixSize + 4 * leftMargin, (int) (20 + textHeight) + topMargin, this);
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
    }

    @Override
    public void draw() {
        try {
            background(255);
            if(selector != null) {
                selector.draw(this);
            }
            if (FIRE.client.getConfig() != null) {
                if (FIRE.client.getConfig().payoffFunction instanceof TwoStrategyPayoffFunction) {
                    strategyChart.draw(this);
                } else if (FIRE.client.getConfig().payoffFunction instanceof ThreeStrategyPayoffFunction) {
                    rChart.draw(this);
                    pChart.draw(this);
                    sChart.draw(this);
                }
            }
            payoffChart.draw(this);
            legend.draw(this);
            if (!initialStrategyChosen) {
                float textHeight = textDescent() + textAscent() + 8;
                fill(255, 50, 50);
                text("Please choose an initial strategy.", countdown.origin.x, countdown.origin.y - textHeight);
            }
            countdown.draw(this);
            pointsDisplay.draw(this);
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
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    private void setupFonts() {
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

    private void loadLibraries() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        List<JarEntry> entries = new LinkedList<JarEntry>();
        for (String pathItem : System.getProperty("java.class.path").split(":")) {
            if (pathItem.contains("jar")) {
                try {
                    JarFile jar = new JarFile(pathItem);
                    JarInputStream jarInputStream = new JarInputStream(new FileInputStream(pathItem));
                    JarEntry entry = jarInputStream.getNextJarEntry();
                    while (entry != null) {
                        if (entry.getName().endsWith("so")
                                || entry.getName().endsWith("dll")
                                || entry.getName().endsWith("jnilib")) {
                            entries.add(entry);
                        }
                        entry = jarInputStream.getNextJarEntry();
                    }
                    for (JarEntry toExtract : entries) {
                        File tmpFile = new File(tmpDir, toExtract.getName());
                        OutputStream out = new FileOutputStream(tmpFile);
                        InputStream in = jar.getInputStream(toExtract);
                        byte[] buffer = new byte[4096];
                        while (true) {
                            int nBytes = in.read(buffer);
                            if (nBytes <= 0) {
                                break;
                            }
                            out.write(buffer, 0, nBytes);
                        }
                        out.flush();
                        out.close();
                        in.close();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                break;
            }
        }

        addDir(tmpDir);
    }

    public static void addDir(String s) {
        try {
            // This enables the java.library.path to be modified at runtime
            // From a Sun engineer at http://forums.sun.com/thread.jspa?threadID=707176
            //
            Field field = ClassLoader.class.getDeclaredField("usr_paths");
            field.setAccessible(true);
            String[] paths = (String[]) field.get(null);
            for (int i = 0; i < paths.length; i++) {
                if (s.equals(paths[i])) {
                    return;
                }
            }
            String[] tmp = new String[paths.length + 1];
            System.arraycopy(paths, 0, tmp, 0, paths.length);
            tmp[paths.length] = s;
            field.set(null, tmp);
            System.setProperty("java.library.path", System.getProperty("java.library.path") + File.pathSeparator + s);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        FIRE.startClient();
    }
}
