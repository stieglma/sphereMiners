package me.stieglmaier.sphereMiners.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;

/**
 * Player class, represents an AI in the framework.
 *
 * @author stieglma
 *
 */
public class Player {

    private final String internalAIName;
    private StringProperty displayName;
    private final IntegerProperty displayedOverallSize;
    private Color color;

    /**
     * Create a Player, needs the internally used name, and the initial size of
     * the ai.
     *
     * @param internalAiName This will be the name used for identification in the
     *                       framework, usually the classname, as it is unique
     * @param aiSize this is the initial size of the player, usually the initial
     *               sphere size from the constants class
     */
    public Player(String internalAiName, int aiSize) {
        this.internalAIName = internalAiName;
        displayedOverallSize = new SimpleIntegerProperty(aiSize);
        color = Color.BLACK;
        displayName = new SimpleStringProperty(internalAiName);
    }

    /**
     * Returns the internally used name.
     *
     * @return the internal name used for representing this AI.
     */
    public String getInternalName() {
        return internalAIName;
    }

    /**
     * Returns the name that can be set by the ai as a property.
     *
     * @return the name of the AI
     */
    public ReadOnlyStringProperty getNameProperty() {
        return displayName;
    }

    /**
     * package private such that only model and the aimanager / the ai itself
     * may call this method
     *
     * @param name The name the player should have from now on.
     */
    void setName(String name) {
        displayName.setValue(name);
    }

    /**
     * Returns the color the player chose to have.
     *
     * @return The color of the AI
     */
    public Color getColor() {
        return color;
    }

    /**
     * package private such that only model and the aimanager / the ai itself
     * may call this method
     *
     * @param The color the player should have from now on.
     */
    void setColor(Color newColor) {
        color = newColor;
    }

    /**
     * The current size of the AI as a property (it can be listened for changes).
     * The size is the sum of all spheres sizes owned by this player.
     *
     * @return The current size of the ai.
     */
    public IntegerProperty getSizeProperty() {
        return displayedOverallSize;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return displayName.get() + " (" + color + ")";
    }
}
