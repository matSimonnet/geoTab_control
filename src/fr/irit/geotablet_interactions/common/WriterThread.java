package fr.irit.geotablet_interactions.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.util.Log;

/**
 * This thread runs an HTTP query to download data from OSM
 * and store it into a OSM (XML) file (map_data.osm) in cache
 * 
 * @author helene jonin
 * @mail helene.jonin@gmail.com
 * 
 */
public class WriterThread extends Thread {
	private Context context;
	private String urlStr;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            The context
	 */
	public WriterThread(Context context) {
		super();
		this.context = context;
		this.urlStr = "";
	}

	/**
	 * Constructor
	 * 
	 * @param context
	 *            The context
	 * @param urlStr
	 *            The URL to query
	 */
	public WriterThread(Context context, String urlStr) {
		super();
		this.context = context;
		this.urlStr = urlStr;
	}

	@Override
	public void run() {
		System.setProperty("http.keepAlive", "false");

		URL url;
		HttpURLConnection connexion = null;
		BufferedReader reader = null;
		BufferedWriter writer = null;

		if (!urlStr.isEmpty()) {
			try {
				url = new URL(urlStr);
				connexion = (HttpURLConnection) url.openConnection();
				if (connexion.getResponseCode() == HttpURLConnection.HTTP_OK) {
					reader = new BufferedReader(
						new InputStreamReader(connexion.getInputStream()));
					writer = new BufferedWriter(
						new FileWriter(new File(context.getCacheDir(), "map_data.osm")));
					String ligne = "";
					while ((ligne = reader.readLine()) != null) {
						writer.write(ligne);
						writer.newLine();
					}
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				connexion.disconnect();
			}
		} else {
			Log.e("WriterThread", "No URL specified");
		}
	}

	/**
	 * Setter for URL
	 * 
	 * @param url
	 *            The URL to query
	 */
	public void setUrl(String url) {
		this.urlStr = url;
	}

}
