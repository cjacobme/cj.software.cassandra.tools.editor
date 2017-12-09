package cj.software.cassandra.tools.editor.main;

import java.net.URL;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class CassandraEditorApp
		extends Application
{
	private Pane rootPane;

	public static String INITIAL_TITLE = "Cassandra Editor - by CJ";

	public static String FMT_TITLE_HOST_CONNECTED = "Cassandra Editor - Host %s";

	public static final String FMT_TITLE_HOST_KEYSPACE_CONNECTED = "Cassandra Editor - Host %s Keyspace %s";

	private Stage primaryStage;

	public static void main(String[] pArgs)
	{
		Application.launch(pArgs);
	}

	@Override
	public void start(Stage pPrimaryStage) throws Exception
	{
		this.primaryStage = pPrimaryStage;
		this.primaryStage.setTitle(INITIAL_TITLE);
		this.primaryStage.setOnCloseRequest(e ->
		{
			Platform.exit();
			System.exit(0);
		});

		String lFxmlFilename = "CassandraEditorApp.fxml";
		URL lURL = this.getClass().getResource(lFxmlFilename);
		if (lURL == null)
		{
			throw new NullPointerException("Resource \"" + lFxmlFilename + "\" not found");
		}

		FXMLLoader lLoader = new FXMLLoader(lURL);
		this.rootPane = lLoader.load();
		Scene lScene = new Scene(this.rootPane);

		CassandraEditorAppController lController = lLoader.getController();
		lController.setMain(this);

		this.primaryStage.setScene(lScene);
		lController.initializeAccelorators(lScene);
		this.primaryStage.show();
	}

	public Stage getPrimaryStage()
	{
		return this.primaryStage;
	}
}
