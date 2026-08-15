package com.eia.reproductor.logica;

import com.eia.reproductor.modelo.Cancion;
import java.util.List;

public interface ModoReproduccion {
    void cargar(List<Cancion> canciones);
    void agregar(Cancion c);
    void eliminar(Cancion c);
    void actualizar(List<Cancion> canciones, Cancion c);
    Cancion siguiente();
    Cancion anterior();
    Cancion actual();
    boolean permiteRetroceder();
    String getNombre();
}