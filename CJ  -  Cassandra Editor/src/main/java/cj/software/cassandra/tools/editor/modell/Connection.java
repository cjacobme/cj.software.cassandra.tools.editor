package cj.software.cassandra.tools.editor.modell;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Connection
		implements
		Serializable
{
	private static final long serialVersionUID = 1L;

	private String hostname;

	private String userid;

	private String password;

	private List<String> keyspaces = new ArrayList<>();

	public Connection(String pHostname, String pUserid, String pPassword)
	{
		this.hostname = pHostname;
		this.userid = pUserid;
		this.password = pPassword;
	}

	public String getHostname()
	{
		return this.hostname;
	}

	public String getUserid()
	{
		return this.userid;
	}

	public String getPassword()
	{
		return this.password;
	}

	public List<String> getKeyspaces()
	{
		return Collections.unmodifiableList(this.keyspaces);
	}

	public void addKeyspace(String pKeyspace)
	{
		int lIndexOf = this.keyspaces.indexOf(pKeyspace);
		if (lIndexOf >= 0)
		{
			this.keyspaces.remove(lIndexOf);
		}
		this.keyspaces.add(0, pKeyspace);
	}

	@Override
	public int hashCode()
	{
		HashCodeBuilder lBuilder = new HashCodeBuilder().append(this.hostname);
		int lResult = lBuilder.build();
		return lResult;
	}

	@Override
	public boolean equals(Object pOther)
	{
		boolean lResult;
		if (pOther instanceof Connection)
		{
			Connection lOther = (Connection) pOther;
			EqualsBuilder lBuilder = new EqualsBuilder().append(this.hostname, lOther.hostname);
			lResult = lBuilder.build();
		}
		else
		{
			lResult = false;
		}
		return lResult;
	}
}
