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
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.Data;
import dev.mathops.db.cfg.Database;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.DatabaseConfigXml;
import dev.mathops.db.cfg.Facet;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.cfg.Server;
import dev.mathops.text.parser.ParsingException;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public final class Admin extends Application implements EventHandler<ActionEvent> {

    /** An user data object to identify the action to perform. */
    private static final String LOGIN_CMD = "LOGIN";

    /** An user data object to identify the action to perform. */
    private static final String CANCEL_CMD = "CANCEL";

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

    /** The production radio buttons. */
    private final List<RadioButton> productionRadioButtons;

    /** The development radio buttons. */
    private final List<RadioButton> developmentRadioButtons;

    /** The username. */
    private TextField username;

    /** The password. */
    private PasswordField password;

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

        this.productionRadioButtons = new ArrayList<>(10);
        this.developmentRadioButtons = new ArrayList<>(10);
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

        final Parameters parameters = getParameters();

        final VBox root = new VBox();
        root.setPadding(new Insets(20.0, 10.0, 20.0, 10.0));

        final Font headingFont1 = Font.font(22.0);
        final Font headingFont2 = Font.font(18.0);
        final Font bodyFont = Font.font(15.0);

        // Dialog heading

        final FlowPane flow1 = new FlowPane();
        flow1.setAlignment(Pos.BASELINE_CENTER);

        final BorderStroke bottomStroke = new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, null,
                new BorderWidths(0.0, 0.0, 1.0, 0.0));
        final Border bottomBorder = new Border(bottomStroke);

        final BorderStroke topStroke = new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, null,
                new BorderWidths(1.0, 0.0, 0.0, 0.0));
        final Border topBorder = new Border(topStroke);

        flow1.setBorder(bottomBorder);

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
        flow4.setBorder(topBorder);

        final Label lbl4 = new Label("Select servers to provide schemas:");
        lbl4.setFont(headingFont2);
        lbl4.setTextFill(Color.FIREBRICK);
        final ObservableList<Node> flow4Children = flow4.getChildren();
        flow4Children.add(lbl4);

        final StackPane stack = new StackPane();

        this.productionGrid = buildSchemaGrid(databaseConfig, EDbUse.PROD, bodyFont, this.productionRadioButtons);
        this.developmentGrid = buildSchemaGrid(databaseConfig, EDbUse.DEV, bodyFont, this.developmentRadioButtons);
        this.developmentGrid.setVisible(false);
        stack.getChildren().addAll(this.productionGrid, this.developmentGrid);

        // Username and password

        final FlowPane flow5 = new FlowPane();
        flow5.setAlignment(Pos.BASELINE_LEFT);
        flow5.setBorder(topBorder);

        final Label lbl5 = new Label("Provide login credentials:");
        lbl5.setFont(headingFont2);
        lbl5.setTextFill(Color.FIREBRICK);
        final ObservableList<Node> flow5Children = flow5.getChildren();
        flow5Children.add(lbl5);

        final GridPane credentialsGrid = new GridPane(10, 10);
        credentialsGrid.setPadding(new Insets(6.0, 0.0, 10.0, 20.0));

        final Label usernameLbl = new Label("Username:");
        usernameLbl.setFont(bodyFont);
        GridPane.setConstraints(usernameLbl, 0, 0);

        this.username = new TextField();
        this.username.setFont(bodyFont);
        GridPane.setConstraints(this.username, 1, 0);

        final Label passwordLbl = new Label("Password:");
        passwordLbl.setFont(bodyFont);
        GridPane.setConstraints(passwordLbl, 0, 1);

        this.password = new PasswordField();
        this.password.setFont(bodyFont);
        GridPane.setConstraints(this.password, 1, 1);

        final ObservableList<Node> credentialsGridChildren = credentialsGrid.getChildren();
        credentialsGridChildren.addAll(usernameLbl, this.username, passwordLbl, this.password);

        // Buttons

        final FlowPane flow9 = new FlowPane(24.0, 0.0);
        flow9.setPadding(new Insets(10.0, 0.0, 0.0, 0.0));

        flow9.setBorder(topBorder);

        flow9.setAlignment(Pos.BASELINE_CENTER);

        final Button login = new Button("Login");
        login.setFont(bodyFont);
        login.setUserData(LOGIN_CMD);
        login.setOnAction(this);
        final Button cancel = new Button("Cancel");
        cancel.setFont(bodyFont);
        cancel.setUserData(CANCEL_CMD);
        cancel.setOnAction(this);
        flow9.getChildren().addAll(login, cancel);

        final ObservableList<Node> rootChildren = root.getChildren();
        rootChildren.addAll(flow1, flow2, flow3, flow4, stack, flow5, credentialsGrid, flow9);

        return new Scene(root);
    }

    /**
     * Constructs a grid of available schema implementations.
     *
     * @param databaseConfig the database configuration
     * @param use            the use (production or development)
     * @param bodyFont       the font for grid entries
     * @param list           the list to which to add all constructed buttons
     * @return the grid pane
     */
    private GridPane buildSchemaGrid(final DatabaseConfig databaseConfig, final EDbUse use, final Font bodyFont,
                                     final List<RadioButton> list) {

        final List<Server> servers = databaseConfig.getServers();

        final GridPane grid = new GridPane(10.0, 10.0);
        grid.setPadding(new Insets(0.0, 0.0, 10.0, 20.0));

        // In each grid, present the list of schemas along the top
        final Label prodLegacy = new Label("Legacy");
        prodLegacy.setFont(bodyFont);
        GridPane.setConstraints(prodLegacy, 1, 0);
        final Label prodSystem = new Label("System");
        prodSystem.setFont(bodyFont);
        GridPane.setConstraints(prodSystem, 2, 0);
        final Label prodMain = new Label("Main");
        prodMain.setFont(bodyFont);
        GridPane.setConstraints(prodMain, 3, 0);
        final Label prodExtern = new Label("External");
        prodExtern.setFont(bodyFont);
        GridPane.setConstraints(prodExtern, 4, 0);
        final Label prodAnalytic = new Label("Analytic");
        prodAnalytic.setFont(bodyFont);
        GridPane.setConstraints(prodAnalytic, 5, 0);
        final Label prodTerm = new Label("Term");
        prodTerm.setFont(bodyFont);
        GridPane.setConstraints(prodTerm, 6, 0);
        final Label prodOds = new Label("ODS");
        prodOds.setFont(bodyFont);
        GridPane.setConstraints(prodOds, 7, 0);

        final ObservableList<Node> gridChildren = grid.getChildren();
        gridChildren.addAll(prodLegacy, prodSystem, prodMain, prodExtern, prodAnalytic, prodTerm, prodOds);

        final ToggleGroup legacyGroup = new ToggleGroup();
        final ToggleGroup systemGroup = new ToggleGroup();
        final ToggleGroup mainGroup = new ToggleGroup();
        final ToggleGroup externGroup = new ToggleGroup();
        final ToggleGroup analyticsGroup = new ToggleGroup();
        final ToggleGroup termGroup = new ToggleGroup();

        final ObservableList<Toggle> legacyToggles = legacyGroup.getToggles();
        final ObservableList<Toggle> systemToggles = systemGroup.getToggles();
        final ObservableList<Toggle> mainToggles = mainGroup.getToggles();
        final ObservableList<Toggle> externToggles = externGroup.getToggles();
        final ObservableList<Toggle> analyticsToggles = analyticsGroup.getToggles();
        final ObservableList<Toggle> termToggles = termGroup.getToggles();

        boolean needLegacy = true;
        boolean needSystem = true;
        boolean needMain = true;
        boolean needExtern = true;
        boolean needAnalytics = true;
        boolean needTerm = true;
        boolean needOds = true;
        boolean needLive = true;

        int row = 1;
        for (final Server server : servers) {
            for (final Database database : server.getDatabases()) {

                // See if we should include a row for this database
                boolean hasLegacy = false;
                boolean hasSystem = false;
                boolean hasMain = false;
                boolean hasExtern = false;
                boolean hasAnalytics = false;
                boolean hasTerm = false;
                boolean hasOds = false;
                boolean hasLive = false;
                for (final Data data : database.getData()) {
                    if (data.use == EDbUse.PROD) {
                        hasLegacy = hasLegacy || data.schema == ESchema.LEGACY;
                        hasSystem = hasSystem || data.schema == ESchema.SYSTEM;
                        hasMain = hasMain || data.schema == ESchema.MAIN;
                        hasExtern = hasExtern || data.schema == ESchema.EXTERN;
                        hasAnalytics = hasAnalytics || data.schema == ESchema.ANALYTICS;
                        hasTerm = hasTerm || data.schema == ESchema.TERM;
                        hasOds = hasOds || data.schema == ESchema.ODS;
                        hasLive = hasLive || data.schema == ESchema.LIVE;
                    }
                }

                if (hasLegacy || hasSystem || hasMain || hasExtern || hasAnalytics || hasTerm) {
                    final String name = server.type.name + " (" + database.id + ")";
                    final Label nameLbl = new Label(name);
                    nameLbl.setFont(bodyFont);
                    GridPane.setConstraints(nameLbl, 0, row);
                    gridChildren.add(nameLbl);

                    // We "turn off" the "hasTerm" boolean when we find a match so we don't display duplicates.
                    // A database may have many data objects that provide the "TERM" schema for various terms, but
                    // client code can choose the proper one at runtime once we can log in.

                    for (final Data data : database.getData()) {
                        if (data.use == EDbUse.PROD) {

                            if (data.schema == ESchema.LEGACY) {
                                final RadioButton btn = new RadioButton();
                                btn.setUserData(data);
                                legacyToggles.add(btn);
                                btn.setFont(bodyFont);
                                GridPane.setConstraints(btn, 1, row);
                                gridChildren.add(btn);
                                list.add(btn);
                                if (needLegacy) {
                                    btn.setSelected(true);
                                    needLegacy = false;
                                }
                            } else if (data.schema == ESchema.SYSTEM) {
                                final RadioButton btn = new RadioButton();
                                btn.setUserData(data);
                                systemToggles.add(btn);
                                btn.setFont(bodyFont);
                                GridPane.setConstraints(btn, 2, row);
                                gridChildren.add(btn);
                                list.add(btn);
                                if (needSystem) {
                                    btn.setSelected(true);
                                    needSystem = false;
                                }
                            } else if (data.schema == ESchema.MAIN) {
                                final RadioButton btn = new RadioButton();
                                btn.setUserData(data);
                                mainToggles.add(btn);
                                btn.setFont(bodyFont);
                                GridPane.setConstraints(btn, 3, row);
                                gridChildren.add(btn);
                                list.add(btn);
                                if (needMain) {
                                    btn.setSelected(true);
                                    needMain = false;
                                }
                            } else if (data.schema == ESchema.EXTERN) {
                                final RadioButton btn = new RadioButton();
                                btn.setUserData(data);
                                externToggles.add(btn);
                                btn.setFont(bodyFont);
                                GridPane.setConstraints(btn, 4, row);
                                gridChildren.add(btn);
                                list.add(btn);
                                if (needExtern) {
                                    btn.setSelected(true);
                                    needExtern = false;
                                }
                            } else if (data.schema == ESchema.ANALYTICS) {
                                final RadioButton btn = new RadioButton();
                                btn.setUserData(data);
                                analyticsToggles.add(btn);
                                btn.setFont(bodyFont);
                                GridPane.setConstraints(btn, 5, row);
                                gridChildren.add(btn);
                                list.add(btn);
                                if (needAnalytics) {
                                    btn.setSelected(true);
                                    needAnalytics = false;
                                }
                            } else if (hasTerm && data.schema == ESchema.TERM) {
                                final RadioButton btn = new RadioButton();
                                btn.setUserData(data);
                                termToggles.add(btn);
                                btn.setFont(bodyFont);
                                GridPane.setConstraints(btn, 6, row);
                                gridChildren.add(btn);
                                list.add(btn);
                                if (needTerm) {
                                    btn.setSelected(true);
                                    needTerm = false;
                                }
                                hasTerm = false;
                            }
                        }
                    }

                    ++row;
                }

                if (hasOds && needOds) {

                    // TODO: Add a radio button with this data (but do not add to grid), so we include it in the
                    //  synthetic profile

                    needOds = false;
                }

                if (hasLive && needLive) {

                    // TODO: Add a radio button with this data (but do not add to grid), so we include it in the
                    //  synthetic profile

                    needLive = false;
                }
            }
        }

        return grid;
    }

    /**
     * Handles actions generated by buttons.
     *
     * @param actionEvent the action event
     */
    @Override
    public void handle(final ActionEvent actionEvent) {

        if (actionEvent.getSource() instanceof final Button btn) {
            final Object cmd = btn.getUserData();

            if (LOGIN_CMD.equals(cmd)) {
                handleLogin();
            } else if (CANCEL_CMD.equals(cmd)) {
                this.stage.close();
            }
        }
    }

    /**
     * Handles a request to log in.
     */
    private void handleLogin() {

        final String theUsername = this.username.getText();
        final String thePassword = this.password.getText();

        if (theUsername.isBlank()) {
            this.username.requestFocus();
        } else if (thePassword.isBlank()) {
            this.password.requestFocus();
        } else {
            final Profile newProfile = new Profile("SYNTHETIC_PROFILE");

            final Map<Database, Login> logins = new HashMap<>(5);

            if (this.production.isSelected()) {
                Log.info("Login to Production");

                for (final RadioButton button : this.productionRadioButtons) {
                    if (button.isSelected()) {
                        final Object userData = button.getUserData();
                        if (userData instanceof final Data data) {
                            final Login login = logins.computeIfAbsent(data.database,
                                    a -> new Login(data.database, data.id, theUsername, thePassword));
                            final Facet facet = new Facet(data, login);
                            newProfile.addFacet(facet);
                        }
                    }
                }
            } else {
                Log.info("Login to Development");

            }
        }

        // TODO: store the user's selections for both production and development schema providers in preferences
        //  object and re-use each time the program runs.
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