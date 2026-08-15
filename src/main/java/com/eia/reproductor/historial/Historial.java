package com.eia.reproductor.historial;

import com.eia.reproductor.estructuras.Pila;
import com.eia.reproductor.modelo.Cancion;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Registra las canciones reproducidas en la sesión actual, usando una {@link Pila}
 * (la reproducción más reciente queda siempre en el tope) y acumula estadísticas
 * simples a partir de ese registro.
 */
public class Historial {

    private static final int MAX_RECIENTES = 50;

    private final Pila<RegistroReproduccion> pila = new Pila<>();
    private final Map<Cancion, Integer> conteoPorCancion = new LinkedHashMap<>();
    private final Map<String, Integer> conteoPorGenero = new LinkedHashMap<>();
    private long segundosEscuchados;
    private int totalReproducciones;

    public void registrar(Cancion c) {
        pila.apilar(new RegistroReproduccion(c, LocalDateTime.now()));
        conteoPorCancion.merge(c, 1, Integer::sum);
        conteoPorGenero.merge(c.getGenero(), 1, Integer::sum);
        segundosEscuchados += c.getDuracionSegundos();
        totalReproducciones++;
    }

    /** Últimas reproducciones, la más reciente primero. */
    public List<RegistroReproduccion> recientes() {
        List<RegistroReproduccion> todas = pila.aLista();
        return todas.size() > MAX_RECIENTES ? todas.subList(0, MAX_RECIENTES) : todas;
    }

    public Optional<Map.Entry<Cancion, Integer>> masEscuchada() {
        return conteoPorCancion.entrySet().stream().max(Map.Entry.comparingByValue());
    }

    public Map<String, Integer> distribucionGeneros() {
        return conteoPorGenero;
    }

    public long segundosEscuchados() {
        return segundosEscuchados;
    }

    public int totalReproducciones() {
        return totalReproducciones;
    }

    public boolean estaVacio() {
        return totalReproducciones == 0;
    }
}
