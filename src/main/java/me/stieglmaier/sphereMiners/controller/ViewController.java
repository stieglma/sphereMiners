package me.stieglmaier.sphereMiners.controller;

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import me.stieglmaier.sphereMiners.main.Constants;
import me.stieglmaier.sphereMiners.model.GameSimulation;
import me.stieglmaier.sphereMiners.model.Player;
import me.stieglmaier.sphereMiners.view.DisplayGameHandler;


/**
 * The controller handling all the stuff between model and view.
 *
 * @author stieglma
 *
 */
public class ViewController implements Initializable{

    @FXML
    private ListView<String> allAIs;

    @FXML
    private TableView<Player> playingAIs;
    @FXML
    private TableColumn<Player, String> aiNameCol;
    @FXML
    private TableColumn<Player, Number> aiSizeCol;

    @FXML
    private Button addAIButton;
    @FXML
    private Button removeAIButton;
    @FXML
    private Button reloadAIButton;

    @FXML
    private Button playButton;
    @FXML
    private Button simulateButton;
    @FXML
    private Button deleteSimulationButton;

    @FXML
    private Slider progressBar;
    @FXML
    private Canvas viewGameCanvas;

    private boolean isSimulationPaused = false;
    private GameSimulation gameSimulation = null;
    private DisplayGameHandler displayGameHandler = null;

    private Constants constants;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        allAIs.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        removeAIButton.setDisable(true);
        simulateButton.setDisable(true);
        playButton.setDisable(true);
        deleteSimulationButton.setDisable(true);

        setTableViewCells();
        createButtonListeners();
    }

    private void setTableViewCells() {
        aiNameCol.setCellValueFactory(p -> p.getValue().getNameProperty());
        aiNameCol.setSortable(false);
        aiSizeCol.setCellValueFactory(p -> p.getValue().getSizeProperty());
        aiSizeCol.setSortType(SortType.DESCENDING);
        playingAIs.setSortPolicy(new Callback<TableView<Player>, Boolean>() {
            
            @Override
            public Boolean call(TableView<Player> param) {
                param.getItems().sort((a,b) -> {
                    return b.getSizeProperty().get() - a.getSizeProperty().get();
                });
                return true;
            }
        });
    }

    /**
     * Set the list containing all AIs that could be used for playing.
     * @param aiList the list containting all ais
     */
    public void setAIList(ObservableList<String> aiList) {
        allAIs.setItems(aiList);
    }

    /**
     * Set the constants used throughout the whole project.
     * @param constants the used constants
     */
    public void setConstants(Constants constants) {
        this.constants = constants;
        // resize canvas to match field size, scaling to viewport is done elsewhere
        viewGameCanvas.setWidth(constants.getFieldWidth());
        viewGameCanvas.setHeight(constants.getFieldHeight());

        // set increment size of progressbar
        progressBar.setBlockIncrement(1.0/constants.getFramesPerSecond());
    }

    /**
     * Sets the simulation listeners / callbacks that are necessary to show 
     * something in the view.
     *
     * @param startMethod the method for starting the simulation
     * @param pauseMethod the method for pausing the compuatation of a simulation
     * @param deleteMethod the method for deleting a simulation
     * @param reloadAIList the method for reloading the list of playable ais
     */
    public void setListeners(final Function<List<Player>, GameSimulation> startMethod,
                                     final Runnable pauseMethod,
                                     final Runnable deleteMethod,
                                     final Runnable reloadAIList) {
        simulateButton.setOnAction(e -> {
            if (gameSimulation == null) {
                try {
                    gameSimulation = startMethod.apply(playingAIs.getItems());
                    gameSimulation.addObserver(t -> progressBar.setMax(gameSimulation.getSize()/constants.getFramesPerSecond()));
                    simulateButton.setText("Pause");
                    deleteSimulationButton.setDisable(false);
                    playButton.setDisable(false);
                    addAIButton.setDisable(true);
                    removeAIButton.setDisable(true);
                    reloadAIButton.setDisable(true);
                } catch (Exception e1) {
                    e1.printStackTrace();
                    throw new RuntimeException("Error during starting Simulation");
                }
            } else {
                if (isSimulationPaused) {
                    simulateButton.setText("Pause");
                } else {
                    simulateButton.setText("Resume");
                }
                isSimulationPaused = !isSimulationPaused;
                pauseMethod.run();
            }
        });

        deleteSimulationButton.setOnAction(e -> {
            deleteMethod.run();
            gameSimulation.removeObservers();
            gameSimulation = null;
            isSimulationPaused = false;
            simulateButton.setText("Simulate");
            playButton.setText("Play");
            progressBar.setMax(0);
            progressBar.setValue(0);
            progressBar.valueProperty().removeListener(displayGameHandler.getSliderChangedListener());
            displayGameHandler.stopAnimation();
            addAIButton.setDisable(false);
            removeAIButton.setDisable(false);
            reloadAIButton.setDisable(false);
            playButton.setDisable(true);
            deleteSimulationButton.setDisable(true);
        });

        reloadAIButton.setOnAction(e -> {
            playingAIs.getItems().clear();
            reloadAIList.run();
        });
    }

    private void createButtonListeners() {
        addAIButton.setOnAction(e -> {
            playingAIs.getItems().addAll(
                allAIs.getSelectionModel()
                      .getSelectedItems()
                      .stream()
                      .filter(p -> playingAIs.getItems()
                                             .stream()
                                             .map(Player::getInternalName)
                                             .noneMatch(a -> a.equals(p)))
                      .map(s -> new Player(s, 10))
                      .collect(Collectors.toList()));
            if (playingAIs.getItems().size() == allAIs.getItems().size()) {
                addAIButton.setDisable(true);
            }
            if (playingAIs.getItems().size() > 0) {
                removeAIButton.setDisable(false);
                simulateButton.setDisable(false);
            } 
        });

        removeAIButton.setOnAction(e -> {
            playingAIs.getItems().removeAll(
                playingAIs.getItems()
                          .stream()
                          .filter(p -> allAIs.getSelectionModel()
                                             .getSelectedItems()
                                             .stream()
                                             .anyMatch(s -> s.equals(p.getInternalName())))
                          .collect(Collectors.toList()));
            if (playingAIs.getItems().isEmpty()) {
                removeAIButton.setDisable(true);
                simulateButton.setDisable(true);
            }
            if (playingAIs.getItems().size() < allAIs.getItems().size()) {
                addAIButton.setDisable(false);
            }
        });

        playButton.setOnAction(e -> {
            if (displayGameHandler != null) {
                if (playButton.getText().equals("pause")) {
                    playButton.setText("play");
                } else {
                    playButton.setText("pause");
                }
                displayGameHandler.pauseResumeAnimation();
            } else {
                playButton.setText("pause");
                displayGameHandler = new DisplayGameHandler(viewGameCanvas.getGraphicsContext2D(),
                                                            gameSimulation,
                                                            progressBar,
                                                            playButton,
                                                            playingAIs,
                                                            constants);
                progressBar.valueProperty().addListener(displayGameHandler.getSliderChangedListener());
                displayGameHandler.startAnimation();
            }
        });

        progressBar.setOnDragDetected(e -> {});
    }

}
