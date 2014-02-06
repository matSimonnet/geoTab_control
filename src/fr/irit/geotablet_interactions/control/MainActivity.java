package fr.irit.geotablet_interactions.control;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.v4.view.MotionEventCompat;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import fr.irit.geotablet_interactions.common.MyMapView;
import fr.irit.geotablet_interactions.common.MyTTS;
import fr.irit.geotablet_interactions.common.OsmNode;

public class MainActivity extends Activity {
	private static final int TARGET_SIZE = 100; // Touch target size for on screen elements

	private MyMapView mapView;
	private Map<View, Set<OsmNode>> selectedItems = new HashMap<View, Set<OsmNode>>(2);
	private Map<View, Integer> isOutsideView = new HashMap<View, Integer>(2);
	private float x = 0.0f, y = 0.0f;
	private PrintWriter output;
	private Date myDate;
	private boolean firstTouch = true;
	private String logContact = "";
	private String logAnnounce = "";
	private String lastAnnounce = "nothing";

	public String getLogAnnounce() {
		return logAnnounce;
	}

	public void setLogAnnounce(String logAnnounce) {
		this.logAnnounce = logAnnounce;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//create file for logging
		myDate = new Date();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss",Locale.getDefault()); 
		new File(Environment.getExternalStorageDirectory().getAbsolutePath()+ "/geoTablet/").mkdir();
		String logFilename = simpleDateFormat.format(new Date())+ "_"+ getString(R.string.app_name) +".csv";
		File logFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+ "/geoTablet/" + logFilename);
		try {
			output = new PrintWriter(new FileWriter(logFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
    	//set Full screen landscape
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		//hide actionBar (up) -> does not work on galaxyTab 10.1
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
				
		//hide menuBar (bottom)
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		
		mapView = (MyMapView) findViewById(R.id.map_view);
		final Set<OsmNode> nodes = mapView.getNodes();


		// Set listener to the map view
		mapView.setOnTouchListener(new OnTouchListener() {
			private static final int INVALID_POINTER_ID = -1;

			private int activePointerId;
			
			public boolean onTouch(View v, MotionEvent ev) {
				int action = MotionEventCompat.getActionMasked(ev);

				switch (action) {
				case MotionEvent.ACTION_DOWN: {
					// Save the ID of this pointer (for dragging)
					activePointerId = MotionEventCompat.getPointerId(ev, 0);
					int pointerIndex = MotionEventCompat.findPointerIndex(ev, activePointerId);
					onTouchMapView(null, MotionEventCompat.getX(ev, pointerIndex), MotionEventCompat.getY(ev, pointerIndex));
					break;
				}

				case MotionEvent.ACTION_MOVE: {
					// Find the index of the active pointer and fetch its position
					int pointerIndex = MotionEventCompat.findPointerIndex(ev, activePointerId);
					onTouchMapView(null, MotionEventCompat.getX(ev, pointerIndex), MotionEventCompat.getY(ev, pointerIndex));
					break;
				}

				case MotionEvent.ACTION_UP: {
					activePointerId = INVALID_POINTER_ID;
					MyTTS.getInstance(getApplicationContext()).stop();
					break;
				}

				case MotionEvent.ACTION_CANCEL: {
					activePointerId = INVALID_POINTER_ID;
					break;
				}

				case MotionEvent.ACTION_POINTER_UP: {

					int pointerIndex = MotionEventCompat.getActionIndex(ev);
					int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);

					if (pointerId == activePointerId) {
						// This was our active pointer going up. Choose a new
						// active pointer and adjust accordingly.
						int newPointerIndex = pointerIndex == 0 ? 1 : 0;
						activePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
					}

					break;
				}
				}
				return true;
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onDestroy() {
		MyTTS.release();
		super.onDestroy();
	}

	/**
	 * Getter for selected item in the list (to be guided to)
	 * 
	 * @return The selected item
	 */
	public Object getSelectedItem(View v) {
		return selectedItems.get(v);
	}

	/**
	 * Setter for selected item in the list (to be guided to)
	 * 
	 * @param selectedItems
	 *            The selected item
	 */
	public void setSelectedItem(View v, Set<OsmNode> selectedItem) {
		selectedItems.put(v, selectedItem);
	}

	@SuppressWarnings("serial")
	private void initializeAdapaterList(ArrayAdapter<Set<OsmNode>> adapter, int itemsCount) {
		List<Set<OsmNode>> verticalNodesArray = new ArrayList<Set<OsmNode>>(itemsCount);
		for (int i = 0; i < itemsCount; i++) {
			verticalNodesArray.add(new HashSet<OsmNode>() {

				@Override
				public String toString() {
					return formatListForTts(super.toString());
				}

			});
		}
		adapter.addAll(verticalNodesArray);
	}

	private String formatListForTts(String str) {
		str = str.replace("[", "");
		str = str.replace("]", "");
		return str;
	}

	private void displayAdapter(ArrayAdapter<Set<OsmNode>> adapter, ViewGroup v, LayoutParams layoutParams) {
		for (int i = 0; i < adapter.getCount(); i++) {
			View adapterView = adapter.getView(i, null, null);
			adapterView.setLayoutParams(layoutParams);
			v.addView(adapterView);
		}
	}

	//Mathieu's code's transformations to display all osmnode	
	private void onTouchMapView(View v, float x, float y) {
		

		
		//retrieve all nodes
		final Set<OsmNode> nodes = mapView.getNodes();
		//retrieve selected node
		Set<OsmNode> selectedNodes = new HashSet<OsmNode>();
		
				
		for (OsmNode n : nodes) {
			if ((n.toPoint(mapView).y <= y + TARGET_SIZE / 2)
					&& (n.toPoint(mapView).y >= y - TARGET_SIZE / 2)
					&& (n.toPoint(mapView).x <= x + TARGET_SIZE / 2)
					&& (n.toPoint(mapView).x >= x - TARGET_SIZE / 2)) {
				if (!MyTTS.getInstance(this).isSpeaking()  
						&& (selectedNodes.toString()).contains(n.getName()) ) {
					((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(150);
					MyTTS.getInstance(this).setPitch(1.6f);
					MyTTS.getInstance(this).speak(
							getResources().getString(R.string.found) + 
							n.getName(),
							TextToSpeech.QUEUE_FLUSH,
							null);
					lastAnnounce = n.getName();
					logAnnounce = n.getName();
				}
				else if (!MyTTS.getInstance(this).isSpeaking() ) {
					((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(150);
					MyTTS.getInstance(this).setPitch(1.6f);
					MyTTS.getInstance(this).speak(
							n.getName(),
							TextToSpeech.QUEUE_FLUSH,
							null);
					lastAnnounce = n.getName();
					logAnnounce = n.getName();
				}
				logContact = n.getName();
			}
			else if ( lastAnnounce.contentEquals(n.getName()) 
						&& MyTTS.getInstance(this).isSpeaking() ) {
				MyTTS.getInstance(this).stop();
				lastAnnounce = "";
				logAnnounce = "stopSpeak";
			}
		}
		//for logging
		double lat = mapView.getProjection().fromPixels(x, y).getLatitudeE6();
		double lon = mapView.getProjection().fromPixels(x, y).getLongitudeE6();
		Datalogger(x,y,lat,lon,logContact,logAnnounce);
		logAnnounce = "";
		logContact = "";
	}
	
	
	public void Datalogger (float x, float y, double lat, double lon, String logContact, String logAnnounce){
		if (firstTouch){
			output.println("time(ms);x;y;lat;lon;contact;announce");
			firstTouch = false;
		}
		Date touchDate = new Date();
		String str = touchDate.getTime()-myDate.getTime() + ";" 
		+ (int)x + ";" + (int)y + ";" 
		+ lat/100000 + ";" + lon/100000 + ";"
		+ logContact + ";" + logAnnounce;
		output.println(str);
		output.flush();
	}
		
	
	//Hélène's code to only display one osmnode
//	private void onTouchMapView(View v, float x, float y) {
//		Set<OsmNode> selectedNodes = new HashSet<OsmNode>();
//		if ((v != null) && ((v.getId() == R.id.vertical_list_layout) || (v.getId() == R.id.horizontal_list_layout))) {
//			selectedNodes = selectedItems.get(v);
//		} else {
//			for (Set<OsmNode> allSelectedNodes : selectedItems.values()) {
//				if (allSelectedNodes != null) {
//					selectedNodes.addAll(allSelectedNodes);
//				}
//			}
//		}
//		if (selectedNodes != null) {
//			for (OsmNode n : selectedNodes) {
//				if ((n.toPoint(mapView).y <= y + TARGET_SIZE / 2)
//						&& (n.toPoint(mapView).y >= y - TARGET_SIZE / 2)
//						&& (n.toPoint(mapView).x <= x + TARGET_SIZE / 2)
//						&& (n.toPoint(mapView).x >= x - TARGET_SIZE / 2)) {
//					if (!MyTTS.getInstance(this).isSpeaking()) {
//						String from = "";
//						if (v != null) {
//							from = " " + getResources().getString(R.string.finger) + " ";
//							if (v.getId() == R.id.vertical_list_layout) {
//								from += getResources().getString(R.string.left);
//							}
//							if (v.getId() == R.id.horizontal_list_layout) {
//								from += getResources().getString(R.string.bottom);
//							}
//						}
//						((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(150);
//						MyTTS.getInstance(this).speak(
//								getResources().getString(R.string.found) + " " + n.getName() + from,
//								TextToSpeech.QUEUE_ADD,
//								null);
//					}
//				}
//			}
//		}
//	}

}
