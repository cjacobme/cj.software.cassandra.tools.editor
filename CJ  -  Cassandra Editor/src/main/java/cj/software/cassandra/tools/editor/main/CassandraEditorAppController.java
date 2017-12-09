package cj.software.cassandra.tools.editor.main;

import java.net.URL;
import java.util.List;
import java.util.prefs.BackingStoreException;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ExecutionInfo;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;

import cj.software.cassandra.tools.editor.connection.ConnectionDialogController;
import cj.software.cassandra.tools.editor.connection.KeyspacesSelectDialogController;
import cj.software.cassandra.tools.editor.modell.Connection;
import cj.software.cassandra.tools.editor.storage.RecentConnectionsRepository;
import cj.software.javafx.ThrowableStackTraceAlertFactory;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CassandraEditorAppController
{
	private CassandraEditorApp main;

	private ObjectProperty<Connection> connectionProperty = new SimpleObjectProperty<>();

	private ObjectProperty<Cluster> clusterProperty = new SimpleObjectProperty<>();

	private RecentConnectionsRepository recentConnectionsRepository = new RecentConnectionsRepository();

	private ObjectProperty<Session> sessionProperty = new SimpleObjectProperty<>();

	@FXML
	private SeparatorMenuItem connectionBeforeRecentList;

	@FXML
	private SeparatorMenuItem beforeRecentKeyspaces;

	@FXML
	private Menu keyspacesMenu;

	@FXML
	private TextArea command;

	@FXML
	private Button executeCql;

	void setMain(CassandraEditorApp pMain)
	{
		this.main = pMain;
	}

	@FXML
	private void initialize()
	{
		try
		{
			this.insertRecents();
			this.keyspacesMenu.disableProperty().bind(this.connectionProperty.isNull());
			this.command.disableProperty().bind(this.sessionProperty.isNull());
			this.command.editableProperty().bind(this.sessionProperty.isNotNull());
			this.executeCql.disableProperty().bind(
					this.sessionProperty.isNull().or(this.command.textProperty().isEmpty()));
		}
		catch (Throwable pThrowable)
		{
			pThrowable.printStackTrace(System.err);
			Alert lAlert = ThrowableStackTraceAlertFactory.createAlert(pThrowable);
			lAlert.showAndWait();
		}
	}

	private void insertRecents() throws BackingStoreException
	{
		List<Connection> lRecents = this.recentConnectionsRepository.readRecents();
		Menu lParentMenu = this.connectionBeforeRecentList.getParentMenu();
		ObservableList<MenuItem> lItems = lParentMenu.getItems();
		int lInsertPosition = lItems.indexOf(this.connectionBeforeRecentList) + 1;
		for (Connection bRecent : lRecents)
		{
			MenuItem lNewItem = new MenuItem(bRecent.getHostname());
			lNewItem.setOnAction(new EventHandler<ActionEvent>()
			{

				@Override
				public void handle(ActionEvent pEvent)
				{
					try
					{
						setConnection(bRecent);
					}
					catch (Throwable pThrowable)
					{
						pThrowable.printStackTrace(System.err);
						Alert lAlert = ThrowableStackTraceAlertFactory.createAlert(pThrowable);
						lAlert.showAndWait();
					}
				}
			});
			lItems.add(lInsertPosition, lNewItem);
			lInsertPosition++;
		}
	}

	private void clearRecentKeyspaces()
	{
		Menu lParentMenu = this.beforeRecentKeyspaces.getParentMenu();
		ObservableList<MenuItem> lItems = lParentMenu.getItems();
		int lSeparatorIndex = lItems.indexOf(this.beforeRecentKeyspaces);
		int lNumItems = lItems.size();
		for (int bI = lNumItems - 1; bI > lSeparatorIndex; bI--)
		{
			lItems.remove(bI);
		}
	}

	private void setRecentKeyspaces(Connection pConnection)
	{
		this.clearRecentKeyspaces();
		Menu lParentMenu = this.beforeRecentKeyspaces.getParentMenu();
		ObservableList<MenuItem> lItems = lParentMenu.getItems();
		int lInsertPosition = lItems.indexOf(this.beforeRecentKeyspaces) + 1;
		List<String> lKeyspaces = pConnection.getKeyspaces();
		for (String bKeyspace : lKeyspaces)
		{
			MenuItem lNewItem = new MenuItem(bKeyspace);
			lNewItem.setOnAction(a ->
			{
				try
				{
					openSession(bKeyspace);
				}
				catch (Throwable pThrowable)
				{
					pThrowable.printStackTrace(System.err);
					Alert lAlert = ThrowableStackTraceAlertFactory.createAlert(pThrowable);
					lAlert.showAndWait();
				}
			});
			lItems.add(lInsertPosition, lNewItem);
			lInsertPosition++;
		}
	}

	public Connection getConnection()
	{
		return this.connectionProperty.get();
	}

	public void setConnection(Connection pConnection) throws BackingStoreException
	{
		this.connectionProperty.set(pConnection);
		String lTitle = (pConnection != null
				? String.format(
						CassandraEditorApp.FMT_TITLE_HOST_CONNECTED,
						pConnection.getHostname())
				: CassandraEditorApp.INITIAL_TITLE);
		if (pConnection != null)
		{
			this.recentConnectionsRepository.save(pConnection);
		}
		this.main.getPrimaryStage().setTitle(lTitle);
		Session lSession = this.getSession();
		if (lSession != null && !lSession.isClosed())
		{
			lSession.close();
		}
		Cluster lCluster = this.getCluster();
		if (lCluster != null && !lCluster.isClosed())
		{
			lCluster.close();
		}
		if (pConnection != null)
		{
			lCluster = Cluster.builder().addContactPoint(pConnection.getHostname()).build();
			this.setCluster(lCluster);
			this.setRecentKeyspaces(pConnection);
		}
	}

	public ObjectProperty<Connection> connectionProperty()
	{
		return this.connectionProperty;
	}

	public void setCluster(Cluster pCluster)
	{
		this.clusterProperty.set(pCluster);
	}

	public Cluster getCluster()
	{
		return this.clusterProperty.get();
	}

	public ObjectProperty<Cluster> clusterProperty()
	{
		return this.clusterProperty;
	}

	public void setSession(Session pSession)
	{
		this.sessionProperty.set(pSession);
	}

	public Session getSession()
	{
		return this.sessionProperty.get();
	}

	public ObjectProperty<Session> sessionProperty()
	{
		return this.sessionProperty;
	}

	@FXML
	private void handleExit()
	{
		System.exit(0);
	}

	@FXML
	private void handleConnectToHost()
	{
		try
		{
			String lFxmlFile = "ConnectionDialog.fxml";
			URL lURL = ConnectionDialogController.class.getResource(lFxmlFile);
			if (lURL == null)
			{
				throw new IllegalArgumentException(lFxmlFile + " not in Classpath");
			}
			FXMLLoader lLoader = new FXMLLoader(lURL);
			AnchorPane lAnchorPane = lLoader.load();

			Stage lDialogStage = new Stage();
			lDialogStage.setTitle("New Connection");
			lDialogStage.initModality(Modality.WINDOW_MODAL);
			lDialogStage.initOwner(this.main.getPrimaryStage());

			Scene lScene = new Scene(lAnchorPane);
			lDialogStage.setScene(lScene);

			ConnectionDialogController lController = lLoader.getController();
			lController.setDialogStage(lDialogStage);

			lDialogStage.showAndWait();

			if (lController.isOkClicked())
			{
				Connection lConnection = lController.getConnection();
				this.setConnection(lConnection);
			}

		}
		catch (Throwable pThrowable)
		{
			Alert lAlert = ThrowableStackTraceAlertFactory.createAlert(pThrowable);
			lAlert.showAndWait();
		}
	}

	@FXML
	private void handleSelectKeyspace()
	{
		try
		{
			String lFxmlFile = "KeyspacesSelectDialog.fxml";
			URL lURL = KeyspacesSelectDialogController.class.getResource(lFxmlFile);
			if (lURL == null)
			{
				throw new IllegalArgumentException(lFxmlFile + " not found in Classpath");
			}
			FXMLLoader lLoader = new FXMLLoader(lURL);
			Pane lPane = lLoader.load();

			Stage lDialogStage = new Stage();
			lDialogStage.setTitle("Select Keyspace");
			lDialogStage.initModality(Modality.WINDOW_MODAL);
			lDialogStage.initOwner(this.main.getPrimaryStage());

			Scene lScene = new Scene(lPane);
			lDialogStage.setScene(lScene);

			KeyspacesSelectDialogController lController = lLoader.getController();
			lController.setDialogStage(lDialogStage);
			lController.setCluster(this.getCluster());

			lDialogStage.showAndWait();

			KeyspaceMetadata lSelectedKeyspace = lController.getSelectedKeyspace();
			if (lSelectedKeyspace != null)
			{
				String lKeyspaceName = lSelectedKeyspace.getName();
				this.openSession(lKeyspaceName);
			}
		}
		catch (Throwable pThrowable)
		{
			pThrowable.printStackTrace(System.err);
			Alert lAlert = ThrowableStackTraceAlertFactory.createAlert(pThrowable);
			lAlert.showAndWait();
		}
	}

	public void openSession(String pKeyspaceName) throws BackingStoreException
	{
		Connection lConnection = this.getConnection();
		lConnection.addKeyspace(pKeyspaceName);
		this.recentConnectionsRepository.save(lConnection);
		this.setRecentKeyspaces(lConnection);

		Session lCurrentSession = this.getSession();
		if (lCurrentSession != null && !lCurrentSession.isClosed())
		{
			lCurrentSession.close();
		}
		lCurrentSession = this.getCluster().connect(pKeyspaceName);
		this.setSession(lCurrentSession);

		String lTitle = String.format(
				CassandraEditorApp.FMT_TITLE_HOST_KEYSPACE_CONNECTED,
				lConnection.getHostname(),
				pKeyspaceName);
		this.main.getPrimaryStage().setTitle(lTitle);
	}

	@FXML
	private void executeCql()
	{
		try
		{
			String lContent = this.command.getText().trim();
			if (lContent.length() > 0)
			{
				ResultSet lRS = this.getSession().execute(lContent);
				ExecutionInfo lExecInfo = lRS.getExecutionInfo();
				Statement lStatement = lExecInfo.getStatement();
			}
			else
			{
				Alert lAlert = new Alert(AlertType.WARNING);
				lAlert.setTitle("No command entered");
				lAlert.setContentText("Please enter a command");
				lAlert.showAndWait();
			}

		}
		catch (Throwable pThrowable)
		{
			pThrowable.printStackTrace(System.err);
			Alert lAlert = ThrowableStackTraceAlertFactory.createAlert(pThrowable);
			lAlert.showAndWait();
		}
	}
}
