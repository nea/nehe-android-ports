package com.nea.nehe.lesson16;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLSurfaceView.Renderer;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * This is a port of the {@link http://nehe.gamedev.net} OpenGL 
 * tutorials to the Android 1.5 OpenGL ES platform. Thanks to 
 * NeHe and all contributors for their great tutorials and great 
 * documentation. This source should be used together with the
 * textual explanations made at {@link http://nehe.gamedev.net}.
 * The code is based on the original Visual C++ code with all
 * comments made. It has been altered and extended to meet the
 * Android requirements. The Java code has according comments.
 * 
 * If you use this code or find it helpful, please visit and send
 * a shout to the author under {@link http://www.insanitydesign.com/}
 * 
 * @DISCLAIMER
 * This source and the whole package comes without warranty. It may or may
 * not harm your computer or cell phone. Please use with care. Any damage
 * cannot be related back to the author. The source has been tested on a
 * virtual environment and scanned for viruses and has passed all tests.
 * 
 * 
 * This is an interpretation of "Lesson 16: Cool Looking Fog"
 * for the Google Android platform.
 * 
 * @author INsanityDesign
 */
public class Lesson16 extends GLSurfaceView implements Renderer {
	
	/** Cube instance */
	private Cube cube;	
	
	/* Rotation values */
	private float xrot;					//X Rotation
	private float yrot;					//Y Rotation

	/* Rotation speed values */
	private float xspeed;				//X Rotation Speed
	private float yspeed;				//Y Rotation Speed
	
	private float z = -5.0f;			//Depth Into The Screen
	
	private int filter = 0;				//Which texture filter?
	
	/** Is light enabled */
	private boolean light = false;

	private int fogFilter = 0;			//Which Fog To Use ( NEW ) 
	/*
	 * Init the three fog filters we will use
	 * and the fog color ( NEW )
	 */
	private int fogMode[]= { 			
							GL10.GL_EXP, 
							GL10.GL_EXP2, 
							GL10.GL_LINEAR 
										};		
	private float[] fogColor = {0.5f, 0.5f, 0.5f, 1.0f};
	private FloatBuffer fogColorBuffer;	//The Fog Color Buffer  ( NEW )

	/* 
	 * The initial light values for ambient and diffuse
	 * as well as the light position 
	 */
	private float[] lightAmbient = {0.5f, 0.5f, 0.5f, 1.0f};
	private float[] lightDiffuse = {1.0f, 1.0f, 1.0f, 1.0f};
	private float[] lightPosition = {0.0f, 0.0f, 2.0f, 1.0f};
		
	/* The buffers for our light values */
	private FloatBuffer lightAmbientBuffer;
	private FloatBuffer lightDiffuseBuffer;
	private FloatBuffer lightPositionBuffer;
	
	/*
	 * These variables store the previous X and Y
	 * values as well as a fix touch scale factor.
	 * These are necessary for the rotation transformation
	 * added to this lesson, based on the screen touches.
	 */
	private float oldX;
    private float oldY;
	private final float TOUCH_SCALE = 0.2f;		//Proved to be good for normal rotation
	
	/** The Activity Context */
	private Context context;
	
	/**
	 * Instance the Cube object and set the Activity Context 
	 * handed over. Initiate the light and fog buffers and set 
	 * this class as renderer for this now GLSurfaceView.
	 * Request Focus and set if focusable in touch mode to
	 * receive the Input from Screen and Buttons  
	 * 
	 * @param context - The Activity Context
	 */
	public Lesson16(Context context) {
		super(context);
		
		//Set this as Renderer
		this.setRenderer(this);
		//Request focus, otherwise buttons won't react
		this.requestFocus();
		this.setFocusableInTouchMode(true);
		
		//
		this.context = context;		
		
		//
		ByteBuffer byteBuf = ByteBuffer.allocateDirect(lightAmbient.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		lightAmbientBuffer = byteBuf.asFloatBuffer();
		lightAmbientBuffer.put(lightAmbient);
		lightAmbientBuffer.position(0);
		
		byteBuf = ByteBuffer.allocateDirect(lightDiffuse.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		lightDiffuseBuffer = byteBuf.asFloatBuffer();
		lightDiffuseBuffer.put(lightDiffuse);
		lightDiffuseBuffer.position(0);
		
		byteBuf = ByteBuffer.allocateDirect(lightPosition.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		lightPositionBuffer = byteBuf.asFloatBuffer();
		lightPositionBuffer.put(lightPosition);
		lightPositionBuffer.position(0);
		
		//Build the new Buffer ( NEW )
		byteBuf = ByteBuffer.allocateDirect(fogColor.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		fogColorBuffer = byteBuf.asFloatBuffer();
		fogColorBuffer.put(fogColor);
		fogColorBuffer.position(0);
		
		//
		cube = new Cube();
	}

	/**
	 * The Surface is created/init()
	 */
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {		
		//And there'll be light!
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, lightAmbientBuffer);		//Setup The Ambient Light
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightDiffuseBuffer);		//Setup The Diffuse Light
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPositionBuffer);	//Position The Light
		gl.glEnable(GL10.GL_LIGHT0);											//Enable Light 0

		//Settings
		gl.glDisable(GL10.GL_DITHER);						//Disable dithering
		gl.glEnable(GL10.GL_TEXTURE_2D);					//Enable Texture Mapping
		gl.glShadeModel(GL10.GL_SMOOTH); 					//Enable Smooth Shading
		gl.glClearColor(0.5f, 0.5f, 0.5f, 1.0f); 			//We'll Clear To The Color Of The Fog ( Modified )
		gl.glClearDepthf(1.0f); 							//Depth Buffer Setup
		gl.glEnable(GL10.GL_DEPTH_TEST); 					//Enables Depth Testing
		gl.glDepthFunc(GL10.GL_LEQUAL); 					//The Type Of Depth Testing To Do
		
		//The Fog/The Mist
		gl.glFogf(GL10.GL_FOG_MODE, fogMode[fogFilter]);	//Fog Mode ( NEW )
		gl.glFogfv(GL10.GL_FOG_COLOR, fogColorBuffer);		//Set Fog Color ( NEW )
		gl.glFogf(GL10.GL_FOG_DENSITY, 0.35f);				//How Dense Will The Fog Be ( NEW )
		gl.glHint(GL10.GL_FOG_HINT, GL10.GL_DONT_CARE);		//Fog Hint Value ( NEW )
		gl.glFogf(GL10.GL_FOG_START, 1.0f);					//Fog Start Depth ( NEW )
		gl.glFogf(GL10.GL_FOG_END, 5.0f);					//Fog End Depth ( NEW )
		gl.glEnable(GL10.GL_FOG);							//Enables GL_FOG ( NEW )
		
		//Really Nice Perspective Calculations
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST); 
				
		//Load the texture for the cube once during Surface creation
		cube.loadGLTexture(gl, this.context);
	}

	/**
	 * Here we do our drawing
	 */
	public void onDrawFrame(GL10 gl) {
		//Clear Screen And Depth Buffer
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);	
		gl.glLoadIdentity();					//Reset The Current Modelview Matrix
		
		//Check if the light flag has been set to enable/disable lighting
		if(light) {
			gl.glEnable(GL10.GL_LIGHTING);
		} else {
			gl.glDisable(GL10.GL_LIGHTING);
		}
		
		//Set Fog Mode ( NEW )
		gl.glFogf(GL10.GL_FOG_MODE, fogMode[fogFilter]);
		
		//Drawing
		gl.glTranslatef(0.0f, 0.0f, z);			//Move z units into the screen
		gl.glScalef(0.8f, 0.8f, 0.8f); 			//Scale the Cube to 80 percent, otherwise it would be too large for the screen
		
		//Rotate around the axis based on the rotation matrix (rotation, x, y, z)
		gl.glRotatef(xrot, 1.0f, 0.0f, 0.0f);	//X
		gl.glRotatef(yrot, 0.0f, 1.0f, 0.0f);	//Y
				
		cube.draw(gl, filter);					//Draw the Cube	
		
		//Change rotation factors
		xrot += xspeed;
		yrot += yspeed;
	}		

	/**
	 * If the surface changes, reset the view
	 */
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		if(height == 0) { 						//Prevent A Divide By Zero By
			height = 1; 						//Making Height Equal One
		}

		gl.glViewport(0, 0, width, height); 	//Reset The Current Viewport
		gl.glMatrixMode(GL10.GL_PROJECTION); 	//Select The Projection Matrix
		gl.glLoadIdentity(); 					//Reset The Projection Matrix

		//Calculate The Aspect Ratio Of The Window
		GLU.gluPerspective(gl, 45.0f, (float)width / (float)height, 0.1f, 100.0f);

		gl.glMatrixMode(GL10.GL_MODELVIEW); 	//Select The Modelview Matrix
		gl.glLoadIdentity(); 					//Reset The Modelview Matrix
	}
	
/* ***** Listener Events ***** */	
	/**
	 * Override the key listener to receive keyUp events.
	 * 
	 * Check for the DPad presses left, right, up, down and middle.
	 * Change the rotation speed according to the presses
	 * or change the texture filter used through the middle press.
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		//
		if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			yspeed -= 0.1f;
			
		} else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			yspeed += 0.1f;
			
		} else if(keyCode == KeyEvent.KEYCODE_DPAD_UP) {
			xspeed -= 0.1f;
			
		} else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			xspeed += 0.1f;
			
		} else if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
			filter += 1;
			if(filter > 2) {
				filter = 0;
			}
		}

		//We handled the event
		return true;
	}
		
	/**
	 * Override the touch screen listener.
	 * 
	 * React to moves and presses on the touchscreen.
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//
		float x = event.getX();
        float y = event.getY();
        
        //If a touch is moved on the screen
        if(event.getAction() == MotionEvent.ACTION_MOVE) {
        	//Calculate the change
        	float dx = x - oldX;
	        float dy = y - oldY;
        	//Define an upper area of 10% on the screen
        	int upperArea = this.getHeight() / 10;
        	
        	//Zoom in/out if the touch move has been made in the upper
        	if(y < upperArea) {
        		z -= dx * TOUCH_SCALE / (this.getWidth() /16);
        	
        	//Rotate around the axis otherwise
        	} else {        		
    	        xrot += dy * TOUCH_SCALE;
    	        yrot += dx * TOUCH_SCALE;
        	}        
        
        //A press on the screen
        } else if(event.getAction() == MotionEvent.ACTION_UP) {
        	//Define an upper area of 10% to define a lower area
        	int upperArea = this.getHeight() / 10;
        	int lowerArea = this.getHeight() - upperArea;
        	
        	//
        	if(y > lowerArea) {
        		//Change the blend setting if the lower area left has been pressed 
        		if(x < (this.getWidth() / 2)) {
					fogFilter += 1; 	//Increase fogFilter By One ( NEW )
					
					//Is fogFilter Greater Than 2? ( NEW )
					if(fogFilter > 2) {
						fogFilter = 0; 	//If So, Set fogFilter To Zero back again ( NEW )
					}					
        			
        		//Change the light setting if the lower area right has been pressed 
        		} else {
        			if(light) {
            			light = false;
            		} else {
            			light = true;
            		}	
        		}
        	}
        }
        
        //Remember the values
        oldX = x;
        oldY = y;
        
        //We handled the event
		return true;
	}
}
