package fr.irit.geotablet_interactions.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import fr.irit.geotablet_interactions.control.R;


/**
 * This class customizes the map view
 * 
 * @author helene jonin
 * @mail helene.jonin@gmail.com
 *
 */
public class MyMapView extends MapView {
	private static final int TARGET_SIZE = 96; // Touch target size for on screen elements

	private Context context;
	private BoundingBoxE6 bbox;
	private Set<OsmNode> nodes;
	private Map<String, List<OsmNode>> ways;

	public MyMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.initialize(context);
	}

	public MyMapView(Context context, int tileSizePixels) {
		super(context, tileSizePixels);
		this.initialize(context);
	}

	public MyMapView(Context context, int tileSizePixels,
			ResourceProxy resourceProxy) {
		super(context, tileSizePixels, resourceProxy);
		this.initialize(context);
	}

	public MyMapView(Context context, int tileSizePixels,
			ResourceProxy resourceProxy, MapTileProviderBase aTileProvider) {
		super(context, tileSizePixels, resourceProxy, aTileProvider);
		this.initialize(context);
	}

	public MyMapView(Context context, int tileSizePixels,
			ResourceProxy resourceProxy, MapTileProviderBase aTileProvider,
			Handler tileRequestCompleteHandler) {
		super(context, tileSizePixels, resourceProxy, aTileProvider,
				tileRequestCompleteHandler);
		this.initialize(context);
	}

	public MyMapView(Context arg0, int arg1, ResourceProxy arg2,
			MapTileProviderBase arg3, Handler arg4, AttributeSet arg5) {
		super(arg0, arg1, arg2, arg3, arg4, arg5);
		this.initialize(context);
	}

	/**
	 * Initializes map view (for constructor)
	 * 
	 * @param context
	 *            The context
	 */
	private void initialize(Context context) {
		this.context = context;
		this.nodes = new HashSet<OsmNode>();
		this.ways = new HashMap<String, List<OsmNode>>();

		Configuration config = null;

		try {
			// Load map properties from properties file in assets (map.properties)
			config = new PropertiesConfiguration("assets/map.properties");

			this.bbox = new BoundingBoxE6(
					config.getDouble("north"),
					config.getDouble("east"),
					config.getDouble("south"),
					config.getDouble("west"));

			if (!config.getBoolean("downloadData")) {
				this.getDataFromFile(config.getString("filename"));
			} else {
				this.downloadDataFromOSM(config.getString("url"));
			}

			this.parseData();

			this.display();
		} catch (ConfigurationException e) { // TODO Personalize catch
			e.printStackTrace();
		} catch (NoSuchElementException e) {
			e.printStackTrace();
		} catch (ConversionException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get data from file in assets and copy to a file in cache (map_data.osm)
	 * 
	 * @param filename
	 *            The name of the file (must be in assets)
	 * @throws IOException 
	 */
	private void getDataFromFile(String filename) throws IOException {
		File tmpFile = new File(context.getCacheDir(), "map_data.osm");
		InputStream is = context.getAssets().open(filename);
		int size = is.available();
		byte[] buffer = new byte[size];
		is.read(buffer);
		is.close();
		FileOutputStream fos = new FileOutputStream(tmpFile);
		fos.write(buffer);
		fos.close();
	}

	/**
	 * Download data from OSM
	 * 
	 * @param url
	 *            The API request URL for the HTTP query
	 */
	private void downloadDataFromOSM(String url) {
		WriterThread writerThread = new WriterThread(context);
		writerThread.setUrl(url);
		writerThread.start();
		while (writerThread.isAlive()) {
			try {
				writerThread.join();
			} catch (InterruptedException e) {
				/* do nothing */
			}
		}
	}

	/**
	 * Parse OSM data
	 * 
	 * @return The parsed nodes
	 */
	private void parseData() {
		ReaderThread readerThread = new ReaderThread(context);
		readerThread.start();
		while (readerThread.isAlive()) {
			try {
				readerThread.join();
			} catch (InterruptedException e) {
				/* do nothing */
			}
		}

		nodes = readerThread.getNodes();
		ways = readerThread.getWays();
	}

	/**
	 * Display map view
	 */
	private void display() {
		setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);

		// Set boundaries (with bounding box) and disable scrolling
		if (getHeight() > 0) {
			zoomToBoundingBox(bbox);
		} else {
			getViewTreeObserver().addOnGlobalLayoutListener(
					new OnGlobalLayoutListener() {
						private int i = 0;

						// Wait for display layout complete
						// to be able to use the method zoomToBoundingBox
						@SuppressWarnings("deprecation")
						public void onGlobalLayout() {
							i++;
							zoomToBoundingBox(bbox);
							if (i >= 2) {
								// Remove listener after called twice - doesn't
								// work otherwise - don't know why
								try {
									getViewTreeObserver().removeOnGlobalLayoutListener(this);
								} catch (NoSuchMethodError x) {
									getViewTreeObserver().removeGlobalOnLayoutListener(this);
								}
							}
						}
					});
		}
		setScrollableAreaLimit(bbox);

		// Disable zoom controls
		setBuiltInZoomControls(false);

		// Add overlays to the map view
		List<Overlay> mapOverlays = getOverlays();
		mapOverlays.clear();
		mapOverlays.add(new MyOverlay(context));

		// Path overlay is to display ways
		for (List<OsmNode> waysNodes : ways.values()) {
			PathOverlay pathOverlay = new PathOverlay(Color.RED, context);
			Paint paint = pathOverlay.getPaint();
			paint.setStrokeWidth(TARGET_SIZE);
			pathOverlay.setPaint(paint);
			for (OsmNode n : sortNodes(waysNodes)) { // Sort nodes so that they are drawn correctly
				pathOverlay.addPoint(new GeoPoint(n.getLatitude(), n.getLongitude()));
			}
//			mapOverlays.add(pathOverlay);
		}

		// Itemized overlay is to display nodes
		Drawable drawable = getResources().getDrawable(R.drawable.target_96);
		MyItemizedOverlay itemizedoverlay = 
				new MyItemizedOverlay(drawable, new DefaultResourceProxyImpl(context));
		for (OsmNode n : nodes) {
			itemizedoverlay.addOverlay(
					new OverlayItem(n.getName(), "", new GeoPoint(n.getLatitude(), n.getLongitude())));
		}
		mapOverlays.add(itemizedoverlay);
	}

	/**
	 * Sort nodes so that distance between them is as small as possible
	 * 
	 * @param nodes
	 *            A list of nodes
	 * @return The sorted list
	 */
	private List<OsmNode> sortNodes(List<OsmNode> nodes) {
		List<OsmNode> returnNodes = new ArrayList<OsmNode>();
		OsmNode currentNode = nodes.get(0);
		returnNodes.add(currentNode); // We are sure about the first node
		for (int i = 0; i < nodes.size(); i++) { // For each node in nodes
			int minDistance = Integer.MAX_VALUE;
			OsmNode tmpNode = null;
			for (int j = 0; j < nodes.size(); j++) { // We are searching which other node is the closest
				if (!returnNodes.contains(nodes.get(j))) {
					GeoPoint gp1 = new GeoPoint(currentNode.getLatitude(), currentNode.getLongitude());
					GeoPoint gp2 = new GeoPoint(nodes.get(j).getLatitude(), nodes.get(j).getLongitude());
					int distance = gp1.distanceTo(gp2);
					if (distance < minDistance) {
						minDistance = distance;
						tmpNode = nodes.get(j);
					}
				}
			}
			if (tmpNode != null) {
				currentNode = tmpNode;
				returnNodes.add(currentNode);
			}
		}
		return returnNodes;
	}

	/**
	 * Getter for nodes
	 * 
	 * @return The nodes
	 */
	public Set<OsmNode> getNodes() {
		return nodes;
	}

	/**
	 * Get nodes in a bounding box
	 *
	 * @param bbox
	 *            A bounding box
	 * @return
	 *            A set of nodes
	 */
	public Set<OsmNode> getNodesInBbox(BoundingBoxE6 bbox) {
		Set<OsmNode> nodesInBbox = new HashSet<OsmNode>();
		for (OsmNode n : nodes) {
			if (bbox.contains(new GeoPoint(n.getLatitude(), n.getLongitude()))) {
				nodesInBbox.add(n);
			}
		}
		return nodesInBbox;
	}

	/**
	 * Getter for ways
	 * 
	 * @return The ways
	 */
	public Map<String, List<OsmNode>> getWays() {
		return ways;
	}

}
