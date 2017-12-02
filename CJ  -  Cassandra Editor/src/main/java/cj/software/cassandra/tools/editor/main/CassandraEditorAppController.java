package cj.software.cassandra.tools.editor.main;

import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class CassandraEditorAppController
{
	private CassandraEditorApp main;

	@FXML
	private TreeView<String> connectionsTree;

	void setMain(CassandraEditorApp pMain)
	{
		this.main = pMain;
	}

	@FXML
	private void initialize()
	{
		this.connectionsTree.setRoot(new TreeItem<String>("Connections"));
	}

	@FXML
	private void handleExit()
	{
		System.exit(0);
	}
}
