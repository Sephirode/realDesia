package desia.gui;

import desia.Game;
import desia.io.Io;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.text.Font;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.PrintStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * JavaFX wrapper that runs the game in a window.
 *
 * Strategy:
 * - Redirect System.out/System.err to a TextArea.
 * - Provide Io implementation (FxIo) that renders inputs as on-screen buttons / text field.
 * - Run the game loop on a background thread to keep the UI responsive.
 */
public final class FxApp extends Application {

    private TextArea output;
    private Label promptLabel;
    private FlowPane choiceButtons;
    private TextField textField;
    private Button textOk;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        output = new TextArea();
        output.setEditable(false);
        output.setWrapText(true);
        // parchment-friendly
        output.setStyle(
                "-fx-control-inner-background: rgba(255,255,255,0.0);" +
                "-fx-background-color: rgba(255,255,255,0.0);" +
                "-fx-text-fill: #111111;" +
                "-fx-highlight-fill: rgba(0,0,0,0.18);" +
                "-fx-highlight-text-fill: #111111;" +
                "-fx-font-size: 15px;"
        );

        promptLabel = new Label("");

        choiceButtons = new FlowPane(Orientation.HORIZONTAL);
        choiceButtons.setHgap(8);
        choiceButtons.setVgap(8);
        choiceButtons.setPrefWrapLength(860);

        textField = new TextField();
        textField.setPromptText("텍스트 입력");
        textOk = new Button("확인");
        HBox textRow = new HBox(8, textField, textOk);

        // default hidden; shown only for text input
        textField.setVisible(false);
        textOk.setVisible(false);

        ScrollPane choiceScroll = new ScrollPane(choiceButtons);
        choiceScroll.setFitToWidth(true);
        choiceScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        choiceScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        choiceScroll.setPrefViewportHeight(160);
        choiceScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox bottom = new VBox(8, promptLabel, choiceScroll, textRow);
        bottom.setPadding(new Insets(10));
        bottom.setStyle(
                "-fx-background-color: rgba(255,255,255,0.92);" +
                "-fx-border-color: rgba(0,0,0,0.65);" +
                "-fx-border-width: 2 0 0 0;"
        );

        // LEFT: log pane (parchment background + output)
        ImageView parchmentView = new ImageView();
        parchmentView.setImage(loadFirstAvailable(
                "/ui/parchment.png",
                "/ui/parchment.jpg"
        ));
        parchmentView.setPreserveRatio(false);

        StackPane logPane = new StackPane(parchmentView, output);
        logPane.setPadding(new Insets(12));
        logPane.setMinSize(0, 0);
        parchmentView.fitWidthProperty().bind(logPane.widthProperty());
        parchmentView.fitHeightProperty().bind(logPane.heightProperty());

        BorderPane overlay = new BorderPane();
        overlay.setCenter(logPane);
        overlay.setBottom(bottom);
        // IMPORTANT: allow SplitPane to shrink this side if needed.
        // (We will also prevent the right side from claiming a huge min width.)
        overlay.setMinSize(0, 0);

        // RIGHT: background + title overlay
        // Start screen rule: show ONLY the title (no chapter background yet).
        ImageView battleBgView = new ImageView();
        battleBgView.setImage(null);
        battleBgView.setPreserveRatio(false);

        ImageView titleView = new ImageView();
        titleView.setImage(loadFirstAvailable(
                "/ui/title.png",
                "/ui/title.jpg"
        ));
        titleView.setPreserveRatio(true);
        titleView.setSmooth(true);
        titleView.setVisible(true);

        // placeholder layer for battle UI (enemy/player images, hp bars, status icons)
        StackPane battleUiLayer = new StackPane();
        battleUiLayer.setPickOnBounds(false);
        battleUiLayer.setMouseTransparent(true);

        StackPane battlePane = new StackPane(battleBgView, battleUiLayer, titleView);
        battlePane.setStyle("-fx-background-color: #000000;");
        // CRITICAL: SplitPane uses each item's min/pref sizes to decide divider layout.
        // StackPane computes its min width from its child ImageView (i.e., the *original image size*)
        // unless we force the min size down.
        battlePane.setMinSize(0, 0);
        battlePane.setPrefWidth(Region.USE_COMPUTED_SIZE);

        SplitPane split = new SplitPane();
        split.getItems().addAll(overlay, battlePane);
        // Set divider after first layout pass so min sizes are already applied.
        Platform.runLater(() -> split.setDividerPositions(0.35));
        split.setStyle("-fx-background-color: transparent;");

        BorderPane root = new BorderPane(split);

        // Load custom UI font (optional). Place your .ttf/.otf at src/main/resources/fonts/
        String fontFamily = loadAppFontFamily();

        Scene scene = new Scene(root, 900, 700);
        if (fontFamily != null && !fontFamily.isBlank()) {
            // Apply globally so newly created nodes (buttons, labels) also use it.
            root.setStyle(appendStyle(root.getStyle(), "-fx-font-family: '" + fontFamily + "'; -fx-font-size: 14px;"));
            output.setStyle(appendStyle(output.getStyle(), "-fx-font-family: '" + fontFamily + "'; -fx-font-size: 14px;"));
            promptLabel.setStyle(appendStyle(promptLabel.getStyle(), "-fx-font-family: '" + fontFamily + "';"));
            textField.setStyle(appendStyle(textField.getStyle(), "-fx-font-family: '" + fontFamily + "';"));
            textOk.setStyle(appendStyle(textOk.getStyle(), "-fx-font-family: '" + fontFamily + "';"));
        }

        battleBgView.fitWidthProperty().bind(battlePane.widthProperty());
        battleBgView.fitHeightProperty().bind(battlePane.heightProperty());
        // Make title as large as possible while keeping the whole image visible.
        titleView.fitWidthProperty().bind(battlePane.widthProperty());
        titleView.fitHeightProperty().bind(battlePane.heightProperty());
        stage.setTitle("Desia (JavaFX)");
        stage.setScene(scene);
        stage.show();

        // Wire stdout/stderr -> TextArea
        PrintStream ps = new PrintStream(new FxTextAreaOutputStream(output), true, StandardCharsets.UTF_8);
        System.setOut(ps);
        System.setErr(ps);

        // Input bridge (buttons / text)
        FxInputView inputView = new FxInputView(promptLabel, choiceButtons, textField, textOk, battleBgView, titleView);
        Io io = new FxIo(inputView);

        // Start the game loop on a background thread
        Thread gameThread = new Thread(() -> {
            try {
                new Game(io).start();
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                Platform.runLater(() -> {
                    output.appendText("\n\n[게임 종료]\n");
                    // disable inputs
                    choiceButtons.getChildren().clear();
                    textField.setDisable(true);
                    textOk.setDisable(true);
                });
            }
        }, "desia-game-thread");
        gameThread.setDaemon(true);
        gameThread.start();

        stage.setOnCloseRequest(e -> {
            inputView.shutdown();
            Platform.exit();
        });
    }


    private static String appendStyle(String base, String extra) {
        String b = (base == null) ? "" : base.trim();
        if (!b.isEmpty() && !b.endsWith(";")) b += ";";
        String e = (extra == null) ? "" : extra.trim();
        if (!e.isEmpty() && !e.endsWith(";")) e += ";";
        return b + e;
    }

    private String loadAppFontFamily() {
        // Change this path if you use a different font file name.
        try (InputStream is = getClass().getResourceAsStream("/fonts/NanumMyeongjo.otf")) {
            if (is == null) return null;
            Font f = Font.loadFont(is, 16);
            return (f == null) ? null : f.getFamily();
        } catch (Exception e) {
            return null;
        }
    }

    private Image loadFirstAvailable(String... resourcePaths) {
        if (resourcePaths == null) return null;
        for (String p : resourcePaths) {
            if (p == null || p.isBlank()) continue;
            try (InputStream is = getClass().getResourceAsStream(p)) {
                if (is == null) continue;
                return new Image(is);
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
