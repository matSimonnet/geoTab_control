package fr.irit.geotablet_interactions.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;

import android.util.SparseArray;

/**
 * This class parses data from the associated OSM file into nodes (OsmNode),
 * ways and relations (lists of nodes)
 * 
 * @author helene jonin
 * @mail helene.jonin@gmail.com
 * 
 */
public class ReaderSink implements Sink {
	private Set<OsmNode> nodes;
	private SparseArray<OsmNode> nodesNoName;
	private Map<String, List<OsmNode>> ways;
	private SparseArray<List<OsmNode>> waysNoName;
	private Map<String, List<OsmNode>> relations;

	/**
	 * Constructor
	 */
	public ReaderSink() {
		super();
		this.nodes = new HashSet<OsmNode>();
		this.nodesNoName = new SparseArray<OsmNode>();
		this.ways = new HashMap<String, List<OsmNode>>();
		this.waysNoName = new SparseArray<List<OsmNode>>();
		this.relations = new HashMap<String, List<OsmNode>>();
	}

	@Override
	public void initialize(Map<String, Object> arg) {
	}

	@Override
	public void complete() {
	}

	@Override
	public void release() {
	}

	@Override
	public void process(EntityContainer entityContainer) {
		Entity entity = entityContainer.getEntity();
		if (entity instanceof Node) {

			/* Do something with the node */

			// Iterate through tags to get name
			for (Tag t : entity.getTags()) {
				if (t.getKey().equals("name")) {
					// Then add the node to the list
					nodes.add(
							new OsmNode(
									t.getValue(),
									((Node) entity).getLatitude(),
									((Node) entity).getLongitude()));
					break;
				}
			}

			// Populate nodesNoName
			nodesNoName.put((int) ((Node) entity).getId(),
					new OsmNode(
							"",
							((Node) entity).getLatitude(),
							((Node) entity).getLongitude()));

		} else if (entity instanceof Way) {

			/* Do something with the way */

			for (Tag t : entity.getTags()) {
				// Iterate through tags to get name
				if (t.getKey().equals("name")) {
					List<OsmNode> tmpNodes = new ArrayList<OsmNode>();
					// Then iterate through nodes and add them to a list
					for (WayNode n : ((Way) entity).getWayNodes()) {
						OsmNode tmpNode = nodesNoName.get((int) n.getNodeId());
						if (tmpNode != null) {
							tmpNodes.add(tmpNode);
						}
					}
					// Then link it to the way and add it to the list
					if (!ways.containsKey(t.getValue())) {
						ways.put(t.getValue(), tmpNodes);
					} else {
						ways.get(t.getValue()).addAll(tmpNodes);
					}
					break;
				}
			}

			// Populate waysNoName
			List<OsmNode> tmpNodes = new ArrayList<OsmNode>();
			for (WayNode n : ((Way) entity).getWayNodes()) {
				OsmNode tmpNode = nodesNoName.get((int) n.getNodeId());
				if (tmpNode != null) {
					tmpNodes.add(tmpNode);
				}
			}
			waysNoName.put((int) ((Way) entity).getId(), tmpNodes);

		} else if (entity instanceof Relation) {

			/* Do something with the relation */

			for (Tag t : entity.getTags()) {
				// Iterate through tags to get description
				if (t.getKey().equals("description")) {
					// Then iterate through relation members
					for (RelationMember m : ((Relation) entity).getMembers()) {
						if (m.getMemberType() == EntityType.Node) { // Member is node
							// Initialize array if not done yet
							if (!relations.containsKey(t.getValue())) {
								relations.put(t.getValue(), new ArrayList<OsmNode>());
							}
							// If node, link it to the relation and add it to the list
							if (nodesNoName.get((int) m.getMemberId()) != null) {
								relations.get(t.getValue()).add(nodesNoName.get((int) m.getMemberId()));
							}
						}
						if (m.getMemberType() == EntityType.Way) { // Member is way
							// Initialize array if not done yet
							if (!relations.containsKey(t.getValue())) {
								relations.put(t.getValue(), new ArrayList<OsmNode>());
							}
							// If way, link it to the relation and add it (list of nodes) to the list
							if (waysNoName.get((int) m.getMemberId()) != null) {
								relations.get(t.getValue()).addAll(waysNoName.get((int) m.getMemberId()));
							}
						}
					}
					break;
				}
			}

		}
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
	 * Getter for ways
	 * 
	 * @return The ways
	 */
	public Map<String, List<OsmNode>> getWays() {
		return ways;
	}

	/**
	 * Getter for relations
	 * 
	 * @return The relations
	 */
	public Map<String, List<OsmNode>> getRelations() {
		return relations;
	}

}
