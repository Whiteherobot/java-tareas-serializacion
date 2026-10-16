package tareas.vista;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tareas.modelo.Estado;
import tareas.modelo.Prioridad;
import tareas.modelo.Tablero;
import tareas.persistencia.ArchivoTablero;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PanelTableroTest {

    private static final LocalDate HOY = LocalDate.of(2022, 7, 22);

    @TempDir
    Path carpeta;

    /** Cada vez que se abre el formulario, se completa con la siguiente acción de la cola. */
    private final Deque<Consumer<FormularioTarea>> respuestas = new ArrayDeque<>();
    private final List<String> errores = new ArrayList<>();
    private final Dialogos dialogos = new Dialogos() {
        @Override
        public boolean pedirTarea(String titulo, FormularioTarea f) {
            Consumer<FormularioTarea> r = respuestas.poll();
            if (r == null) {
                return false;
            }
            r.accept(f);
            return true;
        }

        @Override
        public boolean confirmar(String p) {
            return true;
        }

        @Override
        public void error(String m) {
            errores.add(m);
        }
    };

    @Test
    void crearMoverYGuardar() throws IOException {
        Path archivo = carpeta.resolve("t.ser");
        PanelTablero panel = new PanelTablero(new Tablero("T"), archivo, () -> HOY, dialogos);

        respuestas.add(f -> {
            f.titulo.setText("Informe");
            f.prioridad.setSelectedItem(Prioridad.ALTA);
            f.fechaLimite.setText("31/02/2022");             // fecha imposible: primero da error
        });
        respuestas.add(f -> f.fechaLimite.setText("20/07/2022"));
        panel.nueva.doClick();
        assertTrue(errores.get(0).startsWith("La fecha límite no es válida"));
        assertEquals(1, panel.columnas.get(Estado.PENDIENTE).modelo.size());
        assertTrue(panel.resumen.getText().contains("1 vencida(s)"));

        assertTrue(panel.adelante.isEnabled());              // quedó seleccionada al crearla
        assertFalse(panel.atras.isEnabled());
        panel.adelante.doClick();
        panel.adelante.doClick();
        assertEquals(1, panel.columnas.get(Estado.HECHA).modelo.size());
        assertEquals("100 % hecho", panel.progreso.getString());

        Tablero guardado = ArchivoTablero.cargar(archivo, "x");
        assertEquals(Estado.HECHA, guardado.tareas().get(0).getEstado());
    }

    @Test
    void elTextoDeLaTareaAvisaSiEstaVencida() {
        Tablero t = new Tablero("T");
        var tarea = t.crear("Pagar <luz>", "", Prioridad.ALTA, HOY.minusDays(1), HOY);
        String texto = ColumnaTareas.texto(tarea, HOY);
        assertTrue(texto.contains("Pagar &lt;luz&gt;"));
        assertTrue(texto.contains("vencida el 21/07/2022"));
    }
}
