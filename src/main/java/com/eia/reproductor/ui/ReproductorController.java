package com.eia.reproductor.ui;

import com.eia.reproductor.logica.*;
import com.eia.reproductor.metadatos.InfoAudio;
import com.eia.reproductor.metadatos.LectorMetadatos;
import com.eia.reproductor.modelo.Cancion;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class ReproductorController {

    private static final PseudoClass SONANDO = PseudoClass.getPseudoClass("sonando");

    @FXML private Label lblContador;
    @FXML private TextField txtBuscar;
    @FXML private ToggleButton filtroTodos, filtroGenero, filtroArtista, filtroFavoritos;
    @FXML private ListView<Cancion> listaCanciones;
    @FXML private ToggleButton modoAleatorio, modoLlegada, modoAlfabetico;
    @FXML private Label lblNombre, lblMeta, lblCalificacion;
    @FXML private ControlEstrellas estrellasPanel;
    @FXML private ProgressBar barraProgreso;
    @FXML private Label lblTiempoActual, lblTiempoTotal;
    @FXML private Button btnAnterior, btnReproducir, btnPausar;
    @FXML private ImageView imgPortada;
    @FXML private Label lblPortadaFallback;
    @FXML private HBox ecualizador;
    @FXML private Rectangle barra1, barra2, barra3, barra4;

    private final Reproductor reproductor = new Reproductor();
    private final ServicioAudio audio = new ServicioAudio();
    private final LectorMetadatos lectorMetadatos = new LectorMetadatos();
    private Timeline timeline;
    private Timeline animacionEcualizador;
    private Rectangle[] barrasEcualizador;
    private int segundosTranscurridos;
    private boolean reproduciendo;

    @FXML
    public void initialize() {
        ToggleGroup modos = new ToggleGroup();
        modoAleatorio.setToggleGroup(modos);
        modoLlegada.setToggleGroup(modos);
        modoAlfabetico.setToggleGroup(modos);
        modos.selectedToggleProperty().addListener((obs, viejo, nuevo) -> {
            if (nuevo == null) { viejo.setSelected(true); return; }
            cambiarModoSegunToggle();
        });

        ToggleGroup filtros = new ToggleGroup();
        filtroTodos.setToggleGroup(filtros);
        filtroGenero.setToggleGroup(filtros);
        filtroArtista.setToggleGroup(filtros);
        filtroFavoritos.setToggleGroup(filtros);
        filtroTodos.setOnAction(e -> refrescarLista());
        filtroGenero.setOnAction(e -> filtrarPorCampo("Género"));
        filtroArtista.setOnAction(e -> filtrarPorCampo("Artista"));
        filtroFavoritos.setOnAction(e -> mostrarEnLista(
                reproductor.getBiblioteca().stream().filter(Cancion::isFavorita).toList()));

        listaCanciones.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Cancion c, boolean vacio) {
                super.updateItem(c, vacio);
                if (vacio || c == null) {
                    setGraphic(null);
                    setText(null);
                    pseudoClassStateChanged(SONANDO, false);
                    return;
                }
                Label nombre = new Label((c.isFavorita() ? "♥ " : "") + c.getNombre());
                nombre.setStyle("-fx-text-fill: #F2F0F3; -fx-font-size: 14px;");
                Label artista = new Label(c.getArtista() + " · " + c.getDuracionFormateada());
                artista.setStyle("-fx-text-fill: #9E8F94; -fx-font-size: 12px;");
                setGraphic(new VBox(2, nombre, artista));
                pseudoClassStateChanged(SONANDO, c.equals(reproductor.getModoActivo().actual()));
            }
        });

        txtBuscar.textProperty().addListener((obs, viejo, nuevo) -> {
            if (nuevo.isBlank()) refrescarLista();
            else mostrarEnLista(reproductor.buscar(nuevo));
        });

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> tickProgreso()));
        timeline.setCycleCount(Timeline.INDEFINITE);

        barrasEcualizador = new Rectangle[] {barra1, barra2, barra3, barra4};
        animacionEcualizador = new Timeline(new KeyFrame(Duration.millis(160), e -> animarEcualizador()));
        animacionEcualizador.setCycleCount(Timeline.INDEFINITE);

        estrellasPanel.setOnCambio(this::onCalificarDesdePanel);

        cambiarModoSegunToggle();
    }

    private void animarEcualizador() {
        for (Rectangle barra : barrasEcualizador) {
            barra.setHeight(6 + Math.random() * 14);
        }
    }

    private void onCalificarDesdePanel(int calificacion) {
        Cancion actual = reproductor.getModoActivo().actual();
        if (actual == null) return;
        actual.setCalificacion(calificacion);
        reproductor.notificarEdicion(actual);
        lblCalificacion.setText("Calificación  " + actual.getCalificacion() + "/100"
                + (actual.isFavorita() ? "  ♥" : ""));
        listaCanciones.refresh();
    }

    /**
     * Se usa un event filter en vez de {@code scene.getAccelerators()} porque los
     * accelerators pueden perder la carrera contra el manejo propio de teclas de
     * TextField/ListView (p. ej. Ctrl+Flechas) cuando esos controles tienen el foco.
     * Un filter corre en la fase de captura, antes de que el nodo con foco reciba
     * el evento, así que el atajo siempre gana.
     */
    public void configurarAtajos(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::manejarAtajo);
    }

    private void manejarAtajo(KeyEvent evento) {
        if (!evento.isControlDown()) return;
        switch (evento.getCode()) {
            case R -> { onReproducir(); evento.consume(); }
            case P -> { onPausar(); evento.consume(); }
            case RIGHT -> { onSiguiente(); evento.consume(); }
            case LEFT -> {
                if (!btnAnterior.isDisabled()) onAnterior();
                evento.consume();
            }
            case N -> { onAgregar(); evento.consume(); }
            default -> { }
        }
    }

    // ---------- Modos ----------

    private void cambiarModoSegunToggle() {
        audio.detener();
        detenerProgreso();
        if (modoAleatorio.isSelected()) reproductor.cambiarModo(new ModoAleatorio());
        else if (modoLlegada.isSelected()) reproductor.cambiarModo(new ModoOrdenLlegada());
        else reproductor.cambiarModo(new ModoAlfabetico());
        btnAnterior.setDisable(!reproductor.getModoActivo().permiteRetroceder());
        actualizarPanelCancion(null);
        refrescarLista();
        actualizarEstadoReproduccion();
    }

    // ---------- Reproducción ----------

    private void reproducirCancion(Cancion c) {
        audio.detener();
        timeline.stop();
        segundosTranscurridos = 0;
        barraProgreso.setProgress(0);
        lblTiempoActual.setText("0:00");
        reproduciendo = false;
        actualizarPanelCancion(c);
        if (c == null) { actualizarEstadoReproduccion(); return; }
        boolean audioReal = audio.cargar(c.getRutaArchivo(),
                this::onSiguiente,
                progreso -> {
                    barraProgreso.setProgress(progreso);
                    lblTiempoActual.setText(formatear((int) audio.segundoActual()));
                });
        if (audioReal) audio.reproducir();
        else timeline.play();
        reproduciendo = true;
        actualizarEstadoReproduccion();
    }

    @FXML
    private void onReproducir() {
        Cancion actual = reproductor.getModoActivo().actual();
        if (actual == null) actual = reproductor.getModoActivo().siguiente();
        if (actual == null) { actualizarPanelCancion(null); return; }
        if (audio.activo()) {
            audio.reproducir();
            reproduciendo = true;
            actualizarEstadoReproduccion();
        } else if (segundosTranscurridos > 0) {
            timeline.play();
            reproduciendo = true;
            actualizarEstadoReproduccion();
        } else {
            reproducirCancion(actual);
        }
    }

    @FXML
    private void onPausar() {
        audio.pausar();
        timeline.pause();
        reproduciendo = false;
        actualizarEstadoReproduccion();
    }

    /** Refleja en la UI si algo está sonando: resalta y hace scroll a la canción activa
     * en la lista, habilita/deshabilita Reproducir/Pausar y anima el ecualizador. */
    private void actualizarEstadoReproduccion() {
        btnReproducir.setDisable(reproduciendo);
        btnPausar.setDisable(!reproduciendo);

        ecualizador.setVisible(reproduciendo);
        ecualizador.setManaged(reproduciendo);
        if (reproduciendo) animacionEcualizador.play();
        else animacionEcualizador.stop();

        Cancion sonando = reproductor.getModoActivo().actual();
        listaCanciones.getSelectionModel().select(sonando);
        if (sonando != null) listaCanciones.scrollTo(sonando);
        listaCanciones.refresh();
    }

    @FXML
    private void onSiguiente() {
        Cancion c = reproductor.getModoActivo().siguiente();
        reproducirCancion(c);
        if (modoLlegada.isSelected()) refrescarLista();
    }

    @FXML
    private void onAnterior() {
        Cancion c = reproductor.getModoActivo().anterior();
        if (c != null) reproducirCancion(c);
    }

    private void tickProgreso() {
        Cancion actual = reproductor.getModoActivo().actual();
        if (actual == null) { detenerProgreso(); return; }
        segundosTranscurridos++;
        if (segundosTranscurridos >= actual.getDuracionSegundos()) {
            onSiguiente();
            return;
        }
        barraProgreso.setProgress((double) segundosTranscurridos / actual.getDuracionSegundos());
        lblTiempoActual.setText(formatear(segundosTranscurridos));
    }

    private void detenerProgreso() {
        timeline.stop();
        segundosTranscurridos = 0;
        barraProgreso.setProgress(0);
        lblTiempoActual.setText("0:00");
        reproduciendo = false;
        actualizarEstadoReproduccion();
    }

    private String formatear(int segundos) {
        return String.format("%d:%02d", segundos / 60, segundos % 60);
    }

    // ---------- CRUD ----------

    @FXML
    private void onAgregar() {
        dialogoCancion(null).ifPresent(c -> {
            reproductor.agregarCancion(c);
            refrescarLista();
        });
    }

    @FXML
    private void onEliminar() {
        Cancion sel = listaCanciones.getSelectionModel().getSelectedItem();
        if (sel == null) { alerta("Selecciona una canción de la lista."); return; }
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar \"" + sel.getNombre() + "\"?", ButtonType.YES, ButtonType.NO);
        conf.showAndWait().filter(b -> b == ButtonType.YES).ifPresent(b -> {
            audio.detener();
            detenerProgreso();
            reproductor.eliminarCancion(sel);
            refrescarLista();
            actualizarPanelCancion(null);
        });
    }

    @FXML
    private void onEditar() {
        Cancion sel = listaCanciones.getSelectionModel().getSelectedItem();
        if (sel == null) { alerta("Selecciona una canción de la lista."); return; }
        dialogoCancion(sel).ifPresent(editada -> {
            reproductor.notificarEdicion(editada);
            refrescarLista();
            actualizarPanelCancion(reproductor.getModoActivo().actual());
        });
    }

    // ---------- Filtros ----------

    private void filtrarPorCampo(String campo) {
        List<String> valores = reproductor.getBiblioteca().stream()
                .map(c -> campo.equals("Género") ? c.getGenero() : c.getArtista())
                .distinct().sorted().toList();
        if (valores.isEmpty()) { refrescarLista(); return; }
        ChoiceDialog<String> dialogo = new ChoiceDialog<>(valores.get(0), valores);
        dialogo.setTitle("Filtrar");
        dialogo.setHeaderText("Filtrar por " + campo.toLowerCase());
        dialogo.showAndWait().ifPresent(valor -> mostrarEnLista(
                reproductor.getBiblioteca().stream()
                        .filter(c -> valor.equals(campo.equals("Género") ? c.getGenero() : c.getArtista()))
                        .toList()));
    }

    // ---------- Auxiliares ----------

    private Optional<Cancion> dialogoCancion(Cancion existente) {
        Dialog<Cancion> dialogo = new Dialog<>();
        dialogo.setTitle(existente == null ? "Agregar canción" : "Editar canción");
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nombre = new TextField(existente == null ? "" : existente.getNombre());
        TextField artista = new TextField(existente == null ? "" : existente.getArtista());
        TextField album = new TextField(existente == null ? "" : existente.getAlbum());
        TextField duracion = new TextField(existente == null ? "" : String.valueOf(existente.getDuracionSegundos()));
        TextField genero = new TextField(existente == null ? "" : existente.getGenero());
        TextField anio = new TextField(existente == null ? "" : String.valueOf(existente.getAnioLanzamiento()));
        ControlEstrellas calificacion = new ControlEstrellas();
        calificacion.setCalificacion(existente == null ? 50 : existente.getCalificacion());
        CheckBox favorita = new CheckBox("Favorita");
        if (existente != null) favorita.setSelected(existente.isFavorita());

        final String[] rutas = {
                existente == null ? null : existente.getRutaArchivo(),
                existente == null ? null : existente.getRutaPortada()
        };
        Button btnAudio = new Button("Elegir MP3/WAV...");
        Label lblAudio = new Label(rutas[0] == null ? "Sin archivo" : new File(rutas[0]).getName());
        Button btnPortada = new Button("Elegir portada...");
        Label lblPort = new Label(rutas[1] == null ? "Sin imagen" : new File(rutas[1]).getName());
        btnAudio.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio", "*.mp3", "*.wav"));
            File f = fc.showOpenDialog(dialogo.getDialogPane().getScene().getWindow());
            if (f == null) return;
            rutas[0] = f.getAbsolutePath();
            lblAudio.setText(f.getName());

            InfoAudio info = lectorMetadatos.leer(f);
            if (nombre.getText().isBlank() && info.titulo() != null) nombre.setText(info.titulo());
            if (artista.getText().isBlank() && info.artista() != null) artista.setText(info.artista());
            if (album.getText().isBlank() && info.album() != null) album.setText(info.album());
            if (genero.getText().isBlank() && info.genero() != null) genero.setText(info.genero());
            if (anio.getText().isBlank() && info.anioLanzamiento() != null) anio.setText(String.valueOf(info.anioLanzamiento()));
            if (duracion.getText().isBlank() && info.duracionSegundos() != null) duracion.setText(String.valueOf(info.duracionSegundos()));
            if (rutas[1] == null && info.portada() != null) {
                File portada = guardarPortadaEmbebida(info, f);
                if (portada != null) { rutas[1] = portada.getAbsolutePath(); lblPort.setText(portada.getName()); }
            }
        });
        btnPortada.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imagen", "*.png", "*.jpg", "*.jpeg"));
            File f = fc.showOpenDialog(dialogo.getDialogPane().getScene().getWindow());
            if (f != null) { rutas[1] = f.getAbsolutePath(); lblPort.setText(f.getName()); }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        String[] etiquetas = {"Nombre", "Artista", "Álbum", "Duración (seg)", "Género", "Año", "Calificación"};
        Node[] campos = {nombre, artista, album, duracion, genero, anio, calificacion};
        for (int i = 0; i < etiquetas.length; i++) {
            grid.add(new Label(etiquetas[i]), 0, i);
            grid.add(campos[i], 1, i);
        }
        grid.add(btnAudio, 0, etiquetas.length);
        grid.add(lblAudio, 1, etiquetas.length);
        grid.add(btnPortada, 0, etiquetas.length + 1);
        grid.add(lblPort, 1, etiquetas.length + 1);
        grid.add(favorita, 1, etiquetas.length + 2);
        dialogo.getDialogPane().setContent(grid);

        dialogo.setResultConverter(boton -> {
            if (boton != ButtonType.OK) return null;
            try {
                int dur = Integer.parseInt(duracion.getText().trim());
                int an = Integer.parseInt(anio.getText().trim());
                if (nombre.getText().isBlank() || artista.getText().isBlank()) return null;
                Cancion c = existente != null ? existente
                        : new Cancion(nombre.getText().trim(), artista.getText().trim(),
                                      album.getText().trim(), dur, genero.getText().trim(), an);
                if (existente != null) {
                    c.setNombre(nombre.getText().trim());
                    c.setArtista(artista.getText().trim());
                    c.setAlbum(album.getText().trim());
                    c.setDuracionSegundos(dur);
                    c.setGenero(genero.getText().trim());
                    c.setAnioLanzamiento(an);
                }
                c.setCalificacion(calificacion.getCalificacion());
                c.setFavorita(favorita.isSelected());
                c.setRutaArchivo(rutas[0]);
                c.setRutaPortada(rutas[1]);
                return c;
            } catch (NumberFormatException ex) {
                return null;
            }
        });
        return dialogo.showAndWait();
    }

    private void actualizarPanelCancion(Cancion c) {
        if (c == null) c = reproductor.getModoActivo().actual();
        if (c == null) {
            lblNombre.setText(modoLlegada.isSelected() && !reproductor.getBiblioteca().isEmpty()
                    ? "Fin de la cola" : "Sin reproducción");
            lblMeta.setText("Agrega canciones o presiona Reproducir");
            lblCalificacion.setText("");
            lblTiempoTotal.setText("0:00");
            imgPortada.setImage(null);
            lblPortadaFallback.setVisible(true);
            estrellasPanel.setVisible(false);
            estrellasPanel.setManaged(false);
            return;
        }
        lblNombre.setText(c.getNombre());
        lblMeta.setText(c.getArtista() + " · " + c.getAlbum() + " · "
                + c.getAnioLanzamiento() + " · " + c.getGenero());
        lblCalificacion.setText("Calificación  " + c.getCalificacion() + "/100"
                + (c.isFavorita() ? "  ♥" : ""));
        lblTiempoTotal.setText(c.getDuracionFormateada());
        estrellasPanel.setVisible(true);
        estrellasPanel.setManaged(true);
        estrellasPanel.setCalificacion(c.getCalificacion());

        if (c.getRutaPortada() != null && new File(c.getRutaPortada()).exists()) {
            imgPortada.setImage(new Image(new File(c.getRutaPortada()).toURI().toString(), 280, 280, true, true));
            lblPortadaFallback.setVisible(false);
        } else {
            imgPortada.setImage(null);
            lblPortadaFallback.setVisible(true);
        }
    }

    private void refrescarLista() {
        mostrarEnLista(reproductor.getBiblioteca());
    }

    private void mostrarEnLista(List<Cancion> canciones) {
        listaCanciones.getItems().setAll(canciones);
        lblContador.setText("Biblioteca · " + reproductor.getBiblioteca().size() + " canciones");
    }

    private void alerta(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }

    /** Guarda la carátula embebida en el MP3 como archivo, para poder asignarla como portada. */
    private File guardarPortadaEmbebida(InfoAudio info, File archivoAudio) {
        try {
            Path carpeta = Paths.get(System.getProperty("user.home"), ".reproductor-eia", "covers");
            Files.createDirectories(carpeta);
            String extension = info.portadaMimeType() != null && info.portadaMimeType().contains("png") ? "png" : "jpg";
            String base = archivoAudio.getName().replaceAll("[^a-zA-Z0-9.-]", "_");
            Path destino = carpeta.resolve(base + "." + extension);
            Files.write(destino, info.portada());
            return destino.toFile();
        } catch (IOException e) {
            return null;
        }
    }
}
