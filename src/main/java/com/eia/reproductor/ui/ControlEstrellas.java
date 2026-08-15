package com.eia.reproductor.ui;

import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import java.util.function.IntConsumer;

/**
 * Calificación de 0 a 100 representada como 5 estrellas clicables (20 puntos cada una),
 * con previsualización al pasar el mouse. Reemplaza al Spinner numérico original para
 * que calificar sea algo visual e inmediato en vez de escribir un número.
 */
public class ControlEstrellas extends HBox {

    private static final PseudoClass LLENA = PseudoClass.getPseudoClass("llena");
    private static final int PUNTOS_POR_ESTRELLA = 20;

    private final Label[] estrellas = new Label[5];
    private int calificacion;
    private IntConsumer onCambio;

    public ControlEstrellas() {
        getStyleClass().add("control-estrellas");
        setSpacing(4);
        for (int i = 0; i < estrellas.length; i++) {
            Label estrella = new Label("★");
            estrella.getStyleClass().add("estrella");
            int posicion = i;
            estrella.setOnMouseEntered(e -> pintar(posicion + 1));
            estrella.setOnMouseClicked(e -> seleccionar(posicion));
            estrellas[i] = estrella;
            getChildren().add(estrella);
        }
        setOnMouseExited(e -> pintar(estrellasLlenas()));
    }

    public void setCalificacion(int calificacion) {
        this.calificacion = Math.max(0, Math.min(100, calificacion));
        pintar(estrellasLlenas());
    }

    public int getCalificacion() {
        return calificacion;
    }

    public void setOnCambio(IntConsumer onCambio) {
        this.onCambio = onCambio;
    }

    private void seleccionar(int posicion) {
        setCalificacion((posicion + 1) * PUNTOS_POR_ESTRELLA);
        if (onCambio != null) onCambio.accept(calificacion);
    }

    private int estrellasLlenas() {
        return (int) Math.round(calificacion / (double) PUNTOS_POR_ESTRELLA);
    }

    private void pintar(int llenas) {
        for (int i = 0; i < estrellas.length; i++) {
            estrellas[i].pseudoClassStateChanged(LLENA, i < llenas);
        }
    }
}
