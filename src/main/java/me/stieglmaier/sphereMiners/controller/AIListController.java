package me.stieglmaier.sphereMiners.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.sun.deploy.uitoolkit.impl.fx.ui.FXUIFactory;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import me.stieglmaier.sphereMiners.model.Player;


public class AIListController implements Initializable{

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

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // create overview ai list
        allAIs.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        allAIs.setItems((ObservableList<String>)resources.getObject("aiList"));

        // created current playing ai table
        aiNameCol.setCellValueFactory(p -> p.getValue().getNameProperty());
        aiSizeCol.setCellValueFactory(p -> p.getValue().getSizeProperty());

        // add button listeners
        createButtonListeners();
    }

    private void createButtonListeners() {
        addAIButton.setOnAction(e -> playingAIs.getItems().addAll(
                allAIs.getSelectionModel()
                      .getSelectedItems()
                      .stream()
                      .filter(p -> playingAIs.getItems()
                                             .stream()
                                             .map(Player::getName)
                                             .noneMatch(a -> a.equals(p)))
                      .map(s -> new Player(s, 10))
                      .collect(Collectors.toList())));

        removeAIButton.setOnAction(e -> playingAIs.getItems().removeAll(
                playingAIs.getItems()
                          .stream()
                          .filter(p -> allAIs.getSelectionModel()
                                             .getSelectedItems()
                                             .stream()
                                             .anyMatch(s -> s.equals(p.getName())))
                          .collect(Collectors.toList())));
    }

}
