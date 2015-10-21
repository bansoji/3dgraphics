package ass2.spec;

import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;


/**
 * COMMENT: Comment HeightMap 
 *
 * @author malcolmr
 */
public class Terrain {

    private Dimension mySize;
    private double[][] myAltitude;
    private List<Tree> myTrees;
    private List<Road> myRoads;
    private float[] mySunlight;

    /**
     * Create a new terrain
     *
     * @param width The number of vertices in the x-direction
     * @param depth The number of vertices in the z-direction
     */
    public Terrain(int width, int depth) {
        mySize = new Dimension(width, depth);
        myAltitude = new double[width][depth];
        myTrees = new ArrayList<Tree>();
        myRoads = new ArrayList<Road>();
        mySunlight = new float[3];
    }
    
    public Terrain(Dimension size) {
        this(size.width, size.height);
    }

    public Dimension size() {
        return mySize;
    }

    public List<Tree> trees() {
        return myTrees;
    }

    public List<Road> roads() {
        return myRoads;
    }

    public float[] getSunlight() {
        return mySunlight;
    }

    /**
     * Set the sunlight direction. 
     * 
     * Note: the sun should be treated as a directional light, without a position
     * 
     * @param dx
     * @param dy
     * @param dz
     */
    public void setSunlightDir(float dx, float dy, float dz) {
        mySunlight[0] = dx;
        mySunlight[1] = dy;
        mySunlight[2] = dz;        
    }
    
    /**
     * Resize the terrain, copying any old altitudes. 
     * 
     * @param width
     * @param height
     */
    public void setSize(int width, int height) {
        mySize = new Dimension(width, height);
        double[][] oldAlt = myAltitude;
        myAltitude = new double[width][height];
        
        for (int i = 0; i < width && i < oldAlt.length; i++) {
            for (int j = 0; j < height && j < oldAlt[i].length; j++) {
                myAltitude[i][j] = oldAlt[i][j];
            }
        }
    }

    /**
     * Get the altitude at a grid point
     * 
     * @param x
     * @param z
     * @return
     */
    public double getGridAltitude(int x, int z) {
        return myAltitude[x][z];
    }

    /**
     * Set the altitude at a grid point
     * 
     * @param x
     * @param z
     * @return
     */
    public void setGridAltitude(int x, int z, double h) {
        myAltitude[x][z] = h;
    }

    /**
     * Get the altitude at an arbitrary point. 
     * Non-integer points should be interpolated from neighbouring grid points
     * 
     * TO BE COMPLETED
     *
     * Case 1 - if on pt, get altitude
     * Case 2 - if on triangle - interpolate using 2 points
     * Case 3 - if on face - interpolate using two interpolation
     * TODO: Verify if correct
     *
     *
     * 
     * @param x
     * @param z
     * @return
     */
    public double altitude(double x, double z) {
        double altitude = 0;

        double leftX = Math.floor(x);
        double rightX = Math.ceil(x);

        double topZ = Math.floor(z);
        double bottomZ = Math.ceil(z);

        // calculates the offset b in z = -x + b
        // to find x: x = -z + b
        double offset = leftX + bottomZ;
        double hypotenuseX = offset - z;

        if (x == (int) x && z == (int) z) {
            altitude = getGridAltitude((int) x,(int) z);
        } else if (x == (int) x || z == (int) z) {
            if (x == (int) x) {
                altitude = calcZAltitude(x, topZ, x, bottomZ, z);
            } else {
                //case 2
                //This one's inverted as the x is not fixed instead.
                altitude = calcXAltitude(leftX, z, rightX, z, x);
            }
        } else if (x == hypotenuseX) {
            //case 2
            //calculate pt. X on the triangle line
            altitude = calcZAltitude(leftX, bottomZ, rightX, topZ, z);
        } else if (x < hypotenuseX) {
            //left triangle
            //Take bottom left, top left, top right points
            altitude = bilinearInterp(leftX, bottomZ, leftX, topZ, rightX, topZ, hypotenuseX, x, z);
        } else {
            //right triangle
            //Take top right, bottom right, bottom left points
            altitude = bilinearInterp(rightX, topZ, rightX, bottomZ, leftX, bottomZ, hypotenuseX, x, z);
        }

        
        return altitude;
    }

    private double calcXAltitude(double x1, double z1, double x2, double z2, double x) {
        double altitude = ((x - x1)/(x2 - x1)) * getGridAltitude((int) x2, (int) z2) +
                ((x2 - x)/(x2 - x1)) * getGridAltitude((int) x1, (int) z1);

        return altitude;
    }

    private double calcZAltitude(double x1, double z1, double x2, double z2, double z) {
        double altitude = ((z - z1)/(z2 - z1)) * getGridAltitude((int) x2, (int) z2) +
                ((z2 - z)/(z2 - z1)) * getGridAltitude((int) x1, (int) z1);

        return altitude;
    }

    private double bilinearInterp(double x1, double z1, double x2, double z2, double x3, double z3, double hypotenuseX, double x, double z) {
        double gradient1 = calcZAltitude(x1, z1, x2, z2, z);
        double gradient2 = calcZAltitude(x1, z1, x3, z3, z);

        double altitude = ((x - x1)/(hypotenuseX - x1)) * gradient2 +
                ((hypotenuseX - x)/(hypotenuseX - x1)) * gradient1;

        return altitude;
    }

    /**
     * Add a tree at the specified (x,z) point. 
     * The tree's y coordinate is calculated from the altitude of the terrain at that point.
     * 
     * @param x
     * @param z
     */
    public void addTree(double x, double z) {
        double y = altitude(x, z);
        Tree tree = new Tree(x, y, z);
        myTrees.add(tree);
    }


    /**
     * Add a road. 
     * 
     * @param width
     * @param spine
     */
    public void addRoad(double width, double[] spine) {
        Road road = new Road(width, spine, this);
        myRoads.add(road);        
    }

    public void draw(GL2 gl, GLU glu, Texture groundTex, Texture trunkTex, Texture roadTex, Texture leavesTex){

        //draw terrain
        groundTex.enable(gl);
        groundTex.bind(gl);
        TextureCoords coords = groundTex.getImageTexCoords();
        for(int z = 0; z < mySize.height - 1; z++){
            for(int x = 0; x < mySize.width - 1; x++){

                double[] pt1 = {x, myAltitude[x][z], z};
                double[] pt2 = {x, myAltitude[x][z+1], z + 1};
                double[] pt3 = {x + 1, myAltitude[x+1][z], z};
                double[] normal = MathUtil.findNormalToPlane(pt1, pt2, pt3);
                gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_TRIANGLES);

                float[] diffuseCoeff = {1.0f, 1.0f, 1.0f, 1.0f};
                gl.glMaterialfv( GL2.GL_FRONT, GL2.GL_DIFFUSE, diffuseCoeff, 0);
                gl.glBegin(GL2.GL_POLYGON);
                gl.glNormal3d(normal[0], normal[1], normal[2]);
                {
                    gl.glTexCoord2f(coords.left(),coords.bottom());
                    gl.glVertex3d(x, myAltitude[x][z], z);
                    gl.glTexCoord2f(coords.right(),coords.bottom());
                    gl.glVertex3d(x, myAltitude[x][z + 1], z + 1);
                    gl.glTexCoord2f(coords.left(),coords.top());
                    gl.glVertex3d(x + 1, myAltitude[x + 1][z], z);
                }
                gl.glEnd();

                double[] nextPt1 = {x + 1, myAltitude[x + 1][z], z};
                double[] nextPt2 = {x, myAltitude[x][z + 1], z + 1};
                double[] nextPt3 = {x + 1, myAltitude[x + 1][z + 1], z + 1};
                double[] nextNormal = MathUtil.findNormalToPlane(nextPt1, nextPt2, nextPt3);
                gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_TRIANGLES);
                gl.glBegin(GL2.GL_POLYGON);
                gl.glNormal3d(nextNormal[0], nextNormal[1], nextNormal[2]);
                {
                    gl.glTexCoord2f(coords.left(),coords.top());
                    gl.glVertex3d(x + 1, myAltitude[x + 1][z], z);
                    gl.glTexCoord2f(coords.right(),coords.bottom());
                    gl.glVertex3d(x, myAltitude[x][z + 1], z + 1);
                    gl.glTexCoord2f(coords.right(),coords.top());
                    gl.glVertex3d(x + 1, myAltitude[x + 1][z + 1], z + 1);
                }
                gl.glEnd();
            }
            gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
        }

        //draw trees
        for(Tree tree : myTrees){
            gl.glLoadIdentity();
            tree.draw(gl,glu,trunkTex,leavesTex);
        }

        for(Road road: myRoads) {
            gl.glLoadIdentity();
            road.draw(gl,roadTex);
        }

    }


}
