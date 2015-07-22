package me.stieglmaier.sphereMiners.view;

import javafx.scene.Parent;
import me.stieglmaier.sphereMiners.model.Model;

import java.util.Observable;
import java.util.Observer;

public class GUI extends Parent implements Observer {

    public GUI(Model m) {
        m.addObserver(this);
    }



    @Override
    public void update(Observable o, Object arg) {
        // TODO Auto-generated method stub

    }
}
