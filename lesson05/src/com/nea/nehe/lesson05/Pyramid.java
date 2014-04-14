package com.nea.nehe.lesson05;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * 
 * @author INsanityDesign
 */
public class Pyramid {
	
	/** The buffer holding the vertices */
	private FloatBuffer vertexBuffer;
	/** The buffer holding the color values */
	private FloatBuffer colorBuffer;
		
	/** The initial vertex definition */
	private float vertices[] = { 
					 	0.0f,  1.0f,  0.0f,		//Top Of Triangle (Front)
						-1.0f, -1.0f, 1.0f,		//Left Of Triangle (Front)
						 1.0f, -1.0f, 1.0f,		//Right Of Triangle (Front)
						 0.0f,  1.0f, 0.0f,		//Top Of Triangle (Right)
						 1.0f, -1.0f, 1.0f,		//Left Of Triangle (Right)
						 1.0f, -1.0f, -1.0f,	//Right Of Triangle (Right)
						 0.0f,  1.0f, 0.0f,		//Top Of Triangle (Back)
						 1.0f, -1.0f, -1.0f,	//Left Of Triangle (Back)
						-1.0f, -1.0f, -1.0f,	//Right Of Triangle (Back)
						 0.0f,  1.0f, 0.0f,		//Top Of Triangle (Left)
						-1.0f, -1.0f, -1.0f,	//Left Of Triangle (Left)
						-1.0f, -1.0f, 1.0f		//Right Of Triangle (Left)
											};
	/** The initial color definition */	
	private float colors[] = {
			    		1.0f, 0.0f, 0.0f, 1.0f, //Red
			    		0.0f, 1.0f, 0.0f, 1.0f, //Green
			    		0.0f, 0.0f, 1.0f, 1.0f, //Blue
			    		1.0f, 0.0f, 0.0f, 1.0f, //Red
			    		0.0f, 0.0f, 1.0f, 1.0f, //Blue
			    		0.0f, 1.0f, 0.0f, 1.0f, //Green
			    		1.0f, 0.0f, 0.0f, 1.0f, //Red
			    		0.0f, 1.0f, 0.0f, 1.0f, //Green
			    		0.0f, 0.0f, 1.0f, 1.0f, //Blue
			    		1.0f, 0.0f, 0.0f, 1.0f, //Red
			    		0.0f, 0.0f, 1.0f, 1.0f, //Blue
			    		0.0f, 1.0f, 0.0f, 1.0f 	//Green
						    					};

	
	/**
	 * The Pyramid constructor.
	 * 
	 * Initiate the buffers.
	 */
	public Pyramid() {
		//
		ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertices.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		vertexBuffer = byteBuf.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);
		
		//
		byteBuf = ByteBuffer.allocateDirect(colors.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		colorBuffer = byteBuf.asFloatBuffer();
		colorBuffer.put(colors);
		colorBuffer.position(0);
	}

	/**
	 * The object own drawing function.
	 * Called from the renderer to redraw this instance
	 * with possible changes in values.
	 * 
	 * @param gl - The GL Context
	 */
	public void draw(GL10 gl) {	
		//Set the face rotation
		gl.glFrontFace(GL10.GL_CW);
		
		//Point to our buffers
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuffer);
		
		//Enable the vertex and color state
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
		
		//Draw the vertices as triangles
		gl.glDrawArrays(GL10.GL_TRIANGLES, 0, vertices.length / 3);
		
		//Disable the client state before leaving
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
	}
}
