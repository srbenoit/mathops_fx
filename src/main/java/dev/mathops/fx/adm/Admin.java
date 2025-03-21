package dev.mathops.fx.adm;

import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.installation.EPath;
import dev.mathops.commons.installation.PathList;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.log.LogSettings;
import dev.mathops.commons.log.LogWriter;
import dev.mathops.commons.log.LoggingSubsystem;
import dev.mathops.db.DbConnection;
import dev.mathops.db.EDbUse;
import dev.mathops.db.cfg.Data;
import dev.mathops.db.cfg.Database;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.DatabaseConfigXml;
import dev.mathops.db.cfg.Server;
import dev.mathops.text.parser.ParsingException;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * A JavaFX version of the administrative application.  This class simply presents a login window and manages the login
 * process.  Once the user is logged in, the main application window is created.
 *
 * <p>
 * The admin application needs access to one instance of each defined schema.  It will use pre-configured login
 * credentials for the ODS and LIVE schemas, but uses entered credentials for the remaining schemas.
 *
 * <p>
 * The login window first asks the user to choose between "Production" and "Development".  It then presents an array of
 * servers and the schemas they provide in the selected context, and asks the user to select which to use, with some
 * default values pre-selected.
 *
 * <p>
 * The user is asked to enter a username and password to log in.  Those credentials are used for all selected servers.
 * If the provided credentials fail for all selected servers, the user is prompted to retry.  If they work for some but
 * not all servers, the user is prompted for another set to try for the remaining servers, and so on until either a
 * connection has been made to a server for each schema type, or the user cancels the attempt.
 *
 * <p>
 * Once there is a successful login for each schema type, this class constructs a custom database profile using the
 * selected schemas and presents that to the main application window.
 */
public final class Admin extends Application {

    /** The stage. */
    private Stage stage;

    /** Radio button to select "Production". */
    private RadioButton production;

    /** Radio button to select "Development". */
    private RadioButton development;

    /** A grid pane to display available production schemas. */
    private GridPane productionGrid;

    /** A grid pane to display available development schemas. */
    private GridPane developmentGrid;

    /**
     * Constructs a new {@code Admin}.
     */
    public Admin() {

        super();

        DbConnection.registerDrivers();

        final LogSettings logSettings = LoggingSubsystem.getSettings();
        logSettings.setLogToFiles(false);
        logSettings.setLogToConsole(true);

        final LogWriter logWriter = Log.getWriter();
        logWriter.startList(1000);

    }

    /**
     * Starts the application.
     *
     * @param theStage the main stage
     */
    @Override
    public void start(final Stage theStage) {

        final DatabaseConfig databaseConfig = loadDatabaseConfig();

        if (databaseConfig == null) {
            final Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to load database configuration.",
                    ButtonType.OK);
            alert.showAndWait();
        } else {
            this.stage = theStage;
            theStage.setTitle("Precalculus Program Administration");

            addIcon("icon24.png", theStage);
            addIcon("icon32.png", theStage);
            addIcon("icon48.png", theStage);

            final Scene scene = createScene(databaseConfig);

            theStage.setScene(scene);
            theStage.sizeToScene();

            theStage.centerOnScreen();
            theStage.show();
        }
    }

    /**
     * Adds an icon to the stage.
     *
     * @param name  the icon filename
     * @param stage the stage
     */
    private static void addIcon(final String name, final Stage stage) {

        final byte[] imgBytes = FileLoader.loadFileAsBytes(Admin.class, name, true);

        if (imgBytes != null) {
            final Image image = new Image(new ByteArrayInputStream(imgBytes));
            final ObservableList<Image> icons = stage.getIcons();
            icons.add(image);
        }
    }

    /**
     * Attempts to load the database configuration.
     *
     * @return the loaded configuration; {@code null} if it could not be loaded
     */
    private static DatabaseConfig loadDatabaseConfig() {

        final File dbPath = PathList.getInstance().get(EPath.DB_PATH);
        File cfgFile = new File(dbPath, DatabaseConfigXml.FILENAME);
        if (!cfgFile.exists()) {
            final String path = System.getProperty("user.dir");
            final File dir = new File(path);
            cfgFile = new File(dir, DatabaseConfigXml.FILENAME);
        }

        DatabaseConfig databaseConfig;
        if (cfgFile.exists()) {
            try {
                databaseConfig = DatabaseConfigXml.load(cfgFile);
            } catch (final IOException | ParsingException ex) {
                databaseConfig = DatabaseConfig.getDefault();
                Log.warning(ex);
            }
        } else {
            databaseConfig = DatabaseConfig.getDefault();
        }

        return databaseConfig;
    }

    /**
     * Creates the scene.
     *
     * @param databaseConfig the database configuration
     * @return the scene
     */
    private Scene createScene(final DatabaseConfig databaseConfig) {

        final List<Server> servers = databaseConfig.getServers();

        final Parameters parameters = getParameters();

        final VBox root = new VBox();
        root.setPadding(new Insets(20.0, 10.0, 20.0, 10.0));

        final Font headingFont1 = Font.font(22.0);
        final Font headingFont2 = Font.font(18.0);
        final Font bodyFont = Font.font(15.0);

        // Dialog heading

        final FlowPane flow1 = new FlowPane();
        flow1.setAlignment(Pos.BASELINE_CENTER);

        final BorderStroke grayStroke = new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, null,
                new BorderWidths(0.0, 0.0, 1.0, 0.0));
        final Border border = new Border(grayStroke);
        flow1.setBorder(border);

        final Label lbl1 = new Label("Precalculus Program Administration");
        lbl1.setFont(headingFont1);
        lbl1.setTextFill(Color.NAVY);
        final ObservableList<Node> topFlowChildren = flow1.getChildren();
        topFlowChildren.add(lbl1);

        // Radio button to select "Production" or "Development"

        final FlowPane flow2 = new FlowPane();
        flow2.setPadding(new Insets(10.0, 0.0, 6.0, 0.0));
        flow2.setAlignment(Pos.BASELINE_LEFT);

        final Label lbl2 = new Label("Select system to administer:");
        lbl2.setFont(headingFont2);
        lbl2.setTextFill(Color.FIREBRICK);
        final ObservableList<Node> flow2Children = flow2.getChildren();
        flow2Children.add(lbl2);

        final FlowPane flow3 = new FlowPane(16.0, 0.0);
        flow3.setPadding(new Insets(0.0, 0.0, 10.0, 20.0));
        flow3.setAlignment(Pos.BASELINE_LEFT);

        this.production = new RadioButton("Production");
        this.production.setFont(bodyFont);
        this.development = new RadioButton("Development");
        this.development.setFont(bodyFont);
        final ToggleGroup group = new ToggleGroup();
        group.getToggles().addAll(this.production, this.development);
        this.production.setSelected(true);
        flow3.getChildren().addAll(this.production, this.development);

        // A stack pane with two components (one for Production and one for Development), where only the component
        // corresponding to the selection above is visible.

        final FlowPane flow4 = new FlowPane();
        flow4.setPadding(new Insets(10.0, 0.0, 6.0, 0.0));
        flow4.setAlignment(Pos.BASELINE_LEFT);

        final Label lbl4 = new Label("Select servers to provide schemas:");
        lbl4.setFont(headingFont2);
        lbl4.setTextFill(Color.FIREBRICK);
        final ObservableList<Node> flow4Children = flow4.getChildren();
        flow4Children.add(lbl4);

        final StackPane stack = new StackPane();

        this.productionGrid = new GridPane(10.0, 10.0);
        this.productionGrid.setPadding(new Insets(0.0, 0.0, 10.0, 20.0));
        this.developmentGrid = new GridPane();
        this.developmentGrid.setPadding(new Insets(0.0, 0.0, 10.0, 20.0));
        this.developmentGrid.setVisible(false);
        stack.getChildren().addAll(this.productionGrid, this.developmentGrid);

        // In each grid, present the list of schemas along the top
        final Label prodLegacy = new Label("Legacy");
        GridPane.setConstraints(prodLegacy, 1, 0); // column=2 row=0
        final Label prodSystem = new Label("System");
        GridPane.setConstraints(prodSystem, 2, 0); // column=2 row=0
        final Label prodMain = new Label("Main");
        GridPane.setConstraints(prodMain, 3, 0); // column=2 row=0
        final Label prodExtern = new Label("External");
        GridPane.setConstraints(prodExtern, 4, 0); // column=2 row=0
        final Label prodAnalytic = new Label("Analytic");
        GridPane.setConstraints(prodAnalytic, 5, 0); // column=2 row=0
        final Label prodTerm = new Label("Term");
        GridPane.setConstraints(prodTerm, 6, 0); // column=2 row=0
        this.productionGrid.getChildren().addAll(prodLegacy, prodSystem, prodMain, prodExtern, prodAnalytic, prodTerm);

        for (final Server server : servers) {
            for (final Database database : server.getDatabases()) {
                for (final Data data : database.getData()) {
                    if (data.use == EDbUse.PROD) {
                        // TODO: If the database provides any of the needed schemas, create a grid row with
                        //  radio buttons for each supported schema.  If this is the first database that supports
                        //  a schema, select that one.  Do we need some way to mark in the database config XML
                        //  that a particular database should be "default" for a schema?
                    }
                }
            }
        }

        // TODO: store the user's selections for both production and development schema providers in preferences
        //  object and re-use each time the program runs.

        final Label devLegacy = new Label("Legacy");
        GridPane.setConstraints(devLegacy, 1, 0); // column=2 row=0
        final Label devSystem = new Label("System");
        GridPane.setConstraints(devSystem, 2, 0); // column=2 row=0
        final Label devMain = new Label("Main");
        GridPane.setConstraints(devMain, 3, 0); // column=2 row=0
        final Label devExtern = new Label("External");
        GridPane.setConstraints(devExtern, 4, 0); // column=2 row=0
        final Label devAnalytic = new Label("Analytic");
        GridPane.setConstraints(devAnalytic, 5, 0); // column=2 row=0
        final Label devTerm = new Label("Term");
        GridPane.setConstraints(devTerm, 6, 0); // column=2 row=0
        this.developmentGrid.getChildren().addAll(devLegacy, devSystem, devMain, devExtern, devAnalytic, devTerm);

        // Buttons

        final FlowPane flow9 = new FlowPane(24.0, 0.0);
        flow9.setPadding(new Insets(10.0, 0.0, 0.0, 0.0));

        final BorderStroke topStroke = new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, null,
                new BorderWidths(1.0, 0.0, 0.0, 0.0));
        final Border border9 = new Border(topStroke);
        flow9.setBorder(border9);

        flow9.setAlignment(Pos.BASELINE_CENTER);

        final Button login = new Button("Login");
        login.setFont(bodyFont);
        final Button cancel = new Button("Cancel");
        cancel.setFont(bodyFont);
        flow9.getChildren().addAll(login, cancel);

        final ObservableList<Node> rootChildren = root.getChildren();
        rootChildren.addAll(flow1, flow2, flow3, flow4, stack, flow9);

        return new Scene(root);
    }

    /**
     * Runs the main application.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        launch(args);
    }
}