package com.eia.reproductor.modelo;

public class Cancion {
    private String nombre;
    private String artista;
    private String album;
    private int duracionSegundos;
    private String genero;
    private int anioLanzamiento;
    private int calificacion;
    private String rutaArchivo;
    private String rutaPortada;
    private boolean favorita;

    public Cancion(String nombre, String artista, String album,
                   int duracionSegundos, String genero, int anioLanzamiento) {

        this.nombre = nombre;
        this.artista = artista;
        this.album = album;
        this.duracionSegundos = duracionSegundos;
        this.genero = genero;
        this.anioLanzamiento = anioLanzamiento;
        this.calificacion = 0;
    }

    public void setCalificacion(int calificacion) {
        if (calificacion < 0 || calificacion > 100) {
            throw new IllegalArgumentException("La calificación debe estar entre 0 y 100");
        }
        this.calificacion = calificacion;
    }

    public String getDuracionFormateada() {
        return String.format("%d:%02d", duracionSegundos / 60, duracionSegundos % 60);
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getArtista() { return artista; }
    public void setArtista(String artista) { this.artista = artista; }

    public String getAlbum() { return album; }
    public void setAlbum(String album) { this.album = album; }

    public int getDuracionSegundos() { return duracionSegundos; }
    public void setDuracionSegundos(int duracionSegundos) { this.duracionSegundos = duracionSegundos; }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    public int getAnioLanzamiento() { return anioLanzamiento; }
    public void setAnioLanzamiento(int anioLanzamiento) { this.anioLanzamiento = anioLanzamiento; }

    public int getCalificacion() { return calificacion; }

    public String getRutaArchivo() { return rutaArchivo; }
    public void setRutaArchivo(String rutaArchivo) { this.rutaArchivo = rutaArchivo; }

    public String getRutaPortada() { return rutaPortada; }
    public void setRutaPortada(String rutaPortada) { this.rutaPortada = rutaPortada; }

    public boolean isFavorita() { return favorita; }
    public void setFavorita(boolean favorita) { this.favorita = favorita; }

    @Override
    public String toString() {
        return nombre + " - " + artista;
    }

}
