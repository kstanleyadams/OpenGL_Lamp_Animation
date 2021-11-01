/**
 * The Render class is used for rendering Mesh objects
 * Set up for teaching purposes. A better structure may have used inheritance to define the 
 * the different rendering modes. However, it is useful to have them all here to demonstrate their 
 * differences. 
 * Note that the list of vertices, triangles and normals are in a simple array format
 * in contrast to the more complex Mesh structure. 
 * The use of arrays here is to support efficient rendering, whereas a Mesh structure is typically structured
 * to support ease of editing.
 * If the Mesh structure was to be updated, e.g. a vertex moved, then then relevant data would
 * need to be copied here again, before rendering would produce the correct result.
 *
 * @author    Dr Steve Maddock
 * @version   3.0 (29/07/2013)
 * 
 * I declare that this code is my own work
 * Modified by Kevin Stanley-Adams coa10ks@sheffield.ac.uk. Modified methods are labelled.
 *
 *
 * @author Kevin Stanley-Adams 2013
 */

import javax.media.opengl.*;

import com.jogamp.opengl.util.texture.*;
 
public class Render {
  private Mesh mesh;
  private double[] vertices;
  private double[] normals;
  private int[] triangles;
  private double[] textureCoords;
  private int dlist;
  private Texture tex;

  /**
   * Constructor. Copies the vertex, triangle and normal data from the Mesh structure.
   * If the Mesh structure were to change, e.g. a vertex moved, then the data would
   * need to be copied again.
   * The use of simple arrays makes the rendering process more efficient.
   * May also takes a Texture parameter which may be used when rendering the object.
   */  
   
  public Render(Mesh m) {
    reset(m, null);
  }
  
  public Render(Mesh m, Texture t) {
    reset(m, t);
  }
  
  public void reset(Mesh m, Texture t) {
    mesh = m;
    vertices = m.getVertexList();
    normals = m.getNormalList();
    triangles = m.getTriangleList();
    textureCoords = m.getTextureCoordsList();
    dlist = 0;
    tex = t;
  }

  /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
  /* Immediate mode sending individual triangles */
  /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
  
  /**
   * Render a mesh using immediate mode.
   * 
   * @param  gl  the OpenGL context.
   */ 
  
  private void setMaterial(GL2 gl) {
    Material material = mesh.getMaterial();
    gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, material.getAmbient(), 0);    
    gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, material.getDiffuse(), 0);    
    gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, material.getSpecular(), 0);
    gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_EMISSION, material.getEmission(), 0);
    gl.glMaterialf(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, material.getShininess());
  }  
  
  private void sendNVData(GL2 gl, int t, int i) {
    int index = triangles[t*3+i]*3;
    gl.glNormal3d(normals[index], normals[index+1], normals[index+2]);
    gl.glVertex3d(vertices[index], vertices[index+1], vertices[index+2]);
  }
  
  private void sendAllNVData(GL2 gl) {
    for (int t=0; t<triangles.length/3; t++) {
      for (int i=0; i<3; i++) {
        sendNVData(gl, t,i);
      }
    }
  }
  
  public void renderImmediateMode(GL2 gl, boolean texit, int repeat) {
    if (texit) {
      tex.enable(gl);
      tex.bind(gl);
    }
    setMaterial(gl);
    if (texit) tex.setTexParameteri(gl, GL2.GL_TEXTURE_ENV_MODE,GL2.GL_MODULATE);
    
    gl.glBegin(GL2.GL_TRIANGLES);
      if (texit) {
        for (int t=0; t<triangles.length/3; t++) {
          for (int i=0; i<3; i++) {
            int tindex = triangles[t*3+i]*2;
            gl.glTexCoord2d(repeat*textureCoords[tindex], repeat*textureCoords[tindex+1]);
            sendNVData(gl, t,i);
          }
        }
      }
      else {
        sendAllNVData(gl);
      }
    gl.glEnd();
    
    if (texit) tex.disable(gl);
  }

  /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
  /* Display List */
  /* ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ */
  
   /**
   * Initialise a mesh using a display list.
   * 
   * Modified by Kevin Stanley-Adams 2013 to allow textures to be repeated in a square matrix with in input parameter
   * defining the size of the square.
   * @param  gl  the OpenGL context.
   * @param texit Texture enable parameter
   * @param repeat The number of times to repeat the texture on the object
   */ 
   
  public void initialiseDisplayList(GL2 gl, boolean texit, int repeat) {
    dlist = gl.glGenLists(1);
    gl.glNewList(dlist, GL2.GL_COMPILE);
      renderImmediateMode(gl, texit, repeat);
    gl.glEndList(); 
  }
  
  /**
   * Render a mesh using a display list.
   * 
   * Modified by Kevin Stanley-Adams 2013 to allow textures to be switched on
   * and off
   * @param  gl  the OpenGL context.
   * @param isTexturesOn Allows for switching between textured and plain rendering
   */ 
  
  public void renderDisplayList(GL2 gl, boolean isTexturesOn) {
        if (isTexturesOn) gl.glCallList(dlist);
        else renderImmediateMode(gl, false, 1);
  }
  
  /**
   * Wireframe a mesh using immediate mode.
   * 
   * @param  gl  the OpenGL context.
   */ 
 
  public void wireframeImmediateMode(GL2 gl, boolean lightingOn) {
    setMaterial(gl);
    if (!lightingOn) gl.glDisable(GL2.GL_LIGHTING);
    gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
    gl.glBegin(GL2.GL_TRIANGLES);
      sendAllNVData(gl);
    gl.glEnd();
    if (!lightingOn) gl.glEnable(GL2.GL_LIGHTING);
    gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
  }

}
