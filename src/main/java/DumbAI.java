

import java.util.stream.Collectors;

import javafx.scene.paint.Color;
import me.stieglmaier.sphereMiners.model.Position;
import me.stieglmaier.sphereMiners.model.SphereMiners2015;

public class DumbAI extends SphereMiners2015 {

    @Override
    protected void init() {
        setColor(Color.RED);
    }

    @Override
    protected void playTurn() {
        changeMoveDirection(ownSpheres.stream().collect(Collectors.toMap(s -> s, s -> new Position(1, 1))));
    }

}
