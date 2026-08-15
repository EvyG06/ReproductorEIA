# Reproductor EIA

Reproductor musical de escritorio en Java + JavaFX donde cada modo de reproducción está respaldado por una estructura de datos distinta, implementada desde cero. Trabajo 1 de la asignatura Lenguajes y Compiladores, Universidad EIA.


## Modos de reproducción

| Modo | Estructura | Comportamiento |
|---|---|---|
| Aleatorio | Lista Ligada Circular Doble | Orden barajado al activar el modo, navegación infinita en ambas direcciones, sin final de reproducción |
| Orden de llegada | Cola Simple (FIFO) | Reproduce en el orden en que se agregaron las canciones, lo reproducido sale de la cola, sin retroceso |
| Alfabético | Árbol Binario de Búsqueda | Orden por recorrido inorden del árbol, navegación bidireccional circular |

Las tres estructuras son genéricas (`ListaCircularDoble<T>`, `ColaSimple<T>`, `ArbolBinarioBusqueda<T>`) y no usan las colecciones equivalentes de `java.util` como almacenamiento. El árbol recibe su criterio de orden como `Comparator<T>` inyectado.

## Arquitectura

```
com.eia.reproductor
├── modelo          Cancion
├── estructuras     ListaCircularDoble, ColaSimple, ArbolBinarioBusqueda y sus nodos
├── logica          ModoReproduccion (interfaz), las 3 estrategias, Reproductor
├── persistencia    PersistenciaBiblioteca
└── ui              App, ReproductorController, ServicioAudio, FXML y CSS
```

Separación estricta entre lógica y presentación: la capa `logica` es Java puro, sin imports de `javafx.*`. Los modos implementan el patrón Strategy, el controlador opera sobre una única referencia polimórfica `ModoReproduccion` y cambiar de modo es cambiar de estrategia y de estructura en vivo.

## Funcionalidades

- Agregar, eliminar (con confirmación), editar y buscar canciones en vivo
- Calificación de 0 a 100 validada en el modelo
- Reproducción real de MP3/WAV con `MediaPlayer`, con degradación automática a simulación si la canción no tiene archivo
- Barra de progreso con el tiempo real del audio, o simulada con `Timeline`
- Portadas de álbum elegidas por el usuario, con glifo de respaldo
- Modo oscuro con acento carmesí, diseñado primero como mockup en Figma
- Persistencia automática de la biblioteca en `~/.reproductor-eia/biblioteca.txt` (formato de texto propio con escape de delimitadores, tolerante a líneas corruptas)
- Filtros por género, artista y favoritos
- Atajos de teclado

## Atajos

| Atajo | Acción |
|---|---|
| Ctrl + R | Reproducir |
| Ctrl + P | Pausar |
| Ctrl + Derecha | Siguiente |
| Ctrl + Izquierda | Anterior (cuando el modo lo permite) |
| Ctrl + N | Agregar canción |

## Requisitos

- JDK 21
- Maven (o el Maven embebido de IntelliJ)
- Dependencias: `javafx-controls`, `javafx-fxml`, `javafx-media` (se resuelven solas desde el `pom.xml`)

## Ejecución

Desde IntelliJ, panel Maven, `Plugins > javafx > javafx:run`. O por terminal

```
mvn javafx:run
```


##Sebas la puse en master jeje

## Autora

Evelyn Gómez Aristizábal ([@EvyG06](https://github.com/EvyG06))
Ingeniería de Sistemas, Universidad EIA
