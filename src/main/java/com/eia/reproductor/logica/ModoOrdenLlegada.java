package com.eia.reproductor.logica;

import com.eia.reproductor.estructuras.ColaSimple;
import com.eia.reproductor.modelo.Cancion;

import java.util.List;

public class ModoOrdenLlegada implements ModoReproduccion {

    private final ColaSimple<Cancion> cola = new ColaSimple<>();
    private Cancion enReproduccion;

    @Override
    public void cargar(List<Cancion> canciones) {
        cola.vaciar();
        enReproduccion = null;
        for (Cancion c : canciones) {
            cola.encolar(c);
        }
    }

    @Override
    public void agregar(Cancion c) {
        cola.encolar(c);
    }

    @Override
    public void eliminar(Cancion c) {
        cola.eliminar(c);
        if (c.equals(enReproduccion)) enReproduccion = null;
    }

    @Override
    public void actualizar(List<Cancion> canciones, Cancion c) {
        // El orden FIFO no depende del contenido de la canción.
    }

    @Override
    public Cancion siguiente() {
        enReproduccion = cola.desencolar();
        return enReproduccion;
    }

    @Override
    public Cancion anterior() {
        return null;
    }

    @Override
    public Cancion actual() {
        return enReproduccion;
    }

    @Override
    public boolean permiteRetroceder() {
        return false;
    }

    @Override
    public String getNombre() {
        return "Orden de llegada";
    }
}