package edu.ucsc.leeps.fire.cong.client;

import edu.ucsc.leeps.fire.config.Configurable;
import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.config.Config;
import edu.ucsc.leeps.fire.cong.server.ThreeStrategyPayoffFunction;
import edu.ucsc.leeps.fire.cong.server.TwoStrategyPayoffFunction;

/**
 *
 * @author jpettit
 */
public class StrategyChanger extends Thread implements Configurable<Config>, Runnable {

    private Config config;
    private volatile boolean shouldUpdate;
    private float[] previousStrategy;
    private float[] deltaStrategy;
    private float[] lastStrategy;
    private float strategyDelta;
    private long nextAllowedChangeTime;
    private boolean initialLock;
    public volatile boolean isLocked;
    public Selector selector;

    public StrategyChanger() {
        FIRE.client.addConfigListener(this);
        start();
    }

    public void configChanged(Config config) {
        this.config = config;
        int size = 0;
        if (config.payoffFunction instanceof TwoStrategyPayoffFunction) {
            size = 1;
        } else if (config.payoffFunction instanceof ThreeStrategyPayoffFunction) {
            size = 3;
        }
        previousStrategy = new float[size];
        deltaStrategy = new float[size];
        lastStrategy = new float[size];
    }

    private void update() {
        if (Client.state.target == null) {
            return;
        }
        float tickDelta = config.percentChangePerSecond / (1000f / config.strategyUpdateMillis) * 2f;
        float[] current = Client.state.getMyStrategy();
        if (current.length == 1) {
            tickDelta /= 2f;
        }
        boolean same = true;
        for (int i = 0; i < Client.state.target.length; i++) {
            if (Math.abs(Client.state.target[i] - current[i]) > Float.MIN_NORMAL) {
                same = false;
            }
        }
        if (same) {
            for (int i = 0; i < Client.state.target.length; i++) {
                Client.state.target[i] = current[i];
            }
            return;
        }
        float totalDelta = 0f;
        for (int i = 0; i < current.length; i++) {
            deltaStrategy[i] = Client.state.target[i] - current[i];
            totalDelta += Math.abs(deltaStrategy[i]);
        }
        if (config.percentChangePerSecond < 1f && totalDelta > tickDelta) {
            for (int i = 0; i < deltaStrategy.length; i++) {
                deltaStrategy[i] = tickDelta * (deltaStrategy[i] / totalDelta);
                current[i] += deltaStrategy[i];
            }
        } else {
            for (int i = 0; i < current.length; i++) {
                current[i] = Client.state.target[i];
            }
        }

        sendUpdate();
        if (config.delay != null) {
            int delay = config.delay.getDelay();
            nextAllowedChangeTime = System.currentTimeMillis() + Math.round(1000 * delay);
        }
    }

    private void sendUpdate() {
        float[] current = Client.state.getMyStrategy();
        if (config.subperiods == 0) {
            float total = 0;
            for (int i = 0; i < previousStrategy.length; i++) {
                total += Math.abs(previousStrategy[i] - current[i]);
            }
            strategyDelta += total / 2;
        }
        FIRE.client.getServer().strategyChanged(
                current,
                Client.state.target,
                FIRE.client.getID());
    }

    @Override
    public void run() {
        while (true) {
            if (config == null) {
                try {
                    Thread.sleep(50);
                    continue;
                } catch (InterruptedException ex) {
                }
            }
            long nanoWait = config.strategyUpdateMillis * 1000000;
            long start = System.nanoTime();
            isLocked = decisionDelayed();
            if (shouldUpdate) {
                selector.setEnabled(!isLocked);
            }
            if (!isLocked && shouldUpdate) {
                update();
            }
            long elapsed = System.nanoTime() - start;
            long sleepNanos = nanoWait - elapsed;
            if (sleepNanos > 0) {
                try {
                    Thread.sleep(sleepNanos / 1000000);
                } catch (InterruptedException ex) {
                }
            }
        }
    }

    private boolean decisionDelayed() {
        return System.currentTimeMillis() < nextAllowedChangeTime;
    }

    public float getCost() {
        if (config == null) {
            return 0f;
        }
        return strategyDelta * config.changeCost;
    }

    public void startPeriod() {
        shouldUpdate = true;
        strategyDelta = 0;
        nextAllowedChangeTime = System.currentTimeMillis();
        if (config.initialDelay != null && initialLock) {
            int delay = config.initialDelay.getDelay();
            nextAllowedChangeTime = System.currentTimeMillis() + Math.round(1000 * delay);
            initialLock = false;
        }
    }

    public void setPause(boolean paused) {
        this.shouldUpdate = !paused;
        selector.setEnabled(!paused);
    }

    public void endSubperiod(int subperiod) {
        if (subperiod == 1) {
            System.arraycopy(config.initialStrategy, 0, lastStrategy, 0, lastStrategy.length);
        }
        float[] subperiodStrategy = Client.state.getMyStrategy();
        float total = 0;
        for (int i = 0; i < subperiodStrategy.length; i++) {
            total += Math.abs(subperiodStrategy[i] - lastStrategy[i]);
        }
        strategyDelta += total / 2;
        System.arraycopy(subperiodStrategy, 0, lastStrategy, 0, lastStrategy.length);
        selector.endSubperiod(subperiod);
    }

    public void endPeriod() {
        shouldUpdate = false;
        initialLock = true;
        selector.setEnabled(false);
    }

    public static interface Selector {

        public void startPrePeriod();

        public void startPeriod();

        public void endSubperiod(int subperiod);

        public void setEnabled(boolean enabled);
    }
}
