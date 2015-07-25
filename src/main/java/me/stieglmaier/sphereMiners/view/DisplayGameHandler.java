package me.stieglmaier.sphereMiners.view;

import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;
import me.stieglmaier.sphereMiners.main.Constants;
import me.stieglmaier.sphereMiners.model.GameSimulation;
import me.stieglmaier.sphereMiners.model.Player;
import me.stieglmaier.sphereMiners.model.Sphere;
import me.stieglmaier.sphereMiners.model.Tick;

/**
 * This class handles the drawing on the canvas, such that the simulation
 * can be viewed.
 *
 * @author stieglma
 *
 */
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

    /**
     * The constructor creates the handler and some listeners that are attached
     * e.g. to the progressBar.
     *
     * @param graphicsContext the graphics object that is used to draw
     * @param simulation the simulation that should be played
     * @param progressBar the progressBar displaying the current viewed state
     * @param playButton the button that is used to start/stop/pause the replay
     * @param playingAIs the tableview showing the current size of the players
     * @param constants the constants which are necessary for computing the view
     */
    public DisplayGameHandler(GraphicsContext graphicsContext, GameSimulation simulation,
                              Slider progressBar, Button playButton, TableView<Player> playingAIs,
                              Constants constants) {
        this.constants = constants;
        this.progressBar = progressBar;

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
                int size = 0;
                for (Sphere s : e.getValue()) {
                    size += s.getSize();
                    double radius = s.getRadius();
                    graphicsContext.fillOval(s.getPosition().getX()-radius, s.getPosition().getY()-radius, radius, radius);
                }
                e.getKey().getSizeProperty().set(size);
            }
            playingAIs.sort();

            for (Sphere s : tick.getDots()) {
                graphicsContext.setFill(s.getColor());
                double radius = s.getRadius();
                graphicsContext.fillOval(s.getPosition().getX()-radius, s.getPosition().getY()-radius, radius, radius);
            }
        };

        currentSliderTickListener = (a, b, n) -> {
            currentTick = (int) (n.doubleValue() * constants.getFramesPerSecond());
            showCurrentTick.run();
        };
    }

    /**
     * Returns the listener for the slider change event
     * @return the listener
     */
    public ChangeListener<Number> getSliderChangedListener() {
        return currentSliderTickListener;
    }

    /**
     * Starts the animation.
     */
    public void startAnimation() {
        future = scheduler.scheduleAtFixedRate(() -> Platform.runLater(playTick), 0, constants.getFramesPerSecond(), TimeUnit.MILLISECONDS);
    }

    /**
     * Pauses or resumes the animation.
     */
    public void pauseResumeAnimation() {
        isPaused = !isPaused;
        if (isPaused) {
            future.cancel(true);
        } else {
            currentTick = (int) progressBar.getValue();
            future = scheduler.scheduleAtFixedRate(() -> Platform.runLater(playTick), 0, constants.getFramesPerSecond(), TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Stops the animation, cannot be undone, the DisplayHandler cannot be used
     * anymore afterwards.
     */
    public void stopAnimation() {
        scheduler.shutdownNow();
    }
}
