package me.stieglmaier.sphereMiners.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;

public class Player {

    private final String internalAIName;
    private StringProperty displayName;
    private final IntegerProperty aiSize;
    private Color color;

    public Player(String internalAiName, int aiSize) {
        this.internalAIName = internalAiName;
        this.aiSize = new SimpleIntegerProperty(aiSize);
        color = Color.BLACK;
        displayName = new SimpleStringProperty(internalAiName);
    }

    public String getInternalName() {
        return internalAIName;
    }

    public ReadOnlyStringProperty getNameProperty() {
        return displayName;
    }

    /**
     * package private such that only model and the aimanager / the ai itself
     * may call this method
     */
    void setName(String name) {
        displayName.setValue(name);
    }

    public Color getColor() {
        return color;
    }

    /**
     * package private such that only model and the aimanager / the ai itself
     * may call this method
     */
    void setColor(Color newColor) {
        color = newColor;
    }

    public int getSize() {
        return aiSize.get();
    }

    public ReadOnlyIntegerProperty getSizeProperty() {
        return aiSize;
    }

    /**
     * package private such that only model and the aimanager
     * may call this method
     */
    void setSize(int newSize) {
        aiSize.set(newSize);
    }

    public String toString() {
        return displayName.get() + " (" + aiSize.get() + ", " + color + ")";
    }
}
