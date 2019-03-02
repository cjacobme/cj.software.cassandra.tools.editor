package cj.software.cassandra.migration;

import java.time.Duration;
import java.time.Instant;

import org.apache.log4j.Logger;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;
import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.core.PreparedStatement;
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
		PreparedStatement lPrepInsert = pSession.prepare(
				"INSERT INTO marktdaten "
						+ "(service_provider, lieferzone, marktpreiskurve, von, bis, "
						+ "prognose, migrated, wert)  "
						+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
		PreparedStatement lPrepDelete = pSession.prepare(
				"DELETE FROM marktdaten "
						+ "WHERE service_provider = ? "
						+ "AND lieferzone = ? "
						+ "AND marktpreiskurve = ? "
						+ "AND von = ? "
						+ "AND bis = ? "
						+ "AND prognose = ?");
		this.iterate(pSession, lPrepDelete, lPrepInsert);
	}

	private void iterate(
			Session pSession,
			PreparedStatement pPrepDelete,
			PreparedStatement pPrepInsert)
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
			if (!lMigrated)
			{
				String lServiceProvider = bRow.getString("service_provider");
				String lLieferzone = bRow.getString("lieferzone");
				String lMarktpreisekurve = bRow.getString("marktpreiskurve");
				Instant lVon = bRow.get("von", Instant.class);
				Instant lBis = bRow.get("bis", Instant.class);

				Instant lPrognoseCorrected = lPrognose.minus(Duration.ofDays(1));
				BatchStatement lBatch = new BatchStatement();
				BoundStatement lBoundInsert = pPrepInsert.bind(
						lServiceProvider,
						lLieferzone,
						lMarktpreisekurve,
						lVon,
						lBis,
						lPrognoseCorrected,
						true,
						lWert);
				lBatch.add(lBoundInsert);
				BoundStatement lBoundDelete = pPrepDelete.bind(
						lServiceProvider,
						lLieferzone,
						lMarktpreisekurve,
						lVon,
						lBis,
						lPrognose);
				lBatch.add(lBoundDelete);
				pSession.execute(lBatch);
				this.logger.info(
						String.format(
								"corrected prognose from %s to %s",
								lPrognose,
								lPrognoseCorrected));
			}
		}
	}
}
