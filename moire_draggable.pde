/*
moire_draggable by Izaak van Dongen
 A program to interactively explore the moire interference effect on grids,
 using a visual UI with the mouse, with further features on the keyboard.
 probably not a good idea for people with epilepsy and the like
 1234567890-=[back][tab] to add grids
 mouse interface for most manipulation. click and drag on the colourful UIElements
  to change grid parameters. use the green button to clone a grid, the red to remove.
 r to reset
 p to toggle auto mode
 aq, sw, de for delta manipulation
 space to toggle delta info box
 enter to toggle ui
 click on a draggable element and then use arrow keys for finer manipulation
 use f to serialise
 use i to serialise with image and postscript file - this hasn't been tested much, use at own risk
*/

//note that a very large portion of the code for this sketch is .java. This is
//because this allows me to circumvent all of Processing's preprocessing, and I
//can use more of Java pure OOP functionality like static fields and methods

//the init object takes the PApplet and provides all the .java files with the
//PApplet. I found this because I read somewhere that this was a bad idea if you
//were writing a library but seeing as this isn't a library I thought it would
//be ok, and it's convenient
Init init = new Init(this);

//setup is run at the start, or when called again. it resets the state of the
//program
void setup() {
  size(1700, 950);
  //call init's setup - everything else resets
  init.init();
}

//draw is called 30-60 times per second. This doesn't actually need to do
//anything as the Init has alread registered the "pre" method, which is called
//before draw()
void draw() {
}

//handle key press - this doesn't need to do much, because the Init has also
//registered keyEvent. all this does is very abtract control flow for the sketch
void keyPressed() {
  switch (keyCode) {
  case 'R': //reset
    setup();
    break;
  }
}

//there is no mousePressed, even though the mouse is used, because the Init has
//registered mouseEvent