package tareas.modelo;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * Tarea del tablero. Se modifica solo a través del {@link Tablero}, que lleva
 * el control de los estados y las fechas.
 */
public class Tarea implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final int id;
    private final LocalDate creada;
    private String titulo;
    private String descripcion;
    private Prioridad prioridad;
    private LocalDate fechaLimite;
    private Estado estado = Estado.PENDIENTE;
    private LocalDate completada;

    Tarea(int id, LocalDate creada, String titulo, String descripcion, Prioridad prioridad, LocalDate fechaLimite) {
        this.id = id;
        this.creada = creada;
        editar(titulo, descripcion, prioridad, fechaLimite);
    }

    final void editar(String nuevoTitulo, String nuevaDescripcion, Prioridad nuevaPrioridad, LocalDate nuevaFecha) {
        if (nuevoTitulo == null || nuevoTitulo.isBlank()) {
            throw new IllegalArgumentException("Ingrese el título de la tarea");
        }
        if (nuevaPrioridad == null) {
            throw new IllegalArgumentException("Seleccione la prioridad");
        }
        titulo = nuevoTitulo.strip();
        descripcion = nuevaDescripcion == null ? "" : nuevaDescripcion.strip();
        prioridad = nuevaPrioridad;
        fechaLimite = nuevaFecha;
    }

    void cambiarEstado(Estado nuevo, LocalDate hoy) {
        estado = nuevo;
        completada = nuevo == Estado.HECHA ? hoy : null;
    }

    /** Pendiente o en curso con la fecha límite ya pasada. */
    public boolean vencida(LocalDate hoy) {
        return estado != Estado.HECHA && fechaLimite != null && hoy.isAfter(fechaLimite);
    }

    public int getId() {
        return id;
    }

    public LocalDate getCreada() {
        return creada;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Prioridad getPrioridad() {
        return prioridad;
    }

    public LocalDate getFechaLimite() {
        return fechaLimite;
    }

    public Estado getEstado() {
        return estado;
    }

    public LocalDate getCompletada() {
        return completada;
    }

    @Override
    public String toString() {
        return titulo;
    }
}
