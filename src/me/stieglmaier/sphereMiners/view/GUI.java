package me.stieglmaier.sphereMiners.view;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;

import me.stieglmaier.sphereMiners.model.Model;

public class GUI extends JFrame implements Observer {

    public GUI(Model m) {
        m.addObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        // TODO Auto-generated method stub
        
    }
}
