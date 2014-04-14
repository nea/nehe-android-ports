package com.nea.nehe.lesson10;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;

/**
 * This class is our World representation and loads
 * the world from the textual representation, and 
 * draws the World according to the read.
 * 
 * @author INsanityDesign
 */
public class World implements OnKeyListener, OnTouchListener {
	
	/** Our texture pointer */
	private int[] textures = new int[3];
	
	/** The Activity Context */
	private Context context;
	
	/** The World sector */
	private Sector sector1;
	
	/*
	 * The following values are new values, used
	 * to navigate the world and heading
	 */
	private final float piover180 = 0.0174532925f;
	private float heading;
	private float xpos;
	private float zpos;
	private float yrot;	 				//Y Rotation
	private float walkbias = 0;
	private float walkbiasangle = 0;
	private float lookupdown = 0.0f;
	
	/* Variables and factor for the input handler */
	private float oldX;
    private float oldY;
	private final float TOUCH_SCALE = 0.2f;			//Proved to be good for normal rotation
	
	/** The buffer holding the vertices */
	private FloatBuffer vertexBuffer;
	/** The buffer holding the texture coordinates */
	private FloatBuffer textureBuffer;
	
	/** The initial vertex definition */
	private float[] vertices;
	
	/** The texture coordinates (u, v) */	
	private float[] texture;
	
	/**
	 * The World constructor
	 * 
	 * @param context - The Activity Context
	 */
	public World(Context context) {
		this.context = context;
	}
	
	/**
	 * This method is a representation of the original SetupWorld(). 
	 * Here, we load the world into the structure from the
	 * original world.txt file, but in a Java way.
	 * 
	 * Please note that this is not necessarily the way to got,
	 * and definitely not the nicest way to do the reading and
	 * loading from a file. But it is near to the original
	 * and a quick solution for this example. It does not check
	 * for anything NOT part of the original file and can easily 
	 * be corrupted by a changed file.
	 * 
	 * @param fileName - The file name to load from the Asset directory
	 */
	public void loadWorld(String fileName) {
		try {
			//Some temporary variables
			int numtriangles = 0;
			int counter = 0;
			sector1 = new Sector();
			
			List<String> lines = null;
			StringTokenizer tokenizer;
			
			//Quick Reader for the input file
			BufferedReader reader = new BufferedReader(new InputStreamReader(this.context.getAssets().open(fileName)));
			
			//Iterate over all lines
			String line = null;
			while((line = reader.readLine()) != null) {
				//Skip comments and empty lines
				if(line.startsWith("//") || line.trim().equals("")) {
					continue;
				}
				
				//Read how many polygons this file contains
				if(line.startsWith("NUMPOLLIES")) {
					numtriangles = Integer.valueOf(line.split(" ")[1]);					
					sector1.numtriangles = numtriangles;
					sector1.triangle = new Triangle[sector1.numtriangles];
				
				//Read every other line
				} else {
					if(lines == null) {
						lines = new ArrayList<String>();
					}
					
					lines.add(line);
				}
			}
			
			//Clean up!
			reader.close();
			
			//Now iterate over all read lines...
			for(int loop = 0; loop < numtriangles; loop++) {
				//...define triangles...
				Triangle triangle = new Triangle();
				
				//...and fill these triangles with the five read data 
				for(int vert = 0; vert < 3; vert++) {
					//
					line = lines.get(loop * 3 + vert);
					tokenizer = new StringTokenizer(line);
					
					//
					triangle.vertex[vert] = new Vertex();
					//
					triangle.vertex[vert].x = Float.valueOf(tokenizer.nextToken());
					triangle.vertex[vert].y = Float.valueOf(tokenizer.nextToken());
					triangle.vertex[vert].z = Float.valueOf(tokenizer.nextToken());
					triangle.vertex[vert].u = Float.valueOf(tokenizer.nextToken());
					triangle.vertex[vert].v = Float.valueOf(tokenizer.nextToken());
				}
				
				//Finally, add the triangle to the sector
				sector1.triangle[counter++] = triangle;
			}
			
		//If anything should happen, write a log and return
		} catch(Exception e) {
			Log.e("World", "Could not load the World file!", e);
			return;
		}
		
		/*
		 * Now, convert the original structure of the NeHe 
		 * lesson to our classic buffer structure. 
		 * Could/Should be done in one step above. Kept
		 * separated to stick near and clear to the original.
		 * 
		 * This is a quick and not recommended solution.
		 * Just made to quickly present the tutorial.
		 */
		vertices = new float[sector1.numtriangles * 3 * 3];
		texture = new float[sector1.numtriangles * 3 * 2];
		
		int vertCounter = 0;
		int texCounter = 0;
				
		//
		for(Triangle triangle : sector1.triangle) {
			//
			for(Vertex vertex : triangle.vertex) {
				//
				vertices[vertCounter++] = vertex.x;
				vertices[vertCounter++] = vertex.y;
				vertices[vertCounter++] = vertex.z;
				//
				texture[texCounter++] = vertex.u;
				texture[texCounter++] = vertex.v;
			}
		}		
		
		//Build the buffers
		ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		vertexBuffer = byteBuf.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);

		//
		byteBuf = ByteBuffer.allocateDirect(texture.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		textureBuffer = byteBuf.asFloatBuffer();
		textureBuffer.put(texture);
		textureBuffer.position(0);
	}
		
	/**
	 * The world drawing function.
	 * 
	 * @param gl - The GL Context
	 * @param filter - Which texture filter to use
	 */
	public void draw(GL10 gl, int filter) {
		//Bind the texture based on the given filter
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[filter]);

		float xtrans = -xpos;						//Used For Player Translation On The X Axis
		float ztrans = -zpos;						//Used For Player Translation On The Z Axis
		float ytrans = -walkbias - 0.25f;			//Used For Bouncing Motion Up And Down
		float sceneroty = 360.0f - yrot;			//360 Degree Angle For Player Direction
		
		//View
		gl.glRotatef(lookupdown, 1.0f, 0, 0);		//Rotate Up And Down To Look Up And Down
		gl.glRotatef(sceneroty, 0, 1.0f, 0);		//Rotate Depending On Direction Player Is Facing
		gl.glTranslatef(xtrans, ytrans, ztrans);	//Translate The Scene Based On Player Position
					
		//Enable the vertex, texture and normal state
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
				
		//Point to our buffers
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
		
		//Draw the vertices as triangles
		gl.glDrawArrays(GL10.GL_TRIANGLES, 0, vertices.length / 3);
		
		//Disable the client state before leaving
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	}
		
	/**
	 * Load the textures
	 * 
	 * @param gl - The GL Context
	 * @param context - The Activity context
	 */
	public void loadGLTexture(GL10 gl, Context context) {
		//Get the texture from the Android resource directory
		InputStream is = context.getResources().openRawResource(R.drawable.mud);
		Bitmap bitmap = null;
		try {
			//BitmapFactory is an Android graphics utility for images
			bitmap = BitmapFactory.decodeStream(is);

		} finally {
			//Always clear and close
			try {
				is.close();
				is = null;
			} catch (IOException e) {
			}
		}

		//Generate there texture pointer
		gl.glGenTextures(3, textures, 0);

		//Create Nearest Filtered Texture and bind it to texture 0
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

		//Create Linear Filtered Texture and bind it to texture 1
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[1]);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);

		//Create mipmapped textures and bind it to texture 2
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[2]);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR_MIPMAP_NEAREST);
		/*
		 * This is a change to the original tutorial, as buildMipMap does not exist anymore
		 * in the Android SDK.
		 * 
		 * We check if the GL context is version 1.1 and generate MipMaps by flag.
		 * Otherwise we call our own buildMipMap implementation
		 */
		if(gl instanceof GL11) {
			gl.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_GENERATE_MIPMAP, GL11.GL_TRUE);
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
			
		//
		} else {
			buildMipmap(gl, bitmap);
		}		
		
		//Clean up
		bitmap.recycle();
	}
	
	/**
	 * Our own MipMap generation implementation.
	 * Scale the original bitmap down, always by factor two,
	 * and set it as new mipmap level.
	 * 
	 * Thanks to Mike Miller (with minor changes)!
	 * 
	 * @param gl - The GL Context
	 * @param bitmap - The bitmap to mipmap
	 */
	private void buildMipmap(GL10 gl, Bitmap bitmap) {
		//
		int level = 0;
		//
		int height = bitmap.getHeight();
		int width = bitmap.getWidth();

		//
		while(height >= 1 || width >= 1) {
			//First of all, generate the texture from our bitmap and set it to the according level
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, level, bitmap, 0);
			
			//
			if(height == 1 || width == 1) {
				break;
			}

			//Increase the mipmap level
			level++;

			//
			height /= 2;
			width /= 2;
			Bitmap bitmap2 = Bitmap.createScaledBitmap(bitmap, width, height, true);
			
			//Clean up
			bitmap.recycle();
			bitmap = bitmap2;
		}
	}
	
/* ***** Structure classes for the "World" ***** */	
	/**
	 * A classic Vertex definition with
	 * texture coordinates.
	 */
	public class Vertex {
		//
		public float x, y, z;
		//
		public float u, v;
	}
	
	/**
	 * The Triangle class, containing
	 * all Vertices for the Triangle
	 */
	public class Triangle {
		//
		public Vertex[] vertex = new Vertex[3];
	}

	/**
	 * The Sector class holding the number and
	 * all Triangles.
	 */
	public class Sector {
		//
		public int numtriangles;
		//
		public Triangle[] triangle;
	}

/* ***** Listener Events ***** */	
	/**
	 * Override the key listener to receive onKey events.
	 *  
	 */
	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		//Handle key down events
		if(event.getAction() == KeyEvent.ACTION_DOWN) {
			return onKeyDown(keyCode, event);
		}
		
		return false;
	}
	
	/**
	 * Check for the DPad presses left, right, up and down.
	 * Walk in the according direction or rotate the "head".
	 * 
	 * @param keyCode - The key code
	 * @param event - The key event
	 * @return If the event has been handled
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//
		if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			heading += 1.0f;	
			yrot = heading;					//Rotate The Scene To The Left
			
		} else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			heading -= 1.0f;
			yrot = heading;					//Rotate The Scene To The Right
			
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
			xpos -= (float)Math.sin(heading * piover180) * 0.05f;	//Move On The X-Plane Based On Player Direction
			zpos -= (float)Math.cos(heading * piover180) * 0.05f;	//Move On The Z-Plane Based On Player Direction
			
			if(walkbiasangle >= 359.0f) {							//Is walkbiasangle>=359?
				walkbiasangle = 0.0f;								//Make walkbiasangle Equal 0
			} else {
				walkbiasangle += 10;								//If walkbiasangle < 359 Increase It By 10
			}
			walkbias = (float)Math.sin(walkbiasangle * piover180) / 20.0f;	//Causes The Player To Bounce

		} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			xpos += (float)Math.sin(heading * piover180) * 0.05f;	//Move On The X-Plane Based On Player Direction
			zpos += (float)Math.cos(heading * piover180) * 0.05f;	//Move On The Z-Plane Based On Player Direction
			
			if(walkbiasangle <= 1.0f) {								//Is walkbiasangle<=1?
				walkbiasangle = 359.0f;								//Make walkbiasangle Equal 359
			} else {
				walkbiasangle -= 10;								//If walkbiasangle > 1 Decrease It By 10
			}
			walkbias = (float)Math.sin(walkbiasangle * piover180) / 20.0f;	//Causes The Player To Bounce
		}

		//We handled the event
		return true;
	}

	/**
	 * React to moves on the touchscreen.
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		//
		boolean handled = false;
		
		//
		float x = event.getX();
        float y = event.getY();
        
        //If a touch is moved on the screen
        if(event.getAction() == MotionEvent.ACTION_MOVE) {
        	//Calculate the change
        	float dx = x - oldX;
	        float dy = y - oldY;
        	        		
	        //Up and down looking through touch
    	    lookupdown += dy * TOUCH_SCALE;
    	    //Look left and right through moving on screen
    	    heading += dx * TOUCH_SCALE;
    	    yrot = heading;

    	    //We handled the event
            handled = true;
        }
        
        //Remember the values
        oldX = x;
        oldY = y;
        
        //
		return handled;
	}
}
