package uk.co.withad.conway;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class ConwayActivity extends SherlockActivity implements OnTouchListener, OnScaleGestureListener {
	
	boolean playing = true;
	LifeGridView gridView;
	
	float[] prevXs = new float[5];
	float[] prevYs = new float[5];
	
	int maxFingers = 5;
	
	boolean paint = true;
	
	private ScaleGestureDetector scaleDetector;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);
        
        gridView = (LifeGridView)findViewById(R.id.lifegridview);
        gridView.setOnTouchListener(this);
        gridView.parentActivity = this;
        
        scaleDetector = new ScaleGestureDetector(this, this);
    }
    
    
    /** Create ActionBar */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getSupportMenuInflater();
    	inflater.inflate(R.menu.mainmenu, menu);

		return super.onCreateOptionsMenu(menu);
    }
    
    
    /** Handle ActionBar options */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	
    	case R.id.reset:
    		gridView.reset();
    		return true;
    		
    	case R.id.pause:
    		gridView.pauseGrid();
    		
    		if(playing) {
    			item.setTitle("Play");
    			item.setIcon(R.drawable.ic_media_play);
    		}
    		else {
    			item.setTitle("Pause");
    			item.setIcon(R.drawable.ic_media_pause);
    		}
    		
    		playing = !playing;
    			
    		return true;
    	
    	case R.id.clear:
    		gridView.clearGrid();
    		return true;
    		
    	case R.id.paintOrMove:
    		paint = !paint;
    		return true;
    	}
    	
    	return super.onOptionsItemSelected(item);
    }


    /** Handle touch input */
	@Override
	public boolean onTouch(View v, MotionEvent evt) {
		if(!paint) scaleDetector.onTouchEvent(evt);
		
		if(scaleDetector.isInProgress()) return true;
		
		int pointerCount = evt.getPointerCount();
		
		switch (evt.getAction() & MotionEvent.ACTION_MASK) {
		
		case MotionEvent.ACTION_DOWN:
			for (int i = 0; i < pointerCount; i++) {
				prevXs[i] = evt.getX(i);
				prevYs[i] = evt.getY(i);
				
				// If in "paint" mode, paint the touched cell
				if(paint)
					gridView.setSingleCellByCoord(prevXs[i], prevYs[i]);
			}
			
			break;
			
		case MotionEvent.ACTION_POINTER_DOWN:
			
			for (int i = 0; i < pointerCount; i++) {
				prevXs[i] = evt.getX(i);
				prevYs[i] = evt.getY(i);
				
				// If in "paint" mode, paint the touched cell
				if(paint) 
					gridView.setSingleCellByCoord(prevXs[i], prevYs[i]);
			}
			break;
			
			
		case MotionEvent.ACTION_POINTER_UP:
			int pointer = evt.getActionIndex();
			for (int i = pointer; i < prevXs.length-1; i++) {
				prevXs[i] = prevXs[i+1];
				prevYs[i] = prevYs[i+1];
			}
			break;
			
		
		case MotionEvent.ACTION_MOVE:
			float newX = evt.getX();
			float newY = evt.getY();
			
			// If in "paint" mode, fill in the cells dragged across
			if(paint) {
				for(int i = 0; i < evt.getPointerCount(); i++) {
					newX = evt.getX(i);
					newY = evt.getY(i);
					gridView.setCellsByCoord(prevXs[i], prevYs[i], newX, newY);
					prevXs[i] = newX;
					prevYs[i] = newY;
				}
			}
			// If in "move" mode, move the grid
			else {
				gridView.translateX += newX - prevXs[0];
				gridView.translateY += newY - prevYs[0];
				gridView.invalidate();
				prevXs[0] = newX;
				prevYs[0] = newY;
			}
			
			break;
		}
		
		return true;
	}


	/** Scale the grid proportional to pinch gesture, if 
	 * in "movement" mode. Won't be triggered if in "paint". */
	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		gridView.scale *= detector.getScaleFactor();
		gridView.invalidate();
		return true;
	}
	
	
	/** Required for the OnScaleGestureListener */
	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		return true;
	}

	
	/** Required for the OnScaleGestureListener */
	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {
	}
}