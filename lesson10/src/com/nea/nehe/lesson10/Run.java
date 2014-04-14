package com.nea.nehe.lesson10;

import android.app.Activity;
import android.os.Bundle;

/**
 * The initial Android Activity, setting and initiating
 * the OpenGL ES Renderer Class @see Lesson10.java
 * 
 * @author INsanityDesign
 */
public class Run extends Activity {

	/** Our own OpenGL View overridden */
	private Lesson10 lesson10;

	/**
	 * Initiate our @see Lesson10.java,
	 * which is GLSurfaceView and Renderer
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Initiate our Lesson with this Activity Context handed over
		lesson10 = new Lesson10(this);
		//Set the lesson as View to the Activity
		setContentView(lesson10);
	}

	/**
	 * Remember to resume our Lesson
	 */
	@Override
	protected void onResume() {
		super.onResume();
		lesson10.onResume();
	}

	/**
	 * Also pause our Lesson
	 */
	@Override
	protected void onPause() {
		super.onPause();
		lesson10.onPause();
	}

}