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
    private String rulesX;
    private String rulesY;
    
    public Tree(double x, double y, double z) {
        myPos = new double[3];
        myPos[0] = x;
        myPos[1] = y;
        myPos[2] = z;

        //This is the rewrite system. Change these if you want to change how it generates.
        rulesX = "F - R R [ [ R X ] E + X ] E + F [ R + F E E X ] R R - X";
        rulesY = "F E F";
    }
    
    public double[] getPosition() {
        return myPos;
    }

    public void draw(GL2 gl, GLU glu, Texture barkTex, Texture leavesTex, boolean useFractal, int iteration){


        GLUquadric leaves = glu.gluNewQuadric();
        GLUquadric trunk = glu.gluNewQuadric();
        glu.gluQuadricTexture(trunk, true);
        glu.gluQuadricNormals(trunk, GLU.GLU_SMOOTH);

        glu.gluQuadricTexture(leaves, true);
        glu.gluQuadricNormals(leaves, GLU.GLU_SMOOTH);

        if (!useFractal) {
            gl.glPushMatrix();
            gl.glTranslated(this.getPosition()[0], this.getPosition()[1], this.getPosition()[2]);
            gl.glRotated(-90, 1, 0, 0);
            barkTex.enable(gl);
            barkTex.bind(gl);
            glu.gluCylinder(trunk, 0.1, 0.1, 1, 8, 8);
            gl.glTranslated(0, 0, 0.2);
            barkTex.disable(gl);
            leavesTex.enable(gl);
            leavesTex.bind(gl);
            glu.gluCylinder(leaves, 0.5, 0, 2, 16, 16);
            leavesTex.disable(gl);
            gl.glPopMatrix();

        } else {

            barkTex.enable(gl);
            barkTex.bind(gl);
            //Fractal Tree Gen
            int iterations = iteration;
            String generator = "X";


            //This is the generator, independent of the command rules and rewrite stuff
            for (int i = 0; i < iterations; i++) {
                String[] arguments = generator.split(" ");
                generator = "";
                for (String s : arguments) {
                    if (s.equals("X")) {
                        generator = generator + rulesX + " ";
                    } else if (s.equals("Y")) {
                        generator = generator + rulesY + " ";
                    } else {
                        generator = generator + s + " ";
                    }
                }
            }

            String[] interpreter = generator.split(" ");



            //This code here to create the initial branch
            gl.glTranslated(this.getPosition()[0], this.getPosition()[1], this.getPosition()[2]);
            gl.glPushMatrix();
            gl.glRotated(-90, 1, 0, 0);
            glu.gluCylinder(trunk, 0.01, 0.01, 0.2, 2, 2);
                /*gl.glTranslated(0, 0, 0.2);
                glu.gluCylinder(leaves, 0.05, 0, 0.2, 4, 4);
                */
            gl.glPopMatrix();


            //This handles the rewrite system. Change these if you want to change how to generate
            for (String s : interpreter) {
                if (s.equals("F")) {
                    //upwards 0.2
                    gl.glTranslated(0, 0.2, 0);
                } else if (s.equals("+")) {
                    //rotate 20
                    gl.glRotated(20, 1, 0, 1);
                } else if (s.equals("-")) {
                    //rotate -20
                    gl.glRotated(-20, 1, 0, 1);
                } else if (s.equals("[")) {
                    //push
                    gl.glPushMatrix();
                } else if (s.equals("]")) {
                    //pop
                    gl.glPopMatrix();
                } else if (s.equals("R")) {
                    gl.glRotated(20, 0, 1, 0);
                } else if (s.equals("E")) {
                    gl.glRotated(-20, 0, 1, 0);
                }
                gl.glPushMatrix();
                gl.glRotated(-90, 1, 0, 0);
                glu.gluCylinder(trunk, 0.01, 0.01, 0.2, 2, 2);
                //If you want the little balls as "leaves", uncomment this. This will add far more workload though.
                /*
                gl.glTranslated(0, 0, 0.2);
                glu.gluSphere(leaves, 0.1, 4,4);
                */

                gl.glPopMatrix();
            }
            barkTex.disable(gl);
        }

    }
}
