package me.stieglmaier.sphereMiners.model.ai;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.sosy_lab.common.Pair;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import me.stieglmaier.sphereMiners.exceptions.InvalidAILocationException;
import me.stieglmaier.sphereMiners.main.Constants;
import me.stieglmaier.sphereMiners.model.physics.Physics;


/**
 * This class manages all AIs. It handles the calls for the AIs and submits
 * changes made by the AIs to the physics. It also initializes the AIs and
 * checks if the AI behaves correctly and reinitialize an abnormally behaving
 * AI. For example if the calculation of an AI takes too much time, the AI is
 * terminated by the AI Manager and initialized again.
 */
public final class AIManager {

    /**
     * 
     * All available AIs which can be used to simulate a game.
     */
    private final ObservableList<String> aiList = FXCollections.observableArrayList();

    /**
     * array of the active AIs, each AI is identified by {@link Team}.
     */
    private final Map<Player, SphereMiners2015> ais = new LinkedHashMap<>();

    /**
     * The loader which loads the ais.
     */
    private URLClassLoader loader;

    /**
     * The physics engine responsible for calculating all the stuff.
     */
    private Physics physics;

    /**
     * path to location with stored ais.
     */
    private final String AI_FILELOCATION;
    private final int AI_TIME;
    private final Constants constants;

    /**
     * The constructor of this class. It is responsible for listing the possible
     * AIs, so they can be displayed in the View and chosen to simulate games.
     *
     * @param constants The constants that should be used for the AIs class
     * @throws MalformedURLException  Could appear if the Constants.AI_LOCATION
     *                                was malformed
     */
    public AIManager(Constants constants) throws MalformedURLException {
        this.constants = constants;
        AI_FILELOCATION = getAIPath();
        AI_TIME = constants.getAIComputationTime();
        initalizeClassloader();
        makeAiList();
    }

    /**
     * Adds a physics instance to this class
     *
     * @param physics the Physics object that should be used
     */
    public void setPhysics(Physics physics) {
        this.physics = physics;
    }

    /**
     * This method creates the ai path, depending on the path of this class.
     *
     * @return The file location of the ai.
     */
    private String getAIPath() {
        String fileLoc = null;
        try {
            fileLoc = URLDecoder.decode(AIManager.class.getProtectionDomain()
                                .getCodeSource()
                                .getLocation().getPath(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // if this exception is thrown further executing the framework
            // makes no sense, so throw a runtime exception
            throw new RuntimeException("Invalid encoding chosen for URLDecoder.");
        }

        // if everything is packaged in a jar file remove the last part and add the ai folder
        if (fileLoc.endsWith(".jar")) {
            int index = fileLoc.lastIndexOf("/");
            fileLoc = fileLoc.substring(0, index + 1) + constants.getAILocation();

            // if the program is run without jar file just append the ai folder one step over in the hierarchy
        } else {
            fileLoc += constants.getAILocation();
        }

        return fileLoc;
    }

    /**
     * this function initializes the classloader.
     *
     * @throws MalformedURLException Could appear if the Constants.AI_LOCATION
     *                               was malformed
     */
    private void initalizeClassloader() throws MalformedURLException {
        File fileloc = new File(AI_FILELOCATION);
        final URL[] url = new URL[1];
        url[0] = fileloc.toURI().toURL();
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {loader = new URLClassLoader(url); return null;});
    }

    /**
     * This method creates the list of the possible AIs.
     */
    private void makeAiList() {

        // create Loader
        File fileloc = new File(AI_FILELOCATION);
        File[] classes = fileloc.listFiles();

        // if there are no files, classes will be null. (See JavaManual)
        if (classes == null) {
           return;
        }

        Arrays.stream(classes).map(f -> f.getName())
                               // only add ais if they are valid (extend SphereMiner2015 class)
                              .filter(f -> f.endsWith(".class"))
                              .map(f -> f.split(".class")[0])
                              .filter(f -> isValidAi(f))
                              .forEach(f -> aiList.add(f));
    }

    /**
     * Recreate the list of AI's that could be used for playing
     */
    public void reloadAIList() {
        aiList.clear();
        makeAiList();
    }

    /**
     * This method checks if the selected class is of a valid AI type.
     *
     * @param path The path to the selected class.
     * @return The boolean result, if the class is correct.
     */
    private boolean isValidAi(final String path) {
        boolean validAi = true;
        Class<?> loadedAI;

        try {
            loadedAI = loader.loadClass(path);

            // check if the ai implements the AI interface
            // in this case, the ai is valid because it implements the AI
            // interface, so the isValidAi method can instantly return true
            // without proceeding the check
            validAi = loadedAI.getSuperclass().getName().equals(SphereMiners2015.class.getName());

        } catch (ClassNotFoundException e) {
            // do not throw an exception this method should check if the ai
            // is valid, so if not its not necessary to throw an exception
            validAi = false;
        }

        return validAi;
    }

    /**
     * This method returns the complete list of AIs which can be chosen to
     * simulate a game.
     *
     * @return The available AIs.
     */
    public ObservableList<String> getAIList() {
        return aiList;
    }

    /**
     * This method initializes the AIs which should play against each other in
     * the next simulation.
     *
     * @param aisToPlay The list of AI's that should play against each other
     * @throws InstantiationException     If the method is not able to initialize the given two AIs,
     *                                    this exception will be thrown.
     * @throws InvalidAILocationException If the aiLocations are invalid
     */
    public void initializeGameAIs(final List<Player> aisToPlay)
            throws InstantiationException, InvalidAILocationException {

        // cleaning up the list of the last ais.
        ais.clear();

        if (aisToPlay.stream().map(ai -> isValidAi(ai.getInternalName())).anyMatch(p -> !p)) {
            throw new InvalidAILocationException("Some AIs could not be found.");
        }

        // load ais in discrete thread, so one's able to handle bad constructors.
        ScheduledExecutorService aiLoader = Executors.newSingleThreadScheduledExecutor();
        try {
            aiLoader.invokeAll(aisToPlay.stream().map(ai -> loadAI(ai, loader)).collect(Collectors.toList()));

        // this will never happen, but just to be sure, throw an instantiation exception
        // which will be handled elsewhere
        } catch (InterruptedException e1) {
            throw new InstantiationException("Loading AI's was interrupted");
        }

        // wait for ailoader to finish or kill it in case of too long computation times
        try {
            if (!aiLoader.awaitTermination(AI_TIME, TimeUnit.MILLISECONDS)) {
                aiLoader.shutdownNow();
            }
        } catch (InterruptedException e) {
            // Do nothing, the ais are instantiated or not at this point, in
            // case they aren't the next if clause throws an exception
        }

        // if loading was not successful throw an exception
        // otherwise set the relevant fields in the ai
        // and call the init method
        for (Player player : aisToPlay) {
            SphereMiners2015 ai = ais.get(player);
            if (ai == null) {
                throw new InstantiationException("The Ais could not be loaded properly.");
            } else {
                ai.setPlayer(player);
                ai.setPhysics(physics);
                ai.setConstants(constants);
                ai.init();
            }
        }
    }

    /**
     * This method returns a callable where an ai will be initialized if possible.
     *
     * @param ai he player which should be initialized
     * @param loader the loader which is used for initialization
     * @return the callable that can be used for the initialization
     */
    private Callable<Void> loadAI(final Player ai, final URLClassLoader loader) {

        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Class<?> cl;
                try {
                    cl = loader.loadClass(ai.getInternalName());
                } catch (ClassNotFoundException e) {
                    // do nothing, exception is handled in another method
                    return null;
                }

                // search for constructor with zero arguments, and make it
                // accessible
                for (Constructor<?> ct : cl.getConstructors()) {
                    if (ct.getParameterTypes().length == 0) {
                        ct.setAccessible(true);
                        try {
                            ais.put(ai, (SphereMiners2015) ct.newInstance());

                        } catch (InstantiationException
                                | IllegalAccessException
                                | IllegalArgumentException
                                | InvocationTargetException e) {
                            // if any of these errors occured the ai could not
                            // be loaded properly, so the method returns without
                            // doing anything
                            return null;
                        }
                        break;
                    }
                }
                return null;
            }
        };
    }

    /**
     * This method lets all AIs compute one step. If an AIs calculation lasts
     * too long, it is terminated and reinitialized again.
     */
    public void applyMoves() {
        ais.entrySet()
           .parallelStream() // compute parallely if possible
           .map(e -> Pair.of(e.getKey(), e.getValue().evaluateTurn())) // evaluate the turns
           .filter(p -> !p.getSecond()) // get those ais who did not finish sucessfully
           .forEach(p -> reinitializeAi(p.getFirst())); // and reinitialize them
    }

    /**
     * This method reinitializes an AI.
     *
     * @param ai determinate which AI should be reinitialized.
     */
    private void reinitializeAi(Player ai) {
        try {
            Class<?> cl = loader.loadClass(ais.get(ai).getClass().getName());
            ais.remove(ai);
            SphereMiners2015 newAi = (SphereMiners2015) cl.newInstance();
            newAi.setPlayer(ai);
            newAi.setPhysics(physics);
            newAi.setConstants(constants);
            newAi.init();
            ais.put(ai, newAi);
        } catch (ClassNotFoundException
                | IllegalAccessException
                | InstantiationException e1) {
            constants.getLogger().log(Level.SEVERE, "AI " + ai.getInternalName() + " could not be reinitialized, ai is removed from the game");
            ais.remove(ai);
            throw new Error("Reinitialization of "
                    + ais.get(ai).getClass().getName() + " FAILED!");
        }
    }

}
