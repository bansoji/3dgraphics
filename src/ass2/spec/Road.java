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

    /**
     * Draws the road.
     * @param gl
     */
    public void draw(GL2 gl,Texture texture) {

        /**
         * What to do here:
         * 1. Take a point on the bezier curve
         * 2. Take next point on curve
         * 3. Find tangent
         * 4. Cross-product the normal (the vertical vector), this gives you a side vector.
         * 5. Normalize the side vector so its magnitude is 1
         * 6. Times the side vector by width/2
         * 7. Add to point/Subtract point for left and right
         * 8. Do same for next point
         * 9. Draw dat quad
         *
         */
        double offset = 0.001; //make sure the road is showing
        double increment = 0.01; //accuracy measure
        double[] startPoint = point(0);
        double[] vectorNormal = {0, 1, 0, 1}; //Always vertical, since it's a flat road
        double altitude = myTerrain.altitude(startPoint[0], startPoint[1]) + offset;

        texture.enable(gl);
        texture.bind(gl);
        TextureCoords coords = texture.getImageTexCoords();

        for (double t = 0.0; t < 0.99; t += increment) {
            //Step 1
            double[] currentPoint = point(t);
            double x = currentPoint[0];
            double z = currentPoint[1];
            double[] midPoint = {x, altitude, z};

            //Step 2
            double nextT = t + increment;
            double[] nextPoint = point(nextT);
            double x1 = nextPoint[0];
            double z1 = nextPoint[1];
            double[] nextMidPoint = {x1, altitude, z1};

            //Step 3
            double[] tangent = {x1 - x, 0, z1 - z, 1}; // is a vector, for matrix calc

            //Step 4
            double[] sideVector = MathUtil.crossProduct(vectorNormal, tangent);

            //Step 5
            double[] normalisedVector = MathUtil.normaliseVector(sideVector);

            //Step 6
            double[] translationVector = MathUtil.multiply(MathUtil.scaleMatrix(myWidth / 2), normalisedVector);


            //Step 7 and 8
            double[] currentRightPoint = {translationVector[0] + midPoint[0],
                    translationVector[1] + midPoint[1],
                    translationVector[2] + midPoint[2]};
            double[] currentLeftPoint = {-translationVector[0] + midPoint[0],
                    -translationVector[1] + midPoint[1],
                    -translationVector[2] + midPoint[2]};

            double[] nextRightPoint = {translationVector[0] + nextMidPoint[0],
                    translationVector[1] + nextMidPoint[1],
                    translationVector[2] + nextMidPoint[2]};
            double[] nextLeftPoint = {-translationVector[0] + nextMidPoint[0],
                    -translationVector[1] + nextMidPoint[1],
                    -translationVector[2] + nextMidPoint[2]};

            //Step 9
            gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_QUADS);
            gl.glBegin(GL2.GL_QUADS);
            gl.glNormal3d(0, 1, 0);
            gl.glTexCoord2f(coords.left(),coords.bottom());
            gl.glVertex3d(currentRightPoint[0], currentRightPoint[1], currentRightPoint[2]);
            gl.glTexCoord2f(coords.right(),coords.bottom());
            gl.glVertex3d(nextRightPoint[0], nextRightPoint[1], nextRightPoint[2]);
            gl.glTexCoord2f(coords.right(),coords.top());
            gl.glVertex3d(nextLeftPoint[0], nextLeftPoint[1], nextLeftPoint[2]);
            gl.glTexCoord2f(coords.left(),coords.top());
            gl.glVertex3d(currentLeftPoint[0], currentLeftPoint[1], currentLeftPoint[2]);
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
