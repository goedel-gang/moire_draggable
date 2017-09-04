#!/usr/bin/env python3

"""
A command line script to convert a serialised grid state from the processing
sketch moire_draggable to a postscript file. This means it can be used as a
vector graphic, and can be stored quite compactly.
"""

import sys
import argparse
import re

from math import degrees, sqrt
from compress_ps import compress_ps

def get_args():
    """
    parse command line arguments - most document themselves already
    """

    parser = argparse.ArgumentParser(("a command line script to convert serialised grid "
              "output from moire_draggable to a postscript file. the serialised "
              "grid input should be given in stdin"))
    parser.add_argument("-b", "--background", action="store_true",
            help=("set the background colour to black, as in the processing "
                  "sketch. by default there is no background. If this argument "
                  "is set any entirely black grids will also be flipped to "
                  "white"))
    parser.add_argument("-c", "--convert", action="store", default="96", type=(lambda x: 72 / float(x)),
            help=("make the program convert pixel lengths to dot lengths. it "
                  "expects the screen resolution in pixels per inch, and assumes "
                  "serialised measurement are in pixels. it defaults to 96 "
                  "pixels per inch as that's my screen resolution"))
    parser.add_argument("-a", "--alpha", action="store_true",
            help=("make the program emulate an alpha channel, only works for "
                  "two grids (Postscript does not natively support an alpha "
                  "channel but one can be simulated with judicious use of clip)"))
    parser.add_argument("-p", "--paper", action="store_true",
            help=("set the program to use a4 paper size, filling page. this is "
                  "also still pretty experimental and I'm not sure how well it will "
                  "work"))
    parser.add_argument("-s", "--small", action="store_true",
            help=("output a small PS file. This applies my postscript compression "
                  "from compress_ps.py. It doesn't properly formally parse the "
                  "postscript, so might be a bit shaky although it seems to "
                  "work."))
    parser.add_argument("-f", "--file", action="store", type=str, default="grid_template.ps",
            help=("the file to read grid template from. it defaults to "
                  "'grid_template.ps' in its directory. This parameter can be "
                  "useful if calling the script from outside its directory, you "
                  "can supply a path to the same template"))
    
    return parser.parse_args()

def dist(x1, y1, x2, y2):
    """
    get distance between two points (like the processing function)
    """
    return sqrt((x2 - x1) ** 2 + (y2 - y1) ** 2)

def get_radius(width, height, x, y):
    """
    get the required radius for a grid drawn at a point. read more about the
    radius approach in GridDrawer.java in the sketch. this is used if the paper
    size changed so the radii need to be recalculated.
    """
    return max([dist(0, 0, x, y),
                dist(width, 0, x, y),
                dist(0, height, x, y),
                dist(width, height, x, y)])

def apply_to_str(func):
    """
    a helper function which returns a function applying a given function to a
    string
    """
    return lambda s: str(func(float(s)))

def identity(x):
    """
    an identity function in case an argument does not need to be transformed
    """
    return x

#the set of recognised grids. This is hardcoded for simplicity, and can fail
#gracefully if an unanticipated type is met, rather than carrying this through
#to a Postscript error
RECOGNISED_GRIDS = {"SquareGrid",
                    "ConcentricGrid",
                    "TriangleRadial",
                    "HexagonalGrid",
                    "TriangleGrid",
                    "LineGrid",
                    "StarGrid"}

def check_type(grid):
    """
    a short function to check if a grid is recognised, failing (relatively)
    gracefully otherwise
    """

    if grid in RECOGNISED_GRIDS:
        return grid
    else:
        #crudely overpower the postscript error message
        sys.stdout.write("GRIDNOTSUPPORTED_" * 10)
        sys.exit("unrecognised type {}. currently only {} are implemented".format(grid, list(RECOGNISED_GRIDS)))

class LengthConverter:
    """
    A class to convert lengths. This is implemented as a class because this
    means its methods have a state, which can be changed after passing the
    methods to some other function. In this case, the ratio can be set when it
    becomes apparent
    """

    def __init__(self):
        self.ratio = 1

    def convert(self, val):
        return val * self.ratio

    def deconvert(self, val):
        return val / self.ratio

#the LengthConverter instance to be used
lconv = LengthConverter()

class ColourConverter:
    """
    Similarly to LengthConverter, allows the state to be changed if necessary,
    and converts colour values. Note that postscript colour values must be
    between 0 and 1
    """

    def __init__(self):
        self.flip_black = False

    def convert(self, colour):
        return apply_to_str(lambda x: x / 255)(colour)

def flip_black(grid_params):
    if sum(float(grid_params[i]) for i in "rgb") / 3 > 0.95:
        for i in "rgb":
            grid_params[i] = apply_to_str(lambda x: 1 - x)(grid_params[i])

#the ColourConverter instance to be used
cconv = ColourConverter()

#this stored both recognised fields and how to process them in a dictionary.
#This allows both checking if a field exists and processing it efficiently
FIELD_ACTIONS = {"r": cconv.convert,
                 "g": cconv.convert,
                 "b": cconv.convert,
                 "x": apply_to_str(lconv.convert),
                 "y": apply_to_str(lconv.convert),
                 "rotation": apply_to_str(degrees),
                 "gap_size": apply_to_str(lconv.convert),
                 "stroke_width": apply_to_str(lconv.convert),
                 "radius": apply_to_str(lconv.convert),
                 "type": check_type}

#the template for drawing a grid. each grid draws a path and the draw_grid
#function takes a grid and use its path to draw a filled grid.
grid_call_template = """\
%draw a {type}
{{ {type} }} {x} {y} {rotation} {gap_size} {stroke_width} {radius} {r} {g} {b} draw_grid
"""

#the template for drawing two grids emulating an alpha channel.
alpha_call_template = ("""\
%draw two {type} grids emulating an alpha channel
{{ {type} }} {x} {y} {rotation} {gap_size} {stroke_width} {radius} {r} {g} {b}""",
"""{x} {y} {rotation} {gap_size} {stroke_width} {radius} {r} {g} {b} draw_alpha""")

#the template for drawing a black background
background_template = """
%draw a black background
0 setgray
newpath
0 0 moveto
{screen_width} 0 lineto
{screen_width} {screen_height} lineto
0 {screen_height} lineto
closepath fill
"""

def get_template(name):
    """
    read the postscript template from the file grid_template.ps. Note that it
    assumes that grid_template.ps is the in the current working directory.
    this function also strips out all lines beginning with a # character,
    acting as a sort of little preprocessor
    """
    try:
        with open(name) as template:
            return "".join(line for line in template if not line.startswith("#"))
    except FileNotFoundError:
        sys.exit("the template file could not be found. make sure you are "
            "running the script from the directory where the template file "
            "'{}' is saved".format(name))

def get_serialised_grids():
    """
    finds stdin and handled no stdin being supplied
    """
    if sys.stdin.isatty():
        sys.exit("this program expects stdin")
    else:
        return sys.stdin

def parse_line(grid_line, fields=FIELD_ACTIONS):
    """
    parse and process one line of a grid, which is a name and a value separated
    by a space. it is given the field actions to take - ie which fields exist
    and how to process them. handles failure gracefully
    """

    try:
        name, val = grid_line.split()
    except ValueError:
        sys.exit("the line '{}' could not be split into a field and value".format(grid_line)) 
    if name not in fields: 
        sys.exit("the field '{}' is unrecognised, expecting one of {}".format(name, list(fields.keys())))
    else:
        return name, fields[name](val) #the actions to take for the special screen parameters
SIZE_FIELDS = {"screen_width": apply_to_str(lconv.convert),
               "screen_height": apply_to_str(lconv.convert)}

def read_size(grid_lines):
    """
    read the screen parameters from the first two lines
    """
    screen_params = dict(parse_line(line.strip(), fields=SIZE_FIELDS) for line in (grid_lines.readline(), grid_lines.readline()))
    grid_lines.readline()
    return screen_params 

def parse_grid(grid_lines):
    """
    parses a grid given the lines of the grid
    """
    grid = dict(parse_line(line) for line in grid_lines)
    if "Radial" in grid["type"]:
        grid["gap_size"] = apply_to_str(lconv.deconvert)(grid["gap_size"])

    return grid

def parse_grids(grid_lines):
    """
    parses all grids, handling the end of file
    """
    grids = []
    for grid in grid_lines:
        try:
            grids.append(parse_grid(grid))
        except EndOfGrids:
            break

    return grids

#a control flow exception to indicate the end of the grid file has been reached
class EndOfGrids(Exception):
    pass

def read_grid(grids):
    """
    reads the actual lines of a grid
    """
    line = grids.readline()
    while line != "\n":
        if line == "":
            raise EndOfGrids
        yield line.strip()
        line = grids.readline()

def separate_grids(grids):
    """
    separate out grids by the blank line separators
    """
    while True:
        yield read_grid(grids)

def form_postscript(template, **screen_params):
    """
    forms the postscript output given the template and parameters to the template.
    """
    try:
        return template.format(**screen_params)
    except KeyError as e:
        sys.exit("the screen paremeters were missing the parameter {}".format(e.args[0]))

def form_alpha_grid(template, grid_params):
    """
    form a call to draw_alpha using the template and parameters. the alpha
    template is effectively broken down into two smaller grids, so it can use
    form_grid's functionality for this.
    """
    if len(grid_params) != 2:
        sys.exit("exactly two grids are required for alpha emulation")
    else:
        return "\n".join(form_grid(subtemplate, params) for subtemplate, params in zip(template, grid_params))

def form_grid(template, grid_params):
    """
    form a call to draw_grid using the template and parameters
    """
    try:
        return template.format(**grid_params)
    except KeyError as e:
        sys.exit("the grid {} was missing the parameter {}".format(grid_params, e.args[0]))

def form_background(template, screen_params):
    """
    form code to draw a black background given template and size. this could
    almost have just been replaced by form_grid but the custom error message
    for this function is better.
    """
    try:
        return template.format(**screen_params)
    except KeyError as e:
        sys.exit("the screen paremeters were missing the parameter {}".format(e.args[0]))

def fix_radii(grids, width, height):
    """
    recalculates radius for each grid in case of paper size change
    """
    for grid in grids:
        grid["radius"] = get_radius(*(float(i) for i in (width, height, grid["x"], grid["y"])))

def main():
    args = get_args()
    lconv.ratio = args.convert
    if args.background:
        cconv.flip_black = True
    ps_template = get_template(args.file)
    grid_file = get_serialised_grids()
    screen_params = read_size(grid_file)
    if args.paper:
        screen_params["screen_height"] = apply_to_str(lconv.deconvert)(screen_params["screen_height"])
        screen_params["screen_width"] = apply_to_str(lconv.deconvert)(screen_params["screen_width"])
        lconv.ratio = max([595 / float(screen_params["screen_height"]),
                           842 / float(screen_params["screen_width"])])
        screen_params["screen_height"] = "595"
        screen_params["screen_width"] = "842"
    grids = separate_grids(grid_file)
    grid_params = parse_grids(grids)
    background = form_background(background_template, screen_params) if args.background else ""

    if not args.background:
        for i in grid_params:
            flip_black(i)

    if args.paper:
        fix_radii(grid_params, screen_params["screen_width"], screen_params["screen_height"])

    if not args.alpha:
        ps_grids = "".join(form_grid(grid_call_template, params) for params in grid_params)
    else:
        ps_grids = form_alpha_grid(alpha_call_template, grid_params)

    ps_output = form_postscript(ps_template, grids=ps_grids, background=background, **screen_params)
    if args.small:
        ps_output = compress_ps(ps_output)
    sys.stdout.write(ps_output)

if __name__ == "__main__":
    main()
