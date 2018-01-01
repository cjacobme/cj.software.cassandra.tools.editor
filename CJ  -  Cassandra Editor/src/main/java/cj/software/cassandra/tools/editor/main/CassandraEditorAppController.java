package cj.software.cassandra.tools.editor.main;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.prefs.BackingStoreException;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.ColumnDefinitions.Definition;
import com.datastax.driver.core.ColumnMetadata;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.MaterializedViewMetadata;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.UserType;
import com.datastax.driver.extras.codecs.jdk8.InstantCodec;
import com.datastax.driver.extras.codecs.jdk8.LocalDateCodec;
import com.sun.javafx.collections.ObservableListWrapper;

import cj.software.cassandra.tools.editor.connection.ConnectionDialogController;
import cj.software.cassandra.tools.editor.connection.KeyspacesSelectDialogController;
import cj.software.cassandra.tools.editor.helper.TypeMapper;
import cj.software.cassandra.tools.editor.modell.Connection;
import cj.software.cassandra.tools.editor.storage.RecentConnectionsRepository;
import cj.software.javafx.ThrowableStackTraceAlertFactory;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

@SuppressWarnings("restriction")
public class CassandraEditorAppController
{
	private CassandraEditorApp main;

	private ObjectProperty<Connection> connectionProperty = new SimpleObjectProperty<>();

	private ObjectProperty<Cluster> clusterProperty = new SimpleObjectProperty<>();

	private RecentConnectionsRepository recentConnectionsRepository = new RecentConnectionsRepository();

	private ObjectProperty<Session> sessionProperty = new SimpleObjectProperty<>();

	@FXML
	private SeparatorMenuItem connectionBeforeRecentList;

	@FXML
	private SeparatorMenuItem beforeRecentKeyspaces;

	@FXML
	private Menu keyspacesMenu;

	@FXML
	private TextArea command;

	@FXML
	private Button executeCql;

	@FXML
	private TableView<Object> results;

	@FXML
	private TabPane keyspaceDetails;

	@FXML
	private ListView<String> listOfTables;

	@FXML
	private ListView<String> listOfMaterializedViews;

	@FXML
	private ListView<String> listOfUDTs;

	void setMain(CassandraEditorApp pMain)
	{
		this.main = pMain;
	}

	@FXML
	private void initialize()
	{
		try
		{
			this.insertRecents();
			this.keyspacesMenu.disableProperty().bind(this.connectionProperty.isNull());
			this.command.disableProperty().bind(this.sessionProperty.isNull());
			this.command.editableProperty().bind(this.sessionProperty.isNotNull());
			this.command.addEventFilter(KeyEvent.KEY_PRESSED, new MyKeyEventHandler());
			this.executeCql.disableProperty().bind(
					this.sessionProperty.isNull().or(this.command.textProperty().isEmpty()));
			this.executeCql.setTooltip(new Tooltip("Press Ctrl-Enter to execute immediately"));
			this.keyspaceDetails.disableProperty().bind(this.sessionProperty.isNull());
			ObservableList<Tab> lTabs = this.keyspaceDetails.getTabs();
			for (Tab bTab : lTabs)
			{
				bTab.disableProperty().bind(this.sessionProperty.isNull());
			}
			this.listOfTables.disableProperty().bind(this.sessionProperty.isNull());
			this.listOfMaterializedViews.disableProperty().bind(this.sessionProperty.isNull());
			this.listOfUDTs.disableProperty().bind(this.sessionProperty.isNull());
		}
		catch (Throwable pThrowable)
		{
			pThrowable.printStackTrace(System.err);
			Alert lAlert = ThrowableStackTraceAlertFactory.createAlert(pThrowable);
			lAlert.showAndWait();
		}
	}

	public void initializeAccelorators(Scene pScene)
	{
		pScene.getAccelerators().put(
				new KeyCodeCombination(KeyCode.ENTER, KeyCombination.CONTROL_DOWN),
				new Runnable()
				{

					@Override
					public void run()
					{
						CassandraEditorAppController.this.executeCql.fire();
					}
				});
	}

	private class MyKeyEventHandler
			implements
			EventHandler<KeyEvent>
	{

		@Override
		public void handle(KeyEvent pEvent)
		{
			if (pEvent.getCode() == KeyCode.SPACE && pEvent.isControlDown())
			{
				System.out.println("Ctrl-Space entered");
				// TODO autocompletion here, described in
				// https://stackoverflow.com/questions/36861056/javafx-textfield-auto-suggestions
			}
		}
	}

	private void insertRecents() throws BackingStoreException
	{
		List<Connection> lRecents = this.recentConnectionsRepository.readRecents();
		Menu lParentMenu = this.connectionBeforeRecentList.getParentMenu();
		ObservableList<MenuItem> lItems = lParentMenu.getItems();
		int lInsertPosition = lItems.indexOf(this.connectionBeforeRecentList) + 1;
		for (Connection bRecent : lRecents)
		{
			MenuItem lNewItem = new MenuItem(bRecent.getHostname());
			lNewItem.setOnAction(new EventHandler<ActionEvent>()
			{

				@Override
				public void handle(ActionEvent pEvent)
				{
					try
					{
						setConnection(bRecent);
					}
					catch (Throwable pThrowable)
					{
						pThrowable.printStackTrace(System.err);
						Alert lAlert = ThrowableStackTraceAlertFactory.createAlert(pThrowable);
						lAlert.showAndWait();
					}
				}
			});
			lItems.add(lInsertPosition, lNewItem);
			lInsertPosition++;
		}
	}

	private void clearRecentKeyspaces()
	{
		Menu lParentMenu = this.beforeRecentKeyspaces.getParentMenu();
		ObservableList<MenuItem> lItems = lParentMenu.getItems();
		int lSeparatorIndex = lItems.indexOf(this.beforeRecentKeyspaces);
		int lNumItems = lItems.size();
		for (int bI = lNumItems - 1; bI > lSeparatorIndex; bI--)
		{
			lItems.remove(bI);
		}
	}

	private void setRecentKeyspaces(Connection pConnection)
	{
		this.clearRecentKeyspaces();
		Menu lParentMenu = this.beforeRecentKeyspaces.getParentMenu();
		ObservableList<MenuItem> lItems = lParentMenu.getItems();
		int lInsertPosition = lItems.indexOf(this.beforeRecentKeyspaces) + 1;
		List<String> lKeyspaces = pConnection.getKeyspaces();
		for (String bKeyspace : lKeyspaces)
		{
			MenuItem lNewItem = new MenuItem(bKeyspace);
			lNewItem.setOnAction(a ->
			{
				try
				{
					openSession(bKeyspace);
				}
				catch (Throwable pThrowable)
				{
					pThrowable.printStackTrace(System.err);
					Alert lAlert = ThrowableStackTraceAlertFactory.createAlert(pThrowable);
					lAlert.showAndWait();
				}
			});
			lItems.add(lInsertPosition, lNewItem);
			lInsertPosition++;
		}
	}

	public Connection getConnection()
	{
		return this.connectionProperty.get();
	}

	public void setConnection(Connection pConnection) throws BackingStoreException
	{
		this.connectionProperty.set(pConnection);
		String lTitle = (pConnection != null
				? String.format(
						CassandraEditorApp.FMT_TITLE_HOST_CONNECTED,
						pConnection.getHostname())
				: CassandraEditorApp.INITIAL_TITLE);
		if (pConnection != null)
		{
			this.recentConnectionsRepository.save(pConnection);
		}
		this.main.getPrimaryStage().setTitle(lTitle);
		Session lSession = this.getSession();
		if (lSession != null && !lSession.isClosed())
		{
			lSession.close();
		}
		Cluster lCluster = this.getCluster();
		if (lCluster != null && !lCluster.isClosed())
		{
			lCluster.close();
		}
		if (pConnection != null)
		{
			lCluster = Cluster.builder().addContactPoint(pConnection.getHostname()).build();
			this.setCluster(lCluster);
			this.setRecentKeyspaces(pConnection);
		}
	}

	public ObjectProperty<Connection> connectionProperty()
	{
		return this.connectionProperty;
	}

	public void setCluster(Cluster pCluster)
	{
		this.clusterProperty.set(pCluster);
		if (pCluster != null)
		{
			CodecRegistry lCodecRegistry = pCluster.getConfiguration().getCodecRegistry();
			lCodecRegistry.register(InstantCodec.instance).register(LocalDateCodec.instance);

		}
	}

	public Cluster getCluster()
	{
		return this.clusterProperty.get();
	}

	public ObjectProperty<Cluster> clusterProperty()
	{
		return this.clusterProperty;
	}

	public void setSession(Session pSession)
	{
		this.sessionProperty.set(pSession);

		this.listOfMaterializedViews.getItems().clear();
		this.listOfTables.getItems().clear();
		this.listOfUDTs.getItems().clear();

		if (pSession != null)
		{
			Metadata lMetadata = pSession.getCluster().getMetadata();
			KeyspaceMetadata lKeyspaceMeta = lMetadata.getKeyspace(pSession.getLoggedKeyspace());
			this.insertTableNames(lKeyspaceMeta);
			this.insertMaterializedViews(lKeyspaceMeta);
			this.insertUDTs(lKeyspaceMeta);
		}
	}

	private void insertTableNames(KeyspaceMetadata pMeta)
	{
		Collection<TableMetadata> lTables = pMeta.getTables();
		List<String> lNames = new ArrayList<>();
		for (TableMetadata bTableMeta : lTables)
		{
			String lName = bTableMeta.getName();
			lNames.add(lName);
		}
		Collections.sort(lNames);
		this.listOfTables.getItems().addAll(lNames);
	}

	private void insertMaterializedViews(KeyspaceMetadata pMeta)
	{
		Collection<MaterializedViewMetadata> lMaterializedViews = pMeta.getMaterializedViews();
		List<String> lNames = new ArrayList<>();
		for (MaterializedViewMetadata bMatrlzdViewMeta : lMaterializedViews)
		{
			String lName = bMatrlzdViewMeta.getName();
			lNames.add(lName);
		}
		Collections.sort(lNames);
		this.listOfMaterializedViews.getItems().addAll(lNames);
	}

	private void insertUDTs(KeyspaceMetadata pMeta)
	{
		Collection<UserType> lUserTypes = pMeta.getUserTypes();
		List<String> lNames = new ArrayList<>();
		for (UserType bUserType : lUserTypes)
		{
			String lName = bUserType.getTypeName();
			lNames.add(lName);
		}
		Collections.sort(lNames);
		this.listOfUDTs.getItems().addAll(lNames);
	}

	public Session getSession()
	{
		return this.sessionProperty.get();
	}

	public ObjectProperty<Session> sessionProperty()
	{
		return this.sessionProperty;
	}

	@FXML
	private void handleExit()
	{
		System.exit(0);
	}

	@FXML
	private void handleConnectToHost()
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
				this.setConnection(lConnection);
			}

		}
		catch (Throwable pThrowable)
		{
			Alert lAlert = ThrowableStackTraceAlertFactory.createAlert(pThrowable);
			lAlert.showAndWait();
		}
	}

	@FXML
	private void handleSelectKeyspace()
	{
		try
		{
			String lFxmlFile = "KeyspacesSelectDialog.fxml";
			URL lURL = KeyspacesSelectDialogController.class.getResource(lFxmlFile);
			if (lURL == null)
			{
				throw new IllegalArgumentException(lFxmlFile + " not found in Classpath");
			}
			FXMLLoader lLoader = new FXMLLoader(lURL);
			Pane lPane = lLoader.load();

			Stage lDialogStage = new Stage();
			lDialogStage.setTitle("Select Keyspace");
			lDialogStage.initModality(Modality.WINDOW_MODAL);
			lDialogStage.initOwner(this.main.getPrimaryStage());

			Scene lScene = new Scene(lPane);
			lDialogStage.setScene(lScene);

			KeyspacesSelectDialogController lController = lLoader.getController();
			lController.setDialogStage(lDialogStage);
			lController.setCluster(this.getCluster());

			lDialogStage.showAndWait();

			KeyspaceMetadata lSelectedKeyspace = lController.getSelectedKeyspace();
			if (lSelectedKeyspace != null)
			{
				String lKeyspaceName = lSelectedKeyspace.getName();
				this.openSession(lKeyspaceName);
			}
		}
		catch (Throwable pThrowable)
		{
			pThrowable.printStackTrace(System.err);
			Alert lAlert = ThrowableStackTraceAlertFactory.createAlert(pThrowable);
			lAlert.showAndWait();
		}
	}

	public void openSession(String pKeyspaceName) throws BackingStoreException
	{
		Connection lConnection = this.getConnection();
		lConnection.addKeyspace(pKeyspaceName);
		this.recentConnectionsRepository.save(lConnection);
		this.setRecentKeyspaces(lConnection);

		Session lCurrentSession = this.getSession();
		if (lCurrentSession != null && !lCurrentSession.isClosed())
		{
			lCurrentSession.close();
		}
		lCurrentSession = this.getCluster().connect(pKeyspaceName);
		this.setSession(lCurrentSession);

		String lTitle = String.format(
				CassandraEditorApp.FMT_TITLE_HOST_KEYSPACE_CONNECTED,
				lConnection.getHostname(),
				pKeyspaceName);
		this.main.getPrimaryStage().setTitle(lTitle);
	}

	private class MyCellValueFactory<T>
			implements
			Callback<TableColumn.CellDataFeatures<Object, T>, ObservableValue<T>>
	{

		@SuppressWarnings("unchecked")
		@Override
		public ObservableValue<T> call(CellDataFeatures<Object, T> pParam)
		{
			List<Object> lRow = (List<Object>) pParam.getValue();
			TableColumn<Object, T> lTableColumn = pParam.getTableColumn();
			TableView<Object> lTableView = pParam.getTableView();
			int lIndex = lTableView.getColumns().indexOf(lTableColumn);
			T lValue = (T) lRow.get(lIndex);
			ObservableValue<T> lResult = new ReadOnlyObjectWrapper<T>(lValue);
			return lResult;
		}
	}

	@FXML
	private void executeCql()
	{
		try
		{
			String lContent = this.command.getText().trim();
			if (lContent.length() > 0)
			{
				ResultSet lRS = this.getSession().execute(lContent);
				ColumnDefinitions lColumnDefinitions = lRS.getColumnDefinitions();
				this.results.getColumns().clear();
				this.results.getItems().clear();
				List<Definition> lDefinitions = lColumnDefinitions.asList();
				int lNumDefinitions = lDefinitions.size();
				for (int bDefinition = 0; bDefinition < lNumDefinitions; bDefinition++)
				{
					Definition lDefinition = lDefinitions.get(bDefinition);
					TableColumn<Object, ?> lTableColumn = TypeMapper.createTableColumn(lDefinition);
					lTableColumn.setCellValueFactory(new MyCellValueFactory<>());
					this.results.getColumns().add(lTableColumn);
				}
				lRS.forEach(pRow ->
				{
					List<Object> lRow = new ArrayList<>(lColumnDefinitions.size());
					for (int bCol = 0; bCol < lColumnDefinitions.size(); bCol++)
					{
						Object lEntry = TypeMapper.readValue(pRow, bCol, lDefinitions.get(bCol));
						lRow.add(lEntry);
					}
					this.results.getItems().add(FXCollections.observableArrayList(lRow));
				});
			}
			else
			{
				Alert lAlert = new Alert(AlertType.WARNING);
				lAlert.setTitle("No command entered");
				lAlert.setContentText("Please enter a command");
				lAlert.showAndWait();
			}

		}
		catch (Throwable pThrowable)
		{
			pThrowable.printStackTrace(System.err);
			Alert lAlert = ThrowableStackTraceAlertFactory.createAlert(pThrowable);
			lAlert.showAndWait();
		}
	}

	private void callTableMetaDataAction(Function<TableMetadata, String> pFunction)
	{
		String lSelectedTableName = this.listOfTables.getSelectionModel().getSelectedItem();
		if (lSelectedTableName != null)
		{
			Session lSession = this.getSession();
			Metadata lMetadata = lSession.getCluster().getMetadata();
			KeyspaceMetadata lKeyspaceMeta = lMetadata.getKeyspace(lSession.getLoggedKeyspace());
			TableMetadata lTableMeta = lKeyspaceMeta.getTable(lSelectedTableName);
			String lStatement = pFunction.apply(lTableMeta);
			this.command.setText(lStatement);
			this.command.requestFocus();
		}
	}

	@FXML
	private void handleCreateInsertStmt()
	{
		try
		{
			this.callTableMetaDataAction(this::createInsertStmt);
		}
		catch (Throwable pThrowable)
		{
			pThrowable.printStackTrace(System.err);
			Alert lAlert = ThrowableStackTraceAlertFactory.createAlert(pThrowable);
			lAlert.showAndWait();
		}
	}

	private String createInsertStmt(TableMetadata pMetadata)
	{
		String lTableName = pMetadata.getName();
		List<ColumnMetadata> lColumns = pMetadata.getColumns();
		StringBuilder lCols = new StringBuilder("INSERT INTO ").append(lTableName).append(" (");
		StringBuilder lVals = new StringBuilder(" VALUES (");
		for (ColumnMetadata bCol : lColumns)
		{
			String lName = bCol.getName();
			lCols.append(lName).append(", ");
			lVals.append("?, ");
		}

		lCols.deleteCharAt(lCols.length() - 1);
		lCols.deleteCharAt(lCols.length() - 1);
		lCols.append(") ");
		lVals.deleteCharAt(lVals.length() - 1);
		lVals.deleteCharAt(lVals.length() - 1);
		lVals.append(")");

		String lTotal = lCols.toString() + lVals.toString();
		return lTotal;
	}

	@FXML
	private void handleCreateInsertStmtWithTypes()
	{
		try
		{
			this.callTableMetaDataAction(this::createInsertStmtWithTypes);
		}
		catch (Throwable pThrowable)
		{
			pThrowable.printStackTrace(System.err);
			Alert lAlert = ThrowableStackTraceAlertFactory.createAlert(pThrowable);
			lAlert.showAndWait();
		}
	}

	private int getMaxNameLength(List<ColumnMetadata> pColumns)
	{
		int lMaxNameLength = -1;
		for (ColumnMetadata bCol : pColumns)
		{
			String lName = bCol.getName();
			lMaxNameLength = Math.max(lMaxNameLength, lName.length());
		}
		return lMaxNameLength;
	}

	private String createInsertStmtWithTypes(TableMetadata pTableMetadata)
	{
		String lTableName = pTableMetadata.getName();
		List<ColumnMetadata> lColumns = pTableMetadata.getColumns();
		StringBuilder lCols = new StringBuilder("INSERT INTO ").append(lTableName).append(" (\r\n");
		StringBuilder lVals = new StringBuilder(" VALUES (\r\n");

		int lMaxNameLength = this.getMaxNameLength(lColumns) + 1;
		String lFormat = "%-" + lMaxNameLength + "s";

		for (ColumnMetadata bCol : lColumns)
		{
			String lName = String.format(lFormat, bCol.getName());
			DataType lType = bCol.getType();
			lCols.append("     ").append(lName).append(", -- ").append(lType).append("\r\n");
			lVals.append("\t?, -- ").append(lName).append("\r\n");
		}

		int lLastComma = lCols.lastIndexOf(",");
		lCols.replace(lLastComma, lLastComma + 1, " ");
		lCols.append(") ");
		lLastComma = lVals.lastIndexOf(",");
		lVals.replace(lLastComma, lLastComma + 1, " ");
		lVals.append(")");

		String lTotal = lCols.toString() + lVals.toString();
		return lTotal;
	}

	@FXML
	private void handleDdl()
	{
		try
		{
			this.callTableMetaDataAction(this::ddl);
		}
		catch (Throwable pThrowable)
		{
			pThrowable.printStackTrace(System.err);
			Alert lAlert = ThrowableStackTraceAlertFactory.createAlert(pThrowable);
			lAlert.showAndWait();
		}
	}

	private String ddl(TableMetadata pTableMetadata)
	{
		String lDDL = pTableMetadata.asCQLQuery();
		return lDDL;
	}

	@FXML
	private void handleCreateSelectSimple()
	{
		try
		{
			this.callTableMetaDataAction(this::createSelectSimple);
		}
		catch (Throwable pThrowable)
		{
			pThrowable.printStackTrace(System.err);
			Alert lAlert = ThrowableStackTraceAlertFactory.createAlert(pThrowable);
			lAlert.showAndWait();
		}
	}

	private String createSelectSimple(TableMetadata pTableMetadata)
	{
		String lName = pTableMetadata.getName();
		String lStatement = "SELECT * FROM " + lName;
		return lStatement;
	}

	@FXML
	private void handleCreateSelectWithPartitionKeys()
	{
		try
		{
			this.callTableMetaDataAction(this::createSelectWithPartitionKeys);
		}
		catch (Throwable pThrowable)
		{
			pThrowable.printStackTrace(System.err);
			Alert lAlert = ThrowableStackTraceAlertFactory.createAlert(pThrowable);
			lAlert.showAndWait();
		}
	}

	private String createSelectWithPartitionKeys(TableMetadata pTableMetadata)
	{
		String lTableName = pTableMetadata.getName();
		StringBuilder lSB = new StringBuilder("SELECT * FROM ").append(lTableName).append("\r\n");

		List<ColumnMetadata> lPartitionKeyColumns = pTableMetadata.getPartitionKey();
		int lMaxNameLength = this.getMaxNameLength(lPartitionKeyColumns) + 1;
		String lNameFormat = String.format("%%%ds", lMaxNameLength);

		ColumnMetadata lColumnMeta = lPartitionKeyColumns.get(0);
		String lColName = String.format(lNameFormat, lColumnMeta.getName());
		lSB
				.append("WHERE ")
				.append(lColName)
				.append(" = ?   -- ")
				.append(lColumnMeta.getType().toString())
				.append("\r\n");

		for (int bCol = 1; bCol < lPartitionKeyColumns.size(); bCol++)
		{
			lColumnMeta = lPartitionKeyColumns.get(bCol);
			lColName = String.format(lNameFormat, lColumnMeta.getName());
			lSB
					.append("   AND")
					.append(lColName)
					.append(" = ?   -- ")
					.append(lColumnMeta.getType().toString())
					.append("\r\n");
		}
		lSB.append(";");
		return lSB.toString();
	}

	@FXML
	private void handleCreateSelectWithPrimaryKeys()
	{
		try
		{
			this.callTableMetaDataAction(this::createSelectWithPrimaryKeys);
		}
		catch (Throwable pThrowable)
		{
			pThrowable.printStackTrace(System.err);
			Alert lAlert = ThrowableStackTraceAlertFactory.createAlert(pThrowable);
			lAlert.showAndWait();
		}
	}

	private String createSelectWithPrimaryKeys(TableMetadata pTableMetadata)
	{
		StringBuilder lSB = new StringBuilder(this.createSelectWithPartitionKeys(pTableMetadata));
		int lLength = lSB.length();
		lSB.deleteCharAt(lLength - 1);

		List<ColumnMetadata> lClusteringColumns = pTableMetadata.getClusteringColumns();
		int lMaxNameLength = this.getMaxNameLength(lClusteringColumns) + 1;
		String lNameFormat = String.format("%%%ds", lMaxNameLength);

		for (ColumnMetadata bColumnMeta : lClusteringColumns)
		{
			String lColName = String.format(lNameFormat, bColumnMeta.getName());
			lSB
					.append("   AND")
					.append(lColName)
					.append(" > ?   -- ")
					.append(bColumnMeta.getType().toString())
					.append("\r\n");
		}
		lSB.append(";");
		return lSB.toString();
	}

	@FXML
	private void showTypeDescription()
	{
		String lTypeName = this.listOfUDTs.getSelectionModel().getSelectedItem();
		if (lTypeName != null)
		{
			Session lSession = this.getSession();
			Metadata lMetadata = lSession.getCluster().getMetadata();
			KeyspaceMetadata lKeyspaceMeta = lMetadata.getKeyspace(lSession.getLoggedKeyspace());
			UserType lUserType = lKeyspaceMeta.getUserType(lTypeName);
			this.describeUserType(lUserType);
		}
	}

	private void describeUserType(UserType pUserType)
	{
		String lCqlQuery = pUserType.asCQLQuery();
		this.command.setText(lCqlQuery);
	}

	@SuppressWarnings("restriction")
	@FXML
	private void copyTableCellContent()
	{
		try
		{
			Object lSelectedItem = this.results.getSelectionModel().getSelectedItem();
			if (lSelectedItem != null)
			{
				TablePosition<?, ?> lFocusedCell = this.results.getFocusModel().getFocusedCell();
				ClipboardContent lClipboardContent = new ClipboardContent();
				if (lSelectedItem instanceof ObservableListWrapper<?> && lFocusedCell != null)
				{
					ObservableListWrapper<?> lObservableListWrapper = (ObservableListWrapper<?>) lSelectedItem;
					int lColumn = lFocusedCell.getColumn();
					Object lObject = lObservableListWrapper.get(lColumn);
					lClipboardContent.putString(lObject.toString());
				}
				else
				{
					lClipboardContent.putString(lSelectedItem.toString());
				}
				Clipboard lClipboard = Clipboard.getSystemClipboard();
				lClipboard.setContent(lClipboardContent);
			}
		}
		catch (Throwable pThrowable)
		{
			pThrowable.printStackTrace(System.err);
			Alert lAlert = ThrowableStackTraceAlertFactory.createAlert(pThrowable);
			lAlert.showAndWait();
		}
	}
}
