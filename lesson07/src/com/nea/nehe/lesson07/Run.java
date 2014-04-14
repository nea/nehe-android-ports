package com.nea.nehe.lesson07;

import android.app.Activity;
import android.os.Bundle;

/**
 * The initial Android Activity, setting and initiating
 * the OpenGL ES Renderer Class @see Lesson07.java
 * 
 * @author INsanityDesign
 */
public class Run extends Activity {

	/** Our own OpenGL View overridden */
	private Lesson07 lesson07;

	/**
	 * Initiate our @see Lesson07.java,
	 * which is GLSurfaceView and Renderer
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Initiate our Lesson with this Activity Context handed over
		lesson07 = new Lesson07(this);
		//Set the lesson as View to the Activity
		setContentView(lesson07);
	}

	/**
	 * Remember to resume our Lesson
	 */
	@Override
	protected void onResume() {
		super.onResume();
		lesson07.onResume();
	}

	/**
	 * Also pause our Lesson
	 */
	@Override
	protected void onPause() {
		super.onPause();
		lesson07.onPause();
	}

}