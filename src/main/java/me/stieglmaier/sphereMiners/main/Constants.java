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
    private String aiFolderName = "ais";

    @Option(description="Timeout for the computation done by the ais in milliseconds")
    private int aiComputationTime = 50;

    /* Physics related constants */

    @Option(description="The initial distance between the spheres of all ais")
    private int initialDistance = 50;

    @Option(description="The width of the game field in meters")
    private int fieldWidth = 800;

    @Option(description="The height of the game field in meters")
    private int fieldHeight = 800;

    @Option(description="The frames that should be displayed per second")
    private int framesPerSecond = 25;

    @Option(description="The amount of calculations that should be done per tick."
            + " Changing this changes the granularity of the calculations")
    private int calcsPerTick = 1;

    @Option(description="The maximum speed a sphere may have in meter/tick")
    private double maxSpeed = 1.0;

    @Option(description="The minimum speed a sphere may have in meter/tick")
    private double minSpeed = 1.0;

    /* Sphere related constants */

    @Option(description="The initial size for a sphere with which a player starts.")
    private int initialSphereSize = 10;

    @Option(description="The minimal size a sphere has to have before it can be splitted")
    private int minSplittingsize = 20;

    @Option(description="The maximal distance between two spheres that should be merged")
    private int maxMergeDist = 1;



    public Constants(Configuration configuration) throws InvalidConfigurationException {
        configuration.inject(this);
    }

    public String getAILocation() {
        return aiFolderName;
    }

    public int getAIComputationTime() {
        return aiComputationTime;
    }

    public int getInitialDistance() {
        return initialDistance;
    }

    public int getFieldWidth() {
        return fieldWidth;
    }

    public int getFieldHeight() {
        return fieldHeight;
    }

    public int getFramesPerSecond() {
        return framesPerSecond;
    }

    public int getCalcsPerTick() {
        return calcsPerTick;
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public double getMinSpeed() {
        return minSpeed;
    }

    public int getInitialSphereSize() {
        return initialSphereSize;
    }

    public int getMinSplittingsize() {
        return minSplittingsize;
    }

    public int getMaxMergeDist() {
        return maxMergeDist;
    }
}
