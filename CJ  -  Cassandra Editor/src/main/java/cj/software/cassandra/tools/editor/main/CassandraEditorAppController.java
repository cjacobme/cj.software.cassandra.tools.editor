package cj.software.cassandra.tools.editor.main;

import java.net.URL;

import com.datastax.driver.core.Cluster;

import cj.software.cassandra.tools.editor.connection.ConnectionDialogController;
import cj.software.javafx.ThrowableStackTraceAlertFactory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CassandraEditorAppController
{
	private CassandraEditorApp main;

	@FXML
	private TreeView<String> connectionsTree;

	void setMain(CassandraEditorApp pMain)
	{
		this.main = pMain;
	}

	@FXML
	private void initialize()
	{
		this.connectionsTree.setRoot(new TreeItem<String>("Connections"));
	}

	@FXML
	private void handleExit()
	{
		System.exit(0);
	}

	@FXML
	private void handleNewConnection()
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
				Cluster lCluster = lController.getCluster();
				// TODO insert cluster into tree if it is not already there
			}

		}
		catch (Throwable pThrowable)
		{
			Alert lAlert = ThrowableStackTraceAlertFactory.createAlert(pThrowable);
			lAlert.showAndWait();
		}
	}
}
