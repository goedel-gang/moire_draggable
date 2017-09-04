/*further functional constants which can be changed by the user, although they aren't bad as they are.
 they are set so that it can run somewhat on my laptop. if running on a more powerful machine
 it makes sense to increase or decrease some of these as appropriate
 the default values can be found at https://github.com/elterminad0r/processing/blob/master/moire_draggable/GridConstants.java
*/

//extends GridAbsoluteConstants, so classes implementing this interface can also
//access the absolute constants

import processing.core.PConstants;

public interface GridConstants extends GridAbsoluteConstants {
  //the range of an Extendable
  public final float EXTENDABLE_RANGE = 300;
  //the radius of an Extendable
  public final float EXTENDABLE_RADIUS = 15;
  //the default delta for an Extendable
  public final float EXTENDABLE_DELTA_DEFAULT = 1;
  //the delta delta for an Extendable
  public final float EXTENDABLE_DELTADELTA = 0.1f;

  //the radius of a Draggable
  public final float DRAGGABLE_RADIUS = 10;
  //the default delta for a Draggable
  public final float DRAGGABLE_DELTA_DEFAULT = 1;
  //the delta delta for a Draggable
  public final float DRAGGABLE_DELTADELTA = 0.1f;

  //the length of a Rotatable
  public final float ROTATABLE_LENGTH = 100;
  //the radius of a Rotatable
  public final float ROTATABLE_RADIUS = 15;
  //the default delta for a Rotatable
  public final float ROTATABLE_DELTA_DEFAULT = TWO_PI / 360;
  //the delta delta for a Rotatable
  public final float ROTATABLE_DELTADELTA = TWO_PI / 3600;

  //the radius for a Button
  public final float BUTTON_RADIUS = 15;

  //the minimum gap size in interactive mode
  public final float GAP_SIZE_MIN = 15;
  //the maximum gap size in interactive mode
  public final float GAP_SIZE_MAX = 50;

  //the minimum stroke width in interactive mode
  public final float STROKE_WIDTH_MIN = 0.5f;
  //the maximum stroke width in interactive mode
  public final float STROKE_WIDTH_MAX = 10;

  //the default speed of a SineVariable
  public final float SINEVARIABLE_SPEED_DEFAULT = 0.01f;

  //the minimum for the lower colour value in auto mode
  public final float AUTO_GENERIC_COLOUR_MIN_RANGE_MIN = 0;
  //the maximum for the lower colour value in auto mode
  public final float AUTO_GENERIC_COLOUR_MIN_RANGE_MAX = 255;
  //the minimum for the higher colour value in auto mode
  public final float AUTO_GENERIC_COLOUR_MAX_RANGE_MIN = 255;
  //the maximum for the higher colour value in auto mode
  public final float AUTO_GENERIC_COLOUR_MAX_RANGE_MAX = 255 * 2;
  //the maximum rotation of a grid in auto mode (+-)
  public final float AUTO_GENERIC_ROTATION_MAX = TWO_PI / 50;
  //the stroke of a generic grid in auto mode (+- 1)
  public final float AUTO_GENERIC_STROKE = 5;
  //the maximum distance for a radial grid from the origin in auto mode
  public final float AUTO_RADIAL_MAX_DIST_FROM_ORIGIN = 200;
  //the minimum value for the factor of the screen that a grid can offset in auto mode
  public final float AUTO_GENERIC_MAX_FACTOR_DIST_FROM_ORIGIN_RANGE_MIN = 0.5f;
  //the maximum value for the factor of the screen that a grid can offset in auto mode
  public final float AUTO_GENERIC_MAX_FACTOR_DIST_FROM_ORIGIN_RANGE_MAX = 1;
  //the default gap_size for a radial grid in auto mode
  public final float AUTO_RADIAL_GAP_SIZE_DEFAULT = 38;
  //the default gap_size for a triangle grid in auto mode
  public final float AUTO_TRIANGLE_GAP_SIZE_DEFAULT = 42;
  //the default gap_size for a grid defined by units (eg honeycomb) in auto mode
  public final float AUTO_UNIT_GRID_GAP_SIZE_DEFAULT = 30;
  //the default gap_size for any other generic grid (eg, square, concentric) in auto mode
  public final float AUTO_GENERIC_GAP_SIZE_DEFAULT = 17;
}