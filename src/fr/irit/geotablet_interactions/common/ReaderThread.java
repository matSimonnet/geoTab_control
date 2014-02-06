package fr.irit.geotablet_interactions.common;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.osmosis.core.task.v0_6.RunnableSource;
import org.openstreetmap.osmosis.xml.common.CompressionMethod;
import org.openstreetmap.osmosis.xml.v0_6.XmlReader;

import android.content.Context;

/**
 * This thread instantiates a runnable XmlReader with a OSM file in cache (map_data.osm),
 * associates the parser (ReaderSink) and processes
 * 
 * @author helene jonin
 * @mail helene.jonin@gmail.com
 * 
 */
public class ReaderThread extends Thread {
	private File osmFile;
	private RunnableSource reader;
	private ReaderSink readerSink;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            The context
	 */
	public ReaderThread(Context context) {
		super();
		this.osmFile = new File(context.getCacheDir(), "map_data.osm");
		this.reader = new XmlReader(osmFile, false, CompressionMethod.None);
		this.readerSink = new ReaderSink();
		this.reader.setSink(readerSink);
	}

	@Override
	public void run() {
		reader.run();
	}

	/**
	 * Getter for nodes
	 * 
	 * @return The nodes
	 */
	public Set<OsmNode> getNodes() {
		return readerSink.getNodes(); // Get nodes from the sink
	}

	/**
	 * Getter for ways
	 * 
	 * @return The ways
	 */
	public Map<String, List<OsmNode>> getWays() {
		return readerSink.getWays(); // Get ways from the sink
	}

	/**
	 * Getter for relations
	 * 
	 * @return The ways
	 */
	public Map<String, List<OsmNode>> getRelations() {
		return readerSink.getRelations(); // Get relations from the sink
	}

}
