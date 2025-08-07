package frontier.based;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

public class ElegantUIWithSvgIcons extends Application {

    private double mouseAnchorX, mouseAnchorY, translateX = 0, translateY = 0, scale = 1.0;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(4));
        sidebar.setAlignment(Pos.TOP_CENTER);
        sidebar.setStyle("-fx-background-color: #1e1e1e; -fx-pref-width: 30;");

        // Contoh ikon home SVG path (material filled home)
        //String homePath = "M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z";

        String openPath = "M6.25 4.5A1.75 1.75 0 0 0 4.5 6.25v11.5c0 .966.783 1.75 1.75 1.75h11.5a1.75 1.75 0 0 0 1.75-1.75v-4a.75.75 0 0 1 1.5 0v4A3.25 3.25 0 0 1 17.75 21H6.25A3.25 3.25 0 0 1 3 17.75V6.25A3.25 3.25 0 0 1 6.25 3h4a.75.75 0 0 1 0 1.5h-4ZM13 3.75a.75.75 0 0 1 .75-.75h6.5a.75.75 0 0 1 .75.75v6.5a.75.75 0 0 1-1.5 0V5.56l-5.22 5.22a.75.75 0 0 1-1.06-1.06l5.22-5.22h-4.69a.75.75 0 0 1-.75-.75Z";
        sidebar.getChildren().add(createIconButton(openPath, 1, 1, "Home"));

        String texturePath = "M3.075 20.925v-1.4l16.45-16.45h1.425v1.4L4.475 20.925h-1.4ZM3 14.7v-2.8L11.9 3h2.8L3 14.7ZM3 7V3h4L3 7Zm14 14l4-4v4h-4Zm-7.7 0L21 9.3v2.8L12.1 21H9.3Z";
        sidebar.getChildren().add(createIconButton(texturePath, 1, 1, "Home"));

        String linePath = "M21.71 3.29a1 1 0 0 0-1.42 0l-18 18a1 1 0 0 0 0 1.42a1 1 0 0 0 1.42 0l18-18a1 1 0 0 0 0-1.42Z";
        sidebar.getChildren().add(createIconButton(linePath, 1, 1, "Home"));

        String lineDashPath = "M21.707 2.297a1 1 0 0 1 0 1.414l-.5.5a1 1 0 1 1-1.414-1.414l.5-.5a1 1 0 0 1 1.414 0Zm-4.004 4a1 1 0 0 1 0 1.414l-.997.997a1 1 0 1 1-1.414-1.414l.997-.997a1 1 0 0 1 1.414 0Zm-4.496 4.496a1 1 0 0 1 0 1.414l-1 1a1 1 0 0 1-1.414-1.414l1-1a1 1 0 0 1 1.414 0ZM8.703 16.71a1 1 0 1 0-1.414-1.414l-.998.997a1 1 0 1 0 1.414 1.415l.998-.998Zm-4.491 4.496a1 1 0 0 0-1.414-1.414l-.5.5a1 1 0 0 0 1.414 1.414l.5-.5Z";
        sidebar.getChildren().add(createIconButton(lineDashPath, 1, 1, "Home"));

        String savePath = "M2 1a1 1 0 0 0-1 1v12a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1V2a1 1 0 0 0-1-1H9.5a1 1 0 0 0-1 1v7.293l2.646-2.647a.5.5 0 0 1 .708.708l-3.5 3.5a.5.5 0 0 1-.708 0l-3.5-3.5a.5.5 0 1 1 .708-.708L7.5 9.293V2a2 2 0 0 1 2-2H14a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2V2a2 2 0 0 1 2-2h2.5a.5.5 0 0 1 0 1H2z";
        sidebar.getChildren().add(createIconButton(savePath, 1.1, 1.1, "Home"));

        String runPath = "M28.182 10.573c-1.552-.875-3.51.266-3.823.464l-6.714 3.807a2.621 2.621 0 0 0-3.359.083a2.62 2.62 0 0 0-.526 3.318c.646 1.089 1.979 1.557 3.167 1.115s1.885-1.672 1.667-2.917l6.74-3.828c.438-.276 1.464-.693 1.938-.427c.344.198.542.844.557 1.802h-.01v8.401c0 .786-.417 1.51-1.094 1.901l-9.63 5.563a2.197 2.197 0 0 1-2.193 0l-9.63-5.563a2.187 2.187 0 0 1-1.099-1.901v-11.12c0-.786.417-1.51 1.099-1.901l8.714-5.026a2.622 2.622 0 0 0 4.437-2.62a2.621 2.621 0 0 0-5.084.848l-8.99 5.193a4.041 4.041 0 0 0-2.031 3.505v11.115c0 1.453.771 2.786 2.026 3.51l9.625 5.563a4.051 4.051 0 0 0 4.052 0l9.63-5.563a4.063 4.063 0 0 0 2.026-3.505v-8.083h.005c.047-1.896-.464-3.151-1.5-3.734z";
        sidebar.getChildren().add(createIconButton(runPath, 0.7, 0.7, "Home"));

        String playpausePath = "M11 7H8v10h3V7Zm2 10h3V7h-3v10Z";
        sidebar.getChildren().add(createIconButton(playpausePath, 1, 1, "Home"));

        String restartPath = "m10 16.5l6-4.5l-6-4.5M22 12c0-5.54-4.46-10-10-10c-1.17 0-2.3.19-3.38.56l.7 1.94c.85-.34 1.74-.53 2.68-.53c4.41 0 8.03 3.62 8.03 8.03c0 4.41-3.62 8.03-8.03 8.03c-4.41 0-8.03-3.62-8.03-8.03c0-.94.19-1.88.53-2.72l-1.94-.66C2.19 9.7 2 10.83 2 12c0 5.54 4.46 10 10 10s10-4.46 10-10M5.47 3.97c.85 0 1.53.71 1.53 1.5C7 6.32 6.32 7 5.47 7c-.79 0-1.5-.68-1.5-1.53c0-.79.71-1.5 1.5-1.5Z";
        sidebar.getChildren().add(createIconButton(restartPath, 1, 1, "Home"));


        // Tambahkan ikon lainnya (misal search, notifications) menggunakan path SVG

        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(10));
        topBar.setStyle("-fx-background-color:#2e2e2e;");
        TextField searchField = new TextField();
        searchField.setPromptText("Cari");
        searchField.setStyle("-fx-background-color:#3e3e3e; -fx-text-fill:white; -fx-prompt-text-fill:gray; -fx-background-radius:20;");
        searchField.setPrefWidth(400);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Circle avatar = new Circle(15, Color.DARKORANGE);
        Label avatarLabel = new Label("S");
        avatarLabel.setTextFill(Color.WHITE);
        StackPane avatarPane = new StackPane(avatar, avatarLabel);
        topBar.getChildren().addAll(searchField, spacer, avatarPane);

        HBox tabBar = new HBox(20);
        tabBar.setPadding(new Insets(10));
        tabBar.setStyle("-fx-background-color:#2b2b2b;");
        tabBar.setAlignment(Pos.CENTER_LEFT);
        String[] tabs = {"Semua", "Vehicle", "Desain Meja", "Desain rumah", "Rumah kontainer", "Desain furnitur", "Yang Saya Simpan"};
        for (String t : tabs) {
            Label lbl = new Label(t);
            lbl.setTextFill(Color.LIGHTGRAY);
            tabBar.getChildren().add(lbl);
        }

        VBox topArea = new VBox(topBar, tabBar);

        StackPane contentPane = new StackPane();
        contentPane.setStyle("-fx-background-color:#1a1a1a;");
        Canvas canvas = new Canvas(800, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.DARKGRAY);
        gc.fillText("Canvas Area (Zoom & Drag)", 50, 50);
        contentPane.getChildren().add(canvas);

        setupCanvasInteraction(canvas);

        BorderPane root = new BorderPane();
        root.setLeft(sidebar);
        root.setTop(topArea);
        root.setCenter(contentPane);

        Scene scene = new Scene(root, 1200, 600);
        scene.setFill(Color.web("#1e1e1e"));
        stage.setScene(scene);
        stage.setTitle("Elegant UI with Material SVG Icons");
        stage.show();
    }

    private Button createIconButton(String svgPathData, double scaleX, double scaleY, String tooltip) {
        SVGPath path = new SVGPath();
        path.setContent(svgPathData);
        path.setFill(Color.LIGHTGRAY);
        path.setScaleX(scaleX);
        path.setScaleY(scaleY);
        Button btn = new Button();
        btn.setGraphic(path);
        btn.setTooltip(new Tooltip(tooltip));
        btn.setStyle("-fx-background-color: transparent;");
        //btn.setOnMouseEntered(e -> path.setFill(Color.WHITE));
        btn.setOnMouseEntered(e -> path.setFill(Color.valueOf("#0096FF")));
        btn.setOnMouseExited(e -> path.setFill(Color.LIGHTGRAY));
        return btn;
    }

    private void setupCanvasInteraction(Canvas canvas) {
        canvas.setOnScroll(e -> {
            double delta = e.getDeltaY() > 0 ? 0.1 : -0.1;
            scale = Math.max(0.5, Math.min(2.0, scale + delta));
            canvas.setScaleX(scale);
            canvas.setScaleY(scale);
        });
        canvas.setOnMousePressed(e -> {
            mouseAnchorX = e.getSceneX();
            mouseAnchorY = e.getSceneY();
        });
        canvas.setOnMouseDragged(e -> {
            translateX += e.getSceneX() - mouseAnchorX;
            translateY += e.getSceneY() - mouseAnchorY;
            canvas.setTranslateX(translateX);
            canvas.setTranslateY(translateY);
            mouseAnchorX = e.getSceneX();
            mouseAnchorY = e.getSceneY();
        });
    }
}
