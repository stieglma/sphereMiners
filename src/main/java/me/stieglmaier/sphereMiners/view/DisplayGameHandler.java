package me.stieglmaier.sphereMiners.view;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javafx.beans.value.ChangeListener;
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
    private final Runnable showCurrentTick;
    private final Slider progressBar;
    private final Constants constants;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> future;
    private boolean isPaused = false;

    private final ChangeListener<Number> currentSliderTickListener;

    public DisplayGameHandler(GraphicsContext graphicsContext, GameSimulation simulation, Slider progressBar, Button playButton, Constants constants) {
        this.constants = constants;
        this.progressBar = progressBar;

        currentSliderTickListener = (a, b, n) -> {
            currentTick = (int) (n.doubleValue() * constants.getFramesPerSecond());
            showCurrentTick.run();
        };

        playTick = () -> {
            // check if current tick is available before retrieving it
            if (currentTick >= simulation.getSize()) {
                future.cancel(true);
                playButton.setText("pause");
            }
            progressBar.increment();
        };

        showCurrentTick = () -> {
            // clear drawing area
            graphicsContext.clearRect(0, 0, constants.getFieldWidth(), constants.getFieldHeight());

            // retrieve tick
            Tick tick = simulation.getTick(currentTick);

            //do drawing on graphics object
            for (Entry<Player, List<Sphere>> e : tick.getSpheresMap().entrySet()) {
                graphicsContext.setFill(e.getKey().getColor());
                for (Sphere s : e.getValue()) {
                    double radius = Math.sqrt(s.getSize());
                    graphicsContext.fillOval(s.getPosition().getX()-radius, s.getPosition().getY()-radius, radius, radius);
                }
            }
        };
    }

    public ChangeListener<Number> getSliderChangedListener() {
        return currentSliderTickListener;
    }

    public void setCurrentTick(int tick) {
        currentTick = tick;
    }

    public void showCurrentTick() {
        showCurrentTick.run();
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
