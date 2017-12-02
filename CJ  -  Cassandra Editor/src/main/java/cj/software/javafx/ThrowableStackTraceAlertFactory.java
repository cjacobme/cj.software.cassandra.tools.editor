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
		Alert lResult = new Alert(AlertType.ERROR);
		lResult.setTitle("Connection failed");
		lResult.setHeaderText("Exception in Connection attempt");
		lResult.setContentText(
				"You caught a "
						+ pThrowable.getClass().getSimpleName()
						+ ": "
						+ pThrowable.getMessage());
		String lStackTrace = toStackTrace(pThrowable);
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

		Throwable lThrowable = pThrowable;
		lSB = toStackTrace(lSB, lThrowable, "");

		lThrowable = lThrowable.getCause();
		while (lThrowable != null)
		{
			lSB = toStackTrace(lSB, lThrowable, "caused by ");
			lThrowable = lThrowable.getCause();
		}

		return lSB.toString();
	}

	private static StringBuilder toStackTrace(
			StringBuilder pSource,
			Throwable pThrowable,
			String pIntro)
	{
		StackTraceElement[] lStackTrace = pThrowable.getStackTrace();
		pSource
				.append(pIntro)
				.append(pThrowable.getClass().getName())
				.append(": ")
				.append(pThrowable.getMessage())
				.append("\r\n");
		for (StackTraceElement bElem : lStackTrace)
		{
			pSource.append("\tat ").append(bElem.toString()).append("\r\n");
		}
		return pSource;
	}
}
