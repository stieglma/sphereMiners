package me.stieglmaier.sphereMiners.main;

import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.configuration.Option;
import org.sosy_lab.common.configuration.Options;

@Options(prefix="constants")
public class Constants {

    /* AI related constants */

    @Option(description="In which folder should the framework search for ais?"
            + " (Base is the root of the project/ the folder where the jar file is located)")
    private String aiFolderName = "";

    @Option(description="Timeout for the computation done by the ais in milliseconds")
    private int aiComputationTime = 50;

    /* Physics related constants */

    @Option(description="The initial distance between the spheres of all ais")
    private int initialDistance = 50;

    @Option(description="The width of the game field in meters")
    private int fieldWidth = 400;

    @Option(description="The height of the game field in meters")
    private int fieldHeight = 400;

    @Option(description="The frames that should be displayed per second")
    private int framesPerSecond = 25;

    @Option(description="The amount of calculations that should be done per tick."
            + " Changing this changes the granularity of the calculations")
    private int calcsPerTick = 10;

    @Option(description="The maximum speed a sphere may have in meter/tick")
    private double maxSpeed = 20.0;

    @Option(description="The minimum speed a sphere may have in meter/tick")
    private double minSpeed = 2.0;

    /* Sphere related constants */

    @Option(description="The initial size for a sphere with which a player starts.")
    private int initialSphereSize = 10;

    @Option(description="The minimal size a sphere has to have before it can be splitted")
    private int minSplittingsize = 20;

    @Option(description="The minimal overlapping area between two spheres that should be merged")
    private int minMergeDist = 1;


    /**
     * Create the Constants object. It only consists of configurable constants
     * that are used in the whole framework. These constants are private and
     * can be accessed via getters, but not changed.
     *
     * @param configuration The configuration object to set the values of all constants
     * @throws InvalidConfigurationException if the configuration is invalid
     */
    public Constants(Configuration configuration) throws InvalidConfigurationException {
        configuration.inject(this);
    }

    /**
     * The relative location in the file system where the ais can be found.
     * @return the location where AIs can be found.
     */
    public String getAILocation() {
        return aiFolderName;
    }

    /**
     * The maximal computation time for one tick per AI.
     * @return the maximal computation time for one tick
     */
    public int getAIComputationTime() {
        return aiComputationTime;
    }

    /**
     * The initial distance all AI's should have to each other
     * @return the initial distance between two ais
     */
    public int getInitialDistance() {
        return initialDistance;
    }

    /**
     * The field width of the playground.
     * @return the field width of the playground
     */
    public int getFieldWidth() {
        return fieldWidth;
    }

    /**
     * The field height of the playground.
     * @return the field height of the playground
     */
    public int getFieldHeight() {
        return fieldHeight;
    }

    /**
     * The amount of frames per second.
     * @return the amount of frames per second
     */
    public int getFramesPerSecond() {
        return framesPerSecond;
    }

    /**
     * The amount of (physical) computations per frame
     * @return the amount of computations per frame
     */
    public int getCalcsPerTick() {
        return calcsPerTick;
    }

    /**
     * The maximum speed of a sphere.
     * @return The maximum speed of a sphere
     */
    public double getMaxSpeed() {
        return maxSpeed;
    }

    /**
     * The minimum speed of a sphere
     * @return The minimum speed of a sphere
     */
    public double getMinSpeed() {
        return minSpeed;
    }

    /**
     * The initial size of a sphere.
     * @return the initial size of a sphere
     */
    public int getInitialSphereSize() {
        return initialSphereSize;
    }

    /**
     * The minimal size a sphere has to have for splitting.
     * @return the minimal sphere size for splitting
     */
    public int getMinSplittingsize() {
        return minSplittingsize;
    }

    /**
     * The minimal overlapping distance of to sphere to merge the smaller one
     * into the bigger one
     * @return the minimal overlapping distance for merging spheres
     */
    public int getMinMergeDist() {
        return minMergeDist;
    }
}
