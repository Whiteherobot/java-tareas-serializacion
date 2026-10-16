package tareas.vista;

import tareas.modelo.Estado;
import tareas.modelo.Prioridad;
import tareas.modelo.Tarea;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;

/**
 * Una columna del tablero: la lista de tareas de un estado.
 */
class ColumnaTareas extends JPanel {

    private static final Color VENCIDA = new Color(0xC62828);
    private static final Color SEPARADOR = new Color(0xE0E0E0);

    final Estado estado;
    final DefaultListModel<Tarea> modelo = new DefaultListModel<>();
    final JList<Tarea> lista = new JList<>(modelo);

    ColumnaTareas(Estado estado, Supplier<LocalDate> hoy) {
        super(new BorderLayout());
        this.estado = estado;
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lista.setCellRenderer((l, tarea, indice, seleccionada, foco) -> {
            JLabel celda = new JLabel(texto(tarea, hoy.get()));
            celda.setOpaque(true);
            celda.setBackground(seleccionada ? l.getSelectionBackground() : l.getBackground());
            celda.setForeground(seleccionada ? l.getSelectionForeground() : l.getForeground());
            celda.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, SEPARADOR),
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 4, 0, 0, colorPrioridad(tarea.getPrioridad())),
                            BorderFactory.createEmptyBorder(6, 8, 6, 6))));
            return celda;
        });
        add(new JScrollPane(lista), BorderLayout.CENTER);
    }

    void mostrar(List<Tarea> tareas) {
        modelo.clear();
        tareas.forEach(modelo::addElement);
        setBorder(BorderFactory.createTitledBorder(estado + " (" + tareas.size() + ")"));
    }

    Tarea seleccionada() {
        return lista.getSelectedValue();
    }

    static String texto(Tarea t, LocalDate hoy) {
        StringBuilder detalle = new StringBuilder(t.getPrioridad().toString());
        if (t.getEstado() == Estado.HECHA && t.getCompletada() != null) {
            detalle.append(" · hecha el ").append(t.getCompletada().format(FormularioTarea.FECHA));
        } else if (t.getFechaLimite() != null) {
            String fecha = t.getFechaLimite().format(FormularioTarea.FECHA);
            detalle.append(t.vencida(hoy)
                    ? " · <font color='#" + Integer.toHexString(VENCIDA.getRGB() & 0xFFFFFF) + "'>vencida el " + fecha + "</font>"
                    : " · vence el " + fecha);
        }
        return "<html><b>" + escapar(t.getTitulo()) + "</b><br><small>" + detalle + "</small></html>";
    }

    static Color colorPrioridad(Prioridad p) {
        return switch (p) {
            case ALTA -> new Color(0xE53935);
            case MEDIA -> new Color(0xFB8C00);
            case BAJA -> new Color(0x9E9E9E);
        };
    }

    private static String escapar(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
