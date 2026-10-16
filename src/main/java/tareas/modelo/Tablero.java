package tareas.modelo;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Tablero de tareas. Se guarda completo, con todas sus tareas, en una sola
 * escritura de serialización.
 */
public class Tablero implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Primero la prioridad más alta; a igual prioridad, la fecha límite más cercana (sin fecha al final). */
    public static final Comparator<Tarea> ORDEN = Comparator
            .comparing(Tarea::getPrioridad, Comparator.reverseOrder())
            .thenComparing(Tarea::getFechaLimite, Comparator.nullsLast(Comparator.naturalOrder()))
            .thenComparingInt(Tarea::getId);

    private final String nombre;
    private final List<Tarea> tareas = new ArrayList<>();
    private int ultimoId;

    public Tablero(String nombre) {
        this.nombre = nombre;
    }

    public Tarea crear(String titulo, String descripcion, Prioridad prioridad, LocalDate fechaLimite, LocalDate hoy) {
        Tarea t = new Tarea(ultimoId + 1, hoy, titulo, descripcion, prioridad, fechaLimite);
        ultimoId++;
        tareas.add(t);
        return t;
    }

    public Tarea editar(int id, String titulo, String descripcion, Prioridad prioridad, LocalDate fechaLimite) {
        Tarea t = buscar(id);
        t.editar(titulo, descripcion, prioridad, fechaLimite);
        return t;
    }

    /** Pasa la tarea a otra columna; al llegar a "Hecha" queda registrada la fecha. */
    public Tarea mover(int id, Estado estado, LocalDate hoy) {
        Tarea t = buscar(id);
        t.cambiarEstado(estado, hoy);
        return t;
    }

    public Tarea eliminar(int id) {
        Tarea t = buscar(id);
        tareas.remove(t);
        return t;
    }

    public Tarea buscar(int id) {
        return tareas.stream().filter(t -> t.getId() == id).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No existe la tarea " + id));
    }

    public List<Tarea> columna(Estado estado) {
        return tareas.stream().filter(t -> t.getEstado() == estado).sorted(ORDEN).toList();
    }

    public List<Tarea> tareas() {
        return List.copyOf(tareas);
    }

    public long vencidas(LocalDate hoy) {
        return tareas.stream().filter(t -> t.vencida(hoy)).count();
    }

    /** Porcentaje de tareas hechas, de 0 a 100. */
    public int progreso() {
        if (tareas.isEmpty()) {
            return 0;
        }
        long hechas = tareas.stream().filter(t -> t.getEstado() == Estado.HECHA).count();
        return (int) Math.round(hechas * 100.0 / tareas.size());
    }

    public String getNombre() {
        return nombre;
    }
}
