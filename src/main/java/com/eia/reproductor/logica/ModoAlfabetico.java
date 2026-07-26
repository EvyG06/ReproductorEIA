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