package me.stieglmaier.sphereMiners.view;

import java.util.Observable;
import java.util.Observer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import me.stieglmaier.sphereMiners.model.Model;

public class GUI extends Application implements Observer {

    /**
     * 
     */
    private static final long serialVersionUID = -7595502010828226886L;

    public GUI(Model m) {
        m.addObserver(this);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }

    @Override
    public void update(Observable o, Object arg) {
        // TODO Auto-generated method stub
        
    }
}
