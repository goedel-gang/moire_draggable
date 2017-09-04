import static java.lang.Math.abs;
import static processing.core.PApplet.map;
import static processing.core.PApplet.constrain;

import processing.core.PVector;

//ClickUIElement to extend with the mouse, providing one linear scalar value
public class Extendable extends ClickUIElement implements GridConstants {
  private PVector len, loc; //vectors to keep position, orientation and magnitude
  private float min, max, th; //floats to vary between
  private static final float r = EXTENDABLE_RADIUS;
  private static final float range = EXTENDABLE_RANGE;
  //delta again
  private static float delta;
  private static final float delta_delta = EXTENDABLE_DELTADELTA;
  
  public static void init_delta() {
    delta = EXTENDABLE_DELTA_DEFAULT;
  }
  
  //move with arrow keys using delta functionality
  public void move(int keyCode) {
    switch(keyCode) {
    case UP:
    case RIGHT:
      inc_mag();
      break;

    case LEFT:
    case DOWN:
      dec_mag();
      break;
    }
  }
  
  public void set_value(float val) {
    len.setMag(map(constrain(val, min, max), 0, max, 1, range));
  }

  //delta getters and setters
  private void set_mag(float mag) {
    len.setMag(mag);
  }

  private void inc_mag() {
    len.setMag(len.mag() + delta);
  }

  private void dec_mag() {
    len.setMag(len.mag() - delta);
  }

  public static void increase_delta() {
    delta = abs(delta + delta_delta);
  }

  public static void decrease_delta() {
    delta = abs(delta - delta_delta);
  }

  public static float get_delta() {
    return delta;
  }
  public static float get_deltadelta() {
    return delta_delta;
  }

  //make a copy using the copy constructor
  public Extendable copy() {
    return new Extendable(loc, len, th, min, max, c);
  }

  //private copy constructor
  private Extendable(PVector loc, PVector len, float th, float min, float max, int c) {
    super(c);
    this.loc = loc.copy();
    this.len = len.copy();
    this.min = min;
    this.max = max;
    this.th = th;
  }

  //get value
  public float get_value() {
    return constrain(map(len.mag(), 0, range, 0, max), min, max);
  }

  //set position
  public void set_pos(float x, float y) {
    loc.x = x;
    loc.y = y;
  }

  //set rotation
  public void set_rotation(float th) {
    float m = len.mag();
    len = PVector.fromAngle(th + this.th).mult(m);
  }

  //public constructor
  public Extendable(float x, float y, float th, float min, float max, int c) {
    super(c);
    loc = new PVector(x, y);
    len = PVector.fromAngle(th).mult(range / 2);
    this.min = min;
    this.max = max;
    this.th = th;
  }

  //public constructor setting some default values
  public Extendable(float x, float y) {
    this(x, y, 0, 0, 50, parent.color(0, 255, 0));
  }

  //check if point hovers whith some simple vector calculations
  boolean hovers(float x, float y) {
    return new PVector(x, y).sub(PVector.add(loc, len)).mag() < r;
  }

  //when the mouse is dragged, adjust the length. For a nice effect that allows
  //the mouse to go to the side, I use the dot product to find how far in the
  //direction of the direction vector the mouse vector is positioned
  void drag(float x, float y) {
    float l = PVector.dot(new PVector(x, y).sub(loc), len.normalize());
    l = constrain(l, 1, range);
    len.setMag(l);
  }

  //draw element
  void draw_element() {
    parent.ellipse(loc.x + len.x, loc.y + len.y, r*2, r*2);
    parent.line(loc.x, loc.y, loc.x + len.x, loc.y + len.y);
  }
}