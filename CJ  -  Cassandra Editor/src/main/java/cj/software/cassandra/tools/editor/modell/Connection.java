package cj.software.cassandra.tools.editor.modell;

import java.io.Serializable;

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
