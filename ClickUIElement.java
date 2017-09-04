import java.util.ArrayList;
import processing.core.PApplet;
import processing.event.MouseEvent;

//abtract class for a UI element that interacts with a mouse. It statically
//handles mouse clicks.
public abstract class ClickUIElement {
  private boolean removeMe; //implementing a destructor for statically stored objects
  public static boolean do_draw; //the UI elements can be hidden
  protected int c; //colour
  static PApplet parent;
  //tracking mouse click state
  private static boolean any_pressed = false;
  private static boolean is_residue = false;
  private static ClickUIElement clicked_element;
  //static list of elements
  private static ArrayList<ClickUIElement> elements = new ArrayList<ClickUIElement>();

  //abtract method to call when an arrow key is pressed on a selected element
  public abstract void move(int keyCode);

  //statically call move on the selected element
  public static void move_delta(int keyCode) {
    if (is_residue) {
      clicked_element.move(keyCode);
    }
  }

  //initialiser
  public static void init(PApplet app) {
    parent = app;
    elements = new ArrayList<ClickUIElement>();
    do_draw = true;
  }

  //toggle draw
  public static void toggle_draw() {
    do_draw = !do_draw;
  }

  //generic constructor handling static operations, and checking if it has been
  //initialised
  public ClickUIElement(int c) {
    this.c = c;
    if (parent == null) {
      throw new Error("you haven't called init()");
    }
    removeMe = false;
    elements.add(this);
  }

  //destructor method
  public void kill() {
    removeMe = true;
  }

  //abtract method to draw one element. I have always implemented this as a circle, so that no tranformations have to happen
  abstract void draw_element();
  //statically draws all elements
  public static void draw() {
    if (do_draw) {
      parent.pushMatrix();
      parent.resetMatrix();
      parent.noFill();
      parent.strokeWeight(3);

      //here is the only loop over elements that happens, so it is latched on
      //to for the destructor functionality. it loops backwards as it might
      //remove elements.
      for (int index = elements.size() - 1; index >= 0; index--) {
        ClickUIElement e = elements.get(index);
        if (e.removeMe) {
          elements.remove(index);
        } else {
          parent.stroke(e.c);
          e.draw_element();
        }
      }
      parent.popMatrix();
    }
  }

  //click, drag, and release are methods that are called by mouseEvent. They
  //are basically abstract methods but with default behaviour, meaning not all
  //have to be reimplemented if they are not desired
  void click() {
  }
  void drag(float x, float y) {
  }
  void release() {
  }
  //hovers, however, is absolutely required. Principally, I have not performed
  //any graphics state tranformations to any ui elements, because the mouseX
  //and mouseY which need to be examined do not transform accordingly. This
  //makes it generally easier to use some vector calculation for positioning
  //and hovering, making both a little complicated but neither ridiculously
  //complicated - a midly happy compromise
  abstract boolean hovers(float x, float y);

  //handle a mouse event. on click, the first element to match a hover is
  //selected, and on drag or release the selected element has the appropriate
  //method called
  public static void mouseEvent(MouseEvent event) {
    float x = event.getX();
    float y = event.getY();
    switch (event.getAction()) {
    case MouseEvent.PRESS:
      for (ClickUIElement e : elements) {
        if (e.hovers(x, y)) {
          clicked_element = e;
          e.click();
          any_pressed = true;
          is_residue = true;
          break;
        }
      }
      break;
    case MouseEvent.DRAG:
      if (any_pressed) {
        clicked_element.drag(x, y);
      }
      break;
    case MouseEvent.RELEASE:
      if (any_pressed) {
        clicked_element.release();
        any_pressed = false;
      }
      break;
    }
  }
}
