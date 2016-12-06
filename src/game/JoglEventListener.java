package game;


import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.Animator;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;




public class JoglEventListener extends GLCanvas implements GLEventListener, KeyListener, MouseListener, MouseMotionListener {
		
	/*
	 * Custom variables for mouse drag operations 
	 */

	private Texture floor_texture;
	private Texture skybox_texture;
	
	private static Animator animator;

    private GLU glu = new GLU();
    
    //declare court, basket, and ball objects
    Court court = null;
    Ball ball = null;
	
	private float camera_angle_X = 0;
	private float camera_angle_Y = 0.25f;
	private float camera_X = 0;
	private float camera_Y = 0;
	private float camera_Z = 0;
	private float camera_lookat_X = 0;
	private float camera_lookat_Y = 0;
	private float camera_lookat_Z = 0;
	
	private float mouseX0;
	private float mouseY0;
	
	private float dragX;
	private float dragY;
	

	float windowWidth, windowHeight;
	
	public JoglEventListener(int width, int height, GLCapabilities capabilities) {
		super(capabilities);
		setSize(width, height);
		addGLEventListener(this);
	}

	public void reshape(GLAutoDrawable glDrawable, int x, int y, int width, int height) {
		final GL2 gl2 = glDrawable.getGL().getGL2();
		this.windowWidth = width;
		this.windowHeight = height;
		gl2.glViewport(0, 0, width, height);
	}
	
	
	public void displayChanged(GLAutoDrawable gLDrawable, boolean modeChanged, boolean deviceChanged) {
		
	}

	/** Called by the drawable immediately after the OpenGL context is
	 * initialized for the first time. Can be used to perform one-time OpenGL
	 * initialization such as setup of lights and display lists.
	 * @param gLDrawable The GLAutoDrawable object.
	 */
	public void init(GLAutoDrawable gLDrawable) {
		final GL2 gl2 = gLDrawable.getGL().getGL2();
		gl2.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);    // Black Background
		gl2.glClearDepth(1.0f);                      // Depth Buffer Setup
		
		try {
			floor_texture = TextureIO.newTexture(new File("court_floor.jpg"), false);
			skybox_texture = TextureIO.newTexture(new File("skybox_texture.jpg"), false);
		} catch (GLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//Add event listeners for interactive functionality
		this.addKeyListener(this);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		
		court = new Court();
		ball = new Ball();

		//Start animator
    	animator = new Animator(this);
    	animator.start();
	}

	public void drawCourt(GL2 gl2){
		gl2.glTranslatef(0, 0, 0);
		court.draw(gl2);
		gl2.glTranslatef(camera_X, camera_Y, camera_Z);
	}
	
	public void drawBall(GL2 gl2){
		//getting there...
		gl2.glTranslated((camera_lookat_X / (camera_X + 1)) + 1, (camera_lookat_Y / (camera_Y + 1)) + 1, (camera_lookat_Z  / (camera_Z + 1)) + 1);
		//System.out.println((camera_lookat_X / (camera_X + 1)) + 1);
		//System.out.println(camera_lookat_Z / camera_Z + 1);
		ball.draw(gl2,glu);
		gl2.glTranslatef(camera_X, camera_Y, camera_Z);
	}
	
	
    @Override
	public void display(GLAutoDrawable gLDrawable) {
		// TODO Auto-generated method stub
		final GL2 gl2 = gLDrawable.getGL().getGL2();
        
		////////////3D Rendering
		gl2.glEnable(GL.GL_DEPTH_TEST);              // Enables Depth Testing
		gl2.glDepthFunc(GL.GL_LEQUAL);               // The Type Of Depth Testing To Do
		gl2.glMatrixMode(GL2.GL_PROJECTION);
	    gl2.glLoadIdentity();
	    // camera perspective setup
	    float widthHeightRatio = (float) getWidth() / (float) getHeight();
	    glu.gluPerspective(45, widthHeightRatio, 1,1000);
	    camera_lookat_X = (float) (camera_X + (Math.cos(camera_angle_X)));
		camera_lookat_Y = (float) (camera_Y - 3.0 + (Math.sin(camera_angle_Y)));
		camera_lookat_Z = (float) (camera_Z + (Math.sin(camera_angle_X)));
		//System.out.println(camera_lookat_X);
		//System.out.println(camera_lookat_Y);
	    glu.gluLookAt(camera_X,camera_Y - 3.0,camera_Z,
	    		camera_lookat_X, 
	    		camera_lookat_Y, 
	    		camera_lookat_Z,
	    		0,1,0);
		gl2.glPushMatrix();

	    gl2.glMatrixMode(GL2.GL_MODELVIEW);
	    gl2.glLoadIdentity();
		gl2.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		gl2.glPushMatrix();
		
		drawCourt(gl2);
		drawBall(gl2);
        
        gl2.glPopMatrix();
        
        
        /////////////2D Rendering
        gl2.glMatrixMode(GL2.GL_PROJECTION);
        gl2.glLoadIdentity();

        glu.gluOrtho2D(0.0f, windowWidth, windowHeight, 0.0f);

        gl2.glMatrixMode(GL2.GL_MODELVIEW);
        gl2.glLoadIdentity();
        gl2.glTranslatef(0,0, 0.0f);

        gl2.glDisable(GL.GL_DEPTH_TEST);
        
        gl2.glPushMatrix();
        
        drawHUD(gl2);
        
        gl2.glPopMatrix();
	}
	
	public void drawHUD(final GL2 gl2){
		//this should ideally show the score and some directions
		gl2.glBegin(GL2.GL_QUADS);
		gl2.glColor3f(1, 0, 0);
		gl2.glVertex2f(.1f,.1f);
		gl2.glVertex2f(.2f,.1f);
		gl2.glVertex2f(.2f,.2f);
		gl2.glVertex2f(.1f,.2f);
		gl2.glEnd();
	}
	
	
	//control lateral (X-Z) movement through environment
	@Override
	public void keyTyped(KeyEvent e) {
		char key = e.getKeyChar();
		
		switch(key){
		case 'w':
			camera_X += .3f * Math.sin((Math.PI/2) - camera_angle_X);
			camera_Z += .3f * Math.cos((Math.PI/2) - camera_angle_X);
			break;
		case 'a':
			camera_X -= .3f * Math.cos((Math.PI/2) + camera_angle_X);
			camera_Z -= .3f * Math.sin((Math.PI/2) - camera_angle_X);
			break;
		case 's':
			camera_X -= .3f * Math.sin((Math.PI/2) - camera_angle_X);
			camera_Z -= .3f * Math.cos((Math.PI/2) - camera_angle_X);
			break;
		case 'd':
			camera_X += .3f * Math.cos((Math.PI/2) + camera_angle_X);
			camera_Z += .3f * Math.sin((Math.PI/2) - camera_angle_X);
			break;
		}
	}
	
	//determine location of click
	@Override
	public void mousePressed(MouseEvent e) {
		mouseX0 = (e.getX()-800*0.5f)*40/800;
		mouseY0 = -(e.getY()-800*0.5f)*40/800;
		dragX = mouseX0;
		dragY = mouseY0;
	}
	
	//change view direction based on the mouse position relative to the place
	//it was first clicked.
	@Override
	public void mouseDragged(MouseEvent e) {
		float XX = (e.getX()-800*0.5f)*40/800;
		float YY = -(e.getY()-800*0.5f)*40/800;
		boolean directionX;
		boolean directionY;
		
		float dragChangeX = Math.abs(dragX - XX);
		float dragChangeY = Math.abs(dragY - YY);
		
		if (dragX <= XX){
			directionX = true;
		}
		else{
			directionX = false;
		}
		if (dragY <= YY){
			directionY = true;
		}
		else{
			directionY = false;
		}
		
		dragX = XX;
		dragY = YY;

		if (directionX){
			camera_angle_X += dragChangeX / 5;
		}
		else if(!directionX){
			camera_angle_X -= dragChangeX / 5;
		}
		//if we're changing the Y-direction, keep the camera pointed in the same direction if
		//we're already looking straight up or down
		if (directionY && (camera_angle_Y < (Math.PI / 2.0f))){
			camera_angle_Y += dragChangeY / 5;
		}
		else if (!directionY && (camera_angle_Y > (-1 * Math.PI / 2.0f))){
			camera_angle_Y -= dragChangeY / 5;
		}
		
		//reset X angle in case we rotate all the way around
		if (camera_angle_X < -2 * Math.PI) {
			camera_angle_X = (float) (-1 * ((-1 * camera_angle_X) % 2 * Math.PI));
		}
		else if (camera_angle_X > 2 * Math.PI) {
			camera_angle_X %= 2 * Math.PI;
		}
	}
			
	@Override
	public void dispose(GLAutoDrawable arg0) {
	
	}
	
	@Override
	public void mouseReleased(MouseEvent arg0) {
	}
	
	@Override
	public void mouseMoved(MouseEvent arg0) {
		
	}
	@Override
	public void mouseClicked(MouseEvent arg0) {
		
	}
	@Override
	public void mouseEntered(MouseEvent arg0) {
		
	}
	@Override
	public void mouseExited(MouseEvent arg0) {
		
	}
	@Override
	public void keyReleased(KeyEvent e) {
		
	}
	@Override
	public void keyPressed(KeyEvent e) {
		
	}
}



