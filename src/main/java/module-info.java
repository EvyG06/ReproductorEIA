module com.eia.reproductor {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    exports com.eia.reproductor;

    opens com.eia.reproductor to javafx.fxml;
    opens com.eia.reproductor.ui to javafx.fxml;
}
