package tareas.persistencia;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tareas.modelo.Estado;
import tareas.modelo.Prioridad;
import tareas.modelo.Tablero;
import tareas.modelo.Tarea;

import java.awt.Point;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArchivoTableroTest {

    private static final LocalDate HOY = LocalDate.of(2022, 7, 22);

    @TempDir
    Path carpeta;

    private Tablero ejemplo() {
        Tablero t = new Tablero("Proyecto");
        Tarea a = t.crear("Diseño", "Pantallas", Prioridad.ALTA, HOY.plusDays(5), HOY);
        t.crear("Pruebas", "", Prioridad.MEDIA, null, HOY);
        t.mover(a.getId(), Estado.HECHA, HOY);
        return t;
    }

    @Test
    void elTableroSeRecuperaCompleto() throws IOException {
        Path archivo = carpeta.resolve("tablero.ser");
        ArchivoTablero.guardar(ejemplo(), archivo);
        ArchivoTablero.guardar(ejemplo(), archivo);   // guardar dos veces reemplaza, no agrega
        Tablero leido = ArchivoTablero.cargar(archivo, "x");
        assertEquals("Proyecto", leido.getNombre());
        assertEquals(2, leido.tareas().size());
        Tarea diseno = leido.columna(Estado.HECHA).get(0);
        assertEquals("Pantallas", diseno.getDescripcion());
        assertEquals(HOY, diseno.getCompletada());
        assertEquals(3, leido.crear("Nueva", "", Prioridad.BAJA, null, HOY).getId());   // el contador también se guarda
    }

    @Test
    void siNoExisteEmpiezaUnTableroNuevo() throws IOException {
        Tablero t = ArchivoTablero.cargar(carpeta.resolve("no-existe.ser"), "Mis tareas");
        assertEquals("Mis tareas", t.getNombre());
        assertTrue(t.tareas().isEmpty());
    }

    @Test
    void elFiltroRechazaClasesAjenas() throws IOException {
        Path ajeno = carpeta.resolve("ajeno.ser");
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(ajeno))) {
            out.writeObject(new ArrayList<>(List.of(new Point(1, 2))));
        }
        IOException ex = assertThrows(IOException.class, () -> ArchivoTablero.cargar(ajeno, "x"));
        assertEquals("El archivo contiene datos no permitidos o de otra versión", ex.getMessage());
    }

    @Test
    void rechazaArchivosQueNoSonSerializados() throws IOException {
        Path texto = carpeta.resolve("notas.ser");
        Files.writeString(texto, "hola");
        assertThrows(IOException.class, () -> ArchivoTablero.cargar(texto, "x"));
    }

    /** El error de la entrega original: un ObjectOutputStream nuevo por cada guardado en modo agregar. */
    @Test
    void agregarConOtroObjectOutputStreamCorrompeElArchivo() throws Exception {
        Path archivo = carpeta.resolve("agregado.ser");
        for (String texto : List.of("primero", "segundo")) {
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(archivo.toFile(), true))) {
                out.writeObject(texto);
            }
        }
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(archivo))) {
            assertEquals("primero", in.readObject());
            assertThrows(StreamCorruptedException.class, in::readObject);   // el segundo encabezado rompe la lectura
        }
    }
}
