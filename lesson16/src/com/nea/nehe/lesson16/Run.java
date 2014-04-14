package com.nea.nehe.lesson16;

import android.app.Activity;
import android.os.Bundle;

/**
 * The initial Android Activity, setting and initiating
 * the OpenGL ES Renderer Class @see Lesson16.java
 * 
 * @author INsanityDesign
 */
public class Run extends Activity {

	/** Our own OpenGL View overridden */
	private Lesson16 lesson16;

	/**
	 * Initiate our @see Lesson16.java,
	 * which is GLSurfaceView and Renderer
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Initiate our Lesson with this Activity Context handed over
		lesson16 = new Lesson16(this);
		//Set the lesson as View to the Activity
		setContentView(lesson16);
	}

	/**
	 * Remember to resume our Lesson
	 */
	@Override
	protected void onResume() {
		super.onResume();
		lesson16.onResume();
	}

	/**
	 * Also pause our Lesson
	 */
	@Override
	protected void onPause() {
		super.onPause();
		lesson16.onPause();
	}

}