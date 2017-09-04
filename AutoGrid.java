import processing.core.PApplet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.io.InputStream;

//an entire screen state, which automatically cycles grid parameters
public class AutoGrid implements GridConstants {
  private GridDrawer grid;
  private static PApplet parent;

  public static void init(PApplet app) {
    parent = app;
  }

  //it uses SineVariables to vary grid parameters. These are objects that vary
  //between values in the form of a sinewave.
  private SineVariable cv, sv, xv, yv, thv, gapv, strokev;
  private int gridtype;

  //note the heavy use of constants - many default parameters etc here can be
  //customised in GridConstants.java
  public AutoGrid(int gridtype) {
    this.gridtype = gridtype;
    grid = GridDrawer.get_grid(gridtype);

    cv = new SineVariable(parent.random(AUTO_GENERIC_COLOUR_MIN_RANGE_MIN, AUTO_GENERIC_COLOUR_MIN_RANGE_MAX), 
      parent.random(AUTO_GENERIC_COLOUR_MAX_RANGE_MIN, AUTO_GENERIC_COLOUR_MAX_RANGE_MAX));
    sv = new SineVariable(parent.random(AUTO_GENERIC_COLOUR_MIN_RANGE_MIN, AUTO_GENERIC_COLOUR_MIN_RANGE_MAX), 
      parent.random(AUTO_GENERIC_COLOUR_MAX_RANGE_MIN, AUTO_GENERIC_COLOUR_MAX_RANGE_MAX));
    thv = new SineVariable(parent.random(AUTO_GENERIC_ROTATION_MAX), -parent.random(AUTO_GENERIC_ROTATION_MAX));
    strokev = new SineVariable(parent.random(AUTO_GENERIC_STROKE - 1, AUTO_GENERIC_STROKE), 
      parent.random(AUTO_GENERIC_STROKE, AUTO_GENERIC_STROKE + 1));

    if (gridtype == TRIANGLERADIAL || gridtype == RADIALGRID) {
      xv = new SineVariable(parent.random(parent.width / 2 - AUTO_RADIAL_MAX_DIST_FROM_ORIGIN, parent.width / 2), 
        parent.random(parent.width / 2, parent.width / 2 + AUTO_RADIAL_MAX_DIST_FROM_ORIGIN));
      yv = new SineVariable(parent.random(parent.height / 2 - AUTO_RADIAL_MAX_DIST_FROM_ORIGIN, parent.height / 2), 
        parent.random(parent.height / 2, parent.height / 2 + AUTO_RADIAL_MAX_DIST_FROM_ORIGIN));
    } else {
      xv = new SineVariable(parent.random(parent.width * (1 - AUTO_GENERIC_MAX_FACTOR_DIST_FROM_ORIGIN_RANGE_MAX), 
        parent.width * (1 - AUTO_GENERIC_MAX_FACTOR_DIST_FROM_ORIGIN_RANGE_MIN)), 
        parent.random(parent.width * AUTO_GENERIC_MAX_FACTOR_DIST_FROM_ORIGIN_RANGE_MIN, 
        parent.width * AUTO_GENERIC_MAX_FACTOR_DIST_FROM_ORIGIN_RANGE_MAX));
      yv = new SineVariable(parent.random(parent.height * (1 - AUTO_GENERIC_MAX_FACTOR_DIST_FROM_ORIGIN_RANGE_MAX), 
        parent.height * (1 - AUTO_GENERIC_MAX_FACTOR_DIST_FROM_ORIGIN_RANGE_MIN)), 
        parent.random(parent.height * AUTO_GENERIC_MAX_FACTOR_DIST_FROM_ORIGIN_RANGE_MIN, 
        parent.height * AUTO_GENERIC_MAX_FACTOR_DIST_FROM_ORIGIN_RANGE_MAX));
    }

    if (gridtype == TRIANGLERADIAL || gridtype == RADIALGRID) {
      gapv = new SineVariable(AUTO_RADIAL_GAP_SIZE_DEFAULT, AUTO_RADIAL_GAP_SIZE_DEFAULT, 0);
    } else if (gridtype == TRIANGLEGRID) {
      gapv = new SineVariable(parent.random(AUTO_TRIANGLE_GAP_SIZE_DEFAULT - 3, AUTO_TRIANGLE_GAP_SIZE_DEFAULT), 
        parent.random(AUTO_TRIANGLE_GAP_SIZE_DEFAULT, AUTO_TRIANGLE_GAP_SIZE_DEFAULT + 3));

      //the other class of grids which use units
    } else if (gridtype >= HEXAGONALGRID || gridtype != LINEGRID) {
      gapv = new SineVariable(parent.random(AUTO_UNIT_GRID_GAP_SIZE_DEFAULT - 3, AUTO_UNIT_GRID_GAP_SIZE_DEFAULT), 
        parent.random(AUTO_UNIT_GRID_GAP_SIZE_DEFAULT, AUTO_UNIT_GRID_GAP_SIZE_DEFAULT + 3));
    } else {
      gapv = new SineVariable(parent.random(AUTO_GENERIC_GAP_SIZE_DEFAULT - 3, AUTO_GENERIC_GAP_SIZE_DEFAULT), 
        parent.random(AUTO_GENERIC_GAP_SIZE_DEFAULT, AUTO_GENERIC_GAP_SIZE_DEFAULT + 3));
    }
  }

  //serialise the grids to a string
  private String serialise_grid() {
    int c = parent.color(cv.value() % 255, 255, 255);
    int s = parent.color(sv.value() % 255, 255, 255);
    float x = xv.value();
    float y = yv.value();
    float th = thv.value();
    float gap = gapv.value();
    float stroke = strokev.value();

    float x2, y2, th2, gap2, stroke2;
    x2 = parent.width / 2;
    y2 = parent.height / 2;
    th2 = 0;
    stroke2 = AUTO_GENERIC_STROKE;

    if (gridtype == TRIANGLERADIAL || gridtype == RADIALGRID) {
      gap2 = AUTO_RADIAL_GAP_SIZE_DEFAULT;
    } else if (gridtype == TRIANGLEGRID) {
      gap2 = AUTO_TRIANGLE_GAP_SIZE_DEFAULT;
    } else if (gridtype >= HEXAGONALGRID || gridtype != LINEGRID) {
      gap2 = AUTO_UNIT_GRID_GAP_SIZE_DEFAULT;
    } else {
      gap2 = AUTO_GENERIC_GAP_SIZE_DEFAULT;
    }

    return String.format(
      "type %s\nr %f\ng %f\nb %f\nx %f\ny %f\nrotation %f\ngap_size %f\nstroke_width %f\nradius %f\n"
      + "\ntype %s\nr %f\ng %f\nb %f\nx %f\ny %f\nrotation %f\ngap_size %f\nstroke_width %f\nradius %f\n", 
      grid.getClass().getSimpleName(), parent.red(s), parent.green(s), parent.blue(s), x, y, th, gap, stroke, GridDrawer.get_radius(x, y), 
      grid.getClass().getSimpleName(), parent.red(c), parent.green(c), parent.blue(c), x2, y2, th2, gap2, stroke2, GridDrawer.get_radius(x2, y2));
  }

  //use serialise_grid() and add in further screen parameters to completely
  //serialise
  private String[] serialise_to_string() {
    String[] ser = new String[] {String.format("screen_width %d\nscreen_height %d\n", parent.width, parent.height), 
      serialise_grid()};
    return ser;
  }

  //use serialise_to_string to write to file. can also save a tiff image and a
  //ps file containing the grids. the filename used is a simple timestamp with
  //a prefix and file extension
  public void serialise_to_file(boolean sav_img) {
    String stamp = new SimpleDateFormat("yyyy_MM_dd_HHmmss").format(Calendar.getInstance().getTime());

    String filename = "data/auto_" + stamp + ".txt";
    //uses processing's builtin saveStrings to save strings. it's very
    //convenient
    parent.saveStrings(filename, serialise_to_string());
    parent.println(String.format("saving as %s", filename));
    if (sav_img) {
      String img_name = "data/aimg_" + stamp + ".tiff";
      //processing function to save what's on the screen to a file
      parent.save(img_name);

      String psname = "data/aimg_" + stamp + ".ps";

      //for the ps file, it has to run the script convert.py. This required
      //pipes to be used, so the command is actually calling sh, with an inline
      //script to execute, which has pipes in. to get absolute paths for each
      //file used, processing's sketchPath is used.
      String[] cmd = {
        "/bin/sh", 
        "-c", 
        String.format("cat %s | /usr/bin/env python3 %s -abs -f %s > %s", 
        parent.sketchPath(filename), parent.sketchPath("serial_conversion/convert.py"), 
        parent.sketchPath("serial_conversion/grid_template.ps"), parent.sketchPath(psname))
      };
      parent.printArray(cmd);
      try {
        Process p = Runtime.getRuntime().exec(cmd);
        //was used in debugging to see what errors were happening
        InputStream s = p.getErrorStream();
         for (int i = 0; i < 400; i++) {
         parent.print(Character.toString((char)p.getErrorStream().read()));
         }
        p.waitFor();
      } 
      catch (Exception ex) {
        parent.println("ERROR HAPPENED IN WRITING PS FILE");
      }
    }
  }

  //defaults sav_img to false. this might be sloppy but it's nice and Pythonic
  public void serialise_to_file() {
    serialise_to_file(false);
  }

  //update all sliders
  public void update() {
    cv.update();
    sv.update();
    xv.update();
    yv.update();
    thv.update();
    gapv.update();
    strokev.update();
  }

  //draw grids
  public void draw() {
    //set colormode to HSB
    parent.colorMode(HSB, 255, 255, 255);
    //get values from SineVariables
    int c = parent.color(cv.value() % 255, 255, 255);
    int s = parent.color(sv.value() % 255, 255, 255);
    float x = xv.value();
    float y = yv.value();
    float th = thv.value();
    float gap = gapv.value();
    float stroke = strokev.value();

    //draw the first grid
    grid.draw(x, y, th, gap, stroke, c);

    //draw the stationary grid, making adjustments based on what type of grid
    //it is
    if (gridtype == TRIANGLERADIAL || gridtype == RADIALGRID) {
      grid.draw(parent.width / 2, parent.height / 2, 0, AUTO_RADIAL_GAP_SIZE_DEFAULT, 5, s);
    } else if (gridtype == TRIANGLEGRID) {
      grid.draw(parent.width / 2, parent.height / 2, 0, AUTO_TRIANGLE_GAP_SIZE_DEFAULT, 5, s);
    } else if (gridtype >= HEXAGONALGRID || gridtype != LINEGRID) {
      grid.draw(parent.width / 2, parent.height / 2, 0, AUTO_UNIT_GRID_GAP_SIZE_DEFAULT, 5, s);
    } else {
      grid.draw(parent.width / 2, parent.height / 2, 0, AUTO_GENERIC_GAP_SIZE_DEFAULT, 5, s);
    }

    //at this point, two opaque grids have been drawn. this means that at
    //intersections the second grid takes precedence. I want the grids to blend
    //at intersections, so at this point I draw the first grid again, but with
    //an alpha value of one half. This doesn't affect non intersecting parts of
    //the first grid, but averages out colour at intersections

    c = parent.color(cv.value() % 255, 255, 255, 255 / 2);
    
    grid.draw(x, y, th, gap, stroke, c);
  }
}