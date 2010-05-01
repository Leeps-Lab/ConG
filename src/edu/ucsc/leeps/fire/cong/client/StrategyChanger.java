package edu.ucsc.leeps.fire.cong.client;

import edu.ucsc.leeps.fire.cong.config.PeriodConfig;
import edu.ucsc.leeps.fire.cong.server.ServerInterface;
import edu.ucsc.leeps.fire.server.BasePeriodConfig;
import edu.ucsc.leeps.fire.server.PeriodConfigurable;

/**
 *
 * @author jpettit
 */
public class StrategyChanger extends Thread implements PeriodConfigurable {

    private final Object lock = new Object();
    private ServerInterface server;
    private ClientInterface client;
    private PeriodConfig periodConfig;
    private volatile boolean running;
    private volatile boolean shouldUpdate;
    private boolean isMoving;
    private float[] currentStrategy;
    private float[] targetStrategy;
    private long tickTime = 1000;
    private float tickDelta;
    private long sleepTimeMillis;
    private float changeTimeEMA = 0;

    public StrategyChanger(ServerInterface server, ClientInterface client) {
        this.server = server;
        this.client = client;
        isMoving = false;
        start();
    }

    private void update() {
        synchronized (lock) {
            if (targetStrategy[0] > currentStrategy[0]) {
                currentStrategy[0] += tickDelta;
            } else if (targetStrategy[0] < currentStrategy[0]) {
                currentStrategy[0] -= tickDelta;
            } else {
                isMoving = false;
                sleepTimeMillis = 100;
                return;
            }
            if (Math.abs(currentStrategy[0] - targetStrategy[0]) < tickDelta) {
                currentStrategy = targetStrategy;
                server.strategyChanged(client.getID());
                client.setMyStrategy(targetStrategy);
                isMoving = false;
                sleepTimeMillis = 100;
                return;
            }
            long timestamp = System.nanoTime();
            server.strategyChanged(client.getID());
            client.setMyStrategy(currentStrategy);
            float elapsed = (System.nanoTime() - timestamp) / 1000000f;
            changeTimeEMA += 0.1 * (elapsed - changeTimeEMA);
            long estimatedLag = Math.round(changeTimeEMA);
            sleepTimeMillis = tickTime - Math.round(changeTimeEMA);
            if (tickTime > 10.0 * estimatedLag) {
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
        tickDelta = periodConfig.percentChangePerSecond / (1000f / tickTime);
    }

    public boolean strategyIsMoving() {
        return isMoving;
    }

    public float[] getTargetStrategy() {
        return targetStrategy;
    }

    public void setCurrentStrategy(float[] strategy) {
        this.currentStrategy = strategy;
    }

    public void setTargetStrategy(float[] strategy) {
        synchronized (lock) {
            this.targetStrategy = strategy;
            isMoving = true;
        }
    }

    public void setPeriodConfig(BasePeriodConfig basePeriodConfig) {
        synchronized (lock) {
            this.periodConfig = (PeriodConfig) basePeriodConfig;
            recalculateTickDelta();
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
}
