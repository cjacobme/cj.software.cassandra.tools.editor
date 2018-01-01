package cj.software.cassandra.tools.editor.helper;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.datastax.driver.core.ColumnDefinitions.Definition;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.DataType.Name;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.UDTValue;

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
			List<DataType> lMapTypeArguments = pDefinition.getType().getTypeArguments();
			DataType lKeyType = lMapTypeArguments.get(0);
			Class<?> lKeyClass = TypeMapper.classFor(lKeyType);
			DataType lValuesType = lMapTypeArguments.get(1);
			Class<?> lValuesClass = TypeMapper.classFor(lValuesType);
			lResult = pRow.getMap(pIndex, lKeyClass, lValuesClass);
			break;
		case SET:
			List<DataType> lSetTypeArguments = pDefinition.getType().getTypeArguments();
			DataType lSetType = lSetTypeArguments.get(0);
			Class<?> lSetClass = TypeMapper.classFor(lSetType);
			lResult = pRow.getSet(pIndex, lSetClass);
			break;
		case LIST:
			List<DataType> lListTypeArguments = pDefinition.getType().getTypeArguments();
			DataType lListType = lListTypeArguments.get(0);
			Class<?> lListClass = TypeMapper.classFor(lListType);
			lResult = pRow.getList(pIndex, lListClass);
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

	private static Class<?> classFor(DataType pDataType)
	{
		Name lName = pDataType.getName();
		Class<?> lResult;
		switch (lName)
		{
		case ASCII:
		case VARCHAR:
		case TEXT:
			lResult = String.class;
			break;
		case BIGINT:
			lResult = Long.class;
			break;
		case UDT:
			lResult = UDTValue.class;
			break;
		default:
			throw new UnsupportedOperationException("not yet implemented: " + lName);
		}
		return lResult;
	}

}
