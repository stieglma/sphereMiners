

import java.util.Iterator;
import java.util.stream.Collectors;

import javafx.scene.paint.Color;
import me.stieglmaier.sphereMiners.model.Position;
import me.stieglmaier.sphereMiners.model.Sphere;
import me.stieglmaier.sphereMiners.model.SphereMiners2015;

public class DumbAI extends SphereMiners2015 {

    @Override
    protected void init() {
        setColor(Color.RED);
    }

    @Override
    protected void playTurn() {
        Sphere ownSphere = ownSpheres.iterator().next();
        Iterator<Sphere> dotsIt = dots.iterator();
        Sphere nextDot = dotsIt.next();
        double minDist = ownSphere.getPosition().dist(nextDot.getPosition());
        while (dotsIt.hasNext()) {
            Sphere tmpSphere = dotsIt.next();
            double tmpDist = ownSphere.getPosition().dist(tmpSphere.getPosition());
            if (tmpDist < minDist) {
                minDist = tmpDist;
                nextDot = tmpSphere;
            }
        }
        final Position moveTo = nextDot.getPosition().sub(ownSphere.getPosition());
        changeMoveDirection(ownSpheres.stream().collect(Collectors.toMap(s -> s, s -> moveTo)));
    }

}
