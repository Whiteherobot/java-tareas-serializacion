package tareas.vista;

import tareas.modelo.Prioridad;
import tareas.modelo.Tarea;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Datos de una tarea, para crearla o editarla.
 */
class FormularioTarea extends JPanel {

    static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    final JTextField titulo = new JTextField(24);
    final JTextArea descripcion = new JTextArea(4, 24);
    final JComboBox<Prioridad> prioridad = new JComboBox<>(Prioridad.values());
    final JTextField fechaLimite = new JTextField(9);
    private int fila;

    FormularioTarea() {
        super(new GridBagLayout());
        descripcion.setLineWrap(true);
        descripcion.setWrapStyleWord(true);
        prioridad.setSelectedItem(Prioridad.MEDIA);
        agregar("Título *", titulo);
        agregar("Descripción", new JScrollPane(descripcion));
        agregar("Prioridad", prioridad);
        agregar("Fecha límite (dd/mm/aaaa)", fechaLimite);
    }

    private void agregar(String etiqueta, JComponent campo) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = fila++;
        c.insets = new Insets(3, 4, 3, 4);
        c.anchor = GridBagConstraints.FIRST_LINE_END;
        add(new JLabel(etiqueta), c);
        c.gridx = 1;
        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        add(campo, c);
    }

    void cargar(Tarea t) {
        titulo.setText(t.getTitulo());
        descripcion.setText(t.getDescripcion());
        prioridad.setSelectedItem(t.getPrioridad());
        fechaLimite.setText(t.getFechaLimite() == null ? "" : t.getFechaLimite().format(FECHA));
    }

    String leerTitulo() {
        return titulo.getText();
    }

    String leerDescripcion() {
        return descripcion.getText();
    }

    Prioridad leerPrioridad() {
        return (Prioridad) prioridad.getSelectedItem();
    }

    /** La fecha límite es opcional: vacía significa sin fecha. */
    LocalDate leerFechaLimite() {
        String texto = fechaLimite.getText().strip();
        if (texto.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(texto, FECHA);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("La fecha límite no es válida (use dd/mm/aaaa o déjela vacía)");
        }
    }
}
