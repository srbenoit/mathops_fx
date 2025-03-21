/**
 * A sample JavaFX application.
 *
 * <pre>
 * Node
 *  +-Camera
 *  |  +-ParallelCamera
 *  |  +-PerspectiveCamera
 *  +-Canvas
 *  +-ImageView
 *  +-LightBase
 *  |  +-AmbientLight
 *  |  +-DirectionalLight
 *  |  +-PointLight
 *  |     +-SpotLight
 *  +-MediaView
 *  +-Parent
 *  |  +-Group
 *  |  +-Region
 *  |  |  +-Axis
 *  |  |  |  +-CategoryAxis
 *  |  |  |  +-ValueAxis
 *  |  |  |      +-NumberAxis
 *  |  |  +-Chart
 *  |  |  |  +-PieChart
 *  |  |  |  +-XYChart
 *  |  |  |     +-AreaChart
 *  |  |  |     +-BarChart
 *  |  |  |     +-BubbleChart
 *  |  |  |     +-LineChart
 *  |  |  |     +-ScatterChart
 *  |  |  |     +-StackedAreaChart
 *  |  |  |     +-StackedBarChart
 *  |  |  +-Control
 *  |  |  |  +-Accordion
 *  |  |  |  +-ButtonBar
 *  |  |  |  +-ChoiceBox
 *  |  |  |  +-ComboBoxBase
 *  |  |  |  |  +-ColorPicker
 *  |  |  |  |  +-ComboBox
 *  |  |  |  |  +-DatePicker
 *  |  |  |  +-HTMLEditor
 *  |  |  |  +-Labeled
 *  |  |  |  |  +-ButtonBase
 *  |  |  |  |  |  +-Button
 *  |  |  |  |  |  +-CheckBox
 *  |  |  |  |  |  +-Hyperlink
 *  |  |  |  |  |  +-MenuButton
 *  |  |  |  |  |  |  +-SplitMenuButton
 *  |  |  |  |  |  +-ToggleButton
 *  |  |  |  |  |     +-RadioButton
 *  |  |  |  |  +-Cell
 *  |  |  |  |  |  +-DateCell
 *  |  |  |  |  |  +-IndexedCell
 *  |  |  |  |  |     +-ListCell
 *  |  |  |  |  |     |  +-CheckBoxListCell
 *  |  |  |  |  |     |  +-ChoiceBoxListCell
 *  |  |  |  |  |     |  +-ComboBoxListCell
 *  |  |  |  |  |     |  +-TextFieldListCell
 *  |  |  |  |  |     +-TableCell
 *  |  |  |  |  |     |  +-CheckBoxTableCell
 *  |  |  |  |  |     |  +-ChoiceBoxTableCell
 *  |  |  |  |  |     |  +-ComboBoxTableCell
 *  |  |  |  |  |     |  +-ProgressBarTableCell
 *  |  |  |  |  |     |  +-TextFieldTableCell
 *  |  |  |  |  |     +-TableRow
 *  |  |  |  |  |     +-TreeCell
 *  |  |  |  |  |     |  +-CheckBoxTreeCell
 *  |  |  |  |  |     |  +-ChoiceBoxTreeCell
 *  |  |  |  |  |     |  +-ComboBoxTreeCell
 *  |  |  |  |  |     |  +-TextFieldTreeCell
 *  |  |  |  |  |     +-TreeTableCell
 *  |  |  |  |  |     |  +-CheckBoxTableTreeCell
 *  |  |  |  |  |     |  +-ChoiceBoxTableTreeCell
 *  |  |  |  |  |     |  +-ComboBoxTableTreeCell
 *  |  |  |  |  |     |  +-ProgressBarTableTreeCell
 *  |  |  |  |  |     |  +-TextFieldTableTreeCell
 *  |  |  |  |  |     +-TreeTableRow
 *  |  |  |  |  +-Label
 *  |  |  |  |  +-TitledPane
 *  |  |  |  +-ListView
 *  |  |  |  +-MenuBar
 *  |  |  |  +-Pagination
 *  |  |  |  +-ProgressIndicator
 *  |  |  |  |  +-ProgressBar
 *  |  |  |  +-ScrollBar
 *  |  |  |  +-ScrollPane
 *  |  |  |  +-Separator
 *  |  |  |  +-Slider
 *  |  |  |  +-Spinner
 *  |  |  |  +-SplitPane
 *  |  |  |  +-TableView
 *  |  |  |  +-TabPane
 *  |  |  |  +-TextInputControl
 *  |  |  |  |  +-TextArea
 *  |  |  |  |  +-TextField
 *  |  |  |  |      +-PasswordField
 *  |  |  |  +-ToolBar
 *  |  |  |  +-TreeTableView
 *  |  |  |  +-TreeView
 *  |  |  +-Pane
 *  |  |  |  +-AnchorPane
 *  |  |  |  +-BorderPane
 *  |  |  |  +-DialogPane
 *  |  |  |  +-FlowPane
 *  |  |  |  +-GridPane
 *  |  |  |  +-HBox
 *  |  |  |  +-PopupControl.CSSBridge
 *  |  |  |  +-StackPane
 *  |  |  |  |  +-TableHeaderRow
 *  |  |  |  +-TextFlow
 *  |  |  |  +-TilePane
 *  |  |  |  +-VBox
 *  |  |  +-TableColumnHeader
 *  |  |  |  +-NestedTableColumnHeader
 *  |  |  +-VirtualFlow
 *  |  +-WebView
 *  +-Shape
 *  |  +-Arc
 *  |  +-Circle
 *  |  +-CubicCurve
 *  |  +-Ellipse
 *  |  +-Line
 *  |  +-Path
 *  |  +-Polygon
 *  |  +-Polyline
 *  |  +-QuadCurve
 *  |  +-Rectangle
 *  |  +-SVGPath
 *  |  +-Text
 *  +-Shape3D
 *  |  +-Box
 *  |  +-Cylinder
 *  |  +-MeshView
 *  |  +-Sphere
 *  +-SubScene
 *  +-SwingNode
 * </pre>
 */
package dev.mathops.fx.coursebuilder;