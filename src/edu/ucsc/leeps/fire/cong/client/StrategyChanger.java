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
public class StrategyChanger extends Thread implements Configurable<Config> {

    private final Object lock = new Object();
    private Config config;
    private volatile boolean running;
    private volatile boolean shouldUpdate;
    private boolean isMoving;
    private float[] currentStrategy;
    private float[] targetStrategy;
    private float[] deltaStrategy;
    private float[] hoverStrategy_A;
    private float[] hoverStrategy_a;
    private long tickTime = 100;
    private float tickDelta;
    private long sleepTimeMillis;
    private float changeTimeEMA = 0;

    public StrategyChanger() {
        isMoving = false;
        start();
        FIRE.client.addConfigListener(this);
    }

    public void configChanged(Config config) {
        synchronized (lock) {
            this.config = config;
            if (config.payoffFunction instanceof TwoStrategyPayoffFunction) {
                currentStrategy = new float[1];
                targetStrategy = new float[1];
                deltaStrategy = new float[1];
            } else if (config.payoffFunction instanceof ThreeStrategyPayoffFunction) {
                currentStrategy = new float[3];
                targetStrategy = new float[3];
                deltaStrategy = new float[3];
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
                for (int i = 0; i < deltaStrategy.length; ++i) {
                    deltaStrategy[i] = tickDelta * (deltaStrategy[i] / totalDelta);
                    currentStrategy[i] += deltaStrategy[i];
                }
            } else {
                for (int i = 0; i < currentStrategy.length; ++i) {
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
            long estimatedLag = Math.round(changeTimeEMA);
            sleepTimeMillis = tickTime - Math.round(changeTimeEMA);
            if (tickTime > 20.0 * estimatedLag) {
                tickTime = Math.round(5.0 * estimatedLag);
                sleepTimeMillis = tickTime - Math.round(changeTimeEMA);
                recalculateTickDelta();
            } else if (sleepTimeMillis < 0) {
                tickTime = Math.round(5.0 * estimatedLag);
                sleepTimeMillis = 0;
                recalculateTickDelta();
            }
        }
    }

    private void sendUpdate() {
        FIRE.client.getServer().strategyChanged(
                currentStrategy,
                targetStrategy,
                hoverStrategy_A,
                hoverStrategy_a,
                FIRE.client.getID());
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            try {
                if (isMoving && shouldUpdate) {
                    update();
                }
                sleep(sleepTimeMillis);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
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
        for (int i = 0; i < currentStrategy.length; ++i) {
            currentStrategy[i] = strategy[i];
        }
    }

    public void setHoverStrategy(float[] strategy_A, float[] strategy_a) {
        this.hoverStrategy_A = strategy_A;
        this.hoverStrategy_a = strategy_a;
    }

    public void setTargetStrategy(float[] strategy) {
        if (FIRE.client.getConfig().percentChangePerSecond >= 1.0f) {
            for (int i = 0; i < targetStrategy.length; ++i) {
                currentStrategy[i] = strategy[i];
                targetStrategy[i] = strategy[i];
            }
            FIRE.client.getClient().setMyStrategy(strategy);
            sendUpdate();
            return;
        } else {
            synchronized (lock) {
                for (int i = 0; i < targetStrategy.length; ++i) {
                    targetStrategy[i] = strategy[i];
                }
                isMoving = true;
            }
        }
    }

    public void startPeriod() {
        shouldUpdate = true;
    }

    public void setPause(boolean paused) {
        this.shouldUpdate = !paused;
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
