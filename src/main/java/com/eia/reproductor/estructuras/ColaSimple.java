package com.eia.reproductor.estructuras;

import java.util.ArrayList;
import java.util.List;

public class ColaSimple<T> {

    private NodoSimple<T> frente;
    private NodoSimple<T> fin;
    private int tamanio;

    public void encolar(T dato) {
        NodoSimple<T> nuevo = new NodoSimple<>(dato);
        if (fin == null) {
            frente = nuevo;
        } else {
            fin.setSiguiente(nuevo);
        }
        fin = nuevo;
        tamanio++;
    }

    public T desencolar() {
        if (frente == null) return null;
        T dato = frente.getDato();
        frente = frente.getSiguiente();
        if (frente == null) fin = null;
        tamanio--;
        return dato;
    }

    public T verFrente() {
        return frente == null ? null : frente.getDato();
    }

    public List<T> aLista() {
        List<T> resultado = new ArrayList<>();
        NodoSimple<T> cursor = frente;
        while (cursor != null) {
            resultado.add(cursor.getDato());
            cursor = cursor.getSiguiente();
        }
        return resultado;
    }

    public int getTamanio() { return tamanio; }
    public boolean estaVacia() { return tamanio == 0; }

    public void vaciar() {
        frente = null;
        fin = null;
        tamanio = 0;
    }
}