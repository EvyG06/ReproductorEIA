package com.eia.reproductor.logica;

import com.eia.reproductor.modelo.Cancion;
import java.util.List;

public interface ModoReproduccion {
    void cargar(List<Cancion> canciones);
    Cancion siguiente();
    Cancion anterior();
    Cancion actual();
    boolean permiteRetroceder();
    String getNombre();
}