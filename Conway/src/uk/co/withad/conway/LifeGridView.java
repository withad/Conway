package uk.co.withad.conway;

import static uk.co.withad.conway.Constants.ALIVE;
import static uk.co.withad.conway.Constants.DEAD;
import static uk.co.withad.conway.Constants.TAG;

import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
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
	
	public Activity parentActivity;
	public int actionBarHeight = -1;
	
	float scale = 1f;
	float totalScale = 1f;
	
	public float translateX = 0;
	public float translateY = 0;
	
	Paint cellPaint;
	Paint fadePaint;
	Paint gridPaint;
	
	
	// Constructor
	public LifeGridView(Context context, AttributeSet atts) {
		super(context, atts);
		
		cellPaint = new Paint();
		cellPaint.setStyle(Style.FILL);
		cellPaint.setColor(Color.BLACK);
		
		fadePaint = new Paint();
		fadePaint.setStyle(Style.FILL);
		fadePaint.setColor(Color.GRAY);
		
		gridPaint = new Paint();
		gridPaint.setColor(Color.LTGRAY);
		
		setBackgroundColor(Color.WHITE);
	}
	
	
	@Override
	public void onDraw(Canvas canvas) {
		if(matrix == null) matrix = canvas.getMatrix();
		if (actionBarHeight == -1) {
			actionBarHeight = parentActivity.getActionBar().getHeight();
			matrix.reset();
			matrix.postScale(scale, scale);
			matrix.postTranslate(0, actionBarHeight);
		}
		
		canvas.setMatrix(matrix);
		
		super.onDraw(canvas);
		
		matrix.postScale(scale, scale);
		totalScale *= scale;
		scale = 1f;
		
		matrix.postTranslate(translateX, translateY);
		translateX = 0;
		translateY = 0;
		
		if(lifeGrid == null) return;		
		
		int lifeValue;
		
		// Draw cells
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
	
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		
		tickHandler.removeCallbacks(tick);
		
		gridWidth = (int)(Math.floor(w/cellSize));
		gridHeight = (int)(Math.floor(h/cellSize));
		
		lifeGrid = new int[gridWidth][gridHeight];
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
					lifeGrid[x][y] = ALIVE;
				else if (choice == 0)
					lifeGrid[x][y] = DEAD;
			}
		}
	}
	
	
	private void updateLifeGrid() {		
		int[][] newGrid = new int[gridWidth][gridHeight];
		
		for (int x = 0; x < gridWidth; x++) {
			for (int y = 0; y < gridHeight; y++) {
				newGrid[x][y] = updateCell(x, y);
			}
		}
		
		lifeGrid = newGrid;
		invalidate();
	}


	private int updateCell(int x, int y) {
		
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
		
		boolean[] neighbours = {topLeft, left, bottomLeft, top, bottom, topRight, right, bottomRight};
		
		int noOfNeighbours = 0;
		for (boolean b : neighbours) {
			if(b) noOfNeighbours++;
		}
		
		int currentLife = lifeGrid[x][y];
		boolean currentlyAlive = (currentLife == ALIVE);
		
		
		if(currentlyAlive && (noOfNeighbours == 2 || noOfNeighbours == 3)) {
			currentLife = ALIVE;
		}
		else if(!currentlyAlive && noOfNeighbours == 3) {
			currentLife = ALIVE;
		}
		else if(currentLife != DEAD){
			currentLife--;
		}
		
		return currentLife;
	}

	
	private boolean getCellValue(int x, int y) {
		return (lifeGrid[x][y] == ALIVE);
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
	
	
	public void clearGrid() {
		boolean wasPlaying = false;
		if(isPlaying) {
			pauseGrid();
			wasPlaying = true;
		}
		
		for (int x = 0; x < gridWidth; x++) {
			for (int y = 0; y < gridHeight; y++) {
				lifeGrid[x][y] = DEAD;
			}
		}
		
		invalidate();
		
		if(wasPlaying) pauseGrid();
	}
	
	
	public void setCellByCoord(float prevX, float prevY, float newX, float newY) {
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
