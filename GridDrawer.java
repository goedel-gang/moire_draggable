import static java.lang.Math.sqrt;
import static java.lang.Math.pow;
import static processing.core.PApplet.max;
import static processing.core.PApplet.dist;

import processing.core.PApplet;
import processing.core.PVector;

//an abtract class to draw a grid. instances are supplied statically, and within
//this file all grids are defined as an inner subclass.
public abstract class GridDrawer implements GridConstants {
  protected static PApplet parent;

  //there are a number of public methods, starting to implement functionality,
  //such as setting stroke weight. However, some grids may want access to this
  //behaviour, and this is entirely possible. TriangleRadial, for example, has a
  //custom behaviour for stroke weight, so it overrides the appropriate method

  //public method to draw, handling stroke weight and stroke colour
  public void draw_grid(float x, float y, float r, float th, float gap, float stroke, int c) {
    parent.stroke(c);
    parent.strokeWeight(stroke);
    parent.noFill();
    draw_grid(x, y, r, th, gap);
  }

  //method expected to be implemented - drawing a grid at a location, given
  //rotation, gap size, and one more variable - the radius. I draw my grids as a
  //circle, because they rotate, so I don't have to apply more logic for a
  //rotated grid, and it behaves as quite a nicely isolated unit.
  protected abstract void draw_grid(float x, float y, float r, float th, float gap);

  //public method to draw, defaulting with colour
  public void draw(float x, float y, float th, float gap, float stroke) {
    draw(x, y, th, gap, stroke, parent.color(255));
  }

  //public method to draw, given colour
  public void draw(float x, float y, float th, float gap, float stroke, int c) {
    float r = get_radius(x, y);
    draw_grid(x, y, r, th, gap, stroke, c);
  }

  //get radius required to fill the screen from a point, as talked about
  public static float get_radius(float x, float y) {
    return max(new float[] {dist(x, y, 0, 0), 
      dist(x, y, 0, parent.height), 
      dist(x, y, parent.width, parent.height), 
      dist(x, y, parent.width, 0)});
  }

  //most are pretty self documenting
  //they draw a certain type of grid

  private static class SquareGrid extends GridDrawer {
    public void draw_grid(float x, float y, float r, float th, float gap) {
      parent.pushMatrix();
      parent.translate(x, y);
      parent.rotate(th);
      float rsq = (float) pow(r, 2);

      for (float h = 0; h < r; h += gap) {
        float w = (float) sqrt(rsq - pow(h, 2));
        parent.line(w, h, -w, h);
        parent.line(h, w, h, -w);
        parent.line(w, -h - gap, -w, -h - gap);
        parent.line(-h - gap, w, -h - gap, -w);
      }
      parent.popMatrix();
    }
  }

  private static class ConcentricGrid extends GridDrawer {
    public void draw_grid(float x, float y, float r, float th, float gap) {
      parent.pushMatrix();
      parent.translate(x, y);
      parent.rotate(th);
      parent.noFill();
      for (float cr = 0; cr < r; cr += gap) {
        parent.arc(0, 0, 2 * cr, 2 * cr, 0, TWO_PI);
      }
      parent.popMatrix();
    }
  }

  private static class RadialGrid extends GridDrawer {
    public void draw_grid(float x, float y, float r, float th, float gap) {
      parent.pushMatrix();
      parent.translate(x, y);
      parent.rotate(th);

      float angle_gap = PI / (int)gap;

      for (int i = 0; i < gap * 2; i++) {
        parent.rotate(angle_gap);
        parent.line(0, 0, 0, r);
      }

      parent.popMatrix();
    }
  }

  //abstract for drawing an orthogonal grid with a defined grid unit. This
  //abtractly implement draw_grid, leaving a concrete subclass to only implement
  //draw_unit, the new abstract method
  abstract private static class SingleGrid extends GridDrawer {
    float xstep, ystep;
    abstract void draw_unit(float gap);
    public void draw_grid(float x, float y, float r, float th, float gap) {      
      parent.pushMatrix();
      parent.translate(x, y);
      parent.rotate(th);
      for (float xi = 0; xi < r; xi += xstep) {
        float h = (float) sqrt(pow(r, 2) - pow(xi, 2));
        for (float yi = 0; yi < r; yi += ystep) {
          parent.pushMatrix();
          parent.translate(xi, yi);
          draw_unit(gap);
          parent.popMatrix();

          parent.pushMatrix();
          parent.translate(-xi - xstep, yi);
          draw_unit(gap);
          parent.popMatrix();

          parent.pushMatrix();
          parent.translate(xi, -yi - ystep);
          draw_unit(gap);
          parent.popMatrix();

          parent.pushMatrix();
          parent.translate(-xi - xstep, -yi - ystep);
          draw_unit(gap);
          parent.popMatrix();
        }
      }
      parent.popMatrix();
    }
  }

  //abstract for drawing a "dual grid". it turns out that most polysymmetric
  //patterns can be implemented as an overlay of two orthogonal grids. an
  //example is a hexagonal grid, which is implemented with this private static class.
  abstract private static class DualGrid extends SingleGrid {
    public void draw_grid(float x, float y, float r, float th, float gap) {
      //rotation, and therefore translation as rotation depends on that has to be
      //handled by dual grid as translation needs to occur after rotation
      parent.pushMatrix();
      parent.translate(x, y);
      parent.rotate(th); 
      super.draw_grid(0, 0, r, 0, gap);
      parent.translate(xstep / 2, ystep / 2);
      super.draw_grid(0, 0, r, 0, gap);
      parent.popMatrix();
    }
  }

  private static class HexagonalGrid extends DualGrid {
    public void draw_grid(float x, float y, float r, float th, float gap) {
      xstep = 3 * gap;
      ystep = 2 * TRIANGLE_HEIGHT * gap;

      super.draw_grid(x, y, r, th, gap);
    }

    void draw_unit(float gap) {
      parent.line(-0.5f * gap, TRIANGLE_HEIGHT * gap, 0, 0);
      parent.line(0, 0, gap, 0);
      parent.line(gap, 0, 1.5f * gap, TRIANGLE_HEIGHT * gap);
    }
  }

  private static class TriangleGrid extends GridDrawer {
    public void draw_grid(float x, float y, float r, float th, float gap) {
      parent.pushMatrix();
      parent.translate(x, y);
      parent.rotate(th);
      float rsq = (float) pow(r, 2);

      for (float h = 0; h < r; h += gap) {
        float w = (float) sqrt(rsq - pow(h, 2));
        parent.pushMatrix();
        for (int i = 0; i < 3; i++) {
          parent.rotate(TWO_PI / 3);
          parent.line(w, h, -w, h);
          parent.line(w, -h - gap, -w, -h - gap);
        }
        parent.popMatrix();
      }
      parent.popMatrix();
    }
  }

  private static class StarGrid extends DualGrid {
    public void draw_grid(float x, float y, float r, float th, float gap) {
      xstep = 2 * gap;
      ystep = 4 * TRIANGLE_HEIGHT * gap;

      super.draw_grid(x, y, r, th, gap);
    }

    void draw_unit(float gap) {
      parent.beginShape();
      parent.vertex(0, 0);
      parent.vertex(gap, 0);
      parent.vertex(gap * 1.5f, gap * TRIANGLE_HEIGHT);
      parent.vertex(gap, 2 * gap * TRIANGLE_HEIGHT);
      parent.vertex(0, 2 * gap * TRIANGLE_HEIGHT);
      parent.vertex(-0.5f * gap, gap * TRIANGLE_HEIGHT);
      parent.endShape(CLOSE);
    }
  }

  private static class OctGrid extends SingleGrid {
    public void draw_grid(float x, float y, float r, float th, float gap) {
      xstep = gap * (1 + ROOT_TWO);
      ystep = gap * (1 + ROOT_TWO);

      super.draw_grid(x, y, r, th, gap);
    }

    void draw_unit(float gap) {
      parent.noFill();
      parent.beginShape();
      parent.vertex(0, 0);
      parent.vertex(-ROOT_TWO / 2 * gap, ROOT_TWO / 2 * gap);
      parent.vertex(-ROOT_TWO / 2 * gap, gap * (ROOT_TWO / 2 + 1));
      parent.vertex(0, gap * (ROOT_TWO + 1));
      parent.vertex(gap, gap * (ROOT_TWO + 1));
      parent.vertex(gap * (1 + ROOT_TWO / 2), gap * (1 + ROOT_TWO / 2));
      parent.endShape();
      parent.line(gap, 0, gap * (1 + ROOT_TWO / 2), gap * ROOT_TWO / 2);
    }
  }

  private static class SquareStarGrid extends SingleGrid {
    public void draw_grid(float x, float y, float r, float th, float gap) {
      xstep = gap * 1.5f;
      ystep = gap * 1.5f;

      super.draw_grid(x, y, r, th, gap);
    }

    void draw_unit(float gap) {
      parent.beginShape();
      parent.vertex(0, 0);
      parent.vertex(0, gap);
      parent.vertex(gap, gap);
      parent.vertex(gap, 0);
      parent.endShape(CLOSE);
      parent.line(gap, gap, 0, gap * 1.5f);
      parent.line(gap, 0, gap * 1.5f, gap);
    }
  }

  private static class SquareOffsetGrid extends DualGrid {
    public void draw_grid(float x, float y, float r, float th, float gap) {
      xstep = gap;
      ystep = gap * 2;

      super.draw_grid(x, y, r, th, gap);
    }

    void draw_unit(float gap) {
      parent.line(gap, 0, 0, 0);
      parent.line(0, 0, 0, gap);
    }
  }

  private static class CrossGrid extends SingleGrid {
    public void draw_grid(float x, float y, float r, float th, float gap) {
      xstep = 3 * gap;
      ystep = 4 * gap;

      super.draw_grid(x, y, r, th, gap);
    }

    void draw_unit(float gap) {
      parent.beginShape();
      parent.vertex(gap * 3, 0);
      parent.vertex(gap * 2, 0);
      parent.vertex(gap * 2, -gap);
      parent.vertex(gap, -gap);
      parent.vertex(gap, 0);
      parent.vertex(0, 0);
      parent.vertex(0, 2 * gap);
      parent.vertex(-gap, 2 * gap);
      parent.vertex(-gap, 3 * gap);
      parent.vertex(0, 3 * gap);
      parent.vertex(0, 4 * gap);
      parent.endShape();

      parent.beginShape();
      parent.vertex(0, gap);
      parent.vertex(gap, gap);
      parent.vertex(gap, 2 * gap);
      parent.vertex(2 * gap, 2 * gap);
      parent.vertex(2 * gap, gap);
      parent.vertex(3 * gap, gap);
      parent.endShape();
    }
  }

  private static class CircleGrid extends DualGrid {
    public void draw_grid(float x, float y, float r, float th, float gap) {
      xstep = 2 * gap;
      ystep = 2 * gap;

      super.draw_grid(x, y, r, th, gap);
    }

    void draw_unit(float gap) {
      parent.noFill();
      parent.arc(0, 0, gap * 2, gap * 2, 0, PI);
    }
  }

  private static class CircleStarGrid extends DualGrid {
    public void draw_grid(float x, float y, float r, float th, float gap) {
      xstep = 2 * gap;
      ystep = 4 * TRIANGLE_HEIGHT * gap;

      super.draw_grid(x, y, r, th, gap);
    }

    void draw_unit(float gap) {
      parent.noFill();
      parent.arc(0, 0, gap * 2, gap * 2, 0, TWO_PI);
    }
  }

  private static class LineGrid extends GridDrawer {
    public void draw_grid(float x, float y, float r, float th, float gap) {
      parent.pushMatrix();
      parent.translate(x, y);
      parent.rotate(th);
      float rsq = (float) pow(r, 2);

      for (float h = 0; h < r; h += gap) {
        float w = (float) sqrt(rsq - pow(h, 2));
        parent.line(-h - gap, -w, -h - gap, w);
        parent.line(h, -w, h, w);
      }
      parent.popMatrix();
    }
  }

  private static class TriangleRadial extends GridDrawer {
    public void draw_grid(float x, float y, float r, float th, float gap) {
    }

    public void draw_grid(float x, float y, float r, float th, float gap, float stroke, int c) {
      parent.noStroke();
      parent.fill(c);
      parent.pushMatrix();
      parent.translate(x, y);
      parent.rotate(th);

      float stroke_up = new PVector(stroke, parent.width / 2).setMag(r).x;

      float angle_gap = PI / (int)gap;

      for (int i = 0; i < gap * 2; i++) {
        parent.rotate(angle_gap);
        parent.triangle(0, 0, stroke_up * 2, r, -stroke_up * 2, r);
      }

      parent.popMatrix();
    }
  }

  private static GridDrawer[] gridtypes;
  public static int num;
  public static GridDrawer get_grid(int n) {
    return gridtypes[n];
  }

  public static void init(PApplet app) {
    parent = app;
    gridtypes = new GridDrawer[] {
      new SquareGrid(), 
      new ConcentricGrid(), 
      new TriangleRadial(), 
      new HexagonalGrid(), 
      new TriangleGrid(), 
      new StarGrid(), 
      new OctGrid(), 
      new SquareStarGrid(), 
      new SquareOffsetGrid(), 
      new CrossGrid(), 
      new CircleGrid(), 
      new CircleStarGrid(), 
      new LineGrid(), 
      new RadialGrid()
    };
    num = gridtypes.length;
  }
}
