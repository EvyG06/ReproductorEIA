package com.eia.reproductor.estructuras;

import java.util.ArrayList;
import java.util.List;

public class ListaCircularDoble<T> {

    private NodoDoble<T> primero;
    private NodoDoble<T> actual;
    private int tamanio;

    public void insertarAlFinal(T dato) {
        NodoDoble<T> nuevo = new NodoDoble<>(dato);
        if (primero == null) {
            nuevo.setSiguiente(nuevo);
            nuevo.setAnterior(nuevo);
            primero = nuevo;
            actual = nuevo;
        } else {
            NodoDoble<T> ultimo = primero.getAnterior();
            ultimo.setSiguiente(nuevo);
            nuevo.setAnterior(ultimo);
            nuevo.setSiguiente(primero);
            primero.setAnterior(nuevo);
        }
        tamanio++;
    }

    public boolean eliminar(T dato) {
        if (primero == null) return false;
        NodoDoble<T> cursor = primero;
        for (int i = 0; i < tamanio; i++) {
            if (cursor.getDato().equals(dato)) {
                if (tamanio == 1) {
                    primero = null;
                    actual = null;
                } else {
                    cursor.getAnterior().setSiguiente(cursor.getSiguiente());
                    cursor.getSiguiente().setAnterior(cursor.getAnterior());
                    if (cursor == primero) primero = cursor.getSiguiente();
                    if (cursor == actual) actual = cursor.getSiguiente();
                }
                tamanio--;
                return true;
            }
            cursor = cursor.getSiguiente();
        }
        return false;
    }

    public T avanzar() {
        if (actual == null) return null;
        actual = actual.getSiguiente();
        return actual.getDato();
    }

    public T retroceder() {
        if (actual == null) return null;
        actual = actual.getAnterior();
        return actual.getDato();
    }

    public T obtenerActual() {
        return actual == null ? null : actual.getDato();
    }

    public List<T> aLista() {
        List<T> resultado = new ArrayList<>();
        NodoDoble<T> cursor = primero;
        for (int i = 0; i < tamanio; i++) {
            resultado.add(cursor.getDato());
            cursor = cursor.getSiguiente();
        }
        return resultado;
    }

    public int getTamanio() { return tamanio; }
    public boolean estaVacia() { return tamanio == 0; }

    public void vaciar() {
        primero = null;
        actual = null;
        tamanio = 0;
    }
}