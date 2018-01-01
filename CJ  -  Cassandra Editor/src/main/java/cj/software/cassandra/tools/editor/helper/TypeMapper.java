package cj.software.cassandra.tools.editor.helper;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.datastax.driver.core.ColumnDefinitions.Definition;
import com.datastax.driver.core.DataType.Name;
import com.datastax.driver.core.Row;

import javafx.scene.control.TableColumn;

public class TypeMapper
{
	private static final String FIXED_FONT = "-fx-font-family: \"Courier New\";";
	private static final String FIXED_FONT_RIGHT = "-fx-alignment: CENTER-RIGHT;-fx-font-family: \"Courier New\";";

	@SuppressWarnings("rawtypes")
	public static <S, T> TableColumn<S, ?> createTableColumn(Definition pDefinition)
	{
		TableColumn<S, ?> lResult;
		Name lName = pDefinition.getType().getName();
		switch (lName)
		{
		case DOUBLE:
			lResult = new TableColumn<S, Double>();
			lResult.setStyle(FIXED_FONT_RIGHT);
			break;
		case TIMESTAMP:
			lResult = new TableColumn<S, Instant>();
			lResult.setStyle(FIXED_FONT);
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
			lResult.setStyle(FIXED_FONT_RIGHT);
			break;
		case SMALLINT:
			lResult = new TableColumn<S, Short>();
			lResult.setStyle(FIXED_FONT_RIGHT);
			break;
		case DATE:
			lResult = new TableColumn<S, LocalDate>();
			lResult.setStyle(FIXED_FONT);
			break;
		case MAP:
			lResult = new TableColumn<S, Map>();
			break;
		case SET:
			lResult = new TableColumn<S, Set>();
			break;
		case LIST:
			lResult = new TableColumn<S, List>();
			break;
		case BOOLEAN:
			lResult = new TableColumn<S, Boolean>();
			break;
		case INT:
			lResult = new TableColumn<S, Integer>();
			lResult.setStyle(FIXED_FONT_RIGHT);
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
		case DATE:
			lResult = pRow.get(pIndex, LocalDate.class);
			break;
		case MAP:
			lResult = pRow.get(pIndex, Map.class);
			break;
		case SET:
			lResult = pRow.get(pIndex, Set.class);
			break;
		case LIST:
			lResult = pRow.get(pIndex, List.class);
			break;
		case BOOLEAN:
			lResult = pRow.getBool(pIndex);
			break;
		case INT:
			lResult = pRow.getInt(pIndex);
			break;
		default:
			throw new UnsupportedOperationException("not yet implemented: " + lName);
		}
		return lResult;
	}

}
