package com.eia.reproductor.logica;

import com.eia.reproductor.modelo.Cancion;
import com.eia.reproductor.persistencia.PersistenciaBiblioteca;

import java.util.ArrayList;
import java.util.List;

public class Reproductor {

    private final List<Cancion> biblioteca = new ArrayList<>();
    private final PersistenciaBiblioteca persistencia = new PersistenciaBiblioteca();
    private ModoReproduccion modoActivo;

    public Reproductor() {
        biblioteca.addAll(persistencia.cargar());
        cambiarModo(new ModoAleatorio());
    }

    public void cambiarModo(ModoReproduccion modo) {
        this.modoActivo = modo;
        modo.cargar(biblioteca);
    }

    public void agregarCancion(Cancion c) {
        biblioteca.add(c);
        modoActivo.agregar(c);
        persistencia.guardar(biblioteca);
    }

    public boolean eliminarCancion(Cancion c) {
        boolean eliminada = biblioteca.remove(c);
        if (eliminada) {
            modoActivo.eliminar(c);
            persistencia.guardar(biblioteca);
        }
        return eliminada;
    }

    public void notificarEdicion(Cancion editada) {
        modoActivo.actualizar(biblioteca, editada);
        persistencia.guardar(biblioteca);
    }

    public List<Cancion> buscar(String texto) {
        String t = texto.toLowerCase();
        List<Cancion> resultado = new ArrayList<>();
        for (Cancion c : biblioteca) {
            if (c.getNombre().toLowerCase().contains(t)
                    || c.getArtista().toLowerCase().contains(t)
                    || c.getAlbum().toLowerCase().contains(t)) {
                resultado.add(c);
            }
        }
        return resultado;
    }

    public List<Cancion> getBiblioteca() {
        return new ArrayList<>(biblioteca);
    }

    public ModoReproduccion getModoActivo() {
        return modoActivo;
    }
}