package cj.software.cassandra.tools.editor.main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.builder.CompareToBuilder;

import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.TableMetadata;

import javafx.scene.control.TreeItem;

public class TablesTreeItem
		extends TreeItem<String>
{
	public TablesTreeItem(KeyspaceMetadata pMetadata)
	{
		super("Tables");

		Collection<TableMetadata> lTables = pMetadata.getTables();
		List<TableMetadata> lSorted = new ArrayList<>(lTables);
		Collections.sort(lSorted, new Comparator<TableMetadata>()
		{

			@Override
			public int compare(TableMetadata pMeta1, TableMetadata pMeta2)
			{
				CompareToBuilder lBuilder = new CompareToBuilder().append(
						pMeta1.getName(),
						pMeta2.getName());
				int lResult = lBuilder.build();
				return lResult;
			}
		});
		for (TableMetadata bMeta : lSorted)
		{
			super.getChildren().add(new TableTreeItem(bMeta));
		}
	}
}
