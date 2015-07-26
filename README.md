# Sphere Miners
Sphere Miners is a game played by AI's. It is implemented in Java 8, therefore the AI's have to be
implemented in Java, too. In the game an AI initially controls one Sphere, this Sphere can be splitted
into smaller ones or merged with others to create a greater one. The aim is to defeat other AI's by
"mining" them, which is only possible if the sphere is larger then the mined sphere.

This is currently a very early alpha version, it has not much features and is also not very well tested right now.
* * *

#### Installation
After checking out the project you're almost done, just call `gradle build` to download all needed dependencies
from the Maven Central Repositry and Ivy.

For an easy integration into IntelliJ IDEA or Eclipse use `gradle idea` or `gradle eclipse`.

##### Useful Commands
* With `gradle run` the application can be started.
* With `gradle getConfigOptions` the configuration options that can be specified are saved into the file 
`ConfigurationOptions.txt`
* With `gradle fatJar` are jar file containing all dependencies is created, it can be found in `build/libs`
and has the suffix `-all` in its name
* There is also one relevant commandline option that can be set. With `--config=CustomConfigFileName.txt`
the default configuration can be changed. Configfiles are key value pairs, just like in the generated
`ConfigurationOptions.txt` file.

* * *

#### AI development
Each AI has to extend the class `SphereMiners2015`. Documentation for what can be done is available as javadoc,
for merging, splitting and changing the direction of spheres there are methods that have to be used. All computation
has to be done in a predefined method which is then executed from the framework each frame.

If you are working with a checkout of this repository and start the application via `gradle run` or in eclipse the
AI's have to be located in the directory `src/main/java` (the default package). When using the generated jar file
the AI's class files have to reside in the same folder as the jar file.


