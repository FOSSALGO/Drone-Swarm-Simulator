package dsde.simulator;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MainView extends Application {

    // BUTTON
    private final Button btnNew = initializeButton(SVGIcon.newEnv, 1.0, 1.0, "New Environment");// VISUALIZATION
    private final Button btnOpen = initializeButton(SVGIcon.openEnv, 1.0, 1.0, "Open Environment");// VISUALIZATION
    private final Button btnSave = initializeButton(SVGIcon.saveEnv, 1.0, 1.0, "Save Environment");// VISUALIZATION
    private final Button btnTransform = initializeButton(SVGIcon.transform, 1.0, 1.0, "Transform");// VISUALIZATION
    private final Button btnAddDrone = initializeButton(SVGIcon.addDrone, 1.2, 1.2, "Add Drone");// VISUALIZATION
    private final Button btnRemoveDrone = initializeButton(SVGIcon.removeDrone, 1.2, 1.2, "Remove Drone");// VISUALIZATION
    private final Button btnWall = initializeButton(SVGIcon.wall, 1.0, 1.0, "Wall");// VISUALIZATION
    private final Button btnDrawLine = initializeButton(SVGIcon.drawLine, 1.0, 1.0, "Draw Line");// VISUALIZATION
    private final Button btnRemoveLine = initializeButton(SVGIcon.removeLine, 1.0, 1.0, "Remove Line");// VISUALIZATION
    private final Button btnRun = initializeButton(SVGIcon.run, 1.0, 1.0, "Run");// VISUALIZATION
    private final Button btnPlayPause = initializeButton(SVGIcon.playPause, 1.0, 1.0, "Play - Pause");// VISUALIZATION
    private final Button btnReplay = initializeButton(SVGIcon.replay, 1.0, 1.0, "Replay");// VISUALIZATION
    private final int MAX_NUM_ROWS = 200;// GRID
    private final int MAX_NUM_COLS = 400;// GRID
    private final int cellSize = 10;// GRID
    private final int numRows = MAX_NUM_ROWS;// GRID
    private final int numCols = MAX_NUM_COLS;// GRID
    private final int[][] environment = new int[numRows][numCols];// GRID
    private final String help = "DRONE SIMULATOR";
    private Stage primaryStage = null;
    // VISUALIZATION
    private ProgressIndicator progressIndicator;// VISUALIZATION
    private CanvasView canvas = null;
    private StringBuffer logInfo = new StringBuffer();//LOG

    // Create button
    private Button initializeButton(String svgPathData, double scaleX, double scaleY, String tooltip) {
        SVGPath path = new SVGPath();
        path.setContent(svgPathData);
        path.setFill(Color.valueOf("#393E46"));
        path.setScaleX(scaleX);
        path.setScaleY(scaleY);
        Button btn = new Button();
        btn.setGraphic(path);
        btn.setTooltip(new Tooltip(tooltip));
        btn.setStyle("-fx-background-color: transparent;");
        //btn.setOnMouseEntered(e -> path.setFill(Color.WHITE));
        btn.setOnMouseEntered(e -> path.setFill(Color.valueOf("#0096FF")));
        //btn.setOnMouseEntered(e -> path.setFill(Color.valueOf("#209197")));
        btn.setOnMouseExited(e -> path.setFill(Color.valueOf("#393E46")));
        return btn;
    }

    private CanvasView getCanvas() {
        if (canvas == null) {
            canvas = new CanvasView(environment, cellSize);
            canvas.setOnMousePressed(this::handleMousePressed);
            canvas.setOnMouseDragged(this::handleMouseDragged);
            canvas.setOnScroll(this::handleMouseScroll);
            canvas.setOnMouseClicked(this::handleMouseClicked);
            canvas.setOnMouseReleased(this::handleMouseReleased);
            canvas.setOnMouseMoved(this::handleMouseMoved);
        }
        return canvas;
    }

    private void handleMouseMoved(MouseEvent mouseEvent) {
        canvas.handleMouseMoved(mouseEvent);
    }

    private void handleMouseReleased(MouseEvent mouseEvent) {
        canvas.handleMouseReleased(mouseEvent);
    }

    private void handleMouseClicked(MouseEvent mouseEvent) {
        canvas.handleMouseClicked(mouseEvent);

    }

    private void handleMouseScroll(ScrollEvent scrollEvent) {
        canvas.handleMouseScroll(scrollEvent);
    }

    private void handleMouseDragged(MouseEvent mouseEvent) {
        canvas.handleMouseDragged(mouseEvent);
    }

    private void handleMousePressed(MouseEvent mouseEvent) {
        canvas.handleMousePressed(mouseEvent);
    }

    private void handleButtonOpen(ActionEvent actionEvent) {
        //
    }

    private void handleButtonFree(ActionEvent actionEvent) {
        canvas.setFreeTransform();
    }

    private void handleButtonAddDrone(ActionEvent actionEvent) {
        canvas.setState(State.ADD_DRONE);
    }

    private void handleButtonRemoveDrone(ActionEvent actionEvent) {
        canvas.setState(State.REMOVE_DRONE);
    }

    private void handleButtonObstacle(ActionEvent actionEvent) {
        canvas.setState(State.OBSTACLE);
    }

    private void handleButtonLine(ActionEvent actionEvent) {
        canvas.setState(State.LINE);
    }

    private void handleButtonRemoveLine(ActionEvent actionEvent) {
        canvas.setState(State.REMOVE_LINE);
    }

    private void handleButtonSave(ActionEvent actionEvent) {
        //
    }

    private void handleButtonRun(ActionEvent actionEvent) {
        //
    }

    private void handleButtonPlayPause(ActionEvent actionEvent) {
        //
    }

    private void handleButtonReplay(ActionEvent actionEvent) {
        //
    }

    private void handleButtonResetEnvironment(ActionEvent actionEvent) {
        //
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        // === Sidebar ===
        VBox sidebar = new VBox(2);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(4));

        // set on action for buttons
        btnOpen.setOnAction(this::handleButtonOpen);
        btnTransform.setOnAction(this::handleButtonFree);
        btnAddDrone.setOnAction(this::handleButtonAddDrone);
        btnRemoveDrone.setOnAction(this::handleButtonRemoveDrone);
        btnWall.setOnAction(this::handleButtonObstacle);
        btnDrawLine.setOnAction(this::handleButtonLine);
        btnRemoveLine.setOnAction(this::handleButtonRemoveLine);
        btnSave.setOnAction(this::handleButtonSave);
        btnRun.setOnAction(this::handleButtonRun);
        btnPlayPause.setOnAction(this::handleButtonPlayPause);
        btnReplay.setOnAction(this::handleButtonReplay);
        btnNew.setOnAction(this::handleButtonResetEnvironment);

        VBox topButtons = new VBox(2, btnNew, btnOpen, btnTransform, btnAddDrone, btnRemoveDrone, btnWall, btnDrawLine, btnRemoveLine, btnSave, btnRun, btnPlayPause, btnReplay);
        topButtons.setAlignment(Pos.TOP_CENTER);

        progressIndicator = new ProgressIndicator(0);
        progressIndicator.setVisible(false);
        Label statusLabel = new Label("");
        Label statelabel = new Label("");

        VBox bottomButtons = new VBox(2, progressIndicator);
        bottomButtons.setAlignment(Pos.BOTTOM_CENTER);
        VBox.setVgrow(topButtons, Priority.ALWAYS);
        sidebar.getChildren().addAll(topButtons, bottomButtons);

        // === Center Pane ===
        BorderPane centerPane = new BorderPane();
        centerPane.getStyleClass().add("center-pane");
        StackPane canvasPane = new StackPane();

        getCanvas();
        canvas.render();
        canvasPane.getChildren().add(canvas);

        // Scroll Pane
        ScrollPane scrollPane = new ScrollPane(canvasPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setPrefHeight(900);

        // === Log Area ===
        logInfo = new StringBuffer();
        logInfo.append("Simulator start...\n" + help);

        // === Log Area ===
        TextArea logArea = new TextArea();
        logArea.setPrefRowCount(3);
        logArea.setWrapText(false);
        logArea.setEditable(true);
        logArea.setBorder(null);
        logArea.setText("Simulator start...\n" + help);

        // Bungkus dalam ScrollPane
        ScrollPane logScrollPane = new ScrollPane(logArea);
        logScrollPane.setFitToWidth(true);
        logScrollPane.setFitToHeight(true);
        logScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        logScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);


        // === Status Bar ===
        statusLabel = new Label("Ready.");
        statusLabel.getStyleClass().add("status-label");
        statelabel = new Label("STATE: " + canvas.getState().toString() + "\t-\t");
        statelabel.getStyleClass().add("status-label");
        HBox statusBar = new HBox(statelabel, statusLabel);
        statusBar.getStyleClass().add("status-bar");
        statusBar.setPadding(new Insets(5));

        // SplitPane vertikal
        SplitPane splitPane = new SplitPane(scrollPane, logArea);
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.setDividerPositions(0.9);

        // === Bottom (log + status) ===
        VBox bottomBox = new VBox(statusBar);

        centerPane.setCenter(splitPane);
        centerPane.setBottom(bottomBox);

        // === Layout ===
        BorderPane root = new BorderPane();
        //root.setTop(topBar);
        root.setLeft(sidebar);
        root.setCenter(centerPane);
        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(CSS.style);

        // stage
        primaryStage.setTitle("Robotics & Mechatronics Laboratory - Mechanical Engineering - Hasanuddin University");
        primaryStage.setMaximized(true);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                Platform.exit();
                System.exit(0);
            }
        });
        primaryStage.getIcons().add(new Image(this.getClass().getResourceAsStream("icon.png")));
    }
}
