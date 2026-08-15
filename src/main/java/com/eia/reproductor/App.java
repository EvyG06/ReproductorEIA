package com.eia.reproductor;

import com.eia.reproductor.ui.ReproductorController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

public class App extends Application {

    private static final String PATH_ABANICO =
            "M 3 17 C 3 9.54 9.04 3.5 16.5 3.5 C 23.96 3.5 30 9.54 30 17 L 26 19 L 7 19 Z M 14.2 19 L 18.8 19 L 17.6 30 L 15.4 30 Z";

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/eia/reproductor/ui/reproductor.fxml"));
        Scene scene = new Scene(loader.load(), 1280, 800);
        scene.getStylesheets().add(getClass().getResource("/com/eia/reproductor/ui/styles.css").toExternalForm());
        stage.setTitle("Reproductor EIA");
        stage.setScene(scene);
        stage.getIcons().add(crearIconoVentana());

        ReproductorController controller = loader.getController();
        controller.configurarAtajos(scene);

        scene.setOnDragOver(e -> {
            if (e.getDragboard().hasFiles()) e.acceptTransferModes(TransferMode.COPY);
            e.consume();
        });
        scene.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            boolean exito = db.hasFiles();
            if (exito) controller.manejarArchivosSoltados(db.getFiles());
            e.setDropCompleted(exito);
            e.consume();
        });

        stage.show();
    }

    private javafx.scene.image.Image crearIconoVentana() {
        SVGPath abanico = new SVGPath();
        abanico.setContent(PATH_ABANICO);
        abanico.setFill(Color.web("#D32935"));
        StackPane lienzo = new StackPane(abanico);
        lienzo.setStyle("-fx-background-color: #0E080A;");
        lienzo.setPrefSize(33, 33);
        new Scene(lienzo);
        return lienzo.snapshot(null, null);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
