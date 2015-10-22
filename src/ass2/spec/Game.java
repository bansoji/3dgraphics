package ass2.spec;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.media.opengl.*;
import javax.media.opengl.awt.GLJPanel;
import javax.media.opengl.glu.GLU;
import javax.swing.JFrame;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
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

    //shader code
    private float positions[] =  {0,1,-1,
            -1,-1,-1,
            1,-1,-1,
            0, 2,-4,
            -2,-2,-4,
            2,-2,-4};

    //There should be a matching entry in this array for each entry in
    //the positions array
    private float colors[] =     {1,0,0,
            0,1,0,
            1,1,1,
            0,0,0,
            0,0,1,
            1,1,0};

    //Best to use smallest data type possible for indexes
    //We could even use byte here...
    private short indexes[] = {0,1,5,3,4,2};

    //These are not vertex buffer objects, they are just java containers
    private FloatBuffer posData= Buffers.newDirectFloatBuffer(positions);
    private FloatBuffer colorData = Buffers.newDirectFloatBuffer(colors);
    private ShortBuffer indexData = Buffers.newDirectShortBuffer(indexes);

    //We will be using 2 vertex buffer objects
    private int bufferIds[] = new int[2];


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

        //load texture
        //TextureData data = TextureIO.newTextureData(glp,"FILE NAME",false, "bmp");
 
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
		// TODO Auto-generated method stub
        GL2 gl = drawable.getGL().getGL2();
        glu = new GLU();

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

        GLUT glut = new GLUT();

        setCamera(gl, glu);

        myTerrain.draw(gl,glu,grass,bark,gravel,leaves);

        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT1);
        gl.glEnable(GL2.GL_NORMALIZE);
        gl.glPushMatrix();

        float[] globalAmb =
                {1, 1, 1, 1.0f};
        gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, globalAmb, 0);

        float[] d = new float[] {1.0f, 1.0f, 1.0f, 1.0f};
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, d, 0);
        float[] diffusePos = new float[3];
        diffusePos[0] = myTerrain.getSunlight()[0];
        diffusePos[1] = myTerrain.getSunlight()[1];
        diffusePos[2] = myTerrain.getSunlight()[2];
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, diffusePos, 0);
        gl.glPopMatrix();

        //draws avatar according to camera position
        if(thirdperson) {
            gl.glPushMatrix();
            gl.glTranslated(cameraPos[0], myTerrain.altitude(cameraPos[0], cameraPos[1]) + 0.1, cameraPos[1]);
            gl.glRotated(-cameraRot, 0, 1, 0);
            glut.glutSolidTeapot(0.1);
            gl.glPopMatrix();
        }

        /*// set the view matrix based on the camera position
        myCamera.setView(gl);

        // update the mouse position
        Mouse.theMouse.update(gl);

        // update the objects
        update();

        // draw the scene tree
        GameObject.ROOT.draw(gl);*/

        //shader code
        //Use the shader
        gl.glUseProgram(shaderprogram);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER,bufferIds[0]);

        int vertexColLoc = gl.glGetAttribLocation(shaderprogram,"vertexCol");
        int vertexPosLoc = gl.glGetAttribLocation(shaderprogram,"vertexPos");

        // Specify locations for the co-ordinates and color arrays.
        gl.glEnableVertexAttribArray(vertexPosLoc);
        gl.glEnableVertexAttribArray(vertexColLoc);
        gl.glVertexAttribPointer(vertexPosLoc,3, GL.GL_FLOAT, false,0, 0); //last num is the offset
        gl.glVertexAttribPointer(vertexColLoc,3, GL.GL_FLOAT, false,0, positions.length*Float.BYTES);


        gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, bufferIds[1]);


        gl.glDrawElements(GL2.GL_TRIANGLES, 6, GL2.GL_UNSIGNED_SHORT,0);


        gl.glUseProgram(0);

        //Un-bind the buffer.
        //This is not needed in this simple example but good practice
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER,0);
        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER,0);
    }

	@Override
	public void dispose(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub
        GL2 gl = drawable.getGL().getGL2();
        glu = new GLU();
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glClearColor(0.2f,0.8f,1,0);

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
        gl.glGenerateMipmap(GL2.GL_TEXTURE_2D);

        //shader code
        //Generate 2 VBO buffer and get their IDs
        gl.glGenBuffers(2,bufferIds,0);

        //This buffer is now the current array buffer
        //array buffers hold vertex attribute data
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER,bufferIds[0]);

        //This is just setting aside enough empty space
        //for all our data
        gl.glBufferData(GL2.GL_ARRAY_BUFFER,    //Type of buffer
                positions.length * Float.BYTES +  colors.length* Float.BYTES, //size needed
                null,    //We are not actually loading data here yet
                GL2.GL_STATIC_DRAW); //We expect once we load this data we will not modify it


        //Actually load the positions data
        gl.glBufferSubData(GL2.GL_ARRAY_BUFFER, 0, //From byte offset 0
                positions.length*Float.BYTES,posData);

        //Actually load the color data
        gl.glBufferSubData(GL2.GL_ARRAY_BUFFER,
                positions.length*Float.BYTES,  //Load after the position data
                colors.length*Float.BYTES,colorData);


        //Now for the element array
        //Element arrays hold indexes to an array buffer
        gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, bufferIds[1]);

        //We can load it all at once this time since there are not
        //two separate parts like there was with color and position.
        gl.glBufferData(GL2.GL_ELEMENT_ARRAY_BUFFER,
                indexes.length *Short.BYTES,
                indexData, GL2.GL_STATIC_DRAW);

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

        //Perspective camera
    }


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
}