package ass2.spec   ;

/**
 * A collection of useful math methods
 * Update some of the methods for 3D
 * Added CrossProduct, and Normalise vector
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
     * Now in 3D!, needs 4x4 matrices.
     *
     * @param p A 4x4 matrix
     * @param q A 4x4 matrix
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
     * Now in 3D!, now needs 4x4matrix.
     *
     * @param m A 4x4 matrix
     * @param v A 4x1 vector
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
     * Now in 3D!
     * @param scale
     * @return 3D scale matrix
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

    /**
     * For 3D - use for Normal and tangent to get side vector
     * @param v1 - vector1
     * @param v2 - vector2
     * @return the cross product
     */
    public static double[] crossProduct(double[] v1, double[] v2){
        double[] result = { v1[1] * v2[2] - v1[2] * v2[1],
                v1[2] * v2[0] - v1[0] * v2[2],
                v1[0] * v2[1] - v1[1] * v2[0]};
        return result;
    }

    /**
     * For 3D - Turns the vector into magnitude 1
     * @param vector
     * @return normalised vector
     */
    public static double[] normaliseVector(double[] vector){
        double[] result = new double[4];
        double magnitude = Math.sqrt(vector[0] * vector[0]
                + vector[1] * vector[1]
                + vector[2] * vector[2]);
        result[0] = vector[0]/magnitude;
        result[1] = vector[1]/magnitude;
        result[2] = vector[2]/magnitude;
        result[3] = 1;
        return result;
    }



}
