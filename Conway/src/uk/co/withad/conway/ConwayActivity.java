package uk.co.withad.conway;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;


public class ConwayActivity extends SherlockActivity {
	
	boolean playing = true;
	
	LifeGridView gridView;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        gridView = (LifeGridView)findViewById(R.id.lifegridview);
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
    		return true;
    		
    	case R.id.pause:
    		gridView.pauseGrid();
    		
    		if(playing)
    			item.setTitle("Play");
    		else
    			item.setTitle("Pause");
    		
    		playing = !playing;
    			
    		return true;
    	}
    	
    	return super.onOptionsItemSelected(item);
    }
}