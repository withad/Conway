package uk.co.withad.conway;

import static uk.co.withad.conway.Constants.ALIVE;
import static uk.co.withad.conway.Constants.DEAD;

import java.util.Random;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;


public class LifeGridView extends View {
	
	private int[][] lifeGrid = null;
	private Random random = new Random();
	
	private Handler tickHandler = new Handler();
	private final int tickTime = 20;
	
	private boolean isPlaying = true;
	private boolean wraparound = true;
	private boolean drawGrid = true;
	
	private int cellSize = 10;
	
	private int gridWidth = 10;
	private int gridHeight = 10;
	
	Matrix matrix = null;
	
	public ConwayActivity parentActivity;
	public int actionBarHeight = -1;
	
	float scale = 1f;
	float totalScale = 1f;
	
	public float translateX = 0;
	public float translateY = 0;
	
	Paint cellPaint;
	Paint fadePaint;
	Paint gridPaint;
	
	
	/** Constructor */
	public LifeGridView(Context context, AttributeSet atts) {
		super(context, atts);
		
		// Set cell colour
		cellPaint = new Paint();
		cellPaint.setStyle(Style.FILL);
		cellPaint.setColor(Color.BLACK);
		
		// Set "just died" cell colour
		fadePaint = new Paint();
		fadePaint.setStyle(Style.FILL);
		fadePaint.setColor(Color.GRAY);
		
		// Set colour of grid lines
		gridPaint = new Paint();
		gridPaint.setColor(Color.LTGRAY);
	
		// Set background colour of the grid
		setBackgroundColor(Color.WHITE);
	}
	
	
	/** Draw the screen, including the grid. */
	@Override
	public void onDraw(Canvas canvas) {
		
		// Get copy of the screen's matrix
		if(matrix == null) matrix = canvas.getMatrix();
		
		// Translate grid to compensate of the ActionBar
		if (actionBarHeight == -1) {
			actionBarHeight = parentActivity.getSupportActionBar().getHeight();
			matrix.postTranslate(0, (float) (actionBarHeight));
		}
		
		// Set matrix as the screen's matrix
		canvas.setMatrix(matrix);
		
		super.onDraw(canvas);
		
		// Scale grid
		matrix.postScale(scale, scale);
		totalScale *= scale;
		scale = 1f;
		
		// Move grid
		matrix.postTranslate(translateX, translateY);
		translateX = 0;
		translateY = 0;
		
		// Skip if the grid array doesn't exist yet
		if(lifeGrid == null) 
			return;		
		
		// Draw cells
		int lifeValue;
		for (int x = 0; x < gridWidth; x++) {
			for (int y = 0; y < gridHeight; y++) {
				lifeValue = lifeGrid[x][y];
				
				if(lifeValue == ALIVE)
					canvas.drawRect(x*cellSize, y*cellSize, x*cellSize + cellSize, y*cellSize + cellSize, cellPaint);
				else if(lifeValue > 0){
					canvas.drawRect(x*cellSize, y*cellSize, x*cellSize + cellSize, y*cellSize + cellSize, fadePaint);
				}
			}
		}

		// Draw grid
		if(drawGrid) {
			for (int x = 0; x < getWidth(); x+=cellSize) {
				canvas.drawLine(x, 0, x, getHeight(), gridPaint);
			}
			
			for (int y = 0; y < getHeight(); y+=cellSize) {
				canvas.drawLine(0, y, getWidth(), y, gridPaint);
			}
		}
	}
	
	
	/** Triggered when screen size changes.
	 * Used to update the height/width of the grid.
	 */
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		
		tickHandler.removeCallbacks(tick);
		
		gridWidth = (int)(Math.floor(w/cellSize));
		gridHeight = (int)(Math.floor(h/cellSize));
		
		newGrid();
	}
	
	
	/** Creates a new grid array based on the current gridWeight, gridHeight */
	public void newGrid() {
		if(lifeGrid == null) 
			lifeGrid = new int[gridWidth][gridHeight];
		
		// Can't allow the grid to update while refilling it
		tickHandler.removeCallbacks(tick);
		
		// Randomly fill the grid, 50% chance of being alive for each cell
		for (int x = 0; x < gridWidth; x++) {
			for (int y = 0; y < gridHeight; y++) {
				
				if (random.nextBoolean())
					lifeGrid[x][y] = ALIVE;
				else
					lifeGrid[x][y] = DEAD;
			}
		}
		
		if(isPlaying) 
			tickHandler.postDelayed(tick, tickTime);
		
		invalidate();
	}


	/** Runnable that updates the grid */
	private Runnable tick = new Runnable() {
		public void run() {
			updateLifeGrid();
			tickHandler.postDelayed(tick, tickTime);
		}
	};
	
	
	/** Updates all the cells in the grid based on Conway's rules 
	 * Fills a new array with updated cells from the current array then
	 * sets the new array as the current array.*/
	private void updateLifeGrid() {		
		int[][] newGrid = new int[gridWidth][gridHeight];
		
		for (int x = 0; x < gridWidth; x++) {
			for (int y = 0; y < gridHeight; y++) {
				newGrid[x][y] = nextState(x, y);
			}
		}
		
		lifeGrid = newGrid;
		invalidate();
	}


	/** Returns the next state of single cell at coordinates (x,y) */
	private int nextState(int x, int y) {
		
		// Figure out the surrounding rows/columns of a cell
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
		
		// See if the neighbours are alive or dead
		boolean left, topLeft, top, topRight, right, bottomRight, bottom, bottomLeft;
		
		topLeft = getCellValue(leftColumn, topRow);	
		left = getCellValue(leftColumn,y);
		bottomLeft = getCellValue(leftColumn,bottomRow);
		
		top = getCellValue(x,topRow);
		bottom = getCellValue(x,bottomRow);
		
		topRight = getCellValue(rightColumn,topRow);
		right = getCellValue(rightColumn,y);
		bottomRight = getCellValue(rightColumn,bottomRow);		
		
		// Count the living neighbours
		boolean[] neighbours = {topLeft, left, bottomLeft, top, bottom, topRight, right, bottomRight};
		int noOfNeighbours = 0;
		for (boolean b : neighbours) {
			if(b) noOfNeighbours++;
		}
		
		// Calculate the cell's next state based on current state and number of neighbours
		int nextLife = lifeGrid[x][y];
		boolean currentlyAlive = (nextLife == ALIVE);
		
		if(currentlyAlive && (noOfNeighbours == 2 || noOfNeighbours == 3)) {
			nextLife = ALIVE;
		}
		else if(!currentlyAlive && noOfNeighbours == 3) {
			nextLife = ALIVE;
		}
		else if(nextLife != DEAD){
			nextLife--;
		}
		
		return nextLife;
	}

	
	/** Returns true if a cell at (x,y) is alive */
	private boolean getCellValue(int x, int y) {
		return (lifeGrid[x][y] == ALIVE);
	}


	/** Pause/unpause the game */
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
	
	
	/** Clear the grid (set all cells to dead) */
	public void clearGrid() {
		
		// Can't let grid update while clearing it
		tickHandler.removeCallbacks(tick);
		
		for (int x = 0; x < gridWidth; x++) {
			for (int y = 0; y < gridHeight; y++) {
				lifeGrid[x][y] = DEAD;
			}
		}
		
		invalidate();
		
		if(isPlaying) 
			tickHandler.postDelayed(tick, tickTime);
	}
	
	
	/** Set a line of cells between two screen coordinates to alive */
	public void setCellsByCoord(float prevX, float prevY, float newX, float newY) {
		float[] pts = {prevX, prevY, newX, newY};
		Matrix pointMatrix = new Matrix(matrix);
		matrix.invert(pointMatrix);
		pointMatrix.mapPoints(pts);
		
		prevX = pts[0];
		prevY = pts[1];
		newX = pts[2];
		newY = pts[3];
		
		int prevGridX = (int)(Math.floor(prevX/cellSize));
		int prevGridY = (int)(Math.floor((prevY/cellSize)+(5/totalScale)));
		 
		int gridX = (int)(Math.floor(newX/cellSize));
		int gridY = (int)(Math.floor((newY/cellSize)+(5/totalScale)));
		
		int dx = Math.abs(gridX-prevGridX);
		int dy = Math.abs(gridY-prevGridY);
		
		int sx, sy, e2;
		
		if (prevGridX < gridX) sx = 1;
		else sx = -1;
		
		if (prevGridY < gridY) sy = 1;
		else sy = -1;
		
		int err = dx-dy;
		
		while (!(prevGridX == gridX && prevGridY == gridY)) {
			if (prevGridX >= gridWidth || prevGridY >= gridHeight ||
					prevGridX < 0 || prevGridY < 0) break;
			
			lifeGrid[prevGridX][prevGridY] = ALIVE;
		
			e2 = 2*err;
			
			if (e2 > -dy) { 
				err = err - dy;
				prevGridX = prevGridX + sx;
			}
			
			if (e2 <  dx) {
				err = err + dx;
				prevGridY = prevGridY + sy;
			}
		}
		
		if (!(gridX >= gridWidth || gridY >= gridHeight || gridX < 0 || gridY < 0))
			lifeGrid[gridX][gridY] = ALIVE;

		invalidate();
	}
	
	
	/** Set a single cell to alive based on its screen coordinate */
	public void setSingleCellByCoord(float x, float y) {
		float[] pts = {x, y};
		Matrix pointMatrix = new Matrix(matrix);
		matrix.invert(pointMatrix);
		pointMatrix.mapPoints(pts);
		
		x = pts[0];
		y = pts[1];
		
		int gridX = (int)(Math.floor(x/cellSize));
		int gridY = (int)(Math.floor((y/cellSize)+(5/totalScale)));
		
		if (!(gridX >= gridWidth || gridY >= gridHeight || gridX < 0 || gridY < 0))
			lifeGrid[gridX][gridY] = ALIVE;
		
		invalidate();
	}
}
