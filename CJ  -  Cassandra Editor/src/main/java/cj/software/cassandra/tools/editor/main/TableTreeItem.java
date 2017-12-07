package cj.software.cassandra.tools.editor.main;

import com.datastax.driver.core.TableMetadata;

import javafx.scene.control.TreeItem;

public class TableTreeItem
		extends TreeItem<String>
{
	public TableTreeItem(TableMetadata pTableMetadata)
	{
		super(pTableMetadata.getName());
	}
}
