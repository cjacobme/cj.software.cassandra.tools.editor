package cj.software.cassandra.tools.editor.main;

import com.datastax.driver.core.KeyspaceMetadata;

import javafx.scene.control.TreeItem;

public class KeyspaceMetaTreeItem
		extends TreeItem<String>
{
	private KeyspaceMetadata keyspaceMetadata;

	public KeyspaceMetaTreeItem(KeyspaceMetadata pMetadata)
	{
		super(pMetadata.getName());
		this.keyspaceMetadata = pMetadata;
		this.getChildren().add(new TablesTreeItem(pMetadata));
	}

	public KeyspaceMetadata getKeyspaceMetadata()
	{
		return this.keyspaceMetadata;
	}
}
