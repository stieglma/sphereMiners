package me.stieglmaier.sphereMiners.view;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

/**
 * A StackPane that <i>scales</i> its contents to fit (preserving aspect ratio),
 * or fill (scaling independently in X and Y) the available area.
 * <p>
 * Note <code>AutoScalingStackPane</code> applies to the contents a scaling
 * transformation rather than attempting to resize the contents.
 * <p>
 * If the contents is a Canvas with pixel dimension 50 by 50, after scaling the
 * Canvas still will have 50 by 50 pixels and the appearance may be pixelated
 * (this might be desired if the application is interfacing a camera and the
 * Canvas needs to match in size the camera's CCD size).
 * <p>
 * If the content contains FX Controls then these get magnified rather than
 * resized, that is all text and graphics are scaled (this might be desired for
 * Point of Sale full screen applications)
 * <h3>Known Limitations</h3>
 * Rescaling occurs only when the AutoScalingStackPane is resized, it does not
 * occur automatically if and when the content changes size.
 *
 *
 * @author michaelellis
 */
public class AutoScalingStackPane extends StackPane {

    /**
     * Force scale transformation to be recomputed based on the size of this
     * <code>AutoScalingStackPane</code> and the size of the contents.
     */
    public void rescale() {
        if (!getChildren().isEmpty()) {
            getChildren().forEach((c) -> {
                double xScale = getWidth() / c.getBoundsInLocal().getWidth();
                double yScale = getHeight() / c.getBoundsInLocal().getHeight();
                if (autoScale.get() == AutoScale.FILL) {
                    c.setScaleX(xScale);
                    c.setScaleY(yScale);
                } else if (autoScale.get() == AutoScale.FIT) {
                    double scale = Math.min(xScale, yScale);
                    c.setScaleX(scale);
                    c.setScaleY(scale);
                } else {
                    c.setScaleX(1d);
                    c.setScaleY(1d);
                }
            });
        }
    }

    private void init() {
        widthProperty().addListener((b, o, n) -> rescale());
        heightProperty().addListener((b, o, n) -> rescale());
    }

    /**
     * No argument constructor required for Externalizable (need this to work
     * with SceneBuilder).
     */
    public AutoScalingStackPane() {
        super();
        init();
    }

    /**
     * Convenience constructor that takes a content Node.
     *
     * @param content The content of this pane
     */
    public AutoScalingStackPane(Node content) {
        super(content);
        init();
    }

    /**
     * AutoScale scaling options:
     * {@link AutoScale#NONE}, {@link AutoScale#FILL}, {@link AutoScale#FIT}
     */
    public enum AutoScale {

        /**
         * No scaling - revert to behaviour of <code>StackPane</code>.
         */
        NONE,
        /**
         * Independently scaling in x and y so content fills whole region.
         */
        FILL,
        /**
         * Scale preserving content aspect ratio and center in available space.
         */
        FIT
    }

    // AutoScale Property
    private ObjectProperty<AutoScale> autoScale = new SimpleObjectProperty<AutoScale>(this, "autoScale",
            AutoScale.FIT);

    /**
     * AutoScalingStackPane scaling property
     *
     * @return AutoScalingStackPane scaling property
     * @see AutoScale
     */
    public ObjectProperty<AutoScale> autoScaleProperty() {
        return autoScale;
    }

    /**
     * Get AutoScale option
     *
     * @return the AutoScale option
     * @see AutoScale
     */
    public AutoScale getAutoScale() {
        return autoScale.getValue();
    }

    /**
     * Set the AutoScale option
     *
     * @param newAutoScale the AutoScale option to use
     * @see AutoScale
     *
     */
    public void setAutoScale(AutoScale newAutoScale) {
        autoScale.setValue(newAutoScale);
    }
}
