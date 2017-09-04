/*absolute constants which should not be changed*/

//This file defines an interface defining what would be symbolic constants in C.
//You may have seen classes using what appear to be undeclared variables which
//are written in all caps. These classes access these constants by implementing
//this or an interface that extends it.  It extends PConstants, so that also
//gives those classes access to constants like UP or RETURN

import processing.core.PConstants;
import static java.lang.Math.sqrt;

public interface GridAbsoluteConstants extends PConstants {
  //the value of each grid type
  public final int SQUAREGRID = 0;
  public final int CONCENTRICGRID = 1;
  public final int TRIANGLERADIAL = 2;
  public final int HEXAGONALGRID = 3;
  public final int TRIANGLEGRID = 4;
  public final int STARGRID = 5;
  public final int OCTGRID = 6;
  public final int SQUARESTARGRID = 7;
  public final int SQUAREOFFSETGRID = 8;
  public final int CROSSGRID = 9;
  public final int CIRCLEGRID = 10;
  public final int CIRCLESTARGRID = 11;
  public final int LINEGRID = 12;
  public final int RADIALGRID = 13;

  //constants used in drawing some grids
  public final float TRIANGLE_HEIGHT = (float) sqrt(3) / 2;
  public final float ROOT_TWO = (float) sqrt(2);
}
