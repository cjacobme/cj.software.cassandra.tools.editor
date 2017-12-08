package cj.software.cassandra.tools.editor.main;

import java.net.URL;
import java.util.List;
import java.util.prefs.BackingStoreException;

import cj.software.cassandra.tools.editor.connection.ConnectionDialogController;
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
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CassandraEditorAppController
{
	private CassandraEditorApp main;

	private ObjectProperty<Connection> connectionProperty = new SimpleObjectProperty<>();

	private RecentConnectionsRepository recentConnectionsRepository = new RecentConnectionsRepository();

	@FXML
	private SeparatorMenuItem connectionBeforeRecentList;

	@FXML
	private Menu keyspacesMenu;

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
						Alert lAlert = ThrowableStackTraceAlertFactory.createAlert(pThrowable);
						lAlert.showAndWait();
					}
				}
			});
			lItems.add(lInsertPosition, lNewItem);
		}
	}

	public Connection getConnection()
	{
		return this.connectionProperty.get();
	}

	public void setConnection(Connection pConnection) throws BackingStoreException
	{
		this.connectionProperty.set(pConnection);
		this.keyspacesMenu.setDisable(pConnection == null);
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
	}

	public ObjectProperty<Connection> connectionProperty()
	{
		return this.connectionProperty;
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
}
