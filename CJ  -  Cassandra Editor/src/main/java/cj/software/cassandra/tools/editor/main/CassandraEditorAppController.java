package cj.software.cassandra.tools.editor.main;

import java.net.URL;

import cj.software.cassandra.tools.editor.connection.ConnectionDialogController;
import cj.software.cassandra.tools.editor.modell.Connection;
import cj.software.javafx.ThrowableStackTraceAlertFactory;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CassandraEditorAppController
{
	private CassandraEditorApp main;

	private ObjectProperty<Connection> connectionProperty = new SimpleObjectProperty<>();

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
	}

	public Connection getConnection()
	{
		return this.connectionProperty.get();
	}

	public void setConnection(Connection pConnection)
	{
		this.connectionProperty.set(pConnection);
		this.keyspacesMenu.setDisable(pConnection == null);
		String lTitle = (pConnection != null
				? String.format(
						CassandraEditorApp.FMT_TITLE_HOST_CONNECTED,
						pConnection.getHostname())
				: CassandraEditorApp.INITIAL_TITLE);
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
