package cj.software.cassandra.tools.editor.connection;

import com.datastax.driver.core.Cluster;

import cj.software.javafx.ThrowableStackTraceAlertFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ConnectionDialogController
{
	private Stage dialogStage;

	@FXML
	private TextField hostname;

	@FXML
	private TextField userId;

	@FXML
	private PasswordField password;

	private boolean okClicked = false;

	private Cluster cluster;

	@FXML
	private void handleTestBtn()
	{
		String lHostname = this.hostname.getText();
		Alert lAlert;
		try
		{
			Cluster lBuild = Cluster.builder().addContactPoint(lHostname).build();
			if (lBuild != null)
			{
				lAlert = new Alert(AlertType.INFORMATION);
				lAlert.setTitle("Connection OK");
				lAlert.setHeaderText("Successfully checked connection");
				lAlert.setContentText("You successfully connected to host \"" + lHostname + "\"");
			}
			else
			{
				lAlert = new Alert(AlertType.WARNING);
				lAlert.setTitle("Connection failed");
				lAlert.setContentText("You could not connect to host \"" + lHostname + "\"");
			}
		}
		catch (RuntimeException pRuntimeException)
		{
			lAlert = ThrowableStackTraceAlertFactory.createAlert(pRuntimeException);
		}
		lAlert.showAndWait();
	}

	@FXML
	private void handleCancelBtn()
	{
		this.dialogStage.close();
	}

	public void setDialogStage(Stage pDialogStage)
	{
		this.dialogStage = pDialogStage;
	}

	@FXML
	private void handleOkayBtn()
	{
		try
		{
			String lHostname = this.hostname.getText();
			Cluster lCluster = Cluster.builder().addContactPoint(lHostname).build();
			if (lCluster != null)
			{
				this.okClicked = true;
				this.cluster = lCluster;
				this.dialogStage.close();
			}
		}
		catch (RuntimeException pRuntimeException)
		{
			Alert lAlert = ThrowableStackTraceAlertFactory.createAlert(pRuntimeException);
			lAlert.showAndWait();
		}
	}

	public boolean isOkClicked()
	{
		return this.okClicked;
	}

	public Cluster getCluster()
	{
		return this.cluster;
	}
}
