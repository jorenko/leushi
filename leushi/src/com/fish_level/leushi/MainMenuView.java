package com.fish_level.leushi;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainMenuView extends SurfaceView implements SurfaceHolder.Callback {
    private Bitmap background;
    private Rect bgSrc;
    private Rect bgDst;
    private List<MenuItem> items; 
    
    public static abstract class MenuItem {
    	private Bitmap image = null;
    	private double width_percent = 0;
    	private double top_percent = 0;
    	private Rect src = null;
    	private Rect dst = null;
    	public MenuItem(Bitmap b, double width_percent, double top_percent) {
    		this.image = b;
    		this.width_percent = width_percent;
    		this.top_percent = top_percent;
    		src = new Rect(0, 0, image.getWidth(), image.getHeight());
    	}
    	
    	public void updateSize(int total_width, int total_height) {
    		int width = (int)(width_percent * total_width);
    		int height = (int)((double)width/(double)image.getWidth() * image.getHeight());
    		int top = (int)(top_percent * total_height);
    		int left = total_width/2 - width/2;
    		dst = new Rect(left, top, left + width, top + height);
    	}
    	
    	public void draw(Canvas c) {
    		if (dst != null) {
    			c.drawBitmap(image, src, dst, null);
    		}
    	}
    	
    	public abstract void onClick();
    }

	public MainMenuView(Context context, AttributeSet attrs, Bitmap background) {
		super(context, attrs);
		getHolder().addCallback(this);
		this.background = background;
		bgSrc = new Rect(0, 0, background.getWidth(), background.getHeight());
		bgDst = new Rect(0, 0, getWidth(), getHeight());
		items = new ArrayList<MenuItem>();
	}
	
	public void draw() {
		Canvas c = getHolder().lockCanvas();
		if (c != null) {
			c.drawBitmap(background, bgSrc, bgDst, null);
			for (MenuItem i : items) {
				i.draw(c);
			}		
			getHolder().unlockCanvasAndPost(c);
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		bgDst = new Rect(0, 0, width, height);
		for (MenuItem i : items) {
			i.updateSize(getWidth(), getHeight());
		}
		draw();
	}

	public void surfaceCreated(SurfaceHolder holder) {
		bgDst = new Rect(0, 0, getWidth(), getHeight());
		for (MenuItem i : items) {
			i.updateSize(getWidth(), getHeight());
		}
		draw();
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			for (MenuItem i : items) {
				if (i.dst.contains((int)event.getX(), (int)event.getY())) {
					i.onClick();
					return true;
				}
			}
		}
		return false;
	}

	public void addItem(MenuItem item) {
		if (item.width_percent == 0.0) {
			item.width_percent = (double)item.image.getWidth() / (double)background.getWidth();
		}
		items.add(item);
		draw();
	}
	
	public void removeItem(MenuItem item) {
		items.remove(item);
		draw();
	}
}
