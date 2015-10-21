package ass2.spec;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureCoords;

import javax.media.opengl.GL2;
import java.util.ArrayList;
import java.util.List;

/**
 * COMMENT: Comment Road 
 *
 * @author malcolmr
 */
public class Road {

    private List<Double> myPoints;
    private double myWidth;
    private Terrain myTerrain;


    public void draw(GL2 gl) {

        gl.glColor3d(110 / 255d, 110 / 255d, 110 / 255d);

        double offset = 0.001; //make sure the road is showing
        double increment = 0.01; //accuracy measure
        double[] startPoint = point(0);
        double altitude = myTerrain.altitude(startPoint[0], startPoint[1]);

        for (double t = 0.0; t < 0.99; t += increment) {
            double[] currentPoint = point(t);
            double x = currentPoint[0];
            double z = currentPoint[1];
            double[] midpoint = {x, altitude, z};

            double nextT = t + increment;
            double[] nextPoint = point(nextT);
            double x1 = nextPoint[0];
            double z1 = nextPoint[1];

            double[] tangent = {x1 - x, 0, z1 - z, 1}; // is a vector, for matrix calc

            double[] scaleRotateTangent = new double[4]; //Create a matrix that will correctly scale and rotate vectors


            double[] pointA = {Math.floor(x0), myTerrain.altitude(Math.floor(x0), Math.floor(z0)), Math.floor(z0)};
            double[] pointB = {Math.floor(x0), myTerrain.altitude(Math.floor(x0), Math.ceil(z0)), Math.ceil(z0)};
            double[] pointC = {Math.ceil(x0), myTerrain.altitude(Math.ceil(x0), Math.ceil(z0)), Math.ceil(z0)};
            double[] planeNormal = MathUtil.normal(pointA, pointB, pointC);
            double[] rotateTangent = MathUtil.crossProduct(planeNormal, tangent);
            double[] normalisedVector = MathUtil.normaliseVector(rotateTangent);
            double[] nv4cal = {normalisedVector[0], normalisedVector[1], normalisedVector[2], 1};
            scaleRotateTangent = MathUtil.multiply(
                    MathUtil.scaleMatrix(myWidth / 2),
                    nv4cal);


            double[] rightbottom = {scaleRotateTangent[0] + midpoint[0],
                    scaleRotateTangent[1] + midpoint[1],
                    scaleRotateTangent[2] + midpoint[2]};
            double[] leftbottom = {-scaleRotateTangent[0] + midpoint[0],
                    -scaleRotateTangent[1] + midpoint[1],
                    -scaleRotateTangent[2] + midpoint[2]};
            double[] lefttop = {-scaleRotateTangent[0] + x1,
                    -scaleRotateTangent[1] + y1, -scaleRotateTangent[2] + z1};
            double[] righttop = {scaleRotateTangent[0] + x1,
                    scaleRotateTangent[1] + y1, scaleRotateTangent[2] + z1};
            double[] normal = MathUtil
                    .normal(leftbottom, rightbottom, righttop);
            gl.glBegin(GL2.GL_QUADS);
            gl.glNormal3d(-normal[0], -normal[1], -normal[2]);

            gl.glVertex3d(rightbottom[0], rightbottom[1], rightbottom[2]);// --

            gl.glVertex3d(righttop[0], righttop[1], righttop[2]); // --

            gl.glVertex3d(lefttop[0], lefttop[1], lefttop[2]);// --

            gl.glVertex3d(leftbottom[0], leftbottom[1], leftbottom[2]);// --
            gl.glEnd();
        }
    }

    /** 
     * Create a new road starting at the specified point
     */
    public Road(double width, double x0, double y0) {
        myWidth = width;
        myPoints = new ArrayList<Double>();
        myPoints.add(x0);
        myPoints.add(y0);
    }

    /**
     * Create a new road with the specified spine 
     *
     * @param width
     * @param spine
     */
    public Road(double width, double[] spine, Terrain terrain) {
        myTerrain = terrain;
        myWidth = width;
        myPoints = new ArrayList<Double>();
        for (int i = 0; i < spine.length; i++) {
            myPoints.add(spine[i]);
        }
    }

    /**
     * The width of the road.
     * 
     * @return
     */
    public double width() {
        return myWidth;
    }

    /**
     * Add a new segment of road, beginning at the last point added and ending at (x3, y3).
     * (x1, y1) and (x2, y2) are interpolated as bezier control points.
     * 
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param x3
     * @param y3
     */
    public void addSegment(double x1, double y1, double x2, double y2, double x3, double y3) {
        myPoints.add(x1);
        myPoints.add(y1);
        myPoints.add(x2);
        myPoints.add(y2);
        myPoints.add(x3);
        myPoints.add(y3);        
    }
    
    /**
     * Get the number of segments in the curve
     * 
     * @return
     */
    public int size() {
        return myPoints.size() / 6;
    }

    /**
     * Get the specified control point.
     * 
     * @param i
     * @return
     */
    public double[] controlPoint(int i) {
        double[] p = new double[2];
        p[0] = myPoints.get(i*2);
        p[1] = myPoints.get(i*2+1);
        return p;
    }
    
    /**
     * Get a point on the spine. The parameter t may vary from 0 to size().
     * Points on the kth segment take have parameters in the range (k, k+1).
     * 
     * @param t
     * @return
     */
    public double[] point(double t) {
        int i = (int)Math.floor(t);
        t = t - i;

        i *= 6;

        double x0 = myPoints.get(i++);
        double y0 = myPoints.get(i++);
        double x1 = myPoints.get(i++);
        double y1 = myPoints.get(i++);
        double x2 = myPoints.get(i++);
        double y2 = myPoints.get(i++);
        double x3 = myPoints.get(i++);
        double y3 = myPoints.get(i++);

        double[] p = new double[2];

        p[0] = b(0, t) * x0 + b(1, t) * x1 + b(2, t) * x2 + b(3, t) * x3;
        p[1] = b(0, t) * y0 + b(1, t) * y1 + b(2, t) * y2 + b(3, t) * y3;

        return p;
    }
    
    /**
     * Calculate the Bezier coefficients
     * 
     * @param i
     * @param t
     * @return
     */
    private double b(int i, double t) {
        
        switch(i) {
        
        case 0:
            return (1-t) * (1-t) * (1-t);

        case 1:
            return 3 * (1-t) * (1-t) * t;
            
        case 2:
            return 3 * (1-t) * t * t;

        case 3:
            return t * t * t;
        }
        
        // this should never happen
        throw new IllegalArgumentException("" + i);
    }


}
