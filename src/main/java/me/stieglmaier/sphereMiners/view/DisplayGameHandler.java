package me.stieglmaier.sphereMiners.view;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import me.stieglmaier.sphereMiners.main.Constants;
import me.stieglmaier.sphereMiners.model.GameSimulation;
import me.stieglmaier.sphereMiners.model.Player;
import me.stieglmaier.sphereMiners.model.Sphere;
import me.stieglmaier.sphereMiners.model.Tick;

public class DisplayGameHandler {

    private volatile int currentTick = 0;
    private final Runnable playTick;
    private final Slider progressBar;
    private final Constants constants;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> future;
    private boolean isPaused = false;

    public DisplayGameHandler(GraphicsContext graphicsContext, GameSimulation simulation, Slider progressBar, Button playButton, Constants constants) {
        this.constants = constants;
        this.progressBar = progressBar;
        playTick = () -> {
            // clear drawing area
            graphicsContext.clearRect(0, 0, constants.getFieldWidth(), constants.getFieldHeight());

            // check if current tick is available before retrieving it
            if (currentTick >= simulation.getSize()) {
                future.cancel(true);
                playButton.setText("pause");
            }
            Tick tick = simulation.getTick(currentTick++);
            progressBar.setValue(((double)currentTick)/constants.getFramesPerSecond());
            //do drawing on graphics object
            for (Entry<Player, List<Sphere>> e : tick.getSpheresMap().entrySet()) {
                graphicsContext.setFill(e.getKey().getColor());
                for (Sphere s : e.getValue()) graphicsContext.fillOval(s.getPosition().getX(), s.getPosition().getY(), 2, 2);
            }
        };
    }

    public void setCurrentTick(int tick) {
        currentTick = tick;
    }

    public void startAnimation() {
        future = scheduler.scheduleAtFixedRate(playTick, 0, constants.getFramesPerSecond(), TimeUnit.MILLISECONDS);
    }

    public void pauseAnimation() {
        isPaused = !isPaused;
        if (isPaused) {
            future.cancel(true);
        } else {
            currentTick = (int) progressBar.getValue();
            future = scheduler.scheduleAtFixedRate(playTick, 0, constants.getFramesPerSecond(), TimeUnit.MILLISECONDS);
        }
    }

    public void stopAnimation() {
        scheduler.shutdownNow();
    }
}
