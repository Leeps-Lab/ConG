package edu.ucsc.leeps.fire.cong.client;

import edu.ucsc.leeps.fire.config.Configurable;
import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.config.Config;
import edu.ucsc.leeps.fire.cong.config.DecisionDelay;
import edu.ucsc.leeps.fire.cong.server.ThreeStrategyPayoffFunction;
import edu.ucsc.leeps.fire.cong.server.TwoStrategyPayoffFunction;
import java.util.Random;

/**
 *
 * @author jpettit
 */
public class StrategyChanger extends Thread implements Configurable<Config> {

    private final Object lock = new Object();
    private Config config;
    private volatile boolean running;
    private volatile boolean shouldUpdate;
    private boolean isMoving;
    private float[] previousStrategy;
    private float[] currentStrategy;
    private float[] targetStrategy;
    private float[] deltaStrategy;
    private float[] hoverStrategy_A;
    private float[] hoverStrategy_a;
    private float[] lastStrategy;
    private long tickTime = 100;
    private float tickDelta;
    private long sleepTimeMillis;
    private float changeTimeEMA = 0;
    private float strategyDelta;
    private Random random;
    private long nextAllowedChangeTime;

    public StrategyChanger() {
        isMoving = false;
        random = new Random();
        nextAllowedChangeTime = System.currentTimeMillis();
        start();
        FIRE.client.addConfigListener(this);
    }

    public void configChanged(Config config) {
        synchronized (lock) {
            this.config = config;
            if (config.payoffFunction instanceof TwoStrategyPayoffFunction) {
                previousStrategy = new float[2];
                currentStrategy = new float[2];
                targetStrategy = new float[2];
                deltaStrategy = new float[2];
                lastStrategy = new float[2];
            } else if (config.payoffFunction instanceof ThreeStrategyPayoffFunction) {
                previousStrategy = new float[3];
                currentStrategy = new float[3];
                targetStrategy = new float[3];
                deltaStrategy = new float[3];
                lastStrategy = new float[3];
            }
            recalculateTickDelta();
        }
    }

    private void update() {
        if (FIRE.client.getConfig().percentChangePerSecond >= 1.0f) {
            return;
        }
        synchronized (lock) {
            float totalDelta = 0f;
            for (int i = 0; i < currentStrategy.length; i++) {
                deltaStrategy[i] = targetStrategy[i] - currentStrategy[i];
                totalDelta += Math.abs(deltaStrategy[i]);
            }
            if (totalDelta > tickDelta) {
                for (int i = 0; i < deltaStrategy.length; i++) {
                    deltaStrategy[i] = tickDelta * (deltaStrategy[i] / totalDelta);
                    currentStrategy[i] += deltaStrategy[i];
                }
            } else {
                for (int i = 0; i < currentStrategy.length; i++) {
                    currentStrategy[i] = targetStrategy[i];
                }
                isMoving = false;
                sleepTimeMillis = 100;
            }

            long timestamp = System.nanoTime();
            sendUpdate();
            FIRE.client.getClient().setMyStrategy(currentStrategy);
            float elapsed = (System.nanoTime() - timestamp) / 1000000f;
            changeTimeEMA += 0.1 * (elapsed - changeTimeEMA);
            sleepTimeMillis = tickTime - Math.round(changeTimeEMA);
            /*
            long estimatedLag = Math.round(changeTimeEMA);
            if (tickTime > 20.0 * estimatedLag) {
            tickTime = Math.round(5.0 * estimatedLag);
            sleepTimeMillis = tickTime - Math.round(changeTimeEMA);
            recalculateTickDelta();
            } else if (sleepTimeMillis < 0) {
            tickTime = Math.round(5.0 * estimatedLag);
            sleepTimeMillis = 0;
            recalculateTickDelta();
            }
             *
             */
        }
    }

    private void sendUpdate() {
        if (config.subperiods == 0) {
            float total = 0;
            for (int i = 0; i < previousStrategy.length; i++) {
                total += Math.abs(previousStrategy[i] - currentStrategy[i]);
            }
            strategyDelta += total / 2;
        }
        FIRE.client.getServer().strategyChanged(
                currentStrategy,
                targetStrategy,
                hoverStrategy_A,
                hoverStrategy_a,
                FIRE.client.getID());
        if (config.delay != null) {
            float delayTimeInSeconds = 0;
            switch (config.delay.distribution) {
                case uniform:
                    delayTimeInSeconds = random.nextFloat() * config.delay.lambda;
                    break;
                case poisson:
                    throw new UnsupportedOperationException();
                case gaussian:
                    throw new UnsupportedOperationException();
            }
            nextAllowedChangeTime = System.currentTimeMillis() + Math.round(1000 * delayTimeInSeconds);
        }
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            try {
                if (!decisionDelayed() && isMoving && shouldUpdate) {
                    update();
                }
                sleep(sleepTimeMillis);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

    private boolean decisionDelayed() {
        return System.currentTimeMillis() < nextAllowedChangeTime;
    }

    private void recalculateTickDelta() {
        tickDelta = config.percentChangePerSecond / (1000f / tickTime);
    }

    public boolean strategyIsMoving() {
        return isMoving;
    }

    public float[] getTargetStrategy() {
        if (targetStrategy == null) {
            targetStrategy = currentStrategy;
        }
        return targetStrategy;
    }

    public float[] getCurrentStrategy() {
        return currentStrategy;
    }

    public void setCurrentStrategy(float[] strategy) {
        for (int i = 0; i < currentStrategy.length; i++) {
            previousStrategy[i] = strategy[i];
            currentStrategy[i] = strategy[i];
        }
    }

    public void setHoverStrategy(float[] strategy_A, float[] strategy_a) {
        this.hoverStrategy_A = strategy_A;
        this.hoverStrategy_a = strategy_a;
    }

    public void setTargetStrategy(float[] strategy) {
        if (FIRE.client.getConfig().percentChangePerSecond >= 1.0f) {
            for (int i = 0; i < targetStrategy.length; i++) {
                currentStrategy[i] = strategy[i];
                targetStrategy[i] = strategy[i];
            }
            FIRE.client.getClient().setMyStrategy(strategy);
            sendUpdate();
            return;
        } else {
            synchronized (lock) {
                for (int i = 0; i < targetStrategy.length; i++) {
                    targetStrategy[i] = strategy[i];
                }
                isMoving = true;
            }
        }
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
    }

    public void setPause(boolean paused) {
        this.shouldUpdate = !paused;
    }

    public void endSubperiod(int subperiod, float[] subperiodStrategy, float[] counterpartSubperiodStrategy) {
        if (subperiod == 1) {
            System.arraycopy(config.initialStrategy, 0, lastStrategy, 0, lastStrategy.length);
        }
        float total = 0;
        for (int i = 0; i < subperiodStrategy.length; i++) {
            total += Math.abs(subperiodStrategy[i] - lastStrategy[i]);
        }
        strategyDelta += total / 2;
        System.arraycopy(subperiodStrategy, 0, lastStrategy, 0, lastStrategy.length);
    }

    public void endPeriod() {
        shouldUpdate = false;
    }

    public void signalStop() {
        running = false;
    }

    public float getAverageChangeTime() {
        return changeTimeEMA;
    }
}
