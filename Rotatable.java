import static java.lang.Math.abs;

import processing.core.PVector;

//ClickUIElement which can rotate
public class Rotatable extends ClickUIElement implements GridConstants {
  private PVector loc, head; //position, and orientation tracked by vectors
  private float th; //rotation offset
  private static float r = ROTATABLE_RADIUS;
  private static float l = ROTATABLE_LENGTH;
  private static float delta = ROTATABLE_DELTA_DEFAULT;
  private static final float delta_delta = ROTATABLE_DELTADELTA;

  public static void init_delta() {
    delta = TWO_PI / 360;
  }

  //move with deltas
  public void move(int keyCode) {
    switch(keyCode) {
    case UP:
    case RIGHT:
      inc_rotation();
      break;

    case LEFT:
    case DOWN:
      dec_rotation();
      break;
    }
  }

  //delta getters and setters
  private void inc_rotation() {
    set_rotation(get_rotation() + delta);
  }

  private void dec_rotation() {
    set_rotation(get_rotation() - delta);
  }

  private void set_rotation(float th) {
    float m = head.mag();
    head = PVector.fromAngle(th + this.th).mult(m);
  }

  public static float get_delta() {
    return delta;
  }
  public static float get_deltadelta() {
    return delta_delta;
  }

  public static void increase_delta() {
    delta = abs(delta + delta_delta);
  }

  public static void decrease_delta() {
    delta = abs(delta - delta_delta);
  }

  //return a copy with the copy constructor
  public Rotatable copy() {
    return new Rotatable(loc, head, th, c);
  }

  //private copy constructor
  private Rotatable(PVector loc, PVector head, float th, int c) {
    super(c);
    this.loc = loc.copy();
    this.head = head.copy();
    this.th = th;
  }

  //get rotation value
  public float get_rotation() {
    return (head.heading() + TWO_PI - th) % TWO_PI;
  }

  //set position
  public void set_pos(float x, float y) {
    loc.x = x;
    loc.y = y;
  }

  //public constructor
  public Rotatable(float x, float y, float th, int c) {
    super(c);
    loc = new PVector(x, y);
    head = PVector.fromAngle(th).mult(l);
    this.th = th % TWO_PI;
  }

  //constructor with defaults
  public Rotatable(float x, float y) {
    this(x, y, 0, parent.color(0, 0, 255));
  }

  //check if hovers with some vector calculation
  boolean hovers(float x, float y) {
    return new PVector(x, y).sub(PVector.add(loc, head)).mag() < r;
  }

  //drag method. basically, points towards the new location and resets the
  //length
  void drag(float x, float y) {
    head = new PVector(x, y).sub(loc).normalize().mult(l);
  }

  //draw element
  void draw_element() {
    parent.ellipse(loc.x + head.x, loc.y + head.y, r*2, r*2);
    parent.line(loc.x, loc.y, loc.x + head.x, loc.y + head.y);
  }
}
