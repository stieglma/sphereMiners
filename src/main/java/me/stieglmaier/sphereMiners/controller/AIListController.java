package me.stieglmaier.sphereMiners.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;

public class AIListController implements Initializable{

    @FXML
    private ListView<String> aiList;

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        aiList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        aiList.setItems((ObservableList<String>)resources.getObject("aiList"));
    }
}
