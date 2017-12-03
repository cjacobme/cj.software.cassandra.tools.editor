package cj.software.cassandra.tools.editor.modell;

import java.io.Serializable;

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
}
