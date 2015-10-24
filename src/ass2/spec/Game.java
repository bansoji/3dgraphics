package ass2.spec;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import javax.media.opengl.*;
import javax.media.opengl.awt.GLJPanel;
import javax.media.opengl.glu.GLU;
import javax.swing.JFrame;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;


/**
 * @author malcolmr
 */
public class Game extends JFrame implements GLEventListener, KeyListener{

    private Terrain myTerrain;
    private GLU glu;
    private double[] cameraPos;
    private double cameraRot;
    private double moveDistance;

    private Texture grass;
    private Texture bark;
    private Texture gravel;
    private Texture leaves;

    private boolean thirdperson = false;
    private int iteration = 3;
    private boolean useFractal = false;
    private boolean nightMode = false;


    private FloatBuffer sphereVertexBuffer; // Holds the vertex coords, for use with glDrawArrays
    private FloatBuffer sphereNormalBuffer; // Holds the normal vectors, for use with glDrawArrays
    private FloatBuffer sphereTextureBuffer;

    private float[] sphereVertexArray;  // The same data as in sphereVertexBuffer, stored in an array.
    private float[] sphereNormalArray;  // The same data as in sphereNormalBuffer, stored in an array.
    private float[] sphereTextureArray;

    private int vertexVboId;   // identifier for the Vertex Buffer Object to hold the vertex coords
    private int normalVboId;   // identifier for the Vertex Buffer Object to hold the normla vectors
    private int textureId;

//    private int bufferIds[] = new int[2];
    private static final String VERTEX_SHADER = "AttributeVertex.glsl";
    private static final String FRAGMENT_SHADER = "AttributeFragment.glsl";
    private int shaderprogram;


    public Game(Terrain terrain) {
    	super("Assignment 2");
        myTerrain = terrain;
        cameraPos = new double[]{0,0};
        cameraRot = 45;
        moveDistance = 0.1;
   
    }
    
    /** 
     * Run the game.
     *
     */
    public void run() {
        GLProfile glp = GLProfile.getDefault();
        GLCapabilities caps = new GLCapabilities(glp);
        GLJPanel panel = new GLJPanel();
        panel.addGLEventListener(this);
        panel.addKeyListener(this);
        panel.setFocusable(true);

        // Add an animator to call 'display' at 60fps
        FPSAnimator animator = new FPSAnimator(60);
        animator.add(panel);
        animator.start();

        getContentPane().add(panel);
        setSize(800, 600);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
    
    /**
     * Load a level file and display it.
     * 
     * @param args - The first argument is a level file in JSON format
     * @throws java.io.FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {
        Terrain terrain = LevelIO.load(new File(args[0]));
        Game game = new Game(terrain);
        game.run();
    }

	@Override
	public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        glu = new GLU();
        GLUT glut = new GLUT();

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

        setCamera(gl, glu);
        setLighting(gl);

        myTerrain.draw(gl,glu,grass,bark,gravel,leaves,useFractal,iteration);

        //draws avatar according to camera position
        if(thirdperson) {
            gl.glPushMatrix();
            gl.glTranslated(cameraPos[0], myTerrain.altitude(cameraPos[0], cameraPos[1]) + 0.1, cameraPos[1]);
            gl.glRotated(-cameraRot, 0, 1, 0);
            glut.glutSolidTeapot(0.1);
            gl.glPopMatrix();
        }

        // VBO + shader

        // We need to enable the vertex and normal arrays, and set
        // the vertex and normal points for these modes.
//        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
//        gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
//        gl.glEnableClientState(GL2.GL_TEXTURE_COORD_ARRAY);

        gl.glUseProgram(shaderprogram);
        // When using VBOs, the vertex and normal pointers
        // refer to data in the VBOs.
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexVboId);
//        gl.glVertexPointer(3,GL.GL_FLOAT,0,0);
        int vertexPosLoc = gl.glGetAttribLocation(shaderprogram,"vertexPos");
        gl.glEnableVertexAttribArray(vertexPosLoc);
        gl.glVertexAttribPointer(vertexPosLoc, 3, GL.GL_FLOAT, false, 0, 0); //last num is the offset

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, normalVboId);
//        gl.glNormalPointer(GL.GL_FLOAT,0,0);
        int vertexNormLoc = gl.glGetAttribLocation(shaderprogram,"vertexNorm");
        gl.glEnableVertexAttribArray(vertexNormLoc);
        gl.glVertexAttribPointer(vertexNormLoc, 3, GL.GL_FLOAT, false, 0, 0); //last num is the offset

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, textureId);
//        gl.glTexCoordPointer(2,GL.GL_FLOAT,0,0);
        int vertexTexLoc = gl.glGetAttribLocation(shaderprogram, "texCoords");
        gl.glEnableVertexAttribArray(vertexTexLoc);
        gl.glVertexAttribPointer(vertexTexLoc, 2, GL.GL_FLOAT, false, 0,0);

        int textureLoc = gl.glGetUniformLocation(shaderprogram,"texture");
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glBindTexture(GL.GL_TEXTURE_2D,bark.getTextureObject());
        gl.glUniform1i(textureLoc,0);


        gl.glPushMatrix();
        gl.glTranslated(0.5, 0.5, 0.5);
        gl.glScaled(0.1, 0.1, 0.1);

        drawSphereWithDrawArrays(gl);
        gl.glPopMatrix();

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
        gl.glUseProgram(0);
        gl.glBindTexture(GL.GL_TEXTURE_2D,0);

        gl.glDisableClientState(GL2.GL_TEXTURE_COORD_ARRAY);
        gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);

    }

	@Override
	public void dispose(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        glu = new GLU();
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT1);
        gl.glEnable(GL2.GL_NORMALIZE);
        gl.glShadeModel(GL2.GL_SMOOTH);
        gl.glClearColor(0.2f,0.8f,1,0); //background color

        //init textures
        gl.glEnable(GL2.GL_TEXTURE_2D);
        try {
            grass = TextureIO.newTexture(getClass().getClassLoader().getResource("grass.png"),false,"png");
            bark = TextureIO.newTexture(getClass().getClassLoader().getResource("bark.png"),false,"png");
            gravel = TextureIO.newTexture(getClass().getClassLoader().getResource("road.png"),false,"png");
            leaves = TextureIO.newTexture(getClass().getClassLoader().getResource("leaves.png"),false,"png");
        } catch (IOException e){
            e.printStackTrace();
        }
        //set filters
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR);
        gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
        gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
        gl.glGenerateMipmap(GL2.GL_TEXTURE_2D);

        //init vbo + shaders
        createSphereArraysAndVBOs(gl);

        try {
            shaderprogram = Shader.initShaders(gl,VERTEX_SHADER,FRAGMENT_SHADER);

        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		// TODO Auto-generated method stub
        GL2 gl = drawable.getGL().getGL2();
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

    }

    //------------------------------------ HELPER METHODS --------------------------------------//
    private void setCamera(GL2 gl, GLU glu) {
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();

        //Perspective Camera
        float aspectRatio = (float) (800.0 / 600.0);
        glu.gluPerspective(90, aspectRatio, 0.1, 1000);

        if(!thirdperson) {
            double altitudeOffset = 0.5;
            glu.gluLookAt(
                    cameraPos[0],
                    myTerrain.altitude(cameraPos[0], cameraPos[1]) + altitudeOffset,
                    cameraPos[1],
                    cameraPos[0] + Math.cos(Math.toRadians(cameraRot)),
                    myTerrain.altitude(cameraPos[0], cameraPos[1]) + altitudeOffset,
                    cameraPos[1] + Math.sin(Math.toRadians(cameraRot)),
                    0,
                    1,
                    0
            );
        } else {
            double altitudeOffset = 0.5;
            glu.gluLookAt(
                    cameraPos[0] - Math.cos(Math.toRadians(cameraRot)),
                    myTerrain.altitude(cameraPos[0], cameraPos[1]) + altitudeOffset,
                    cameraPos[1] - Math.sin(Math.toRadians(cameraRot)),
                    cameraPos[0] + Math.cos(Math.toRadians(cameraRot)),
                    myTerrain.altitude(cameraPos[0], cameraPos[1]) + altitudeOffset,
                    cameraPos[1] + Math.sin(Math.toRadians(cameraRot)),
                    0,
                    1,
                    0
            );
        }

        // Change back to model view
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    private void setLighting(GL2 gl){
        gl.glPushMatrix();
        float[] globalAmb = {1, 1, 1, 1.0f};
        gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, globalAmb, 0);

        if (nightMode) {
            gl.glDisable(GL2.GL_LIGHT1);
        } else {
            gl.glEnable(GL2.GL_LIGHT1);
        }

        float[] d = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, d, 0);
        float[] diffusePos = new float[3];
        diffusePos[0] = myTerrain.getSunlight()[0];
        diffusePos[1] = myTerrain.getSunlight()[1];
        diffusePos[2] = myTerrain.getSunlight()[2];
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, diffusePos, 0);

        gl.glPopMatrix();

    }


    //------------------------------------KEYLISINTER STUFF --------------------------------------//
    @Override
    public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()){
            case KeyEvent.VK_UP:
                double newCameraX = cameraPos[0] + moveDistance*Math.cos(Math.toRadians(cameraRot));
                double newCameraZ = cameraPos[1] + moveDistance*Math.sin(Math.toRadians(cameraRot));
                if(newCameraX > myTerrain.size().getWidth() - 1 || newCameraZ > myTerrain.size().getHeight() - 1||
                        newCameraX < 0 || newCameraZ < 0){
                    break;
                } else {
                    cameraPos[0] = newCameraX;
                    cameraPos[1] = newCameraZ;
                }
                break;
            case KeyEvent.VK_DOWN :
                double newCameraXR = cameraPos[0] - moveDistance*Math.cos(Math.toRadians(cameraRot));
                double newCameraZR = cameraPos[1] - moveDistance*Math.sin(Math.toRadians(cameraRot));
                if(newCameraXR > myTerrain.size().getWidth() - 1 || newCameraZR > myTerrain.size().getHeight() - 1 ||
                        newCameraXR < 0 || newCameraZR < 0){
                    break;
                } else {
                    cameraPos[0] = newCameraXR;
                    cameraPos[1] = newCameraZR;
                }
                break;
            case KeyEvent.VK_LEFT:
                cameraRot -= 10;
                break;
            case KeyEvent.VK_RIGHT:
                cameraRot += 10;
                break;
            case KeyEvent.VK_SPACE:
                thirdperson = !thirdperson;
                break;
            case KeyEvent.VK_K:
                iteration++;
                break;
            case KeyEvent.VK_J:
                iteration--;
                break;
            case KeyEvent.VK_L:
                useFractal = !useFractal;
                break;
            case KeyEvent.VK_N:
                nightMode = !nightMode;
                break;

            default:
                break;

        }
    }

    @Override
    public void keyReleased(KeyEvent e){

    }

    @Override
    public void keyTyped(KeyEvent e){

    }


    //------------------------------------BORROWED SPHERE GENERATION --------------------------------------//
    /**
     * Draw one sphere.  The VertexPointer and NormalPointer must already
     * be set to point to the data for the sphere, and they must be enabled.
     */
    private void drawSphereWithDrawArrays(GL2 gl) {
        int slices = 32;
        int stacks = 16;
        int vertices = (slices+1)*2;
        for (int i = 0; i < stacks; i++) {
            int pos = i*(slices+1)*2;
            gl.glDrawArrays(GL2.GL_QUAD_STRIP, pos, vertices);
        }
    }

    private void createSphereArraysAndVBOs(GL2 gl) {
        double radius = 0.4;
        int stacks = 16;
        int slices = 32;
        int size = stacks * (slices+1) * 2 * 3;
        sphereVertexBuffer = GLBuffers.newDirectFloatBuffer(size);
        sphereNormalBuffer = GLBuffers.newDirectFloatBuffer(size);
        sphereTextureBuffer = GLBuffers.newDirectFloatBuffer(size);
        sphereVertexArray = new float[size];
        sphereNormalArray = new float[size];
        sphereTextureArray = new float[size];
        for (int j = 0; j < stacks; j++) {
            double latitude1 = (Math.PI/stacks) * j - Math.PI/2;
            double latitude2 = (Math.PI/stacks) * (j+1) - Math.PI/2;
            double sinLat1 = Math.sin(latitude1);
            double cosLat1 = Math.cos(latitude1);
            double sinLat2 = Math.sin(latitude2);
            double cosLat2 = Math.cos(latitude2);
            for (int i = 0; i <= slices; i++) {
                double longitude = (2*Math.PI/slices) * i;
                double sinLong = Math.sin(longitude);
                double cosLong = Math.cos(longitude);
                double x1 = cosLong * cosLat1;
                double y1 = sinLong * cosLat1;
                double z1 = sinLat1;
                double x2 = cosLong * cosLat2;
                double y2 = sinLong * cosLat2;
                double z2 = sinLat2;
                sphereNormalBuffer.put( (float)x2 );
                sphereNormalBuffer.put( (float)y2 );
                sphereNormalBuffer.put( (float)z2 );
                sphereVertexBuffer.put( (float)(radius*x2) );
                sphereVertexBuffer.put( (float)(radius*y2) );
                sphereVertexBuffer.put( (float)(radius*z2) );
                sphereTextureBuffer.put((float)(1/slices * i));
                sphereTextureBuffer.put((float)(1/stacks * (j+1)));
                sphereNormalBuffer.put( (float)x1 );
                sphereNormalBuffer.put( (float)y1 );
                sphereNormalBuffer.put( (float)z1 );
                sphereVertexBuffer.put( (float)(radius*x1) );
                sphereVertexBuffer.put( (float)(radius*y1) );
                sphereVertexBuffer.put( (float)(radius*z1) );
                sphereTextureBuffer.put((float)(1/slices * i));
                sphereTextureBuffer.put((float)(1/stacks * j));
            }
        }
        for (int i = 0; i < size; i++) {
            sphereVertexArray[i] = sphereVertexBuffer.get(i);
            sphereNormalArray[i] = sphereNormalBuffer.get(i);
        }
        for (int i = 0; i < size*2/3; i++){
            sphereTextureArray[i] = sphereTextureBuffer.get(i);
        }
        sphereVertexBuffer.rewind();
        sphereNormalBuffer.rewind();
        sphereTextureBuffer.rewind();
        int[] bufferIDs = new int[3];
        gl.glGenBuffers(3, bufferIDs,0);
        vertexVboId = bufferIDs[0];
        normalVboId = bufferIDs[1];
        textureId = bufferIDs[2];
        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, vertexVboId);
        gl.glBufferData(GL2.GL_ARRAY_BUFFER, size*4, sphereVertexBuffer, GL2.GL_STATIC_DRAW);
        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, normalVboId);
        gl.glBufferData(GL2.GL_ARRAY_BUFFER, size*4, sphereNormalBuffer, GL2.GL_STATIC_DRAW);
        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER,textureId);
        gl.glBufferData(GL2.GL_ARRAY_BUFFER, size*4, sphereTextureBuffer, GL2.GL_STATIC_DRAW);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
    }

}