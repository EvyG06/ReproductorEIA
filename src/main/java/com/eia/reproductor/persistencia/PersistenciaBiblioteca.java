package com.eia.reproductor.persistencia;

import com.eia.reproductor.modelo.Cancion;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PersistenciaBiblioteca {

    private static final Path RUTA = Paths.get(
            System.getProperty("user.home"), ".reproductor-eia", "biblioteca.txt");

    public void guardar(List<Cancion> canciones) {
        List<String> lineas = new ArrayList<>();
        for (Cancion c : canciones) {
            lineas.add(String.join("|",
                    esc(c.getNombre()), esc(c.getArtista()), esc(c.getAlbum()),
                    String.valueOf(c.getDuracionSegundos()), esc(c.getGenero()),
                    String.valueOf(c.getAnioLanzamiento()),
                    String.valueOf(c.getCalificacion()),
                    String.valueOf(c.isFavorita()),
                    esc(c.getRutaArchivo() == null ? "" : c.getRutaArchivo()),
                    esc(c.getRutaPortada() == null ? "" : c.getRutaPortada())));
        }
        try {
            Files.createDirectories(RUTA.getParent());
            Files.write(RUTA, lineas, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("No se pudo guardar la biblioteca: " + e.getMessage());
        }
    }

    public List<Cancion> cargar() {
        List<Cancion> resultado = new ArrayList<>();
        if (!Files.exists(RUTA)) return resultado;
        try {
            for (String linea : Files.readAllLines(RUTA, StandardCharsets.UTF_8)) {
                try {
                    String[] p = linea.split("\\|", -1);
                    if (p.length < 8) continue;
                    Cancion c = new Cancion(des(p[0]), des(p[1]), des(p[2]),
                            Integer.parseInt(p[3]), des(p[4]), Integer.parseInt(p[5]));
                    c.setCalificacion(Integer.parseInt(p[6]));
                    c.setFavorita(Boolean.parseBoolean(p[7]));
                    if (p.length > 8 && !p[8].isEmpty()) c.setRutaArchivo(des(p[8]));
                    if (p.length > 9 && !p[9].isEmpty()) c.setRutaPortada(des(p[9]));
                    resultado.add(c);
                } catch (RuntimeException lineaMala) {
                    System.err.println("Línea corrupta ignorada: " + linea);
                }
            }
        } catch (IOException e) {
            System.err.println("No se pudo cargar la biblioteca: " + e.getMessage());
        }
        return resultado;
    }

    private String esc(String s) {
        return s.replace("\\", "\\\\").replace("|", "\\p");
    }

    private String des(String s) {
        return s.replace("\\p", "|").replace("\\\\", "\\");
    }
}
