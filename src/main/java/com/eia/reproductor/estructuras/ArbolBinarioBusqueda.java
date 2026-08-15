package com.eia.reproductor.estructuras;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ArbolBinarioBusqueda<T> {

    private NodoArbol<T> raiz;
    private int tamanio;
    private final Comparator<T> comparador;

    public ArbolBinarioBusqueda(Comparator<T> comparador) {
        this.comparador = comparador;
    }

    public void insertar(T dato) {
        raiz = insertarRec(raiz, dato);
        tamanio++;
    }

    private NodoArbol<T> insertarRec(NodoArbol<T> nodo, T dato) {
        if (nodo == null) return new NodoArbol<>(dato);
        if (comparador.compare(dato, nodo.getDato()) < 0) {
            nodo.setIzquierdo(insertarRec(nodo.getIzquierdo(), dato));
        } else {
            nodo.setDerecho(insertarRec(nodo.getDerecho(), dato));
        }
        return nodo;
    }

    public boolean eliminar(T dato) {
        int antes = tamanio;
        raiz = eliminarRec(raiz, dato);
        return tamanio < antes;
    }

    private NodoArbol<T> eliminarRec(NodoArbol<T> nodo, T dato) {
        if (nodo == null) return null;
        int cmp = comparador.compare(dato, nodo.getDato());
        if (cmp < 0) {
            nodo.setIzquierdo(eliminarRec(nodo.getIzquierdo(), dato));
        } else if (cmp > 0) {
            nodo.setDerecho(eliminarRec(nodo.getDerecho(), dato));
        } else {
            tamanio--;
            if (nodo.getIzquierdo() == null) return nodo.getDerecho();
            if (nodo.getDerecho() == null) return nodo.getIzquierdo();
            NodoArbol<T> sucesor = minimo(nodo.getDerecho());
            nodo.setDato(sucesor.getDato());
            nodo.setDerecho(eliminarSinContar(nodo.getDerecho(), sucesor.getDato()));
        }
        return nodo;
    }

    private NodoArbol<T> eliminarSinContar(NodoArbol<T> nodo, T dato) {
        if (nodo == null) return null;
        int cmp = comparador.compare(dato, nodo.getDato());
        if (cmp < 0) {
            nodo.setIzquierdo(eliminarSinContar(nodo.getIzquierdo(), dato));
        } else if (cmp > 0) {
            nodo.setDerecho(eliminarSinContar(nodo.getDerecho(), dato));
        } else {
            if (nodo.getIzquierdo() == null) return nodo.getDerecho();
            if (nodo.getDerecho() == null) return nodo.getIzquierdo();
        }
        return nodo;
    }

    private NodoArbol<T> minimo(NodoArbol<T> nodo) {
        while (nodo.getIzquierdo() != null) nodo = nodo.getIzquierdo();
        return nodo;
    }

    public List<T> recorridoInorden() {
        List<T> resultado = new ArrayList<>();
        inordenRec(raiz, resultado);
        return resultado;
    }

    private void inordenRec(NodoArbol<T> nodo, List<T> resultado) {
        if (nodo == null) return;
        inordenRec(nodo.getIzquierdo(), resultado);
        resultado.add(nodo.getDato());
        inordenRec(nodo.getDerecho(), resultado);
    }

    public int getTamanio() { return tamanio; }
    public boolean estaVacia() { return tamanio == 0; }

    public void vaciar() {
        raiz = null;
        tamanio = 0;
    }
}
