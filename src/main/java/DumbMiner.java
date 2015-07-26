

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.scene.paint.Color;
import me.stieglmaier.sphereMiners.model.Position;
import me.stieglmaier.sphereMiners.model.Sphere;
import me.stieglmaier.sphereMiners.model.SphereMiners2015;

public class DumbMiner extends SphereMiners2015 {

    @Override
    protected void init() {
       setColor(Color.CADETBLUE);
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
        Map<Sphere, Sphere> mining = new HashMap<>();
        while (ownIt.hasNext()) {
            ownSphere = ownIt.next();
            Position ownPos = ownSphere.getPosition();
            Position nextEnemy = null;

            // check if a mine is possible
            Set<Sphere> enemySpheres = getSurroundingEnemies(ownSphere);
            if (enemySpheres.size() > 0) {
                double minDist = Double.MAX_VALUE;
                for (Sphere enemy : enemySpheres){
                    if (ownSphere.canBeMergedWidth(enemy)) {
                        mining.put(ownSphere, enemy);
                        break;
                    } else if (minDist > ownPos.dist(enemy.getPosition())){
                        nextEnemy = enemy.getPosition();
                        minDist = ownPos.dist(nextEnemy);
                    }
                }
            }

            //change direction to get to next enemy (if there is one in sight)
            if (nextEnemy != null) {
                final Position moveTo = nextEnemy.sub(ownSphere.getPosition());
                newDirections.put(ownSphere, moveTo);

            // no enemy in sight so just fetch some dots
            } else {
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
        }
        changeMoveDirection(newDirections);
        mine(mining);
    }

}
