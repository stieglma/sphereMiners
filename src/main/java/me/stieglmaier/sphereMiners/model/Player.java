package me.stieglmaier.sphereMiners.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Player {

    private final StringProperty aiName;
    private final IntegerProperty aiSize;

    public Player(String aiName, int aiSize) {
        this.aiName = new SimpleStringProperty(aiName);
        this.aiSize = new SimpleIntegerProperty(aiSize);
    }

    public String getName() {
        return aiName.get();
    }

    public StringProperty getNameProperty() {
        return aiName;
    }


    void setName(String newName) {
        aiName.set(newName);
    }

    public int getSize() {
        return aiSize.get();
    }

    public IntegerProperty getSizeProperty() {
        return aiSize;
    }

    void setSize(int newSize) {
        aiSize.set(newSize);
    }
}
