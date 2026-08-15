package com.eia.reproductor.metadatos;

public record InfoAudio(String titulo, String artista, String album, String genero,
                         Integer anioLanzamiento, Integer duracionSegundos,
                         byte[] portada, String portadaMimeType) {
}
