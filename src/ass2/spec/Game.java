package ass2.spec;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.media.opengl.*;
import javax.media.opengl.awt.GLJPanel;
import javax.media.opengl.glu.GLU;
import javax.swing.JFrame;
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