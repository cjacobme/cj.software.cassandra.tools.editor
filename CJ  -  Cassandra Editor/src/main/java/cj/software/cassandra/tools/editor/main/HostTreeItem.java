package cj.software.cassandra.tools.editor.main;

import com.datastax.driver.core.Host;

import javafx.scene.control.TreeItem;

public class HostTreeItem
		extends TreeItem<String>
{
	private Host host;

	public HostTreeItem(Host pHost)
	{
		super(
				String.format(
						"%s (%s)",
						pHost.getAddress().getHostName(),
						pHost.getAddress().getHostAddress()));
		this.host = pHost;
	}

	public Host getHost()
	{
		return this.host;
	}
}
