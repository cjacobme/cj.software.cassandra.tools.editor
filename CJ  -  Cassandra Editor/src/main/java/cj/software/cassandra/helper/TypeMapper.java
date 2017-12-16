package cj.software.cassandra.helper;

import java.time.Instant;

import com.datastax.driver.core.ColumnDefinitions.Definition;
import com.datastax.driver.core.DataType.Name;
import com.datastax.driver.core.Row;

import javafx.scene.control.TableColumn;

public class TypeMapper
{

	public static <S, T> TableColumn<S, ?> createTableColumn(Definition pDefinition)
	{
		TableColumn<S, ?> lResult;
		Name lName = pDefinition.getType().getName();
		switch (lName)
		{
		case DOUBLE:
			lResult = new TableColumn<S, Double>();
			lResult.setStyle("-fx-alignment: CENTER-RIGHT;-fx-font-family: \"Courier New\";");
			break;
		case TIMESTAMP:
			lResult = new TableColumn<S, Instant>();
			lResult.setStyle("-fx-font-family: \"Courier New\";");
			break;
		case VARCHAR:
		case TEXT:
			lResult = new TableColumn<S, String>();
			break;
		case UUID:
			lResult = new TableColumn<S, String>();
			break;
		case FLOAT:
			lResult = new TableColumn<S, Float>();
			break;
		case SMALLINT:
			lResult = new TableColumn<S, Short>();
			break;
		default:
			throw new UnsupportedOperationException("not yet implemented: " + lName);
		}
		lResult.setText(pDefinition.getName());
		return lResult;
	}

	public static Object readValue(Row pRow, int pIndex, Definition pDefinition)
	{
		Name lName = pDefinition.getType().getName();
		Object lResult;
		switch (lName)
		{
		case DOUBLE:
			double lDoubleValue = pRow.getDouble(pIndex);
			lResult = new Double(lDoubleValue);
			break;
		case TIMESTAMP:
			lResult = pRow.get(pIndex, Instant.class);
			break;
		case VARCHAR:
		case TEXT:
			lResult = pRow.getString(pIndex);
			break;
		case UUID:
			lResult = pRow.getUUID(pIndex);
			break;
		case FLOAT:
			float lFloatValue = pRow.getFloat(pIndex);
			lResult = new Float(lFloatValue);
			break;
		case SMALLINT:
			short lShortValue = pRow.getShort(pIndex);
			lResult = new Short(lShortValue);
			break;
		default:
			throw new UnsupportedOperationException("not yet implemented: " + lName);
		}
		return lResult;
	}

}
