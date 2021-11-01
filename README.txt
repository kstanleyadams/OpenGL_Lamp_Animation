This java application renders an animated scene of a lamp using OpenGL. The user interface lets you do the following:

1. Change camera angle to follow the lamp.
2. Disable/Enable Textures
3. Disable/Enable all objects
4. Turn lights on and off. Light 0 is the room light, and light 1 is the lamp light.
5. Change the size of the room the lamp is in.
6. Pause/Start Animation
7. Animate a single frame
8. Reset the scene

For the purpose of assessment, I choose the camera mode as my advanced feature.

To compile the program:

Double click run.bat on windows, or run.command on UNIX / Mac OSX systems

or

CD to the directory, then run the following commands:

javac Assignment1.java
java Assignment1

A video demonstration of this program can be downloaded here: http://j.mp/LampAnimation

You need to have JOGL (the java binding for OpenGL) installed:

jogamp.org

Kevin Stanley-Adams 2013