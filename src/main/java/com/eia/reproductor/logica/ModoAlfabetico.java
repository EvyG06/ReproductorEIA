package com.eia.reproductor.logica;

import com.eia.reproductor.estructuras.ArbolBinarioBusqueda;
import com.eia.reproductor.modelo.Cancion;

import java.util.Comparator;
import java.util.List;

public class ModoAlfabetico implements ModoReproduccion {

    private final ArbolBinarioBusqueda<Cancion> arbol =
            new ArbolBinarioBusqueda<>(
                    Comparator.comparing((Cancion c) -> c.getNombre().toLowerCase())
                            .thenComparing(c -> c.getArtista().toLowerCase()));

    private List<Cancion> recorrido;
    private int indice = -1;

    @Override
    public void cargar(List<Cancion> canciones) {
        arbol.vaciar();
        for (Cancion c : canciones) {
            arbol.insertar(c);
        }
        recorrido = arbol.recorridoInorden();
        indice = -1;
    }

    @Override
    public void agregar(Cancion c) {
        arbol.insertar(c);
        recalcularManteniendoPosicion(actual());
    }

    @Override
    public void eliminar(Cancion c) {
        Cancion previa = actual();
        boolean eraLaActual = c.equals(previa);
        int indicePrevio = indice;
        arbol.eliminar(c);
        if (eraLaActual) {
            recorrido = arbol.recorridoInorden();
            indice = recorrido.isEmpty() ? -1 : Math.min(indicePrevio, recorrido.size() - 1);
        } else {
            recalcularManteniendoPosicion(previa);
        }
    }

    @Override
    public void actualizar(List<Cancion> canciones, Cancion c) {
        // El nombre/artista pudo cambiar y romper el orden del árbol: se reconstruye
        // por completo y se reubica la posición por referencia (no por clave), sobre
        // la canción que estaba sonando antes (no necesariamente la editada).
        Cancion previa = actual();
        arbol.vaciar();
        for (Cancion cancion : canciones) {
            arbol.insertar(cancion);
        }
        recalcularManteniendoPosicion(previa);
    }

    private void recalcularManteniendoPosicion(Cancion cancionActual) {
        recorrido = arbol.recorridoInorden();
        indice = cancionActual == null ? -1 : recorrido.indexOf(cancionActual);
    }

    @Override
    public Cancion siguiente() {
        if (recorrido == null || recorrido.isEmpty()) return null;
        indice = (indice + 1) % recorrido.size();
        return recorrido.get(indice);
    }

    @Override
    public Cancion anterior() {
        if (recorrido == null || recorrido.isEmpty()) return null;
        indice = (indice - 1 + recorrido.size()) % recorrido.size();
        return recorrido.get(indice);
    }

    @Override
    public Cancion actual() {
        if (indice < 0 || recorrido == null || recorrido.isEmpty()) return null;
        return recorrido.get(indice);
    }

    @Override
    public boolean permiteRetroceder() {
        return true;
    }

    @Override
    public String getNombre() {
        return "Alfabético";
    }
}