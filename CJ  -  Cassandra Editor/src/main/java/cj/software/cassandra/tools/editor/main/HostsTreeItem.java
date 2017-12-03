package cj.software.cassandra.tools.editor.main;

import java.util.Set;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;

import cj.software.javafx.ThrowableStackTraceAlertFactory;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Alert;
import javafx.scene.control.TreeItem;

public class HostsTreeItem
		extends TreeItem<String>
{
	public HostsTreeItem()
	{
		super("Hosts");
		super.expandedProperty().addListener(new ChangeListener<Object>()
		{

			@Override
			public void changed(
					ObservableValue<? extends Object> pObservable,
					Object pOldValue,
					Object pNewValue)
			{
				Boolean lNewBoolean = (Boolean) pNewValue;
				if (lNewBoolean.booleanValue())
				{
					listHosts();
				}
			}
		});
	}

	private void listHosts()
	{
		try
		{
			ClusterTreeItem lParent = (ClusterTreeItem) getParent();
			Cluster lCluster = lParent.getCluster();
			Metadata lMetadata = lCluster.getMetadata();
			Set<Host> lAllHosts = lMetadata.getAllHosts();
			getChildren().clear();
			for (Host bHost : lAllHosts)
			{
				HostTreeItem lHostTreeItem = new HostTreeItem(bHost);
				getChildren().add(lHostTreeItem);
			}
		}
		catch (Throwable pThrowable)
		{
			pThrowable.printStackTrace(System.err);
			Alert lAlert = ThrowableStackTraceAlertFactory.createAlert(pThrowable);
			lAlert.showAndWait();
		}
	}

	@Override
	public boolean isLeaf()
	{
		return false;
	}

}
