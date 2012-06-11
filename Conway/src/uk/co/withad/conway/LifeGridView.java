package uk.co.withad.conway;

import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import static uk.co.withad.conway.Constants.*;


public class LifeGridView extends View {
	
	private boolean[][] lifeGrid = null;
	private Random random = new Random();
	
	private Handler tickHandler = new Handler();
	private final int tickTime = 20;
	
	private boolean isPlaying = true;
	private boolean wraparound = true;
	private boolean drawGrid = true;
	
	private int cellSize = 10;
	
	private int gridWidth = 10;
	private int gridHeight = 10;
	
	
	// Constructor
	public LifeGridView(Context context, AttributeSet atts) {
		super(context, atts);
		
		setBackgroundColor(Color.WHITE);
	}
	
	
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if(lifeGrid == null) return;
		
		Paint cellPaint = new Paint();
		cellPaint.setStyle(Style.FILL);
		cellPaint.setColor(Color.BLACK);
		
		int gridWidth = lifeGrid.length;
		int gridHeight = lifeGrid[0].length;
		
		for (int x = 0; x < gridWidth; x++) {
			for (int y = 0; y < gridHeight; y++) {
				if(lifeGrid[x][y])
					canvas.drawRect(x*cellSize, y*cellSize, x*cellSize + cellSize, y*cellSize + cellSize, cellPaint);
			}
		}
		
		
		Paint gridPaint = new Paint();
		gridPaint.setColor(Color.LTGRAY);
		
		if(drawGrid) {
			for (int x = 0; x < getWidth(); x+=cellSize) {
				canvas.drawLine(x, 0, x, getHeight(), gridPaint);
			}
			
			for (int y = 0; y < getHeight(); y+=cellSize) {
				canvas.drawLine(0, y, getWidth(), y, gridPaint);
			}
		}
	}
	
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		
		tickHandler.removeCallbacks(tick);
		
		gridWidth = (int)(Math.floor(w/cellSize));
		gridHeight = (int)(Math.floor(h/cellSize));
		
		Log.d(TAG, "Width = " + gridWidth + ", Height = " + gridHeight);
		
		lifeGrid = new boolean[gridWidth][gridHeight];
		newGrid();
	}
	
	
	public void newGrid() {
		if(lifeGrid == null) return;
		
		tickHandler.removeCallbacks(tick);
		
		fillLifeGrid();
		//fillLifeGridShape();
		
		if(isPlaying)
			tickHandler.postDelayed(tick, tickTime);
		
		invalidate();
	}


	private Runnable tick = new Runnable() {
		
		public void run() {
			updateLifeGrid();
			tickHandler.postDelayed(tick, tickTime);
		}
	};
	
	
	private void fillLifeGrid() {		
		int choice;
		
		for (int x = 0; x < gridWidth; x++) {
			for (int y = 0; y < gridHeight; y++) {
				choice = random.nextInt(2);
				
				if (choice == 1)
					lifeGrid[x][y] = true;
				else if (choice == 0)
					lifeGrid[x][y] = false;
			}
		}
	}
	
	
	private void fillLifeGridShape() {
		for (int x = 0; x < gridWidth; x++) {
			for (int y = 0; y < gridHeight; y++) {
					lifeGrid[x][y] = false;
			}
		}
		
		
		int xstart = gridWidth/2;
		int ystart = 10;
		
		lifeGrid[xstart][ystart] = true;
		lifeGrid[xstart][ystart + 1] = true;
		lifeGrid[xstart][ystart + 2] = true;
		lifeGrid[xstart][ystart + 3] = true;
		lifeGrid[xstart][ystart + 4] = true;
		lifeGrid[xstart][ystart + 5] = true;
		lifeGrid[xstart][ystart + 6] = true;
		lifeGrid[xstart][ystart + 7] = true;
		
		lifeGrid[xstart][ystart + 9] = true;
		lifeGrid[xstart][ystart + 10] = true;
		lifeGrid[xstart][ystart + 11] = true;
		lifeGrid[xstart][ystart + 12] = true;
		lifeGrid[xstart][ystart + 13] = true;
		
		lifeGrid[xstart][ystart + 17] = true;
		lifeGrid[xstart][ystart + 18] = true;
		lifeGrid[xstart][ystart + 19] = true;
		
		lifeGrid[xstart][ystart + 26] = true;
		lifeGrid[xstart][ystart + 27] = true;
		lifeGrid[xstart][ystart + 28] = true;
		lifeGrid[xstart][ystart + 29] = true;
		lifeGrid[xstart][ystart + 30] = true;
		lifeGrid[xstart][ystart + 31] = true;
		lifeGrid[xstart][ystart + 32] = true;
		
		lifeGrid[xstart][ystart + 34] = true;
		lifeGrid[xstart][ystart + 35] = true;
		lifeGrid[xstart][ystart + 36] = true;
		lifeGrid[xstart][ystart + 37] = true;
		lifeGrid[xstart][ystart + 38] = true;
		
	}
	
	
	private void updateLifeGrid() {		
		boolean[][] newGrid = new boolean[gridWidth][gridHeight];
		
		for (int x = 0; x < gridWidth; x++) {
			for (int y = 0; y < gridHeight; y++) {
				newGrid[x][y] = updateCell(x, y);
			}
		}
		
		lifeGrid = newGrid;
		invalidate();
	}


	private boolean updateCell(int x, int y) {
		
		int leftColumn = x-1;
		int rightColumn = x+1;
		int topRow = y-1;
		int bottomRow = y+1;
		
		if(wraparound) {
			if(leftColumn < 0) leftColumn = gridWidth-1;
			if(rightColumn >= gridWidth) rightColumn = 0;
			if(topRow < 0) topRow = gridHeight-1;
			if(bottomRow >= gridHeight) bottomRow = 0;
		}
		else {
			if(rightColumn >= gridWidth) rightColumn = -1;
			if(bottomRow >= gridHeight) bottomRow = -1;
		}
		
		
		boolean left, topLeft, top, topRight, right,
			bottomRight, bottom, bottomLeft;
		
		topLeft = getCellValue(leftColumn, topRow);	
		left = getCellValue(leftColumn,y);
		bottomLeft = getCellValue(leftColumn,bottomRow);
		
		top = getCellValue(x,topRow);
		bottom = getCellValue(x,bottomRow);
		
		topRight = getCellValue(rightColumn,topRow);
		right = getCellValue(rightColumn,y);
		bottomRight = getCellValue(rightColumn,bottomRow);
		
		/*topLeft = lifeGrid[leftColumn][topRow];
		left = lifeGrid[leftColumn][y];
		bottomLeft = lifeGrid[leftColumn][bottomRow];
		
		top = lifeGrid[x][topRow];
		bottom = lifeGrid[x][bottomRow];
		
		topRight = lifeGrid[rightColumn][topRow];
		right = lifeGrid[rightColumn][y];
		bottomRight = lifeGrid[rightColumn][bottomRow];*/
		
		
		boolean[] neighbours = {topLeft, left, bottomLeft, top, bottom, topRight, right, bottomRight};
		
		int noOfNeighbours = 0;
		for (boolean b : neighbours) {
			if(b) noOfNeighbours++;
		}
		
		
		boolean currentlyAlive = lifeGrid[x][y];
		boolean aliveNow = false;
		
		if(currentlyAlive && (noOfNeighbours == 2 || noOfNeighbours == 3)) {
			aliveNow = true;
		}
		else if(!currentlyAlive && noOfNeighbours == 3) {
			aliveNow = true;
		}
		
		//Log.d(TAG, "Updating cell (" + x + "," + y + ") - " + "Currently " + (currentlyAlive ? "alive" : "dead") + 
		//			", " + noOfNeighbours + " neighbours - " + (aliveNow ? "lives" : "dies"));
		
		return aliveNow;
	}

	
	private boolean getCellValue(int x, int y) {
		
		if(x < 0 || y < 0) 
			return false;
		else 
			return lifeGrid[x][y];
	}


	public void pauseGrid() {
		if(isPlaying) {
			tickHandler.removeCallbacks(tick);
			isPlaying = false;
		}
		else {
			tickHandler.postDelayed(tick, tickTime);
			isPlaying = true;
		}
	}
	
	
	
}
