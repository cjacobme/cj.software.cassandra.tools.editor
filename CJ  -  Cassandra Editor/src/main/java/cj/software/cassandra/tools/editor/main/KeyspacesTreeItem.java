package cj.software.cassandra.tools.editor.main;

import java.util.List;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.KeyspaceMetadata;

import cj.software.javafx.ThrowableStackTraceAlertFactory;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Alert;
import javafx.scene.control.TreeItem;

public class KeyspacesTreeItem
		extends TreeItem<String>
{
	public KeyspacesTreeItem()
	{
		super("Keyspaces");
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
					showKeysets();
				}
			}
		});
	}

	@Override
	public boolean isLeaf()
	{
		return false;
	}

	private void showKeysets()
	{
		try
		{
			ClusterTreeItem lClusterTreeItem = (ClusterTreeItem) super.getParent();
			Cluster lCluster = lClusterTreeItem.getCluster();
			super.getChildren().clear();
			List<KeyspaceMetadata> lKeyspaces = lCluster.getMetadata().getKeyspaces();
			for (KeyspaceMetadata bKeyspace : lKeyspaces)
			{
				KeyspaceMetaTreeItem lTreeItem = new KeyspaceMetaTreeItem(bKeyspace);
				super.getChildren().add(lTreeItem);
			}
		}
		catch (Throwable pThrowable)
		{
			pThrowable.printStackTrace(System.err);
			Alert lAlert = ThrowableStackTraceAlertFactory.createAlert(pThrowable);
			lAlert.showAndWait();
		}
	}
}
