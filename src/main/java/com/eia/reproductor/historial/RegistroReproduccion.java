package com.eia.reproductor.historial;

import com.eia.reproductor.modelo.Cancion;

import java.time.LocalDateTime;

public record RegistroReproduccion(Cancion cancion, LocalDateTime momento) {
}
