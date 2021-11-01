
/**
 * The scene class arranges and animates the scene. Author: Steve Maddock Last
 * updated: 12 November 2013 I declare that this code is my own work Modified by
 * Kevin Stanley-Adams coa10ks@sheffield.ac.uk. Modified methods are labelled. A
 * lamp is rendered in a room with a textured environment, obstacles. The lamp
 * then jumps around the room, and jumps over the obstacles.
 *
 * @author Kevin Stanley-Adams 2013
 */
import java.io.File;
import java.awt.image.*;
import javax.imageio.*;
import com.jogamp.opengl.util.awt.*;

import javax.media.opengl.*;
import com.jogamp.opengl.util.texture.*;
import com.jogamp.opengl.util.texture.awt.*;

import javax.media.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;
import java.io.IOException;

import static java.lang.Math.*;

public class Scene {

    //Creates OpenGL contexts
    private final GLU glu = new GLU();
    private final GLUT glut = new GLUT();

    //Defines initial paramaters that define the appearance of objects in the scene
    private float globalSize = 1;
    private final double initialLowerRot = -30;
    private final double initialUpperRot = 70;

    //Defines the number of jumps the lamp completes before making a full revolution of the room
    private final int jumpSegments = 20;
    private final double jumpAngle = 360 / jumpSegments;

    //Defining variables for animation and appearance
    private int jumpStage, segmentCount, fireplaceAnimationStage;
    private double deltaY, rotate, jumpTimer, coneVertRot, coneHorRot, baseRot, upperRot, lowerRot, roomWidth, roomLength, roomHeight;

    //Control booleans that allow for switching settings on and off
    private boolean objectsOn = true;
    private boolean isTexturesOn = true;
    private boolean lampCamera = false;

    //Defines initial size of window on screen
    private int canvaswidth = 0, canvasheight = 0;

    //New instance of Light, Camera, Mesh, Lamp, Axes and Render classes
    private Light light;
    private Camera camera;
    private Mesh meshPlane, meshCube;   // Define mesh instances for the scene. 
    private Lamp lamp1;
    //Creates render objects for each textured object, and also generic non textured obkects
    private Render planeNT, cubeNT, wallPlane, wallPlaneYT, floorPlane, floorPlaneYT, ceilingPlane, ceilingPlaneYT, doorPlane, doorPlaneYT;
    private Render obstacleCube, obstacleCubeYT, fire1Plane, fire1PlaneYT, fire2Plane, fire2PlaneYT, fire3Plane, fire3PlaneYT, fire4Plane, fire4PlaneYT;   // Define matching render objects for the scene meshes
    private Axes axes;

    // Use JOGL Texture class to deal with textures
    private Texture wallTexture, floorTexture, ceilingTexture, obstacleTexture, doorTexture, fire1Texture, fire2Texture, fire3Texture, fire4Texture;

    /**
     * Constructor.
     *
     * @param gl OpenGL context
     * @param camera Instance of the camera class, which uses the idea of moving
     * around a virtual sphere, centred on the origin, under mouse control.
     */
    public Scene(GL2 gl, Camera camera) {
        reset(); //Initialises all variables and places lamp at starting position
        this.camera = camera;
        axes = new Axes(2.2, 1.8, 1.6);
        lamp1 = new Lamp(gl, glut); //Creates lamp object
        createLight(gl); //Creates general ambient light for room
        createRenderObjects(gl);  // Create/load objects

    }

    /**
     * Creates rooms light.
     *
     * Specifies room light parameters and positions in the room
     *
     * @param gl OpenGL context
     *
     *
     */
    private void createLight(GL2 gl) {
        float[] position = {0, 10f * globalSize, 0, 1};
        float[] ambient = {0.6f, 0.6f, 0.6f};
        float[] diffuse = {0.3f, 0.3f, 0.3f};
        float[] specular = {0.3f, 0.3f, 0.3f};
        light = new Light(GL2.GL_LIGHT0, position, ambient, diffuse, specular, true); // Create a default light
    }

    /**
     * Creates Mesh/Render Objects.
     *
     * Builds textured Mesh and Render objects to be used in the room Created by
     * Kevin Stanley-Adams 2013
     *
     * @param gl OpenGL context
     *
     */
    private void createRenderObjects(GL2 gl) {

        // Some of the objects will have textures applied, so load the relevant textures
        floorTexture = loadTexture(gl, "resources/floor.jpg");
        ceilingTexture = loadTexture(gl, "resources/ceiling.jpg");
        wallTexture = loadTexture(gl, "resources/wall.jpg");
        obstacleTexture = loadTexture(gl, "resources/obstacle.jpg");
        doorTexture = loadTexture(gl, "resources/door.jpg");
        fire1Texture = loadTexture(gl, "resources/fire_1.png");//Fireplace has 4 different textures to give animation
        fire2Texture = loadTexture(gl, "resources/fire_2.png");
        fire3Texture = loadTexture(gl, "resources/fire_3.png");
        fire4Texture = loadTexture(gl, "resources/fire_4.png");

        meshCube = ProceduralMeshFactory.createHardCube();
        meshPlane = ProceduralMeshFactory.createPlane(5, 5, 10, 10, 1, 1);  // Create the mesh cube structure

        //Creates generic non textured Render objects
        planeNT = new Render(meshPlane, floorTexture);
        cubeNT = new Render(meshCube, floorTexture);    // Create a new Render object for the mesh 

        // Creates textured object for each element of the scene
        floorPlaneYT = new Render(meshPlane, floorTexture);    // Create a new Render object for the mesh
        floorPlaneYT.initialiseDisplayList(gl, true, 20);

        ceilingPlaneYT = new Render(meshPlane, ceilingTexture);    // Create a new Render object for the mesh
        ceilingPlaneYT.initialiseDisplayList(gl, true, 10);

        wallPlaneYT = new Render(meshPlane, wallTexture);    // Create a new Render object for the mesh
        wallPlaneYT.initialiseDisplayList(gl, true, 10);

        doorPlaneYT = new Render(meshPlane, doorTexture);    // Create a new Render object for the mesh
        doorPlaneYT.initialiseDisplayList(gl, true, 1);

        fire1PlaneYT = new Render(meshPlane, fire1Texture);    // Create a new Render object for the mesh
        fire1PlaneYT.initialiseDisplayList(gl, true, 1);

        fire2PlaneYT = new Render(meshPlane, fire2Texture);    // Create a new Render object for the mesh
        fire2PlaneYT.initialiseDisplayList(gl, true, 1);

        fire3PlaneYT = new Render(meshPlane, fire3Texture);    // Create a new Render object for the mesh
        fire3PlaneYT.initialiseDisplayList(gl, true, 1);

        fire4PlaneYT = new Render(meshPlane, fire4Texture);    // Create a new Render object for the mesh
        fire4PlaneYT.initialiseDisplayList(gl, true, 1);

        obstacleCubeYT = new Render(meshCube, obstacleTexture);    // Create a new Render object for the mesh
        obstacleCubeYT.initialiseDisplayList(gl, true, 1);

        //Sets textured objects to be the used render objects
        floorPlane = floorPlaneYT;
        ceilingPlane = ceilingPlaneYT;
        wallPlane = wallPlaneYT;
        doorPlane = doorPlaneYT;
        fire1Plane = fire1PlaneYT;
        fire2Plane = fire2PlaneYT;
        fire3Plane = fire3PlaneYT;
        fire4Plane = fire4PlaneYT;
        obstacleCube = obstacleCubeYT;

    }

    /**
     * Loads texture from image file.
     *
     * Builds textured Mesh and Render objects to be used in the room Modified
     * by Kevin Stanley-Adams to enable Mipmapping and repeated textures.
     *
     * @param gl OpenGL context
     * @filename The image file containing texture
     *
     */
    private Texture loadTexture(GL2 gl, String filename) {
        Texture tex = null;
        // since file loading is involved, must use try...catch
        try {
            File f = new File(filename);

            // The following line results in a texture that is flipped vertically (i.e. is upside down)
            // due to OpenGL and Java (0,0) position being different:
            // tex = TextureIO.newTexture(new File(filename), false);
            // So, instead, use the following three lines which flip the image vertically:
            BufferedImage img = ImageIO.read(f); // read file into BufferedImage
            ImageUtil.flipImageVertically(img);

            // No mip-mapping.
            tex = AWTTextureIO.newTexture(GLProfile.getDefault(), img, true);

            // Different filter settings can be used to give different effects when the texture
            // is applied to a set of polygons.
            tex.setTexParameteri(gl, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR_MIPMAP_LINEAR);//Enables mipmapping
            tex.setTexParameteri(gl, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR_MIPMAP_LINEAR);

            tex.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);//Repeats texture
            tex.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);

        } catch (IOException | GLException e) {
            System.out.println("Error loading texture " + filename);
        }
        return tex;
    }

    /**
     * Sets size of window on screen.
     *
     *
     * @param w Width of canvas
     * @param h Height of canvas
     *
     */
    public void setCanvasSize(int w, int h) {
        canvaswidth = w;
        canvasheight = h;
    }

    /**
     * Method used from the GUI to control whether or not all the objects are
     * displayed
     *
     * @param b true if the objects should be displayed
     */
    public void setObjectsDisplay(boolean b) {
        objectsOn = b;
    }

    /**
     * Retrieves the first Light instance so that its attributes can be set from
     * the GUI.
     *
     * @return returns the first Light instance used in this class
     */
    public Light getLight() {
        return light;
    }

    /**
     * Retrieves the lamp's Light instance so that its attributes can be set
     * from the GUI. Written by Kevin Stanley-Adams 2013
     *
     * @return returns the lamp's Light instance
     */
    public Light getLight1() {
        return lamp1.getLight();
    }

    /**
     * Retrieves the current camera mode setting.
     *
     * Written by Kevin Stanley-Adams 2013
     *
     * @return returns true if the camera is set to follow the lamp
     */
    public boolean getLampCamera() {
        return lampCamera;
    }

    /**
     * Retrieves the Axes instance so that its attributes can be set from the
     * GUI, e.g. turned on and off.
     *
     * @return returns the Axes instance used in this class
     */
    public Axes getAxes() {
        return axes;
    }

    /**
     * Reset scene. Sets the animation control attributes to their initial
     * values and sets the objects to display
     *
     * Written by Kevin Stanley-Adams 2013
     */
    public void reset() {
        globalSize = 1;
        rotate = 0.0;
        deltaY = 0;
        lowerRot = initialLowerRot;
        upperRot = initialUpperRot;

        coneVertRot = 0;
        coneHorRot = 0;
        baseRot = 0;
        fireplaceAnimationStage = 0;

        roomLength = 15;
        roomWidth = 12;
        roomHeight = 3;

        jumpStage = 1;
        segmentCount = 1;
        deltaY = 0;
        rotate = 0.0;
        jumpTimer = 361 - jumpAngle;
        coneVertRot = 0;
        segmentCount = 0;
        setObjectsDisplay(true);
    }

    /**
     * Implements animation. Adjusts animation control attributes of the lamp to
     * change the scene over time
     *
     * Written by Kevin Stanley-Adams 2013
     */
    public void animateJump() {
        //Animation paramaters that affect the shape of motion of the lamp

        double segmentCoefficient = 0.5 * jumpSegments; //Used to change number of jumps per time unit
        double jumpAmplitude = 2 * globalSize; //Height of jump
        double lowerRotAmplitude = 7; //Adjusts magnitude of rotation change in the lower arms
        double lowerRotFreq = 2; //Adjusts number of oscillations in the lower arm
        double upperRotAmplitude = -7;//Adjusts magnitude of rotation change in the upper arms
        double upperRotFreq = 2;//Adjusts number of oscillations in the upper arm
        double baseRotAmplitude = -3;//Adjusts magnitude of rotation in the base
        double baseRotFreq = 2;//Adjusts number of oscillations in the base
        double coneRotAmplitude = 3;//Adjusts magnitude of rotation in the lamp head
        double lowerRotChange = 0;//Variables to hold the change to be applied as a transformation
        double upperRotChange = 0;
        double baseRotChange = 0;
        double coneVertChange = 0;
        double armBigCoefficient = 1.5;
        double heightBigCoefficient = 2;
        double baseBigCoefficient = 2;
        boolean isBigJump = true; //Set to true when the lamp is performing a large jump over the obstacle
        //The jumpTimer cycles 1-360 and is used as a measure of time. 360 is a half a revolution of the scene containing many jumps (adjusted using jumpSegments)
        if (jumpTimer <= 360) {
            if ((jumpTimer <= jumpAngle) || (jumpTimer >= (360 - jumpAngle))) { //Sets isBigJump to true when the lamp is in the region near the obstacles
                isBigJump = true;
            } else {
                isBigJump = false;
            }

            if (isBigJump) {//Modify paramaters for a larger jump
                lowerRotAmplitude = lowerRotAmplitude * armBigCoefficient;
                baseRotAmplitude = baseRotAmplitude * baseBigCoefficient;
                jumpAmplitude = jumpAmplitude * heightBigCoefficient;
            }
            //Large Jump animation
            deltaY = jumpAmplitude * sin(Math.toRadians(jumpTimer * segmentCoefficient)); //Calculates height to jump using sin curve
            if (deltaY > 0) { //If the lamp is in the air, the arms and base oscillate to a sine curve. Rotate variable incremented to move lamp laterally.
                lowerRotChange = lowerRotAmplitude * sin(Math.toRadians(jumpTimer * segmentCoefficient * lowerRotFreq));
                upperRotChange = upperRotAmplitude * sin(Math.toRadians(jumpTimer * segmentCoefficient * upperRotFreq));
                rotate += jumpAngle / 18;
                baseRotChange = baseRotAmplitude * sin(Math.toRadians(jumpTimer * segmentCoefficient * baseRotFreq));
            } else { //If the sine curve gives a negative result, the lamp is set to stay on the ground
                deltaY = 0;
                //A cos curve is used for ground oscillations to make the animation more realistic
                lowerRotChange = lowerRotAmplitude * cos(Math.toRadians((jumpTimer) * segmentCoefficient * lowerRotFreq));
                upperRotChange = upperRotAmplitude * cos(Math.toRadians((jumpTimer) * segmentCoefficient * upperRotFreq));

            }
            coneVertChange = coneRotAmplitude * sin(Math.toRadians(jumpTimer * segmentCoefficient * upperRotFreq));
            //A segment represents a set of animation frames. One airtime set of frames, or one ground level set of frames.
            jumpStage += 1;//Tracks the current frame of a jump animation
            if (jumpStage == jumpAngle + 1) {//Resets to 1 after a complete segment, increments segment tracker
                jumpStage = 1;
                segmentCount += 1;
                //Ensures that an equal amount of rotation is applied after each segment. Needed due to precision erros with sine/cosine.
                if ((segmentCount % 2 == 0) && ((2 * rotate / segmentCount) != jumpAngle)) {
                    rotate = segmentCount / 2 * jumpAngle;
                }

            }
            //Resets segment counter after a complete revolution of the room
            if (segmentCount == 2 * jumpSegments + 1) {
                segmentCount = 1;
            }
            //Applies calculated transformation changes to variables that are passed to objects when rendered
            lowerRot += lowerRotChange;
            upperRot += upperRotChange;
            baseRot += baseRotChange;
            coneVertRot += coneVertChange;
            jumpTimer += 1;
        } else {//Resets timer to maintain in range 1-360
            jumpTimer -= 360;
        }
        //Resets rotate variable to maintain in range 0-360
        if (rotate >= 360) {
            rotate -= 359;
        }

    }

    /**
     * Increment fire animation frame. Adjusts animation control attributes of
     * the fireplace texture
     *
     * Written by Kevin Stanley-Adams 2013
     */
    public void incFire() {
        fireplaceAnimationStage = (fireplaceAnimationStage + 1) % 4;

    }

    /**
     * Updates the animation control variables.
     */
    public void update() {

        animateJump();
        incFire();

    }

    /**
     * Increment Size of Room. Increases size of the room
     *
     * @param b Set to true if only the height is increased
     *
     */
    public void incRoomSize(boolean b) {
        if (b) {
            roomHeight = roomHeight * 1.02;
        } else if (roomWidth < 20) {
            roomWidth = roomWidth * 1.02;
            roomHeight = roomHeight * 1.02;
            roomLength = roomLength * 1.02;
        }
    }

    /**
     * Decrement Size of Room. Decreases size of the room. Rejects request if
     * the size will integer with the lamp
     *
     * @param b Set to true if only the height is decreased
     *
     */
    public void decRoomSize(boolean b) {
        if (b) {
            roomHeight = roomHeight * 0.98;
        } else if (roomWidth > 10) {
            roomWidth = roomWidth * 0.98;
            roomHeight = roomHeight * 0.98;
            roomLength = roomLength * 0.98;
        }
    }

    /**
     * Increment Size. Increases size of all objects
     *
     *
     */
    public void incSize() {
        globalSize += 0.1;
    }

    /**
     * Decrement Size. Decreases size of all objects
     *
     *
     */
    public void decSize() {
        globalSize -= 0.1;
    }

    /**
     * Enable light. Enables light for scene
     *
     * @param gl OpenGL context
     *
     */
    private void doLight(GL2 gl) {
        gl.glPushMatrix();
        if (light.getSwitchedOn()) {
            light.use(gl, glut, false);
        } else {
            light.disable(gl);
        }
        gl.glPopMatrix();
    }

    /**
     * Constructs fireplace. Fireplace is made from a series of textured places
     *
     * @param gl OpenGL context
     */
    private void drawFireplace(GL2 gl) {
        double length = roomLength * globalSize;//Size paramaters
        double width = roomWidth * globalSize;
        double height = roomHeight * globalSize;

        //Sizes and places plane in the room
        gl.glPushMatrix();
        gl.glTranslated(0, 0, -width * 3 * globalSize);
        gl.glScaled(length * 0.15, height * 0.4, length * 0.15);
        gl.glTranslated(0, 2.5, 0);
        gl.glRotated(90, 1, 0, 0);

        if (fireplaceAnimationStage == 0) {
            fire1Plane.renderDisplayList(gl, isTexturesOn);
        }
        if (fireplaceAnimationStage == 1) {
            fire2Plane.renderDisplayList(gl, isTexturesOn);
        }
        if (fireplaceAnimationStage == 2) {
            fire3Plane.renderDisplayList(gl, isTexturesOn);
        }
        if (fireplaceAnimationStage == 3) {
            fire4Plane.renderDisplayList(gl, isTexturesOn);
        }
        gl.glPopMatrix();
    }
    /**
     * Constructs room. Room is made from a series of textured places
     * Written by Kevin Stanley-Adams 2013
     * @param gl OpenGL context
     * @param roomWidth
     * @param roomLength
     * @param roomHeight
     */
    private void drawRoom(GL2 gl, double roomWidth, double roomLength, double roomHeight) {

        double length = roomWidth * globalSize;
        double width = roomLength * globalSize;
        double height = roomHeight * globalSize;

        //Ceiling
        gl.glPushMatrix();
        gl.glTranslated(0, 5 * height, -width * 2.5);
        gl.glRotated(90, 1, 0, 0);
        gl.glScaled(length, width, length);
        gl.glTranslated(0, 2.5, 0);
        gl.glRotated(90, 1, 0, 0);
        ceilingPlane.renderDisplayList(gl, isTexturesOn);
        gl.glPopMatrix();

        //Floor
        gl.glPushMatrix();
        gl.glTranslated(0, 0, width * 2.5);
        gl.glRotated(-90, 1, 0, 0);
        gl.glScaled(length, width, length);
        gl.glTranslated(0, 2.5, 0);
        gl.glRotated(90, 1, 0, 0);
        floorPlane.renderDisplayList(gl, isTexturesOn);
        gl.glPopMatrix();

        //Small Walls
        gl.glPushMatrix();
        gl.glTranslated(length * 2.5, 0, 0);
        gl.glRotated(-90, 0, 1, 0);
        gl.glScaled(width, height, width);
        gl.glTranslated(0, 2.5, 0);
        gl.glRotated(90, 1, 0, 0);
        wallPlane.renderDisplayList(gl, isTexturesOn);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslated(-length * 2.5, 0, 0);
        gl.glRotated(90, 0, 1, 0);
        gl.glScaled(width, height, width);
        gl.glTranslated(0, 2.5, 0);
        gl.glRotated(90, 1, 0, 0);
        wallPlane.renderDisplayList(gl, isTexturesOn);
        gl.glPopMatrix();

        //Door
        gl.glPushMatrix();
        gl.glTranslated(0, 0, width * 2.49);
        gl.glRotated(180, 0, 1, 0);
        gl.glScaled(length * 0.2, height * 0.7, length * 0.2);
        gl.glTranslated(0, 2.5, 0);
        gl.glRotated(90, 1, 0, 0);
        doorPlane.renderDisplayList(gl, isTexturesOn);
        gl.glPopMatrix();

        //Long Walls
        gl.glPushMatrix();
        gl.glTranslated(0, 0, width * 2.5);
        gl.glRotated(180, 0, 1, 0);
        gl.glScaled(length, height, length);
        gl.glTranslated(0, 2.5, 0);
        gl.glRotated(90, 1, 0, 0);
        wallPlane.renderDisplayList(gl, isTexturesOn);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslated(0, 0, -width * 2.5);
        gl.glScaled(length, height, length);
        gl.glTranslated(0, 2.5, 0);
        gl.glRotated(90, 1, 0, 0);
        wallPlane.renderDisplayList(gl, isTexturesOn);
        gl.glPopMatrix();

    }
    /**
     * Constructs obstacle. Obstacle is made from a series of textured cubes
     * Written by Kevin Stanley-Adams 2013
     * @param gl OpenGL context
     * @param size
     */
    private void drawObstacle(GL2 gl, double size) {

        gl.glPushMatrix();

        //Draw middle bars
        gl.glPushMatrix();
        gl.glTranslated(2.5 * size, 1.9 * size, 0);
        gl.glRotated(90, 0, 0, 1);
        gl.glScaled(0.3 * size, 5 * size, 0.3 * size);
        gl.glTranslated(0, 0.5, 0);
        obstacleCube.renderDisplayList(gl, isTexturesOn);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslated(2.5 * size, 1.3 * size, 0);
        gl.glRotated(90, 0, 0, 1);
        gl.glScaled(0.3 * size, 5 * size, 0.3 * size);
        gl.glTranslated(0, 0.5, 0);
        obstacleCube.renderDisplayList(gl, isTexturesOn);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslated(2.5 * size, 0.7 * size, 0);
        gl.glRotated(90, 0, 0, 1);
        gl.glScaled(0.3 * size, 5 * size, 0.3 * size);
        gl.glTranslated(0, 0.5, 0);
        obstacleCube.renderDisplayList(gl, isTexturesOn);
        gl.glPopMatrix();

        //Draw side bars
        gl.glPushMatrix();
        gl.glTranslated(-2.5 * size, 0, 0);
        gl.glScaled(0.6 * size, 2.5 * size, 0.6 * size);
        gl.glTranslated(0, 0.5, 0);
        obstacleCube.renderDisplayList(gl, isTexturesOn);
        gl.glPopMatrix();

        gl.glPushMatrix();
        gl.glTranslated(2.5 * size, 0, 0);
        gl.glScaled(0.6 * size, 2.5 * size, 0.6 * size);
        gl.glTranslated(0, 0.5, 0);
        obstacleCube.renderDisplayList(gl, isTexturesOn);
        gl.glPopMatrix();

        gl.glPopMatrix();
    }
    /**
     * Changes camera mode.
     * 
     */
    public void setCameraMode() {
        lampCamera = !lampCamera;
    }
    /**
     * Enables/Disables Textures.
     * 
     * Swaps render objects depending on textures being on or off
     * 
     */
    public void setTextureMode() {
        isTexturesOn = !isTexturesOn;
        if (!isTexturesOn) {
            floorPlane = planeNT;
            ceilingPlane = planeNT;
            wallPlane = planeNT;
            doorPlane = planeNT;
            fire1Plane = planeNT;
            fire2Plane = planeNT;
            fire3Plane = planeNT;
            fire4Plane = planeNT;
            obstacleCube = cubeNT;
        } else {
            floorPlane = floorPlaneYT;
            ceilingPlane = ceilingPlaneYT;
            wallPlane = wallPlaneYT;
            doorPlane = doorPlaneYT;
            fire1Plane = fire1PlaneYT;
            fire2Plane = fire2PlaneYT;
            fire3Plane = fire3PlaneYT;
            fire4Plane = fire4PlaneYT;
            obstacleCube = obstacleCubeYT;
        }
    }

    /**
     * Renders the scene.
     *
     * @param gl OpenGL context
     */
    public void render(GL2 gl) {
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();
        axes.setSwitchedOn(false);
        if (lampCamera) { // Camera angle follows lamp

            camera.followLamp(glu, rotate, deltaY);

        } else {
            camera.view(glu); // Convential camera view
        }
        doLight(gl);                      // Place the light

        if (axes.getSwitchedOn()) {
            axes.display(gl, glut);
        }

        if (objectsOn) {                  // Render the objects

            drawRoom(gl, roomWidth, roomLength, roomHeight);
            drawFireplace(gl);

            //Obstacle
            gl.glPushMatrix();
            gl.glRotated(180 / jumpSegments, 0, 1, 0);
            gl.glPushMatrix();
            gl.glTranslated(20 * globalSize, 0, 0);
            drawObstacle(gl, globalSize);
            gl.glPopMatrix();

            
            //Obstacle
            gl.glPushMatrix();
            gl.glTranslated(-20 * globalSize, 0, 0);
            drawObstacle(gl, globalSize);
            gl.glPopMatrix();
            gl.glPopMatrix();

            //Lamp jump transformation
            gl.glTranslated(0, deltaY * globalSize, 0);

            //Lamp being placed
            gl.glPushMatrix();
            gl.glRotated(rotate, 0, 1, 0);
            gl.glTranslated(-20 * globalSize, 0, 0);
            lamp1.draw(globalSize, lowerRot, upperRot, coneVertRot, coneHorRot, baseRot);
            gl.glPopMatrix();

        }
    }

}
