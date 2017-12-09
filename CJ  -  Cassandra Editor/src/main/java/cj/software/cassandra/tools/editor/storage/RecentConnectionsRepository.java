package cj.software.cassandra.tools.editor.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import cj.software.cassandra.tools.editor.modell.Connection;

public class RecentConnectionsRepository
{
	private static final int MAX = 4;

	private static final String recentsRoot = PreferencesKeys.PREFERENCES_ROOT
			+ "/recent-connections";

	public void save(Connection pToBeSaved) throws BackingStoreException
	{
		List<Connection> lRecents = this.readRecents();
		if (!lRecents.isEmpty())
		{
			int lIndexOf = lRecents.indexOf(pToBeSaved);
			if (lIndexOf < 0)
			{
				int lSize = lRecents.size();
				if (lSize >= MAX)
				{
					lRecents.remove(lSize - 1);
				}
			}
			else
			{
				lRecents.remove(lIndexOf);
			}
		}
		lRecents.add(0, pToBeSaved);
		this.saveList(lRecents);
	}

	private void saveList(List<Connection> pConnectionsList)
	{
		Preferences lRecentRoots = Preferences.userRoot().node(recentsRoot);
		int lNumConnections = pConnectionsList.size();
		for (int bConnection = 0; bConnection < lNumConnections; bConnection++)
		{
			Preferences lChildNode = lRecentRoots.node(String.format("%d", bConnection));
			Connection lConnection = pConnectionsList.get(bConnection);
			lChildNode.put(PreferencesKeys.KEY_HOSTNAME, lConnection.getHostname());
			if (lConnection.getUserid() != null)
			{
				lChildNode.put(PreferencesKeys.KEY_USERID, lConnection.getUserid());
			}
			if (lConnection.getPassword() != null)
			{
				lChildNode.put(PreferencesKeys.KEY_PASSWORD, lConnection.getPassword());
			}
			this.saveKeyspaces(lChildNode, lConnection);
		}
	}

	private void saveKeyspaces(Preferences pParentNode, Connection pConnection)
	{
		Preferences lContainer = pParentNode.node(PreferencesKeys.PREFERENCES_KEYSPACES);
		List<String> lKeyspaces = pConnection.getKeyspaces();
		int lNumKeyspaces = lKeyspaces.size();
		for (int bKeyspace = 0; bKeyspace < lNumKeyspaces; bKeyspace++)
		{
			Preferences lChildNode = lContainer.node(String.format("%d", bKeyspace));
			lChildNode.put("keyspace-name", lKeyspaces.get(bKeyspace));
		}
	}

	public List<Connection> readRecents() throws BackingStoreException
	{
		Preferences lRecentRoots = Preferences.userRoot().node(recentsRoot);
		String[] lChildrenNames = lRecentRoots.childrenNames();
		List<Connection> lResult;
		if (lChildrenNames != null)
		{
			lResult = new ArrayList<>(lChildrenNames.length);
			for (String bChildName : lChildrenNames)
			{
				Preferences lChildNode = lRecentRoots.node(bChildName);
				String lHostname = lChildNode.get(PreferencesKeys.KEY_HOSTNAME, null);
				if (lHostname == null)
				{
					throw new IllegalArgumentException(
							"hostname of " + lChildNode.absolutePath() + " is null");
				}
				String lUserId = lChildNode.get(PreferencesKeys.KEY_USERID, null);
				String lPassword = lChildNode.get(PreferencesKeys.KEY_PASSWORD, null);
				Connection lConnection = new Connection(lHostname, lUserId, lPassword);
				lResult.add(lConnection);
			}
		}
		else
		{
			lResult = Collections.emptyList();
		}
		return lResult;
	}
}
