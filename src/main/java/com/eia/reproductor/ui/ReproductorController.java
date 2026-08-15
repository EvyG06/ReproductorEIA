package com.eia.reproductor.ui;

import com.eia.reproductor.historial.Historial;
import com.eia.reproductor.historial.RegistroReproduccion;
import com.eia.reproductor.logica.*;
import com.eia.reproductor.metadatos.InfoAudio;
import com.eia.reproductor.metadatos.LectorMetadatos;
import com.eia.reproductor.modelo.Cancion;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class ReproductorController {

    private static final PseudoClass SONANDO = PseudoClass.getPseudoClass("sonando");
    private static final double ANCHO_PANEL_HISTORIAL = 340;

    @FXML private Label lblContador;
    @FXML private TextField txtBuscar;
    @FXML private ToggleButton filtroTodos, filtroGenero, filtroArtista, filtroFavoritos;
    @FXML private ListView<Cancion> listaCanciones;
    @FXML private ToggleButton modoAleatorio, modoLlegada, modoAlfabetico;
    @FXML private Label lblNombre, lblMeta, lblCalificacion;
    @FXML private ControlEstrellas estrellasPanel;
    @FXML private ProgressBar barraProgreso;
    @FXML private Slider sliderProgreso;
    @FXML private Label lblTiempoActual, lblTiempoTotal;
    @FXML private Button btnAnterior, btnReproducir, btnPausar, btnSiguiente, btnAgregar, btnEliminar, btnEditar;
    @FXML private Button btnHistorial;
    @FXML private ImageView imgPortada;
    @FXML private ImageView imgFondoDifuminado;
    @FXML private HBox ecualizador;
    @FXML private Rectangle barra1, barra2, barra3, barra4;
    @FXML private StackPane areaPrincipalStack, portadaPane, vinilo;
    @FXML private VBox infoCancionBox;
    @FXML private VBox panelHistorial, statsBox;
    @FXML private ListView<Cancion> listaHistorial;
    @FXML private Label lblToast;

    private final Reproductor reproductor = new Reproductor();
    private final ServicioAudio audio = new ServicioAudio();
    private final LectorMetadatos lectorMetadatos = new LectorMetadatos();
    private final Historial historial = new Historial();
    private Timeline timeline;
    private Timeline animacionEcualizador;
    private RotateTransition rotacionVinilo;
    private SequentialTransition toastEnCurso;
    private Rectangle[] barrasEcualizador;
    private int segundosTranscurridos;
    private boolean reproduciendo;
    private boolean buscando;

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

        listaHistorial.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Cancion c, boolean vacio) {
                super.updateItem(c, vacio);
                if (vacio || c == null) { setGraphic(null); setText(null); return; }
                Label nombre = new Label(c.getNombre());
                nombre.setStyle("-fx-text-fill: #F2F0F3; -fx-font-size: 13px;");
                Label artista = new Label(c.getArtista());
                artista.setStyle("-fx-text-fill: #9E8F94; -fx-font-size: 11px;");
                setGraphic(new VBox(2, nombre, artista));
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

        rotacionVinilo = new RotateTransition(Duration.seconds(6), vinilo);
        rotacionVinilo.setByAngle(360);
        rotacionVinilo.setCycleCount(Timeline.INDEFINITE);
        rotacionVinilo.setInterpolator(Interpolator.LINEAR);

        estrellasPanel.setOnCambio(this::onCalificarDesdePanel);

        sliderProgreso.setOnMousePressed(e -> buscando = true);
        sliderProgreso.setOnMouseReleased(e -> {
            buscando = false;
            aplicarSeek(sliderProgreso.getValue());
        });

        imgFondoDifuminado.fitWidthProperty().bind(areaPrincipalStack.widthProperty());
        imgFondoDifuminado.fitHeightProperty().bind(areaPrincipalStack.heightProperty());
        imgFondoDifuminado.setEffect(new GaussianBlur(80));

        // El vinilo asoma fuera de portadaPane a propósito, pero eso hace que JavaFX
        // expanda el área "clicable" de portadaPane hasta cubrir ese sobrante (incluyendo
        // el desenfoque de la sombra), tapando botones vecinos que quedan geométricamente
        // dentro de esa caja aunque no haya nada pintado ahí. pickOnBounds(false) no sirve
        // aquí porque Region no tiene una geometría propia distinta de sus bounds; nada
        // dentro de portadaPane necesita recibir clics, así que se saca del todo del árbol
        // de picking.
        portadaPane.setMouseTransparent(true);

        for (Node n : List.of(btnReproducir, btnPausar, btnAnterior, btnSiguiente,
                btnAgregar, btnEliminar, btnEditar, btnHistorial,
                modoAleatorio, modoLlegada, modoAlfabetico,
                filtroTodos, filtroGenero, filtroArtista, filtroFavoritos)) {
            aplicarEfectoHover(n);
        }

        cambiarModoSegunToggle();
    }

    private void animarEcualizador() {
        for (Rectangle barra : barrasEcualizador) {
            barra.setHeight(6 + Math.random() * 14);
        }
    }

    /** Escala suavemente un nodo al pasar el mouse; JavaFX CSS no soporta transiciones en :hover. */
    private void aplicarEfectoHover(Node nodo) {
        ScaleTransition agranda = new ScaleTransition(Duration.millis(120), nodo);
        agranda.setToX(1.06);
        agranda.setToY(1.06);
        ScaleTransition reduce = new ScaleTransition(Duration.millis(120), nodo);
        reduce.setToX(1.0);
        reduce.setToY(1.0);
        nodo.setOnMouseEntered(e -> { reduce.stop(); agranda.playFromStart(); });
        nodo.setOnMouseExited(e -> { agranda.stop(); reduce.playFromStart(); });
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
        scene.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> {
            System.err.println("[DEBUG] click en escena, target=" + e.getTarget()
                    + " x=" + e.getSceneX() + " y=" + e.getSceneY());
            System.err.flush();
        });
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

    // ---------- Historial ----------

    /** Abre/cierra el panel deslizable de historial y estadísticas. */
    @FXML
    private void onToggleHistorial() {
        boolean estaAbierto = panelHistorial.getTranslateX() == 0;
        TranslateTransition t = new TranslateTransition(Duration.millis(260), panelHistorial);
        t.setToX(estaAbierto ? ANCHO_PANEL_HISTORIAL : 0);
        t.play();
        if (!estaAbierto) actualizarPanelHistorial();
    }

    private void actualizarPanelHistorial() {
        listaHistorial.getItems().setAll(
                historial.recientes().stream().map(RegistroReproduccion::cancion).toList());
        statsBox.getChildren().setAll(construirEstadisticas());
    }

    private List<Node> construirEstadisticas() {
        List<Node> nodos = new ArrayList<>();
        if (historial.estaVacio()) {
            Label vacio = new Label("Aún no has reproducido ninguna canción.");
            vacio.getStyleClass().add("stat-linea");
            vacio.setWrapText(true);
            nodos.add(vacio);
            return nodos;
        }
        nodos.add(crearLineaStat("Reproducciones totales", String.valueOf(historial.totalReproducciones())));
        nodos.add(crearLineaStat("Tiempo escuchado", formatear((int) historial.segundosEscuchados())));
        historial.masEscuchada().ifPresent(entrada -> nodos.add(crearLineaStat(
                "Más escuchada", entrada.getKey().getNombre() + " (" + entrada.getValue() + ")")));

        Label tituloGeneros = new Label("Por género");
        tituloGeneros.getStyleClass().add("stat-titulo");
        nodos.add(tituloGeneros);

        Map<String, Integer> generos = historial.distribucionGeneros();
        int maximo = generos.values().stream().max(Integer::compareTo).orElse(1);
        for (Map.Entry<String, Integer> entrada : generos.entrySet()) {
            nodos.add(crearBarraGenero(entrada.getKey(), entrada.getValue(), maximo));
        }
        return nodos;
    }

    private Node crearLineaStat(String etiqueta, String valor) {
        Label linea = new Label(etiqueta + ":  " + valor);
        linea.getStyleClass().add("stat-linea");
        return linea;
    }

    private Node crearBarraGenero(String genero, int cantidad, int maximo) {
        double anchoMax = 180;
        Rectangle fondo = new Rectangle(anchoMax, 8);
        fondo.getStyleClass().add("genero-barra-fondo");
        fondo.setArcWidth(4); fondo.setArcHeight(4);
        Rectangle barra = new Rectangle(anchoMax * cantidad / maximo, 8);
        barra.getStyleClass().add("genero-barra");
        barra.setArcWidth(4); barra.setArcHeight(4);
        StackPane pila = new StackPane(fondo, barra);
        pila.setAlignment(Pos.CENTER_LEFT);
        Label etiqueta = new Label((genero == null || genero.isBlank() ? "Sin género" : genero) + " (" + cantidad + ")");
        etiqueta.getStyleClass().add("stat-linea");
        return new VBox(3, etiqueta, pila);
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
        sliderProgreso.setValue(0);
        lblTiempoActual.setText("0:00");
        reproduciendo = false;
        actualizarPanelCancion(c);
        if (c == null) { actualizarEstadoReproduccion(); return; }
        historial.registrar(c);
        if (panelHistorial.getTranslateX() == 0) actualizarPanelHistorial();
        boolean audioReal = audio.cargar(c.getRutaArchivo(),
                this::onSiguiente,
                progreso -> {
                    if (buscando) return;
                    barraProgreso.setProgress(progreso);
                    sliderProgreso.setValue(progreso);
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
     * en la lista, habilita/deshabilita Reproducir/Pausar y anima el ecualizador y el vinilo. */
    private void actualizarEstadoReproduccion() {
        btnReproducir.setDisable(reproduciendo);
        btnPausar.setDisable(!reproduciendo);

        ecualizador.setVisible(reproduciendo);
        ecualizador.setManaged(reproduciendo);
        if (reproduciendo) animacionEcualizador.play();
        else animacionEcualizador.stop();

        if (reproduciendo) rotacionVinilo.play();
        else rotacionVinilo.pause();

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
        if (buscando) return;
        Cancion actual = reproductor.getModoActivo().actual();
        if (actual == null) { detenerProgreso(); return; }
        segundosTranscurridos++;
        if (segundosTranscurridos >= actual.getDuracionSegundos()) {
            onSiguiente();
            return;
        }
        barraProgreso.setProgress((double) segundosTranscurridos / actual.getDuracionSegundos());
        sliderProgreso.setValue((double) segundosTranscurridos / actual.getDuracionSegundos());
        lblTiempoActual.setText(formatear(segundosTranscurridos));
    }

    /** Salta la reproducción (real o simulada) al punto arrastrado en la barra de progreso. */
    private void aplicarSeek(double fraccion) {
        Cancion actual = reproductor.getModoActivo().actual();
        if (actual == null) return;
        if (audio.activo()) {
            audio.seek(fraccion);
        } else {
            segundosTranscurridos = (int) Math.round(fraccion * actual.getDuracionSegundos());
            lblTiempoActual.setText(formatear(segundosTranscurridos));
        }
    }

    private void detenerProgreso() {
        timeline.stop();
        segundosTranscurridos = 0;
        barraProgreso.setProgress(0);
        sliderProgreso.setValue(0);
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
        agregarConDialogo(null);
    }

    /** Punto de entrada compartido por el botón "Agregar" y por soltar un archivo en la ventana. */
    private void agregarConDialogo(File archivoInicial) {
        dialogoCancion(null, archivoInicial).ifPresent(c -> {
            reproductor.agregarCancion(c);
            refrescarLista();
            mostrarToast("Canción agregada: " + c.getNombre());
        });
    }

    /** Se invoca desde App.java cuando el usuario suelta archivos sobre la ventana. */
    public void manejarArchivosSoltados(List<File> archivos) {
        List<File> validos = archivos.stream()
                .filter(f -> {
                    String n = f.getName().toLowerCase();
                    return n.endsWith(".mp3") || n.endsWith(".wav");
                })
                .toList();
        if (validos.isEmpty()) {
            mostrarToast("Ningún archivo de audio válido (.mp3/.wav)");
            return;
        }
        if (validos.size() > 1) {
            mostrarToast("Se soltaron " + validos.size() + " archivos, se usó: " + validos.get(0).getName());
        }
        agregarConDialogo(validos.get(0));
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
            mostrarToast("Canción eliminada: " + sel.getNombre());
        });
    }

    @FXML
    private void onEditar() {
        Cancion sel = listaCanciones.getSelectionModel().getSelectedItem();
        if (sel == null) { alerta("Selecciona una canción de la lista."); return; }
        dialogoCancion(sel, null).ifPresent(editada -> {
            reproductor.notificarEdicion(editada);
            refrescarLista();
            actualizarPanelCancion(reproductor.getModoActivo().actual());
            mostrarToast("Canción actualizada: " + editada.getNombre());
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

    private Optional<Cancion> dialogoCancion(Cancion existente, File archivoInicial) {
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
        Button btnAudio = new Button(existente == null ? "Elegir MP3/WAV... (obligatorio)" : "Elegir MP3/WAV...");
        Label lblAudio = new Label(rutas[0] == null ? "Sin archivo" : new File(rutas[0]).getName());
        Button btnPortada = new Button("Elegir portada...");
        Label lblPort = new Label(rutas[1] == null ? "Sin imagen" : new File(rutas[1]).getName());

        Button okButton = (Button) dialogo.getDialogPane().lookupButton(ButtonType.OK);
        Runnable validar = () -> okButton.setDisable(
                nombre.getText().isBlank() || artista.getText().isBlank()
                        || (existente == null && rutas[0] == null));
        nombre.textProperty().addListener((obs, viejo, nuevo) -> validar.run());
        artista.textProperty().addListener((obs, viejo, nuevo) -> validar.run());

        Consumer<File> cargarAudio = f -> {
            rutas[0] = f.getAbsolutePath();
            lblAudio.setText(f.getName());

            InfoAudio info = lectorMetadatos.leer(f);
            if (nombre.getText().isBlank() && info.titulo() != null) nombre.setText(info.titulo());
            if (artista.getText().isBlank() && info.artista() != null) artista.setText(info.artista());
            if (album.getText().isBlank() && info.album() != null) album.setText(info.album());
            if (genero.getText().isBlank() && info.genero() != null) genero.setText(info.genero());
            if (anio.getText().isBlank() && info.anioLanzamiento() != null) anio.setText(String.valueOf(info.anioLanzamiento()));
            // La duración detectada del archivo es la fuente de verdad: se sobrescribe
            // siempre (a diferencia de los demás campos), pero el usuario puede corregirla.
            if (info.duracionSegundos() != null) duracion.setText(String.valueOf(info.duracionSegundos()));
            if (rutas[1] == null && info.portada() != null) {
                File portada = guardarPortadaEmbebida(info, f);
                if (portada != null) { rutas[1] = portada.getAbsolutePath(); lblPort.setText(portada.getName()); }
            }
            validar.run();
        };
        btnAudio.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio", "*.mp3", "*.wav"));
            File f = fc.showOpenDialog(dialogo.getDialogPane().getScene().getWindow());
            if (f != null) cargarAudio.accept(f);
        });
        if (archivoInicial != null) cargarAudio.accept(archivoInicial);
        validar.run();
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
            imgFondoDifuminado.setImage(null);
            estrellasPanel.setVisible(false);
            estrellasPanel.setManaged(false);
            fundirEntrada(portadaPane, infoCancionBox);
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
            Image imagen = new Image(new File(c.getRutaPortada()).toURI().toString(), 280, 280, true, true);
            imgPortada.setImage(imagen);
            imgFondoDifuminado.setImage(imagen);
        } else {
            imgPortada.setImage(null);
            imgFondoDifuminado.setImage(null);
        }
        fundirEntrada(portadaPane, infoCancionBox);
    }

    /** Breve fundido de entrada al cambiar de canción, para que el cambio se sienta más vivo. */
    private void fundirEntrada(Node... nodos) {
        for (Node n : nodos) {
            FadeTransition f = new FadeTransition(Duration.millis(220), n);
            f.setFromValue(0.4);
            f.setToValue(1);
            f.play();
        }
    }

    /** Notificación no bloqueante (reemplaza los Alert informativos de éxito). */
    private void mostrarToast(String mensaje) {
        if (toastEnCurso != null) toastEnCurso.stop();
        lblToast.setText(mensaje);
        lblToast.setOpacity(0);
        FadeTransition entra = new FadeTransition(Duration.millis(200), lblToast);
        entra.setToValue(1);
        PauseTransition espera = new PauseTransition(Duration.seconds(1.6));
        FadeTransition sale = new FadeTransition(Duration.millis(300), lblToast);
        sale.setToValue(0);
        toastEnCurso = new SequentialTransition(entra, espera, sale);
        toastEnCurso.play();
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
