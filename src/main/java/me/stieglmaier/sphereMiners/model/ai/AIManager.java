package me.stieglmaier.sphereMiners.model.ai;

import static java.util.Objects.requireNonNull;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import me.stieglmaier.sphereMiners.exceptions.InvalidAILocationException;
import me.stieglmaier.sphereMiners.model.physics.PhysicsManager;

import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.configuration.Option;
import org.sosy_lab.common.configuration.Options;


/**
 * This class manages all AIs. It handles the calls for the AIs and submits
 * changes made by the AIs to the physics. It also initializes the AIs and
 * checks if the AI behaves correctly and reinitialise an abnormally behaving
 * AI. For example if the calculation of an AI takes too much time, the AI is
 * terminated by the AI Manager and initialized again.
 */
@Options(prefix="ai")
public final class AIManager {

    /**
     * 
     * All available AIs which can be used to simulate a game.
     */
    private final ObservableList<String> aiList = FXCollections.observableArrayList();

    /**
     * array the the active AIs, each AI is identified by {@link Team}.
     */
    private final Map<String, SphereMiners2015> ais = new HashMap<>();

    /**
     * The loader which loads the ais.
     */
    private URLClassLoader loader;

    /**
     * The physics engine responsible for calculating all the stuff.
     */
    private PhysicsManager physicsManager;

    /**
     * path to location with stored ais.
     */
    private final String AI_FILELOCATION;

    @Option(name="location", description="In which folder should the framework search for ais?"
            + " (Base is the root of the project/ the folder where the jar file is located)")
    private String AI_FOLDER_NAME = "ais";

    @Option(name="timeout", description="Timeout for the computation done by the ais in milliseconds")
    private int AI_TIME = 50;

    /**
     * The constructor of this class. It is responsible for listing the possible
     * AIs, so they can be displayed in the View and chosen to simulate games.
     *
     * @throws ClassNotFoundException Could appear if a class file is deleted
     *                                in the runtime of this method
     * @throws MalformedURLException  Could appear if the Constants.AI_LOCATION
     *                                was malformed
     * @throws InvalidConfigurationException 
     */
    public AIManager(Configuration config) throws ClassNotFoundException, MalformedURLException, InvalidConfigurationException {
        config.inject(this);
        AI_FILELOCATION = getAIPath();
        initalizeClassloader();
        makeAiList();
    }

    /**
     * Adds a physicsManager instance to this class
     * @param physMgr
     */
    public void setPhysicsManager(PhysicsManager physMgr) {
        physicsManager = physMgr;
    }

    /**
     * This method creates the ai path, depending on the path of this class.
     *
     * @throws UnsupportedEncodingException if the encoding is invalid
     *                                      (Will never happen a standard java charset is used)
     * @ return The file location of the ai.
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
            fileLoc = fileLoc.substring(0, index + 1);
            fileLoc += AI_FOLDER_NAME;

            // if the program is run without jar file just append the ai folder one step over in the hierarchy
        } else {
            fileLoc += AI_FOLDER_NAME;
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
     *
     * @throws ClassNotFoundException Could appear if a class file is deleted
     *                                in the runtime of this method
     */
    private void makeAiList() throws ClassNotFoundException {

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

        } catch (ClassFormatError
                | NoClassDefFoundError
                | ClassNotFoundException e) {
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
     * @param ai1 The first AI.
     * @param ai2 The second AI.
     * @throws IllegalArgumentException   If a reference to a given AI is invalid.
     * @throws InstantiationException     If the method is not able to initialize the given two AIs,
     *                                    this exception will be thrown.
     * @throws InterruptedException 
     * @throws InvalidAiLocationException If the aiLocations are invalid
     */
    public void initializeGameAIs(final List<String> ais2)
            throws InstantiationException, InvalidAILocationException {

        // cleaning up the list of the last ais.
        ais.clear();

        if (ais2.stream().map(ai -> isValidAi(ai)).anyMatch(p -> !p)) {
            System.out.println(ais2);
            System.out.println(ais2.stream().map(ai -> isValidAi(ai)).collect(Collectors.toList()));
            throw new InvalidAILocationException("Some AIs could not be found.");
        }

        // load ais in discrete thread, so one's able to handle bad constructors.
        ScheduledExecutorService aiLoader = Executors.newSingleThreadScheduledExecutor();
        try {
            aiLoader.invokeAll(ais2.stream().map(ai -> loadAI(ai, loader)).collect(Collectors.toList()));

            // pretty bad something interrupted our loading process...
        } catch (InterruptedException e1) {
            throw new RuntimeException(); // TODO investigate if this is really necessary
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
        for (String aiStr : ais2) {
            SphereMiners2015 ai = ais.get(aiStr);
            if (ai == null) {
                throw new InstantiationException("The Ais could not be loaded properly.");
            } else {
                ai.setName(aiStr);
                ai.setManager(physicsManager);
                ai.init();
            }
        }
    }

    /**
     * This method returns a thread where an ai will be initialized if possible.
     *
     * @param ai             the name of the class which should be initialized
     * @param loader         the loader which is used for initialization
     * @param aiToInitialize this enum indicates which global variable (ai1, ai2) should be
     *                       assigned the newly initialized ai
     * @return the Thread for the initialization
     */
    private Callable<Void> loadAI(final String ai, final URLClassLoader loader) {

        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Class<?> cl;
                try {
                    cl = loader.loadClass(ai);
                } catch (ClassFormatError
                        | NoClassDefFoundError
                        | ClassNotFoundException e) {
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
                                | InvocationTargetException
                                | VerifyError e) {
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
     * too long, it is terminated and reinitialized again. After the threads
     * finished their calculation all their moves are given to the Physics. If
     * a Thread is terminated before it could finish its calculations all its
     * moves until the termination are given to the Physics.
     *
     * @throws IllegalArgumentException if the tick-parameter is invalid (e.g. null).
     * @throws InterruptedException
     */
    public void applyMoves() throws IllegalArgumentException, InterruptedException {

        ais.values().stream().forEach(ai -> requireNonNull(ai));

        // execute AIs and Launcher
        ExecutorService threadpool = Executors.newCachedThreadPool();
        List<Future<Void>> retvals = threadpool.invokeAll(ais.values().stream()
            .map(ai -> (Callable<Void>)(() -> {ai.evaluateTurn(); return null;}))
            .collect(Collectors.toList()));

        if (!threadpool.awaitTermination(AI_TIME, TimeUnit.MILLISECONDS)) {
            threadpool.shutdownNow();
            String[] aiStr = new String[ais.size()];
            ais.keySet().toArray(aiStr);
            for (int i = 0; i < ais.size(); i++) {
                Future<Void> ret = retvals.get(i);
                if (ret.isCancelled()) {
                    try {
                        reinitializeAi(aiStr[i]);
                    } catch (ClassNotFoundException
                            | IllegalAccessException
                            | InstantiationException e1) {
                        throw new Error("Reinitialization of "
                                + ais.get(aiStr[i]).getClass().getName()
                                + " FAILED!");
                    }
                }
                i++;
            }
        } 
    }

    /**
     * This method reinitializes an AI.
     *
     * @param ai determinate which AI should be reinitialized.
     * @throws ClassNotFoundException Could appear if a class file is deleted in the runtime of
     *                                this method
     * @throws InstantiationException if the class can't be instantiated anymore.
     * @throws IllegalAccessException if the class could not be accessed.
     */
    private void reinitializeAi(String ai) throws ClassNotFoundException,
            IllegalAccessException, InstantiationException {

        Class<?> cl = loader.loadClass(ais.get(ai).getClass().getName());
        ais.remove(ai);
        SphereMiners2015 newAi = (SphereMiners2015) cl.newInstance();
        newAi.setName(ai);
        newAi.setManager(physicsManager);
        newAi.init();
        ais.put(ai, newAi);
    }

}
