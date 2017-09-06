# moire\_draggable
A program to interactively explore the moire interference effect on grids, using a (self implemented) visual UI with the mouse, with further features on the keyboard. Also features some serialisation (to preprocessed handwritten postscript) and an experimental Postscript compresser which works by mangling.  Experimenting with some more OOP, and pure Java integration with Processing.  probably not a good idea for people with epilepsy and the like `1234567890-=[back][tab]` to add grids mouse interface for most manipulation.  click and drag on the colourful UIElements to change grid parameters. use the green button to clone a grid, the red to remove.

    r to reset
    p to toggle auto mode
    aq, sw, de for delta manipulation
    space to toggle delta info box
    enter to toggle ui
    click on a draggable element and then use arrow keys for finer manipulation
    use f to serialise
    use i to serialise with image and postscript file - this hasn't been tested much, use at own risk

note that a very large portion of the code for this sketch is .java. This is because this allows me to circumvent all of Processing's preprocessing, and I can use more of Java pure OOP functionality like static fields and methods

Here are some screenshots of what you might begin to do with it, with the "UI" preserved:

![screenshot](https://github.com/elterminad0r/moire_draggable/tree/master/screenshots/hex.ps)
![screenshot](https://github.com/elterminad0r/moire_draggable/tree/master/screenshots/radial.ps)

Both of these were narrowly obtained before my laptop melted
