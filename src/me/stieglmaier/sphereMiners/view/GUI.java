package me.stieglmaier.sphereMiners.view;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import me.stieglmaier.sphereMiners.model.Model;

public class GUI extends JFrame implements Observer {

    /**
     * 
     */
    private static final long serialVersionUID = -7595502010828226886L;

    public GUI(Model m) {
        m.addObserver(this);
        add(new JLabel("test"));
        setVisible(true);
    }

    @Override
    public void update(Observable o, Object arg) {
        // TODO Auto-generated method stub
        
    }
}
