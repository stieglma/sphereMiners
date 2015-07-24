package me.stieglmaier.sphereMiners.view;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import me.stieglmaier.sphereMiners.model.GameSimulation;
import me.stieglmaier.sphereMiners.model.Physics;
import me.stieglmaier.sphereMiners.model.Tick;

public class DisplayGameHandler {

    private int currentTick = 0;
    private final Runnable playTick;
    private final Slider progressBar;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> future;
    private boolean isPaused = false;

    public DisplayGameHandler(GraphicsContext graphicsContext, GameSimulation simulation, Slider progressBar, Button playButton) {
        this.progressBar = progressBar;
        playTick = () -> {
            // check if current tick is available before retrieving it
            if (currentTick >= simulation.getSize()) {
                future.cancel(true);
                playButton.setText("pause");
            }
            Tick tick = simulation.getTick(currentTick++);
            progressBar.setValue(((double)currentTick)/Physics.getFPS());
            //do drawing on graphics object
        };
    }

    public void startAnimation() {
        future = scheduler.scheduleAtFixedRate(playTick, 0, Physics.getFPS(), TimeUnit.MILLISECONDS);
    }

    public void pauseAnimation() {
        isPaused = !isPaused;
        if (isPaused) {
            future.cancel(true);
        } else {
            currentTick = (int) progressBar.getValue();
            future = scheduler.scheduleAtFixedRate(playTick, 0, Physics.getFPS(), TimeUnit.MILLISECONDS);
        }
    }

    public void stopAnimation() {
        scheduler.shutdownNow();
    }
}
