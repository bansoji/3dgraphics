package ass2.spec;

import com.jogamp.opengl.util.texture.Texture;

import javax.media.opengl.glu.GLUquadric;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.GL2;

/**
 * COMMENT: Comment Tree 
 *
 * @author malcolmr
 */
public class Tree {

    private double[] myPos;
    
    public Tree(double x, double y, double z) {
        myPos = new double[3];
        myPos[0] = x;
        myPos[1] = y;
        myPos[2] = z;
    }
    
    public double[] getPosition() {
        return myPos;
    }

    public void draw(GL2 gl, GLU glu, Texture barkTex, Texture leavesTex){
        gl.glTranslated(this.getPosition()[0],this.getPosition()[1],this.getPosition()[2]);
        gl.glRotated(-90,1,0,0);
        barkTex.enable(gl);
        barkTex.bind(gl);
        GLUquadric leaves = glu.gluNewQuadric();
        GLUquadric trunk = glu.gluNewQuadric();
        glu.gluQuadricTexture(trunk, true);
        glu.gluQuadricNormals(trunk, GLU.GLU_SMOOTH);
        glu.gluCylinder(trunk,0.1,0.1,1,8,8);
        gl.glTranslated(0,0,0.2);
        leavesTex.enable(gl);
        leavesTex.bind(gl);
        glu.gluQuadricTexture(leaves, true);
        glu.gluQuadricNormals(leaves, GLU.GLU_SMOOTH);
        glu.gluCylinder(leaves,0.5,0,2,16,16);
    }
}
