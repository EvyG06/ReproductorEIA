package com.eia.reproductor.ui;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.util.function.Consumer;

public class ServicioAudio {

    private MediaPlayer player;

    public boolean cargar(String ruta, Runnable alTerminar, Consumer<Double> alAvanzar) {
        detener();
        if (ruta == null || ruta.isBlank()) return false;
        File archivo = new File(ruta);
        if (!archivo.exists()) return false;
        try {
            Media media = new Media(archivo.toURI().toString());
            player = new MediaPlayer(media);
            player.setOnEndOfMedia(alTerminar);
            player.currentTimeProperty().addListener((obs, viejo, nuevo) -> {
                double total = player.getTotalDuration().toSeconds();
                if (total > 0) alAvanzar.accept(nuevo.toSeconds() / total);
            });
            return true;
        } catch (Exception e) {
            player = null;
            return false;
        }
    }

    public void reproducir() { if (player != null) player.play(); }
    public void pausar()     { if (player != null) player.pause(); }

    public void detener() {
        if (player != null) {
            player.stop();
            player.dispose();
            player = null;
        }
    }

    public boolean activo() { return player != null; }

    public double segundoActual() {
        return player == null ? 0 : player.getCurrentTime().toSeconds();
    }
}
