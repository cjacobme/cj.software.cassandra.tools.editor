package cj.software.cassandra.migration;

import java.time.Instant;

import org.apache.log4j.Logger;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;
import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.extras.codecs.jdk8.InstantCodec;
import com.datastax.driver.extras.codecs.jdk8.LocalDateCodec;

public class IterateNotMigrated
{
	private Logger logger = Logger.getLogger(IterateNotMigrated.class);

	public static void main(String[] pArgs)
	{
		int lExit = -1;
		try
		{
			IterateNotMigrated lInstance = new IterateNotMigrated();
			lInstance.iterate();
			lExit = 0;
		}
		catch (Throwable pThrowable)
		{
			pThrowable.printStackTrace(System.err);
		}
		System.exit(lExit);
	}

	private void iterate()
	{
		Builder lBuilder = Cluster.builder().addContactPoint("localhost");
		try (Cluster lCluster = lBuilder.build())
		{
			CodecRegistry lCodecRegistry = lCluster.getConfiguration().getCodecRegistry();
			lCodecRegistry.register(InstantCodec.instance).register(LocalDateCodec.instance);
			try (Session lSession = lCluster.connect("marktdaten"))
			{
				this.iterate(lSession);
			}
		}
	}

	private void iterate(Session pSession)
	{
		String lQuery = "SELECT service_provider, lieferzone, marktpreiskurve, von, bis, prognose, "
				+ "false_if_null(migrated) as migrated, "
				+ "wert "
				+ "from marktdaten";
		ResultSet lRS = pSession.execute(lQuery);
		for (Row bRow : lRS)
		{
			Instant lPrognose = bRow.get("prognose", Instant.class);
			double lWert = bRow.getDouble("wert");
			boolean lMigrated = bRow.getBool("migrated");
			this.logger.info(
					String.format(
							"Prognose: %s, Wert: %8.3f, migriert: %s",
							lPrognose,
							lWert,
							String.valueOf(lMigrated)));
		}
	}
}
