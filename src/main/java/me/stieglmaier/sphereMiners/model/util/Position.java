package me.stieglmaier.sphereMiners.model.util;

public class Position {

  /**
   * delta epsilon constant for floating point operations
   */
  public static final double DEPS = 1.19209289551e-08;

  /**
   * The x coordinate.
   */
  private final double x;
  /**
   * The y coordinate.
   */
  private final double y;

  /**
   * Creates a new {@link Position}.
   */
  public Position() {
    x = 0.0;
    y = 0.0;
  }

  /**
   * Creates a new {@link Position}.
   *
   * @param newX The x-component of the new {@link Position}.
   * @param newY The y-component of the new {@link Position}.
   */
  public Position(final double newX, final double newY) {
    if (Double.isNaN(newX) || Double.isNaN(newY)) {
      throw new IllegalArgumentException("The parameters have to be in the range of a double. x: ");
    }
    this.x = newX;
    this.y = newY;
  }

  /**
   * Returns a new {@link Position} calculated by scaling the current
   * {@link Position} by a given factor.
   *
   * @param f The factor to scale the {@link Position} to.
   * @return a new {@link Position} calculated by scaling the current
   *         {@link Position} by a given factor.
   */
  public Position mult(final double f) {
    return new Position(x * f, y * f);
  }

  /**
   * Returns the sum of the current {@link Position} and a given one.
   *
   * @param p The {@link Position} to add.
   * @return the sum of the current {@link Position} and a given one.
   * @throws IllegalArgumentException if the given Position is null this exception will be thrown.
   */
  public Position add(final Position p) {
    return new Position(x + p.getX(), y + p.getY());
  }

  /**
   * Returns the difference of the current {@link Position} and a given one.
   *
   * @param p The {@link Position} to add.
   * @return the difference of the current {@link Position} and a given one.
   * @throws IllegalArgumentException if the given Position is null this exception will be thrown.
   */
  public Position sub(final Position p) {
    return new Position(x - p.getX(), y - p.getY());
  }

  /**
   * Returns the negative {@link Position}.
   *
   * @return the negative {@link Position}.
   */
  public Position negate() {
    return new Position(-x, -y);
  }

  /**
   * Returns the x-component of this {@link Position}.
   *
   * @return the x-component of this {@link Position}.
   */
  public double getX() {
    return x;
  }

  /**
   * Returns the y-component of this {@link Position}.
   *
   * @return the y-component of this {@link Position}.
   */
  public double getY() {
    return y;
  }

  /**
   * Creates a new Position that has the inverted x coordinate compared to this
   * Position.
   *
   * @return The new Position.
   */
  public Position invertX() {
    return new Position(-x, y);
  }

  /**
   * Creates a new Position that has the inverted y coordinate compared to this
   * Position.
   *
   * @return The new Position.
   */
  public Position invertY() {
    return new Position(x, -y);
  }

  /**
   * Returns the length of this {@link Position}. (as seen from 0/0)
   *
   * @return the length of this {@link Position}.
   */
  public double length() {
    double sqLen = x * x + y * y;
    return Math.sqrt(sqLen);
  }

  /**
   * Calculates the distance of this Position to another one.
   *
   * @param p The other Position.
   * @return The double result.
   * @throws IllegalArgumentException if the given Position is null this exception will be thrown.
   */
  public double dist(final Position p) {
    Position dif = this.sub(p);
    return dif.length();
  }

  /**
   * Returns the normalized-vector of this {@link Position}.
   *
   * @return the normalized-vector of this {@link Position}.
   */
  public Position normalize() {
    double len = length();
    if (len < DEPS) {
      return new Position();
    } else {
      return mult(1 / len);
    }
  }

  /**
   * Method that calculates the scalar product of this vector with another
   * one.
   *
   * @param p The other vector as Position.
   * @return The double result.
   */
  public double scalarProduct(final Position p) {
    return x * p.getX() + y * p.getY();
  }

  /**
   * Calculates the angle between two Positions in degrees. The angle is always
   * positive and the smaller one of the two possible angles. The Positions must
   * not be zero.
   *
   * @param v The other Position.
   * @return The angle between the two Positions in degrees.
   */
  public double angleDeg(final Position v) {
    if (this.equals(new Position()) || v.equals(new Position())) {
      throw new IllegalArgumentException("The Positions must not be zero.");
    }
    double val = normalize().scalarProduct(v.normalize());
    if (val < -1) {
      val = -1;
    } else if (val > 1) {
      val = 1;
    }
    double rad = Math.acos(val);
    return rad * 180 / Math.PI;
  }

  /**
   * Calculates the angle between two Positions in degrees. The angle is between
   * -180 and 180 degrees depending on the orientation. Moreover the smaller
   * (in absolute values) angle of the two possible is returned. The Positions
   * must not be zero.
   *
   * @param v The other Position.
   * @return The angle in degrees.
   */
  public double angleDegOriented(final Position v) {
    if (this.equals(new Position()) || v.equals(new Position())) {
      throw new IllegalArgumentException("The Positions must not be zero.");
    }

    double angleBetween = this.angleDeg(v);
    if (angleBetween < DEPS) {
      return 0;
    }

    double angleThis = this.angleDeg(new Position(1, 0));
    if (y < -DEPS) {
      angleThis = 360 - angleThis;
    }

    double angleV = v.angleDeg(new Position(1, 0));
    if (v.getY() < -DEPS) {
      angleV = 360 - angleV;
    }

    if (angleThis - angleV > 180 || angleV - angleThis > 180) {
      angleBetween = -angleBetween;
    }

    return angleBetween;
  }

  /**
   * Method that calculates a normalized Position in the direction the angle
   * points.
   *
   * @param deg The angle in degrees.
   * @return The result as Position.
   */
  public static Position normVecFromAngle(double deg) {
    // fix degrees to range from 0 to 360
    while (deg > 360) {
      deg -= 360;
    }
    while (deg < 0) {
      deg += 360;
    }

    double rad = deg * Math.PI / 180;
    double u = Math.cos(rad);
    double v = Math.sqrt(1 - u * u);
    if (deg > 180) {
      v = -v;
    }
    return new Position(u, v);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(x);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(y);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  /**
   * {@inheritDoc}
   *
   * This equals method compares two Positions in a defined epsilon environment.
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Position other = (Position) obj;
    if (Math.abs(x - other.x) > DEPS) {
      return false;
    }
    if (Math.abs(y - other.y) > DEPS) {
      return false;
    }
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return "(" + x + ", " + y + ")";
  }
}
