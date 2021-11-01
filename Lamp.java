/** I declare that this code is my own work
* Kevin Stanley-Adams coa10ks@sheffield.ac.uk
*
*
* @author Kevin Stanley-Adams 2013
*/

import javax.media.opengl.*;
import com.jogamp.opengl.util.gl2.GLUT;

public class Lamp {

    private final GL2 gl;
    private final GLUT glut;
    private Light light1;

    private Mesh meshCylinder, meshCube;
    private Render cylinder, cube;

    //Red plastic colour for parts of the lamp
    private static final float[] redAmbient = {1.0f, 0.2f, 0.2f, 1.0f}; //
    private static final float[] redDiffuse = {0.2f, 0.2f, 0.2f, 1.0f};
    private static final float[] redSpecular = {0.1f, 0.1f, 0.1f, 1.0f};
    private static final float[] redShininess = {0.1f};

    //Gold colour settings for the switch on the back of the lamp
    private static final float[] goldAmbient = {0.24725f, 0.1995f, 0.0745f, 1.0f};
    private static final float[] goldDiffuse = {0.75164f, 0.60648f, 0.60648f, 1.0f};
    private static final float[] goldSpecular = {0.628281f, 0.555802f, 0.366065f, 1.0f};
    private static final float[] goldShininess = {0.4f};

    /**
     * Constructor.
     *
     * @param gl OpenGL context
     * @param glut GLUT code library for building basic shapes
     *
     */
    public Lamp(GL2 gl, GLUT glut) {

        this.gl = gl;
        this.glut = glut;

        createLight(gl);                  // Create light that sits in the lamp
        createRenderObjects(gl);          // Create/load objects

    }

    /**
     * Makes Light. Defines light position and visual properties, and then makes
     * it a spotlight
     *
     * @param gl OpenGL context
     *
     */
    private void createLight(GL2 gl) {
        float[] lightPosition = {0, 0, 0, 1};
        float[] lightAmbient = {0.1f, 0.1f, 0.1f};
        float[] lightDiffuse = {1.0f, 1.0f, 1.0f}; //Gives 'spotlight like' appearance
        float[] lightSpecular = {1.0f, 1.0f, 1.0f};
        light1 = new Light(GL2.GL_LIGHT1, lightPosition, lightAmbient, lightDiffuse, lightSpecular, true); //Create object of Light class with predefined paramaters
        float[] direction = {0f, 0f, -1f}; // direction from position to origin
        light1.makeSpotlight(direction, 45f); //Makes the light a spotlight with a 45 degree cutoff angle
    }

    /**
     * Gets Light.
     *
     * @return Returns light object to other classes if needed 8
     *
     */
    public Light getLight() {
        return light1;
    }

    /**
     * Creates Mesh/Render Objects.
     *
     * Builds generic Mesh and Render objects to be used multiple times for different parts of the lamp
     * @param gl OpenGL context
     *
     */
    private void createRenderObjects(GL2 gl) {
        meshCylinder = ProceduralMeshFactory.createCylinder(); //Generic cylinder mesh object 
        Material mat = meshCylinder.getMaterial();  //Retrieves material properties of object
        mat.setAmbient(redAmbient); // Sets colour properties defined in constructor
        mat.setDiffuse(redDiffuse);
        mat.setSpecular(redSpecular);
        mat.setShininess(redShininess[0]);
        cylinder = new Render(meshCylinder); //Creates Render object from Mesh

        //Same process as cylinder for cube object (to create the light switch on the back of the lamp)
        
        meshCube = ProceduralMeshFactory.createHardCube();
        mat = meshCube.getMaterial();
        mat.setAmbient(goldAmbient);
        mat.setDiffuse(goldDiffuse);
        mat.setSpecular(goldSpecular);
        mat.setShininess(goldShininess[0]);
        cube = new Render(meshCube);
    }
    /**
     * Enable light. Resizes and enables light for lamp
     *
     * @param gl OpenGL context
     *
     */
    public void doLight1(GL2 gl) {
        gl.glPushMatrix();
        gl.glScaled(2.5, 2.5, 2.5); //Resizes light bulb
        if (light1.getSwitchedOn()) { //Check objects on/off state
            light1.use(gl, glut, true);//Renders and makes visible if switched on
        } else {
            light1.disable(gl);
        }
        gl.glPopMatrix();
    }
    /**
     * setGLUTColours. Sets colour of objects drawn by GLUT to match the colour of gold Mesh/Render objects
     *
     * @param gl OpenGL context
     *
     */
    private void setGLUTColours() {
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, goldAmbient, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, goldDiffuse, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, goldSpecular, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SHININESS, goldShininess, 0);
    }

        /**
     * Draws lamp. Builds lamp by transforming various primitives into a hierarchical model
     *
     * @param size The overall size of the lamp
     * @param lowerRot The rotation of the lower arms. 0 degrees stands them vertically from the base.
     * @param upperRot The rotation of the upper arms. 0 degrees stands them vertically from the lower arms.
     * @param coneVertRot The vertical orientation of the lamp head
     * @param coneHorRot The horizontal orientation of the lamp head
     * @param baseRot The orientation of the lamp base. 0 degrees is flat to the ground
     *
     */
    public void draw(double size, double lowerRot, double upperRot, double coneVertRot, double coneHorRot, double baseRot) {

        //Lamp paramaters initialisation to define shape of each primitive
        double baseWidth = 1.0 * size;
        double baseHeight = baseWidth * 0.1;
        double baseLength = baseWidth;
        double lowerArmLength = 1.5 * size;
        double lowerArmRadius = 0.05 * size;
        double lowerArmRotation = lowerRot;
        double upperArmLength = 1.0 * size;
        double upperArmRadius = lowerArmRadius;
        double upperArmRotation = upperRot;
        double jointRadius = baseWidth * 0.2;
        double coneSize = 0.7 * baseWidth;
        double armSpacing = 0.05 * size; //Spacing for arm structure, as the lamp use two arms for lower and upper sections. 
        int stacks = 100;//GLUT object paramaters to determine complexity of mesh
        int slices = 100;

        setGLUTColours(); //Sets colour of GLUT objects to match gold Mesh/Render objects

        gl.glPushMatrix();

        //Draw base
        gl.glPushMatrix();
        gl.glRotated(baseRot, 1, 0, 0);

        gl.glPushMatrix();
        gl.glScaled(baseWidth, baseHeight, baseLength);
        gl.glRotated(90, -1, 0, 0);
        cylinder.renderImmediateMode(gl, false, 1);
        gl.glPopMatrix();

        // Draw bottom joint
        gl.glPushMatrix();
        gl.glScaled(baseWidth * 0.4, jointRadius, baseWidth * 0.4);
        gl.glRotated(90, -1, 0, 0);
        cylinder.renderImmediateMode(gl, false, 1);
        gl.glPopMatrix();
        gl.glPopMatrix();

        gl.glRotated(lowerArmRotation, 1, 0, 0); //Rotates lower arms

        //Draw lower strength arm
        gl.glPushMatrix();
        gl.glTranslated(armSpacing * baseWidth, lowerArmLength * 0.5 + baseHeight, 0);
        gl.glRotated(90, 0, 0, 1);
        gl.glScaled(lowerArmRadius, armSpacing * baseWidth * 2, lowerArmRadius);
        gl.glRotated(90, -1, 0, 0);
        cylinder.renderImmediateMode(gl, false, 1);
        gl.glPopMatrix();

        //Draw lower arms
        gl.glPushMatrix();
        gl.glTranslated(baseWidth * -armSpacing, baseHeight, 0);
        gl.glScaled(lowerArmRadius, lowerArmLength, lowerArmRadius);
        gl.glRotated(90, -1, 0, 0);
        cylinder.renderImmediateMode(gl, false, 1);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslated(baseWidth * armSpacing, baseHeight, 0);
        gl.glScaled(lowerArmRadius, lowerArmLength, lowerArmRadius);
        gl.glRotated(90, -1, 0, 0);
        cylinder.renderImmediateMode(gl, false, 1);
        gl.glPopMatrix();
        
        //Raises up to height of lower arms
        gl.glTranslated(0, lowerArmLength, 0);
        gl.glRotated(upperArmRotation, 1, 0, 0);

        //Draw middle joint
        gl.glPushMatrix();
        setGLUTColours();
        glut.glutSolidSphere(jointRadius, slices, stacks);
        gl.glPopMatrix();

        //Draw upper arms
        gl.glPushMatrix();
        gl.glTranslated(baseWidth * armSpacing, 0, 0);
        gl.glScaled(upperArmRadius, upperArmLength, upperArmRadius);
        gl.glRotated(90, -1, 0, 0);
        cylinder.renderImmediateMode(gl, false, 1);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslated(baseWidth * -armSpacing, 0, 0);
        gl.glScaled(upperArmRadius, upperArmLength, upperArmRadius);
        gl.glRotated(90, -1, 0, 0);
        cylinder.renderImmediateMode(gl, false, 1);
        gl.glPopMatrix();
        
        //Raises up to height of upper arms
        gl.glTranslated(0, upperArmLength, 0);

        //Draw cone structure
        gl.glPushMatrix();
        gl.glRotated(coneHorRot, 0, 1, 0);
        gl.glRotated(coneVertRot, 1, 0, 0);
        gl.glRotated(270, 1, 0, 0);
        gl.glTranslated(0, -coneSize, 0);
        gl.glPushMatrix();
        gl.glPushMatrix();
        gl.glTranslated(0, coneSize - 0.9 * 0.8 * coneSize, 0);
        gl.glScaled(coneSize * 0.6, coneSize * 0.8, coneSize * 0.6);
        gl.glRotated(90, -1, 0, 0);
        cylinder.renderImmediateMode(gl, false, 1);
        gl.glPopMatrix();
        gl.glPushMatrix();
        gl.glTranslated(0, coneSize, 0);
        gl.glRotated(30, 0, 0, 1);
        gl.glTranslated(-0.05 * coneSize, 0, 0);
        gl.glScaled(0.4 * coneSize, 0.2 * coneSize, 0.1 * coneSize);
        cube.renderImmediateMode(gl, false, 1); //Light switch
        gl.glPopMatrix();
        gl.glPopMatrix();
        gl.glRotated(90, -1, 0, 0);//Cone upright
        gl.glDisable(GL2.GL_CULL_FACE);//Disables face culling so inside of cone is visible
        setGLUTColours();
        glut.glutSolidCone(coneSize, coneSize, slices, stacks);
        gl.glEnable(GL2.GL_CULL_FACE);
        gl.glScaled(size, size, size);
        doLight1(gl);
        gl.glPopMatrix();
        gl.glPopMatrix();
    }

}
