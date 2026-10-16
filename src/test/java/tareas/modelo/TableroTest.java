package tareas.modelo;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TableroTest {

    private static final LocalDate HOY = LocalDate.of(2022, 7, 22);
    private final Tablero t = new Tablero("Proyecto");

    @Test
    void ordenaPorPrioridadYFechaLimite() {
        t.crear("Sin fecha", "", Prioridad.ALTA, null, HOY);
        t.crear("Baja", "", Prioridad.BAJA, HOY.plusDays(1), HOY);
        t.crear("Alta lejana", "", Prioridad.ALTA, HOY.plusDays(10), HOY);
        t.crear("Alta cercana", "", Prioridad.ALTA, HOY.plusDays(2), HOY);
        assertEquals(List.of("Alta cercana", "Alta lejana", "Sin fecha", "Baja"),
                t.columna(Estado.PENDIENTE).stream().map(Tarea::getTitulo).toList());
    }

    @Test
    void moverAHechaRegistraLaFechaYVolverLaQuita() {
        Tarea tarea = t.crear("Informe", "", Prioridad.MEDIA, HOY.minusDays(1), HOY);
        assertTrue(tarea.vencida(HOY));
        t.mover(tarea.getId(), Estado.HECHA, HOY);
        assertEquals(HOY, tarea.getCompletada());
        assertFalse(tarea.vencida(HOY));
        assertEquals(100, t.progreso());
        t.mover(tarea.getId(), Estado.EN_CURSO, HOY);
        assertNull(tarea.getCompletada());
    }

    @Test
    void progresoYVencidas() {
        t.crear("A", "", Prioridad.BAJA, HOY.minusDays(3), HOY);
        Tarea b = t.crear("B", "", Prioridad.BAJA, null, HOY);
        t.crear("C", "", Prioridad.BAJA, HOY.plusDays(3), HOY);
        t.mover(b.getId(), Estado.HECHA, HOY);
        assertEquals(33, t.progreso());
        assertEquals(1, t.vencidas(HOY));
    }

    @Test
    void idsNoSeReutilizanYSeValidaElTitulo() {
        Tarea a = t.crear("A", "", Prioridad.BAJA, null, HOY);
        t.eliminar(a.getId());
        assertEquals(2, t.crear("B", "", Prioridad.BAJA, null, HOY).getId());
        assertThrows(IllegalArgumentException.class, () -> t.crear(" ", "", Prioridad.BAJA, null, HOY));
        assertThrows(IllegalArgumentException.class, () -> t.eliminar(99));
    }

    @Test
    void estadosEnOrden() {
        assertEquals(Estado.EN_CURSO, Estado.PENDIENTE.siguiente());
        assertFalse(Estado.HECHA.tieneSiguiente());
        assertFalse(Estado.PENDIENTE.tieneAnterior());
    }
}
