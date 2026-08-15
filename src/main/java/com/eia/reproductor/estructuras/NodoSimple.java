package com.eia.reproductor.estructuras;

public class NodoSimple<T> {
    private T dato;
    private NodoSimple<T> siguiente;

    public NodoSimple(T dato) {
        this.dato = dato;
    }

    public T getDato() { return dato; }
    public NodoSimple<T> getSiguiente() { return siguiente; }
    public void setSiguiente(NodoSimple<T> siguiente) { this.siguiente = siguiente; }
}