package uk.co.withad.conway;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import static uk.co.withad.conway.Constants.*;

public class ConwayActivity extends SherlockActivity implements OnTouchListener {
	
	boolean playing = true;
	LifeGridView gridView;
	
	/*float prevX = -1;
	float prevY = -1;*/
	float[] prevXs = new float[5];
	float[] prevYs = new float[5];
	
	int maxFingers = 5;
	
	boolean paint = true;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        gridView = (LifeGridView)findViewById(R.id.lifegridview);
        gridView.setOnTouchListener(this);
        gridView.parentActivity = this;
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getSupportMenuInflater();
    	inflater.inflate(R.menu.mainmenu, menu);

		return super.onCreateOptionsMenu(menu);
    }
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	
    	case R.id.reset:
    		gridView.newGrid();
    		gridView.actionBarHeight = -1;
    		return true;
    		
    	case R.id.pause:
    		gridView.pauseGrid();
    		
    		if(playing)
    			item.setTitle("Play");
    		else
    			item.setTitle("Pause");
    		
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


	@Override
	public boolean onTouch(View v, MotionEvent evt) {
		
		int pointerCount = evt.getPointerCount();
		
		switch (evt.getAction() & MotionEvent.ACTION_MASK) {
		
		case MotionEvent.ACTION_DOWN:
			for (int i = 0; i < pointerCount; i++) {
				prevXs[i] = evt.getX(i);
				prevYs[i] = evt.getY(i);
				
				if(paint) {
					gridView.setSingleCellByCoord(prevXs[i], prevYs[i]);
				}
			}
			
			break;
			
		case MotionEvent.ACTION_POINTER_DOWN:
			
			for (int i = 0; i < pointerCount; i++) {
				prevXs[i] = evt.getX(i);
				prevYs[i] = evt.getY(i);
				
				if(paint) {
					gridView.setSingleCellByCoord(prevXs[i], prevYs[i]);
				}
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
			
			if(paint) {
				for(int i = 0; i < evt.getPointerCount(); i++) {
					newX = evt.getX(i);
					newY = evt.getY(i);
					gridView.setCellByCoord(prevXs[i], prevYs[i], newX, newY);
					prevXs[i] = newX;
					prevYs[i] = newY;
				}
				
			}
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
}