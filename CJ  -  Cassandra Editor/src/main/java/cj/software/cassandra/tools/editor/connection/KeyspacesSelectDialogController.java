package cj.software.cassandra.tools.editor.connection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.KeyspaceMetadata;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public class KeyspacesSelectDialogController
{
	private Stage dialogStage;

	private Cluster cluster;

	private KeyspaceMetadata selectedKeyspace;

	private Map<String, KeyspaceMetadata> keyspaces = new HashMap<>();

	@FXML
	private ListView<String> keyspacesList;

	public void setDialogStage(Stage pDialogStage)
	{
		this.dialogStage = pDialogStage;
	}

	public void setCluster(Cluster pCluster)
	{
		this.cluster = pCluster;
		this.reloadKeyspaces();
	}

	@FXML
	private void handleCancelBtn()
	{
		this.dialogStage.close();
	}

	@FXML
	private void initialize()
	{
		this.selectedKeyspace = null;
	}

	@FXML
	private void handleOk()
	{
		String lSelectedName = this.keyspacesList.getSelectionModel().getSelectedItem();
		if (lSelectedName != null)
		{
			this.selectedKeyspace = this.keyspaces.get(lSelectedName);
			this.dialogStage.close();
		}
	}

	@FXML
	private void reloadKeyspaces()
	{
		List<KeyspaceMetadata> lKeyspaces = this.cluster.getMetadata().getKeyspaces();
		ObservableList<String> lItems = this.keyspacesList.getItems();
		lItems.clear();
		this.keyspaces.clear();
		for (KeyspaceMetadata bMeta : lKeyspaces)
		{
			String lName = bMeta.getName();
			this.keyspaces.put(lName, bMeta);
			lItems.add(lName);
		}
	}

	public KeyspaceMetadata getSelectedKeyspace()
	{
		return this.selectedKeyspace;
	}
}
