package com.eia.reproductor.metadatos;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;

import java.io.File;

/**
 * Lee metadatos (título, artista, álbum, género, año, duración, carátula) directamente
 * del archivo de audio usando sus tags ID3, para no tener que digitarlos a mano.
 * Si el archivo no es MP3 o no trae tags, devuelve una {@link InfoAudio} vacía y el
 * usuario completa los campos manualmente, como antes.
 */
public class LectorMetadatos {

    private static final InfoAudio VACIA = new InfoAudio(null, null, null, null, null, null, null, null);

    public InfoAudio leer(File archivo) {
        if (archivo == null || !archivo.getName().toLowerCase().endsWith(".mp3")) {
            return VACIA;
        }
        try {
            Mp3File mp3 = new Mp3File(archivo);
            Integer duracion = (int) mp3.getLengthInSeconds();
            if (mp3.hasId3v2Tag()) {
                return desdeTag(mp3.getId3v2Tag(), duracion);
            } else if (mp3.hasId3v1Tag()) {
                return desdeTag(mp3.getId3v1Tag(), duracion);
            }
            return new InfoAudio(null, null, null, null, null, duracion, null, null);
        } catch (Exception e) {
            return VACIA;
        }
    }

    private InfoAudio desdeTag(ID3v1 tag, Integer duracion) {
        byte[] portada = null;
        String mimePortada = null;
        if (tag instanceof ID3v2 tagV2) {
            portada = tagV2.getAlbumImage();
            mimePortada = tagV2.getAlbumImageMimeType();
        }
        return new InfoAudio(
                limpiar(tag.getTitle()),
                limpiar(tag.getArtist()),
                limpiar(tag.getAlbum()),
                limpiar(tag.getGenreDescription()),
                parsearAnio(tag.getYear()),
                duracion,
                portada,
                mimePortada);
    }

    private String limpiar(String valor) {
        if (valor == null) return null;
        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio;
    }

    private Integer parsearAnio(String anio) {
        String limpio = limpiar(anio);
        if (limpio == null) return null;
        try {
            return Integer.parseInt(limpio.substring(0, Math.min(4, limpio.length())));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
