package ass2.spec   ;

/**
 * A collection of useful math methods 
 *
 * TODO: The methods you need to complete are at the bottom of the class
 *
 * @author malcolmr
 */
public class MathUtil {

    /**
     * Normalise an angle to the range [-180, 180)
     *
     * @param angle
     * @return
     */
    static public double normaliseAngle(double angle) {
        return ((angle + 180.0) % 360.0 + 360.0) % 360.0 - 180.0;
    }

    /**
     * Clamp a value to the given range
     *
     * @param value
     * @param min
     * @param max
     * @return
     */

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Multiply two matrices
     *
     * @param p A 3x3 matrix
     * @param q A 3x3 matrix
     * @return
     */
    public static double[][] multiply(double[][] p, double[][] q) {

        double[][] m = new double[4][4];

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                m[i][j] = 0;
                for (int k = 0; k < 4; k++) {
                    m[i][j] += p[i][k] * q[k][j];
                }
            }
        }

        return m;
    }

    /**
     * Multiply a vector by a matrix
     *
     * @param m A 3x3 matrix
     * @param v A 3x1 vector
     * @return
     */
    public static double[] multiply(double[][] m, double[] v) {

        double[] u = new double[4];

        for (int i = 0; i < 4; i++) {
            u[i] = 0;
            for (int j = 0; j < 4; j++) {
                u[i] += m[i][j] * v[j];
            }
        }

        return u;
    }



    // ===========================================
    // COMPLETE THE METHODS BELOW
    // ===========================================


    /**
     * TODO: A 2D translation matrix for the given offset vector
     *
     * @param pos
     * @return
     */
    public static double[][] translationMatrix(double[] v) {

        double[][] matrix = new double[][] {
                { 1, 0, v[0]},
                { 0, 1, v[1]},
                { 0, 0, 1}
        };

        return matrix;
    }

    /**
     * TODO: A 2D rotation matrix for the given angle
     *
     * @param angle in degrees
     * @return
     */
    public static double[][] rotationMatrix(double angle) {

        double[][] matrix = new double[][] {
                { Math.cos(Math.toRadians(angle)), -Math.sin(Math.toRadians(angle)), 0},
                { Math.sin(Math.toRadians(angle)), Math.cos(Math.toRadians(angle)), 0},
                { 0, 0, 1}
        };

        return matrix;
    }

    /**
     * TODO: A 2D scale matrix that scales both axes by the same factor
     *
     * @param scale
     * @return
     */
    public static double[][] scaleMatrix(double scale) {

        double [][] matrix = new double[][] {
                { scale, 0, 0, 0},
                { 0, scale, 0, 0},
                { 0, 0, scale, 0},
                { 0, 0, 0, 1}
        };

        return matrix;
    }

    public static double[] crossProduct(double[] v1, double[] v2){
        double[] r = { v1[1] * v2[2] - v1[2] * v2[1],
                v1[2] * v2[0] - v1[0] * v2[2], v1[0] * v2[1] - v1[1] * v2[0] };
        return r;
    }

    public static double[] normal(double[] a, double[] b, double[] c) {
        double[] n = new double[3];
        double[] v1 = { a[0] - b[0], a[1] - b[1], a[2] - b[2] };
        double[] v2 = { a[0] - c[0], a[1] - c[1], a[2] - c[2]};
        n = MathUtil.crossProduct(v1,v2);

        return n;
    }

    public static double[] normaliseVector(double[] origin){
        double[] n = new double[3];
        double abs = Math.sqrt(origin[0] * origin[0] + origin[1] * origin[1]
                + origin[2] * origin[2]);
        n[0] = origin[0]/abs;
        n[1] = origin[1]/abs;
        n[2] = origin[2]/abs;
        return n;
    }



}
