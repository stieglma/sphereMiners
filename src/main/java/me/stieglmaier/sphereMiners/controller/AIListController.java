package me.stieglmaier.sphereMiners.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

public class AIListController implements Initializable{

    @FXML
    private ListView<String> aiList;

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (resources == null) {
            aiList.setItems(FXCollections.observableArrayList("a","b","c"));
        } else {
            aiList.setItems((ObservableList<String>)resources.getObject("aiList"));
        }
    }
}
