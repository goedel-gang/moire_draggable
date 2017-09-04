import static java.lang.Math.abs;
import static processing.core.PApplet.dist;

import processing.core.PConstants;

//ClickUIElement which can be dragged around and varies x and y
public class Draggable extends ClickUIElement implements GridConstants {
  private float x, y;
  private static float r = DRAGGABLE_RADIUS;

  //the delta functionality allows a ClickUIElement to be manipulated by arrow
  //keys after being selected
  private static float delta;
  private static final float delta_delta = DRAGGABLE_DELTADELTA;

  public static void init_delta() {
    delta = DRAGGABLE_DELTA_DEFAULT;
  }

  //move according to a key
  public void move(int keyCode) {
    switch(keyCode) {
    case UP:
      y -= delta;
      break;
    case DOWN:
      y += delta;
      break;
    case LEFT:
      x -= delta;
      break;
    case RIGHT:
      x += delta;
      break;
    }
  }

  //various getters and setters for the delta functionality
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

  //make a copy, which can be done with the public constructor trivially
  public Draggable copy() {
    return new Draggable(x, y, c);
  }

  //get x
  public float get_x() {
    return x;
  }

  //get y
  public float get_y() {
    return y;
  }

  //public constructor
  public Draggable(float x, float y, int c) {
    super(c);
    this.x = x;
    this.y = y;
  }

  //simple calculation to determine if a point if nearer to the centre than the
  //radius
  boolean hovers(float x, float y) {
    return dist(x, y, this.x, this.y) < r;
  }

  //simple drag function
  void drag(float x, float y) {
    this.x = x;
    this.y = y;
  }

  //draw the element
  void draw_element() {
    parent.ellipse(x, y, r*2, r*2);
  }
}
