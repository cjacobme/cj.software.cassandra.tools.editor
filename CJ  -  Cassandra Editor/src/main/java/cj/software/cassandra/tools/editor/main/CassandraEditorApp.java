package cj.software.cassandra.tools.editor.main;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class CassandraEditorApp
		extends Application
{
	private Pane rootPane;

	private Stage primaryStage;

	public static void main(String[] pArgs)
	{
		Application.launch(pArgs);
	}

	@Override
	public void start(Stage pPrimaryStage) throws Exception
	{
		this.primaryStage = pPrimaryStage;
		this.primaryStage.setTitle("Cassandra Editor - by CJ");

		String lFxmlFilename = "CassandraEditorApp.fxml";
		URL lURL = this.getClass().getResource(lFxmlFilename);
		if (lURL == null)
		{
			throw new NullPointerException("Resource \"" + lFxmlFilename + "\" not found");
		}

		FXMLLoader lLoader = new FXMLLoader(lURL);
		this.rootPane = lLoader.load();

		CassandraEditorAppController lController = lLoader.getController();
		lController.setMain(this);

		Scene lScene = new Scene(this.rootPane);
		this.primaryStage.setScene(lScene);
		this.primaryStage.show();
	}

}
