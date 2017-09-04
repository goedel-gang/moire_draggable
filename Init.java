import static java.lang.Math.cos;
import static java.lang.Math.sin;

import java.awt.Font;

import processing.core.PApplet;
import processing.core.PFont;
import processing.event.MouseEvent;
import processing.event.KeyEvent;

//the class which handles all intialisation. This is effectively the main pde
//file, but implemented in a java file for all of the nice OOP features

public class Init implements GridConstants {
  private PApplet parent; //parent PApplet
  private PFont f, numfont; //fonts
  public boolean draw_deltas; //whether to draw deltas
  private AutoGrid ag; //for auto mode
  private boolean is_auto; //track if auto mode
  private boolean drawnum; //whether to draw number of grids

  //constructor which does one-off initalisation. This was a fun bug, as if you
  //register methods twice they happen twice
  public Init(PApplet parent) {
    this.parent = parent;
    parent.registerMethod("pre", this);
    parent.registerMethod("mouseEvent", this);
    parent.registerMethod("keyEvent", this);
  }

  //the init method which can be called as setup, setting all initial conditions
  void init() {
    ContainedGrid.init(parent);
    ClickUIElement.init(parent);
    GridDrawer.init(parent);
    AutoGrid.init(parent);
    SineVariable.init(parent);
    draw_deltas = false;
    Draggable.init_delta();
    Extendable.init_delta();
    Rotatable.init_delta();
    f = parent.createFont("courier", 20, true);
    numfont = new PFont(new Font("courier", Font.BOLD | Font.ITALIC, 50), true);

    is_auto = false;
    drawnum = true;
  }

  //this is an interesting function. As far as I can see, in processing, there
  //is no way to draw text with an outline (or stroke). My solution is to draw a
  //black backgroudn version of the text in a small orbit around the final text.
  //It doesn't look all that bad
  private void text_with_outline(int txt, float x, float y, float r, int steps, int primary_c, int secondary_c) {
    parent.textFont(numfont);
    parent.fill(secondary_c);
    for (float i = 0; i < steps; i++) {
      float th = i * TWO_PI / steps;
      float tx = x + r * (float)cos(th);
      float ty = y + r * (float)sin(th);
      parent.text(txt, tx, ty);
    }
    parent.fill(primary_c);
    parent.text(txt, x, y);
  }

  //called before draw
  public void pre() {
    parent.background(0);
    if (is_auto) {
      ag.update();
      ag.draw();
    } else {
      ContainedGrid.draw();
      ClickUIElement.draw();
      print_deltas();
      if (drawnum) {
        text_with_outline(ContainedGrid.get_num_of_grids(), 20, parent.height - 50, 5, 10, parent.color(255), parent.color(0));
      }
    }
  }

  //handle mouse - basically just passes onto ClickUIElement
  public void mouseEvent(MouseEvent event) {
    if (!is_auto) {
      ClickUIElement.mouseEvent(event);
    }
  }

  //routine to print deltas
  private void print_deltas() {
    if (draw_deltas && ClickUIElement.do_draw) {
      String msg = String.format(
        "deltas:\nDraggable: %.1f, changing by %.1f\nExtendable: %.1f, changing by %.1f\nRotatable: %.3f, changing by %.3f", 
        Draggable.get_delta(), Draggable.get_deltadelta(), 
        Extendable.get_delta(), Extendable.get_deltadelta(), 
        Rotatable.get_delta(), Rotatable.get_deltadelta());
      parent.textFont(f);
      parent.fill(0);
      parent.stroke(0);
      parent.rect(0, 0, 450, 130);
      parent.fill(255);
      parent.stroke(255);
      parent.text(msg, 10, 30);
    }
  }

  //handle a key press
  public void keyEvent(KeyEvent event) {
    int keyCode = event.getKeyCode();
    int act = event.getAction();
    if (act == KeyEvent.PRESS) {
      //actions exclusive to normal mode
      if (!is_auto) {
        switch(keyCode) {
        case 'A':
          Draggable.decrease_delta();
          break;
        case 'Q':
          Draggable.increase_delta();
          break;
        case 'S':
          Extendable.decrease_delta();
          break;
        case 'W':
          Extendable.increase_delta();
          break;
        case 'D':
          Rotatable.decrease_delta();
          break;
        case 'E':
          Rotatable.increase_delta();
          break;

        case ENTER:
        case RETURN:
          ClickUIElement.toggle_draw();
          drawnum = !drawnum;

          break;
        case ' ':
          draw_deltas = !draw_deltas;
          break;
        case UP:
        case LEFT:
        case RIGHT:
        case DOWN:
          ClickUIElement.move_delta(keyCode);
          break;
        }
      }

      //actions desired in both modes, with some conditional execution for
      //slight alteration

      //slightly hacky approach - this transforms the keys 123..9 to the values 012..8, which is desired behaviour
      int gridval = keyCode - '1';

      //nonlinear char types where the corresponding grid value should be
      //overridden
      switch(keyCode) {
      case '0': 
        gridval = CROSSGRID; 
        break;
      case '-':
        gridval = CIRCLEGRID;
        break;
      case '=':
        gridval = CIRCLESTARGRID;
        break;
      case BACKSPACE:
        gridval = LINEGRID;
        break;
      case TAB:
        gridval = RADIALGRID;
        break;

      case 'F':
        if (is_auto) {
          ag.serialise_to_file();
        } else {
          ContainedGrid.serialise_to_file();
        }
        break;
      case 'I':
        if (is_auto) {
          ag.serialise_to_file(true);
        } else {
          ContainedGrid.serialise_to_file(true);
        }        
        break;

      case 'P':
        if (is_auto) {
          is_auto = false;
          ag = null;
          init();
        } else {
          is_auto = true;
          drawnum = false;
          ag = new AutoGrid(SQUAREGRID);
        }
        break;
      }

      if (gridval >= 0 && gridval < GridDrawer.num) {
        if (is_auto) {
          ag = new AutoGrid(gridval);
        } else {
          new ContainedGrid(gridval);
        }
      }
    }
  }
}
