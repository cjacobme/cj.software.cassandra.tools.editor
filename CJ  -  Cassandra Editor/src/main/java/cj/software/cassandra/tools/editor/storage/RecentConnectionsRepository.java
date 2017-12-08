package cj.software.cassandra.tools.editor.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import cj.software.cassandra.tools.editor.modell.Connection;

public class RecentConnectionsRepository
{
	private static final String KEY_HOSTNAME = "hostname";
	private static final String KEY_USERID = "user-id";
	private static final String KEY_PASSWORD = "password";
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
		int lCounter = 1;
		for (Connection bConnection : pConnectionsList)
		{
			Preferences lChildNode = lRecentRoots.node(String.format("%d", lCounter));
			lChildNode.put(KEY_HOSTNAME, bConnection.getHostname());
			if (bConnection.getUserid() != null)
			{
				lChildNode.put(KEY_USERID, bConnection.getUserid());
			}
			if (bConnection.getPassword() != null)
			{
				lChildNode.put(KEY_PASSWORD, bConnection.getPassword());
			}
			lCounter++;
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
				String lHostname = lChildNode.get(KEY_HOSTNAME, null);
				if (lHostname == null)
				{
					throw new IllegalArgumentException(
							"hostname of " + lChildNode.absolutePath() + " is null");
				}
				String lUserId = lChildNode.get(KEY_USERID, null);
				String lPassword = lChildNode.get(KEY_PASSWORD, null);
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
