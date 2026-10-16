package tareas.vista;

/**
 * Diálogos que necesita el tablero. En la ventana son JOptionPane; en las
 * pruebas se reemplazan para no abrir ventanas.
 */
interface Dialogos {

    /** Muestra el formulario; devuelve true si el usuario aceptó. */
    boolean pedirTarea(String titulo, FormularioTarea formulario);

    boolean confirmar(String pregunta);

    void error(String mensaje);
}
