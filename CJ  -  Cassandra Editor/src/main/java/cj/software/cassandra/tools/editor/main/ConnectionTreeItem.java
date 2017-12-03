package cj.software.cassandra.tools.editor.main;

import com.datastax.driver.core.Cluster;

import cj.software.cassandra.tools.editor.modell.Connection;
import cj.software.javafx.ThrowableStackTraceAlertFactory;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.TreeItem;

public class ConnectionTreeItem
		extends TreeItem<String>
{
	private Connection connection;

	public ConnectionTreeItem(Connection pConnection)
	{
		super(pConnection.getHostname());
		this.connection = pConnection;
		super.expandedProperty().addListener(new ChangeListener<Boolean>()
		{

			@Override
			public void changed(
					ObservableValue<? extends Boolean> pObservable,
					Boolean pOldValue,
					Boolean pNewValue)
			{
				if (pNewValue.booleanValue())
				{
					showCluster();
				}
				else
				{
					closeCluster();
				}
			}
		});
	}

	private void closeCluster()
	{
		ObservableList<TreeItem<String>> lChildren = this.getChildren();
		for (TreeItem<String> bChild : lChildren)
		{
			ClusterTreeItem lClusterTreeItem = (ClusterTreeItem) bChild;
			Cluster lCluster = lClusterTreeItem.getCluster();
			if (lCluster != null)
			{
				lCluster.close();
			}
		}
	}

	private void showCluster()
	{
		try
		{
			Cluster lCluster = Cluster
					.builder()
					.addContactPoint(this.connection.getHostname())
					.build();
			ClusterTreeItem lClusterTreeItem = new ClusterTreeItem(lCluster);
			this.getChildren().clear();
			this.getChildren().add(lClusterTreeItem);
		}
		catch (Throwable pThrowable)
		{
			pThrowable.printStackTrace(System.err);
			Alert lAlert = ThrowableStackTraceAlertFactory.createAlert(pThrowable);
			lAlert.showAndWait();
		}
	}

	public Connection getConnection()
	{
		return this.connection;
	}

	@Override
	public boolean isLeaf()
	{
		return false;
	}

}
