package me.stieglmaier.sphereMiners.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import me.stieglmaier.sphereMiners.model.GameSimulation;
import me.stieglmaier.sphereMiners.model.Player;


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
    private Button playButton;

    @FXML
    private Button simulateButton;

    @FXML
    private Button deleteSimulationButton;

    @FXML
    private Slider progressBar;

    private boolean isSimulationPaused = false;
    private GameSimulation gameSimulation = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        allAIs.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        removeAIButton.setDisable(true);
        simulateButton.setDisable(true);
        playButton.setDisable(true);
        deleteSimulationButton.setDisable(true);

        setTableViewCells();
        createButtonListeners();
        initializeSlider();
    }

    private void initializeSlider() {
        
    }

    private void setTableViewCells() {
        aiNameCol.setCellValueFactory(p -> p.getValue().getNameProperty());
        aiSizeCol.setCellValueFactory(p -> p.getValue().getSizeProperty());
    }

    public void setAIList(ObservableList<String> aiList) {
        allAIs.setItems(aiList);
    }

    public void setSimulateListeners(final Function<List<String>, GameSimulation> startMethod,
                                     final Runnable pauseMethod,
                                     final Runnable deleteMethod) {
        simulateButton.setOnAction(e -> {
            if (gameSimulation == null) {
                try {
                    gameSimulation = startMethod.apply(playingAIs.getItems().stream().map(i -> i.getName()).collect(Collectors.toList()));
                    gameSimulation.addObserver(t -> progressBar.setMax(gameSimulation.getSize()));
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
            playButton.setDisable(true);
            playButton.setText("Play");
            progressBar.setMax(0);
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
                                             .map(Player::getName)
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
                                             .anyMatch(s -> s.equals(p.getName())))
                          .collect(Collectors.toList()));
            if (playingAIs.getItems().isEmpty()) {
                removeAIButton.setDisable(true);
                simulateButton.setDisable(true);
            }
            if (playingAIs.getItems().size() < allAIs.getItems().size()) {
                addAIButton.setDisable(false);
            }
        });

        // TODO drawing
        playButton.setOnAction(e -> {});
    }

}
