package cj.software.cassandra.tools.editor.main;

import com.datastax.driver.core.Cluster;

import javafx.scene.control.TreeItem;

public class ClusterTreeItem
		extends TreeItem<String>
{
	private Cluster cluster;

	public ClusterTreeItem(Cluster pCluster)
	{
		super(pCluster.getClusterName());
		this.cluster = pCluster;
	}

	public Cluster getCluster()
	{
		return this.cluster;
	}

}
