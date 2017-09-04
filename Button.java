import processing.core.PVector;

//a button, which calls a certain method when clicked.
public class Button extends ClickUIElement implements GridConstants {
  private Action a; //action to call when button is pressed
  private PVector loc, ofs; //positioning
  private float th; //positioning and dimensions of the button
  private int c; //button colour
  private static final float r = BUTTON_RADIUS;
  
  //implements this to satisfy the ClickUIElement specification, but a button
  //does not implement any movement.
  public void move(int keyCode) {}

  //set position
  public void set_pos(float x, float y) {
    loc.x = x;
    loc.y = y;
  }

  //set rotation, using some vector functionality
  public void set_rotation(float th) {
    float m = ofs.mag();
    ofs = PVector.fromAngle(th + this.th).mult(m);
  }

  //button constructor, taking an action, a position, an offset from the
  //position(which is needed for rotation) and a colour
  public Button(Action a, float x, float y, float ox, float oy, int c) {
    super(c);
    this.a = a;
    loc = new PVector(x, y);
    ofs = new PVector(ox, oy);
    this.th = ofs.heading();
    this.c = c;
  }

  //simple calculation to determine if a position is in a circle
  boolean hovers(float x, float y) {
    return new PVector(x, y).sub(PVector.add(loc, ofs)).mag() < r;
  }

  //call the action when clicked
  void click() {
    a.action();
  }

  //draw a circle where the button is
  void draw_element() {
    parent.fill(c);
    parent.stroke(c);
    parent.ellipse(loc.x + ofs.x, loc.y + ofs.y, r*2, r*2);
    parent.noFill();
    parent.strokeWeight(3);
  }
}
