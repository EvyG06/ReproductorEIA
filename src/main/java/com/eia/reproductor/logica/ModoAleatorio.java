package com.eia.reproductor.logica;

import com.eia.reproductor.estructuras.ListaCircularDoble;
import com.eia.reproductor.modelo.Cancion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModoAleatorio implements ModoReproduccion {

    private final ListaCircularDoble<Cancion> lista = new ListaCircularDoble<>();

    @Override
    public void cargar(List<Cancion> canciones) {
        lista.vaciar();
        List<Cancion> copia = new ArrayList<>(canciones);
        Collections.shuffle(copia);
        for (Cancion c : copia) {
            lista.insertarAlFinal(c);
        }
    }

    @Override
    public Cancion siguiente() {
        return lista.avanzar();
    }

    @Override
    public Cancion anterior() {
        return lista.retroceder();
    }

    @Override
    public Cancion actual() {
        return lista.obtenerActual();
    }

    @Override
    public boolean permiteRetroceder() {
        return true;
    }

    @Override
    public String getNombre() {
        return "Aleatorio";
    }
}