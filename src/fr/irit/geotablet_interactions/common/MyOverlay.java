package fr.irit.geotablet_interactions.common;

import org.osmdroid.ResourceProxy;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;

/**
 * Overrides default overlay to disable zooming by double tap
 * 
 * @author helene jonin
 * @mail helene.jonin@gmail.com
 * 
 */
public class MyOverlay extends Overlay {

	public MyOverlay(Context context) {
		super(context);
	}

	public MyOverlay(ResourceProxy resourceProxy) {
		super(resourceProxy);
	}

	@Override
	protected void draw(Canvas canvas, MapView mapView, boolean shadow) {
	}

	@Override
	public boolean onDoubleTap(MotionEvent ev, MapView mapView) {
		// Disable zooming by double tap
		// Because of no call to super
		return true;
	}

}
