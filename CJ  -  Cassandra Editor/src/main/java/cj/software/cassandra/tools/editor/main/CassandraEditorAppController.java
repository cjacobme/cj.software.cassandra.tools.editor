package cj.software.cassandra.tools.editor.main;

import java.net.URL;

import cj.software.cassandra.tools.editor.connection.ConnectionDialogController;
import cj.software.cassandra.tools.editor.modell.Connection;
import cj.software.javafx.ThrowableStackTraceAlertFactory;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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

	private RootItem rootItem;

	void setMain(CassandraEditorApp pMain)
	{
		this.main = pMain;
	}

	@FXML
	private void initialize()
	{
		this.rootItem = new RootItem();
		this.connectionsTree.setRoot(this.rootItem);
		this.connectionsTree.getSelectionModel().selectedItemProperty().addListener(
				new ChangeListener<TreeItem<String>>()
				{

					@Override
					public void changed(
							ObservableValue<? extends TreeItem<String>> pObservable,
							TreeItem<String> pOldValue,
							TreeItem<String> pNewValue)
					{
						String lOldDesc = (pOldValue != null
								? pOldValue.getClass().getSimpleName()
								: "<null>");
						String lNewDesc = (pNewValue != null
								? pNewValue.getClass().getSimpleName()
								: "<null>");
						System.out.println("change " + lOldDesc + " to " + lNewDesc);
					}
				});
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
				Connection lConnection = lController.getConnection();
				ConnectionTreeItem lConnectionTreeItem = new ConnectionTreeItem(lConnection);
				this.rootItem.getChildren().add(lConnectionTreeItem);
			}

		}
		catch (Throwable pThrowable)
		{
			Alert lAlert = ThrowableStackTraceAlertFactory.createAlert(pThrowable);
			lAlert.showAndWait();
		}
	}
}
