package fr.irit.geotablet_interactions.common;

import java.util.ArrayList;

import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapView;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayItem.HotspotPlace;

import android.graphics.Point;
import android.graphics.drawable.Drawable;

/**
 * Only useful for visual feedback
 * 
 * @author helene jonin
 * @mail helene.jonin@gmail.com
 * 
 */
public class MyItemizedOverlay extends ItemizedOverlay<OverlayItem> {
	private ArrayList<OverlayItem> overlays;

	public MyItemizedOverlay(Drawable defaultMarker, ResourceProxy resourceProxy) {
		super(defaultMarker, resourceProxy);
		this.overlays = new ArrayList<OverlayItem>();
	}

	public void addOverlay(OverlayItem overlay) {
		overlay.setMarkerHotspot(HotspotPlace.CENTER);
		overlays.add(overlay);
		populate();
	}

	@Override
	public boolean onSnapToItem(int x, int y, Point snapPoint, IMapView mapView) {
		return false;
	}

	@Override
	protected OverlayItem createItem(int i) {
		return overlays.get(i);
	}

	@Override
	public int size() {
		return overlays.size();
	}

}
