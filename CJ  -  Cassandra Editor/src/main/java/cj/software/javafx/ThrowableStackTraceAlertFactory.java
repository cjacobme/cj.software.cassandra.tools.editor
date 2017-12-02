package cj.software.javafx;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class ThrowableStackTraceAlertFactory
{
	public static Alert createAlert(Throwable pThrowable)
	{
		Throwable lRootCause = toRootOfProblem(pThrowable);
		Alert lResult = new Alert(AlertType.ERROR);
		lResult.setTitle("Connection failed");
		lResult.setHeaderText("Exception in Connection attempt");
		lResult.setContentText(
				"You caught a "
						+ lRootCause.getClass().getSimpleName()
						+ ": "
						+ lRootCause.getMessage());
		String lStackTrace = toStackTrace(lRootCause);
		Label lLabel = new Label("The exception stacktrace was:");

		TextArea lTextArea = new TextArea(lStackTrace);
		lTextArea.setEditable(false);
		lTextArea.setWrapText(true);

		lTextArea.setMaxWidth(Double.MAX_VALUE);
		lTextArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(lTextArea, Priority.ALWAYS);
		GridPane.setHgrow(lTextArea, Priority.ALWAYS);

		GridPane lExpContent = new GridPane();
		lExpContent.setMaxWidth(Double.MAX_VALUE);
		lExpContent.add(lLabel, 0, 0);
		lExpContent.add(lTextArea, 0, 1);

		// Set expandable Exception into the dialog pane.
		lResult.getDialogPane().setExpandableContent(lExpContent);
		return lResult;
	}

	private static String toStackTrace(Throwable pThrowable)
	{
		StringBuilder lSB = new StringBuilder();
		StackTraceElement[] lStackTrace = pThrowable.getStackTrace();
		String lIntro = "";
		for (StackTraceElement bElem : lStackTrace)
		{
			lSB.append(lIntro).append(bElem.toString()).append("\r\n");
			lIntro = "\tat ";
		}
		return lSB.toString();
	}

	private static Throwable toRootOfProblem(Throwable pThrowable)
	{
		Throwable lResult = pThrowable;
		while (lResult.getCause() != null)
		{
			lResult = lResult.getCause();
		}
		return lResult;
	}
}
