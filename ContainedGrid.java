import java.util.ArrayList;
import processing.core.PApplet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.lang.Runtime;
import java.io.InputStream;

//a "contained" class for a grid. this means it handled using ClickUIElements
//to set grid parameters.
public class ContainedGrid implements GridConstants {
  private static PApplet parent;
  private GridDrawer grid; //grid to draw
  //ui elements
  private Extendable gap_size, stroke_width, hue, sat, bri, alp;
  private Draggable position;
  private Rotatable rotation;
  private Button make_copy;
  private Button delete;

  //static list of containedgrids
  private static ArrayList<ContainedGrid> grids = new ArrayList<ContainedGrid>();

  //get number of active grids (number at bottom left)
  public static int get_num_of_grids() {
    return grids.size();
  }

  //initialise
  public static void init(PApplet app) {
    parent = app;
    grids = new ArrayList<ContainedGrid>();
  }

  //initialiser, initialising required ui elements
  public ContainedGrid(int n) {
    parent.colorMode(RGB, 255, 255, 255);
    this.grid = GridDrawer.get_grid(n);
    gap_size = new Extendable(parent.width / 2, parent.height / 2, 0, GAP_SIZE_MIN, GAP_SIZE_MAX, parent.color(0, 0, 255));
    stroke_width = new Extendable(parent.width / 2, parent.height / 2, HALF_PI, STROKE_WIDTH_MIN, STROKE_WIDTH_MAX, parent.color(255, 255, 0));
    hue = new Extendable(parent.width / 2, parent.height / 2, QUARTER_PI / 2, 0, 255, parent.color(255, 100, 100));
    sat = new Extendable(parent.width / 2, parent.height / 2, QUARTER_PI, 100, 355, parent.color(100, 255, 100));
    sat.set_value(355);
    bri = new Extendable(parent.width / 2, parent.height / 2, 3 * QUARTER_PI / 2, 0, 255, parent.color(100, 100, 255));
    bri.set_value(255);
    alp = new Extendable(parent.width / 2, parent.height / 2, 3 * QUARTER_PI, 0, 255, parent.color(255, 255, 100));
    alp.set_value(255);
    position = new Draggable(parent.width / 2, parent.height / 2, parent.color(255, 0, 255));
    rotation = new Rotatable(parent.width / 2, parent.height / 2, PI, parent.color(0, 255, 255));
    make_copy = new Button(new MakeCopy(), position.get_x(), position.get_y(), 25, -40, parent.color(0, 255, 0));
    delete = new Button(new Kill(), position.get_x(), position.get_y(), -25, -40, parent.color(255, 0, 0));
    grids.add(this);
  }

  //an inner class which can make a copy to pass to the copy Button
  private class MakeCopy implements Action {
    public void action() {
      copy();
    }
  }

  //a private constructor used in copying
  private ContainedGrid(GridDrawer grid, Extendable gap_size, Extendable stroke_width, Extendable hue, Extendable sat, Extendable bri, Extendable alp, Draggable position, Rotatable rotation) {
    parent.colorMode(RGB, 255, 255, 255);
    this.grid = grid;
    this.gap_size = gap_size;
    this.stroke_width = stroke_width;
    this.hue = hue;
    this.sat = sat;
    this.bri = bri;
    this.alp = alp;
    this.position = position;
    this.rotation = rotation;
    make_copy = new Button(new MakeCopy(), position.get_x(), position.get_y(), 25, -40, parent.color(0, 255, 0));
    delete = new Button(new Kill(), position.get_x(), position.get_y(), -25, -40, parent.color(255, 0, 0));
    grids.add(this);
  }

  //copy
  ContainedGrid copy() {
    return new ContainedGrid(grid, gap_size.copy(), stroke_width.copy(), hue.copy(), sat.copy(), bri.copy(), alp.copy(), position.copy(), rotation.copy());
  }

  //inner class which can destroy the instance, for the kill Button
  private class Kill implements Action {
    public void action() {
      gap_size.kill();
      stroke_width.kill();
      hue.kill();
      sat.kill();
      bri.kill();
      alp.kill();
      position.kill();
      rotation.kill();
      make_copy.kill();
      delete.kill();
      for (int i = 0; i < grids.size(); i++) {
        if (grids.get(i) == ContainedGrid.this) {
          grids.remove(i);
          break;
        }
      }
    }
  }

  //serialisation functionality - this can be used with convert.py to convert
  //back into postscript. experimentally this can be done immediately with the i
  //button, but this relinquished some control over convert.py's behaviour, such
  //as that it forces -s, for compression

  //serialise the grid instance
  private String serialise_grid() {
    parent.colorMode(HSB, 255, 255, 255);
    float x = position.get_x();
    float y = position.get_y();
    float th = rotation.get_rotation();
    float gap = gap_size.get_value();
    float stroke = stroke_width.get_value();
    float h = hue.get_value();
    float s = 355 - sat.get_value();
    float b = bri.get_value();
    int c = parent.color(h, s, b);
    return String.format(
      "type %s\nr %f\ng %f\nb %f\nx %f\ny %f\nrotation %f\ngap_size %f\nstroke_width %f\nradius %f\n", 
      grid.getClass().getSimpleName(), parent.red(c), parent.green(c), parent.blue(c), x, y, th, gap, stroke, GridDrawer.get_radius(x, y));
  }

  //statically serialise all grid instances
  private static String[] serialise_to_string() {
    String[] ser = new String[grids.size() + 1];
    ser[0] = String.format("screen_width %d\nscreen_height %d\n", parent.width, parent.height);
    int i = 1;
    for (ContainedGrid g : grids) {
      ser[i] = g.serialise_grid();
      i++;
    }
    return ser;
  }

  //serialise grids to a file, optionally with a .tiff and .ps version also
  //stored. uses a timestamp to generate a unique filename
  public static void serialise_to_file(boolean sav_img) {
    String stamp = new SimpleDateFormat("yyyy_MM_dd_HHmmss").format(Calendar.getInstance().getTime());

    String filename = "data/moire_" + stamp + ".txt";
    parent.saveStrings(filename, serialise_to_string());
    parent.println(String.format("saving as %s", filename));
    if (sav_img) {
      String img_name = "data/img_" + stamp + ".tiff";
      parent.save(img_name);
      String psname = "data/img_" + stamp + ".ps";

      String[] cmd = {
        "/bin/sh", 
        "-c", 
        String.format("cat %s | /usr/bin/env python3 %s -bs -f %s > %s", 
        parent.sketchPath(filename), parent.sketchPath("serial_conversion/convert.py"), 
        parent.sketchPath("serial_conversion/grid_template.ps"), parent.sketchPath(psname))
      };
      parent.printArray(cmd);
      try {
        Process p = Runtime.getRuntime().exec(cmd);
        //used in debugging to examine errors
        InputStream s = p.getErrorStream();
        for (int i = 0; i < 400; i++) {
          parent.print(Character.toString((char)p.getErrorStream().read()));
        }
        p.waitFor();
      } 
      catch (Exception ex) {
      }
    }
  }

  //again allows defaulting of a parameter to false
  public static void serialise_to_file() {
    serialise_to_file(false);
  }

  //actually draw the grid, getting and parameters
  private void draw_grid() {
    //first gets position, and updates all other elements to that position
    float x = position.get_x();
    float y = position.get_y();
    gap_size.set_pos(x, y);
    stroke_width.set_pos(x, y);
    hue.set_pos(x, y);
    sat.set_pos(x, y);
    bri.set_pos(x, y);
    alp.set_pos(x, y);
    rotation.set_pos(x, y);
    make_copy.set_pos(x, y);
    delete.set_pos(x, y);
    //then gets rotation, and update elements to rotation
    float th = rotation.get_rotation();
    stroke_width.set_rotation(th);
    gap_size.set_rotation(th);
    hue.set_rotation(th);
    sat.set_rotation(th);
    bri.set_rotation(th);
    alp.set_rotation(th);
    make_copy.set_rotation(th);
    delete.set_rotation(th);
    //then gets the extendable values which have no influence on positioning
    float gap = gap_size.get_value();
    float stroke = stroke_width.get_value();
    float h = hue.get_value();
    float s = 355 - sat.get_value();
    float b = bri.get_value();
    float a = alp.get_value();

    parent.colorMode(HSB, 255, 255, 255);
    int c = parent.color(h, s, b, a);

    grid.draw(x, y, th, gap, stroke, c);
  }

  //statically draw all grids
  public static void draw() {
    for (ContainedGrid g : grids) {
      parent.stroke(255);
      parent.noFill();
      g.draw_grid();
    }
  }
}