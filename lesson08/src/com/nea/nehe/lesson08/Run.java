package com.nea.nehe.lesson08;

import android.app.Activity;
import android.os.Bundle;

/**
 * The initial Android Activity, setting and initiating
 * the OpenGL ES Renderer Class @see Lesson08.java
 * 
 * @author INsanityDesign
 */
public class Run extends Activity {

	/** Our own OpenGL View overridden */
	private Lesson08 lesson08;

	/**
	 * Initiate our @see Lesson08.java,
	 * which is GLSurfaceView and Renderer
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Initiate our Lesson with this Activity Context handed over
		lesson08 = new Lesson08(this);
		//Set the lesson as View to the Activity
		setContentView(lesson08);
	}

	/**
	 * Remember to resume our Lesson
	 */
	@Override
	protected void onResume() {
		super.onResume();
		lesson08.onResume();
	}

	/**
	 * Also pause our Lesson
	 */
	@Override
	protected void onPause() {
		super.onPause();
		lesson08.onPause();
	}

}