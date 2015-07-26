

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
        // first check if we could split
        List<Sphere> splits = new ArrayList<>();
        for(Sphere s : ownSpheres) {
            if (s.getSize() > getConstants().getMinSplittingsize()) {
                splits.add(s);
            }
        }
        split(splits);

        // then change directions
        Sphere ownSphere = null;
        Iterator<Sphere> ownIt = ownSpheres.iterator();
        Map<Sphere, Position> newDirections = new HashMap<>();
        while (ownIt.hasNext()) {
            ownSphere = ownIt.next();
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
            newDirections.put(ownSphere, moveTo);
        }
        changeMoveDirection(newDirections);
    }

}
