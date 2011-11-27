package org.schroe.leushi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class LeushiView extends SurfaceView implements SurfaceHolder.Callback {
	Bitmap background = null;

	public LeushiView(Context context, AttributeSet attrs) {
		super(context, attrs);
		getHolder().addCallback(this);
		
		background = BitmapFactory.decodeResource(getResources(), R.drawable.game_background);
	}
	
	public void draw() {
		Canvas c = getHolder().lockCanvas();
		c.drawBitmap(background, 0, 0, null);
		getHolder().unlockCanvasAndPost(c);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		background = Bitmap.createScaledBitmap(background, width, height, true);
		draw();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		background = Bitmap.createScaledBitmap(background, getWidth(), getHeight(), true);
		draw();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}
}
