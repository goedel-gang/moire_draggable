import processing.core.PApplet;
import processing.core.PApplet;

import static java.lang.Math.sin;
import static processing.core.PApplet.map;

//A class to vary between a minimum and maximum using a sinewave. Graphically,
//this always produces nice pleasant smooth motion, and it's a trick I've been
//using for a while, just generally in a quick hacky way. Now I've properly
//implemented it and everything. This class is pretty useful for processing.
public class SineVariable implements GridConstants {
  private static PApplet parent;
  private float th, min, max, speed; //position and parameters of the sine wave

  //initialiser. You'd expect this class not to have to hook into the PApplet,
  //but for access to processing's convenient random() function you have to.
  //This is because random() is not static so can't be imported statically. It
  //isn't static because it uses a seed, which is a sort of environmental factor
  //it needs to get from a concrete instance
  public static void init(PApplet app) {
    parent = app;
  }

  //constructor
  public SineVariable(float min, float max, float speed) {
    this.min = min;
    this.max = max;
    this.speed = speed;
    th = parent.random(TWO_PI);
  }

  //constructor with defaults
  public SineVariable(float min, float max) {
    this(min, max, parent.random(-SINEVARIABLE_SPEED_DEFAULT, SINEVARIABLE_SPEED_DEFAULT));
  }

  //update position
  public void update() {
    th += speed;
  }

  //get value
  public float value() {
    return map((float)sin(th), -1, 1, min, max);
  }
}
