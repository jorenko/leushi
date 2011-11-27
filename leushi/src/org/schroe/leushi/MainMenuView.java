package org.schroe.leushi;

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
import android.view.View;

public class MainMenuView extends SurfaceView implements SurfaceHolder.Callback {
    private Bitmap menuBackground;
    private List<MenuItem> items; 
    
    public static abstract class MenuItem {
    	private Bitmap image = null;
    	private Rect rect = null;
    	public MenuItem(Bitmap b, int x, int y) {
    		this.image = b;
    		this.rect = new Rect(x, y, x+b.getWidth(), y+b.getHeight());
    	}
    	public MenuItem(Bitmap b) {
    		this.image = b;
    	}
    	
    	public abstract void onClick();
    }

	public MainMenuView(Context context, AttributeSet attrs, Bitmap background) {
		super(context, attrs);
		getHolder().addCallback(this);
		menuBackground = background;
		items = new ArrayList<MenuItem>();
	}
	
	public void draw() {
		Canvas c = getHolder().lockCanvas();
		if (c != null) {
			c.drawBitmap(menuBackground, 0, 0, null);
			for (MenuItem i : items) {
				c.drawBitmap(i.image, null, i.rect, null);
			}		
			getHolder().unlockCanvasAndPost(c);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		menuBackground = Bitmap.createScaledBitmap(menuBackground, width, height, true);
		draw();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		menuBackground = Bitmap.createScaledBitmap(menuBackground, getWidth(), getHeight(), true);
		draw();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			for (MenuItem i : items) {
				if (i.rect.contains((int)event.getX(), (int)event.getY())) {
					i.onClick();
					return true;
				}
			}
		}
		return false;
	}
	
	public void addItem(MenuItem item) {
		if (item.rect == null) {
			if (items.size() == 0) {
				item.rect = new Rect(0, 0, item.image.getWidth(), item.image.getHeight());
			} else {
				MenuItem last = items.get(items.size()-1);
				item.rect = new Rect(last.rect.left, last.rect.bottom+1, last.rect.left+item.image.getWidth(), last.rect.bottom+1+item.image.getHeight());
			}
		}
		items.add(item);
		draw();
	}
	
	public void removeItem(MenuItem item) {
		items.remove(item);
		draw();
	}
}
