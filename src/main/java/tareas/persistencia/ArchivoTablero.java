package tareas.persistencia;

import tareas.modelo.Tablero;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Guarda y recupera el tablero completo con serialización de objetos.
 *
 * <p>El tablero se escribe entero con un solo {@code writeObject}, en un
 * archivo temporal que luego reemplaza al anterior. Nunca se abre el archivo en
 * modo "agregar": cada {@code ObjectOutputStream} escribe su propio encabezado y
 * el archivo dejaría de poder leerse.
 *
 * <p>Al leer se aplica un filtro que solo acepta las clases del tablero y las
 * del JDK que estas usan. Deserializar sin filtro un archivo de origen
 * desconocido permite construir objetos arbitrarios, que es una vía conocida
 * de ataque.
 */
public final class ArchivoTablero {

    /**
     * Clases permitidas: las del modelo, la lista que las contiene (y su arreglo
     * interno Object[], que el filtro evalúa por su tipo de elemento), la clase base
     * de los enums y las fechas.
     * Todo lo demás se rechaza.
     */
    static final ObjectInputFilter FILTRO = ObjectInputFilter.Config.createFilter(
            "tareas.modelo.*;java.util.ArrayList;java.lang.Object;java.lang.Enum;java.time.*;"
                    + "maxdepth=10;maxrefs=100000;maxbytes=50000000;!*");

    private ArchivoTablero() {
    }

    public static void guardar(Tablero tablero, Path archivo) throws IOException {
        Path carpeta = archivo.toAbsolutePath().getParent();
        Files.createDirectories(carpeta);
        Path temporal = Files.createTempFile(carpeta, "tablero", ".tmp");
        try (ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(temporal)))) {
            out.writeObject(tablero);
        } catch (IOException ex) {
            Files.deleteIfExists(temporal);
            throw ex;
        }
        try {
            Files.move(temporal, archivo, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(temporal, archivo, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /** Lee el tablero; si el archivo no existe devuelve uno nuevo y vacío. */
    public static Tablero cargar(Path archivo, String nombreSiNoExiste) throws IOException {
        if (!Files.exists(archivo)) {
            return new Tablero(nombreSiNoExiste);
        }
        try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(archivo)))) {
            in.setObjectInputFilter(FILTRO);
            if (!(in.readObject() instanceof Tablero tablero)) {
                throw new IOException("El archivo no contiene un tablero de tareas");
            }
            return tablero;
        } catch (InvalidClassException ex) {
            throw new IOException("El archivo contiene datos no permitidos o de otra versión", ex);
        } catch (StreamCorruptedException | ClassNotFoundException ex) {
            throw new IOException("El archivo no es un tablero de tareas o está dañado", ex);
        } catch (java.io.EOFException ex) {
            throw new IOException("El archivo está incompleto", ex);
        }
    }
}
