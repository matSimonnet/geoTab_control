package fr.irit.geotablet_interactions.common;

import java.util.Locale;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;

import android.graphics.Point;

/**
 * Represents a node from OSM data with name, latitude and longitude
 * 
 * @author helene jonin
 * @mail helene.jonin@gmail.com
 * 
 */
public class OsmNode {
	private String name;
	private double latitude;
	private double longitude;

	/**
	 * Constructor
	 * 
	 * @param name
	 *            The node name
	 * @param latitude
	 *            The node latitude
	 * @param longitude
	 *            The node longitude
	 */
	public OsmNode(String name, double latitude, double longitude) {
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((name == null) ? 0 : name.toLowerCase(Locale.FRENCH).hashCode()); // Case insensitive
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OsmNode other = (OsmNode) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (name.equals("") || other.name.equals("")) { 
			// If name is empty, check latitude and longitude
			if ((latitude != other.latitude) || (longitude != other.longitude))
				return false;
		} else if (!name.equalsIgnoreCase(other.name)) // Case insensitive
			return false;
		return true;
	}

	/**
	 * Convert the node to a point on the map view
	 * 
	 * @param mapView
	 *            The map view
	 * @return The point in pixels from top left
	 */
	public Point toPoint(MapView mapView) {
		Point returnPoint = new Point();
		Projection projection = mapView.getProjection();
		projection.toPixels(new GeoPoint(latitude, longitude), returnPoint);
		// Get the top left GeoPoint
		GeoPoint geoPointTopLeft = (GeoPoint) projection.fromPixels(0, 0);
		Point topLeftPoint = new Point();
		// Get the top left Point (includes osmdroid offsets)
		projection.toPixels(geoPointTopLeft, topLeftPoint);
		// Remove offsets
		returnPoint.x -= topLeftPoint.x;
		returnPoint.y -= topLeftPoint.y;
		return returnPoint;
	}

	@Override
	public String toString() {
		return name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

}
