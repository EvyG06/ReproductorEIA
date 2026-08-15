# Reproductor EIA

Reproductor musical de escritorio en Java + JavaFX donde cada modo de reproducción está respaldado por una estructura de datos distinta, implementada desde cero. Trabajo 1 de la asignatura Lenguajes y Compiladores, Universidad EIA.

## Modos de reproducción

| Modo | Estructura | Comportamiento |
|---|---|---|
| Aleatorio | Lista Ligada Circular Doble | Orden barajado al activar el modo, navegación infinita en ambas direcciones, sin final de reproducción |
| Orden de llegada | Cola Simple (FIFO) | Reproduce en el orden en que se agregaron las canciones, lo reproducido sale de la cola, sin retroceso |
| Alfabético | Árbol Binario de Búsqueda | Orden por recorrido inorden del árbol, navegación bidireccional circular |

Las tres estructuras son genéricas (`ListaCircularDoble<T>`, `ColaSimple<T>`, `ArbolBinarioBusqueda<T>`) y no usan las colecciones equivalentes de `java.util` como almacenamiento. El árbol recibe su criterio de orden como `Comparator<T>` inyectado.

## Historial y estadísticas

Además de las tres estructuras obligatorias, un panel deslizable (botón **Historial** en la barra lateral) registra la sesión de reproducción usando una cuarta estructura propia: `Pila<T>`, una pila genérica enlazada (no `java.util.Stack`). Cada canción reproducida se apila; el panel la recorre de tope a base para mostrar "reproducidas recientemente" con la más reciente primero, y agrega estadísticas derivadas (reproducciones totales, tiempo escuchado, canción más escuchada, distribución por género).

## Arquitectura

```
com.eia.reproductor
├── modelo          Cancion
├── estructuras     ListaCircularDoble, ColaSimple, ArbolBinarioBusqueda, Pila y sus nodos
├── logica          ModoReproduccion (interfaz), las 3 estrategias, Reproductor
├── metadatos       InfoAudio, LectorMetadatos (lectura de tags/duración desde el archivo de audio)
├── historial       RegistroReproduccion, Historial (usa Pila<T>)
├── persistencia    PersistenciaBiblioteca
└── ui              App, ReproductorController, ServicioAudio, FXML y CSS
```

Separación estricta entre lógica y presentación: la capa `logica` es Java puro, sin imports de `javafx.*`. Los modos implementan el patrón Strategy, el controlador opera sobre una única referencia polimórfica `ModoReproduccion` y cambiar de modo es cambiar de estrategia y de estructura en vivo.

## Funcionalidades

- Agregar, eliminar (con confirmación), editar y buscar canciones en vivo
- **Duración detectada automáticamente del archivo de audio** (MP3 vía tags ID3 con `mp3agic`, WAV vía `javax.sound.sampled`), editable a mano si hace falta corregirla; elegir un archivo de audio es obligatorio al agregar una canción
- **Arrastrar y soltar** un `.mp3`/`.wav` sobre la ventana para agregarlo directamente, con metadatos ya detectados
- Calificación de 0 a 100 validada en el modelo
- Reproducción real de MP3/WAV con `MediaPlayer`, con degradación automática a simulación si la canción no tiene archivo
- **Barra de progreso arrastrable**: se puede hacer clic o arrastrar para saltar a otro punto de la canción, tanto en audio real como simulado
- Portadas de álbum elegidas por el usuario (o extraídas del MP3), con un **disco de vinilo animado** que gira detrás de la carátula mientras suena
- **Fondo difuminado dinámico** (blur de la carátula actual, estilo Apple Music) detrás del panel principal
- **Panel de historial y estadísticas** de reproducción, deslizable desde el borde derecho
- **Microinteracciones**: fundido al cambiar de canción, notificaciones tipo *toast* no bloqueantes al agregar/editar/eliminar, y efecto hover en botones y chips
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
- Dependencias: `javafx-controls`, `javafx-fxml`, `javafx-media`, `mp3agic` (se resuelven solas desde el `pom.xml`)

## Ejecución

Desde IntelliJ, panel Maven, `Plugins > javafx > javafx:run`. O por terminal

```
mvn javafx:run
```

## Autora

Evelyn Gómez Aristizábal ([@EvyG06](https://github.com/EvyG06))
Ingeniería de Sistemas, Universidad EIA
