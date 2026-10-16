package tareas.modelo;

/**
 * Columnas del tablero, en el orden en que avanza una tarea.
 */
public enum Estado {
    PENDIENTE("Pendiente"),
    EN_CURSO("En curso"),
    HECHA("Hecha");

    private final String nombre;

    Estado(String nombre) {
        this.nombre = nombre;
    }

    public boolean tieneSiguiente() {
        return ordinal() < values().length - 1;
    }

    public boolean tieneAnterior() {
        return ordinal() > 0;
    }

    public Estado siguiente() {
        return values()[ordinal() + 1];
    }

    public Estado anterior() {
        return values()[ordinal() - 1];
    }

    @Override
    public String toString() {
        return nombre;
    }
}
