package com.eia.reproductor.estructuras;

import java.util.ArrayList;
import java.util.List;

public class Pila<T> {

    private NodoSimple<T> tope;
    private int tamanio;

    public void apilar(T dato) {
        NodoSimple<T> nuevo = new NodoSimple<>(dato);
        nuevo.setSiguiente(tope);
        tope = nuevo;
        tamanio++;
    }

    public T desapilar() {
        if (tope == null) return null;
        T dato = tope.getDato();
        tope = tope.getSiguiente();
        tamanio--;
        return dato;
    }

    public T verTope() {
        return tope == null ? null : tope.getDato();
    }

    /** De tope a base: el elemento más reciente queda primero. */
    public List<T> aLista() {
        List<T> resultado = new ArrayList<>();
        NodoSimple<T> cursor = tope;
        while (cursor != null) {
            resultado.add(cursor.getDato());
            cursor = cursor.getSiguiente();
        }
        return resultado;
    }

    public int getTamanio() { return tamanio; }
    public boolean estaVacia() { return tamanio == 0; }

    public void vaciar() {
        tope = null;
        tamanio = 0;
    }
}
