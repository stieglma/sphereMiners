package me.stieglmaier.sphereMiners.view;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javafx.scene.canvas.GraphicsContext;
import me.stieglmaier.sphereMiners.model.GameSimulation;
import me.stieglmaier.sphereMiners.model.Physics;
import me.stieglmaier.sphereMiners.model.Tick;

public class DisplayGameHandler {

    private int currentTick = 0;
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> future;
    private GraphicsContext graphics;
    private GameSimulation simulation;
    private boolean isPaused = false;
    private Runnable playTick = () -> {
        // check if current tick is available before retrieving it
        if (currentTick >= simulation.getSize()) {
            future.cancel(true);
        }
        Tick tick = simulation.getTick(currentTick++);

        //do drawing on graphics object
    };

    public DisplayGameHandler(GraphicsContext graphicsContext, GameSimulation gameSimulation) {
        graphics = graphicsContext;
        simulation = gameSimulation;
    }

    public void startAnimation() {
        future = scheduler.scheduleAtFixedRate(playTick, 0, Physics.getTimePerTick(), TimeUnit.MILLISECONDS);
    }

    public void pauseAnimation() {
        isPaused = !isPaused;
        if (isPaused) {
            future.cancel(true);
        } else {
            future = scheduler.scheduleAtFixedRate(playTick, 0, Physics.getTimePerTick(), TimeUnit.MILLISECONDS);
        }
    }

    public void stopAnimation() {
        scheduler.shutdownNow();
    }
}
