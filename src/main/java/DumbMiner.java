
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.scene.paint.Color;
import me.stieglmaier.sphereMiners.model.ai.SphereMiners2015;
import me.stieglmaier.sphereMiners.model.util.Position;
import me.stieglmaier.sphereMiners.model.util.Sphere;

public class DumbMiner extends SphereMiners2015 {

  private Map<Sphere, Position> newDirections = new HashMap<>();
  private Map<Sphere, Sphere> mining = new HashMap<>();
  private List<Sphere> splits = new ArrayList<>();
  private Map<Sphere, Sphere> alreadyTargetedDots = new HashMap<>();

  @Override
  protected void init() {
    setColor(Color.CADETBLUE);
  }

  @Override
  protected void playTurn() {
    newDirections.clear();
    mining.clear();
    splits.clear();
    updateTargetedDots();

    if (ownSpheres.size() < getConstants().getMaxSphereAmount()) {
      growGame();
    } else {
      endGame();
    }

    mine(mining);
    split(splits);
    changeMoveDirection(newDirections);
  }

  private void updateTargetedDots() {
    Iterator<Sphere> it = alreadyTargetedDots.keySet().iterator();
    while (it.hasNext()) {
      Sphere dot = it.next();
      if (!dots.contains(dot)) {
        it.remove();
      }
    }
  }

  private void fetchDots(Sphere ownSphere) {
    // don't change direction if already on the way
    if (alreadyTargetedDots.containsValue(ownSphere)) {
      return;
    }

    Iterator<Sphere> dotsIt = dots.iterator();
    Sphere nextDot = dotsIt.next();
    double minDist = ownSphere.getPosition().dist(nextDot.getPosition());
    while (dotsIt.hasNext()) {
      Sphere dot = dotsIt.next();
      // only go to dots that are not already targeted
      if (!alreadyTargetedDots.containsKey(dot)) {
        double tmpDist = ownSphere.getPosition().dist(dot.getPosition());
        if (tmpDist < minDist) {
          minDist = tmpDist;
          nextDot = dot;
        }
      }
    }
    final Position moveTo = nextDot.getPosition().sub(ownSphere.getPosition());
    alreadyTargetedDots.put(nextDot, ownSphere);
    newDirections.put(ownSphere, moveTo);
  }

  private void growGame() {
    Iterator<Sphere> ownIt = ownSpheres.iterator();

    while (ownIt.hasNext()) {
      Sphere ownSphere = ownIt.next();

      // split if possible
      if (ownSphere.getSize() > getConstants().getMinSplittingsize()) {
        splits.add(ownSphere);
      }

      // mine if possible
      Set<Sphere> enemySpheres = getSurroundingEnemies(ownSphere);
      for (Sphere enemy : enemySpheres) {
        if (ownSphere.canBeMergedWidth(enemy)) {
          mining.put(ownSphere, enemy);
          break;
        }
      }

      // fetch dots
      fetchDots(ownSphere);
    }
  }

  private void endGame() {
    for (Sphere ownSphere : ownSpheres) {
      Position ownPos = ownSphere.getPosition();
      Position nextEnemy = null;

      // check if a mine is possible
      Set<Sphere> enemySpheres = getSurroundingEnemies(ownSphere);
      if (enemySpheres.size() > 0) {
        double minDist = Double.MAX_VALUE;
        for (Sphere enemy : enemySpheres) {
          if (ownSphere.canBeMergedWidth(enemy)) {
            mining.put(ownSphere, enemy);
            break;
          } else if (minDist > ownPos.dist(enemy.getPosition())
              && ownSphere.getSize() > enemy.getSize() + 30) {
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
        fetchDots(ownSphere);
      }
    }
  }
}
