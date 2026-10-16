package tareas.vista;

import tareas.modelo.Estado;
import tareas.modelo.Tablero;
import tareas.modelo.Tarea;
import tareas.persistencia.ArchivoTablero;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Tablero con sus tres columnas, la barra de acciones y el avance. Cada cambio
 * se guarda en el archivo.
 */
class PanelTablero extends JPanel {

    final JButton nueva = new JButton("Nueva tarea");
    final JButton editar = new JButton("Editar");
    final JButton atras = new JButton("← Mover");
    final JButton adelante = new JButton("Mover →");
    final JButton eliminar = new JButton("Eliminar");
    final JProgressBar progreso = new JProgressBar(0, 100);
    final JLabel resumen = new JLabel();
    final Map<Estado, ColumnaTareas> columnas = new EnumMap<>(Estado.class);

    private final Supplier<LocalDate> hoy;
    private final Dialogos dialogos;
    private Tablero tablero;
    private Path archivo;

    PanelTablero(Tablero tablero, Path archivo, Supplier<LocalDate> hoy, Dialogos dialogos) {
        super(new BorderLayout(0, 8));
        this.tablero = tablero;
        this.archivo = archivo;
        this.hoy = hoy;
        this.dialogos = dialogos;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 0));
        for (JButton b : new JButton[] {nueva, editar, atras, adelante, eliminar}) {
            acciones.add(b);
        }
        nueva.addActionListener(e -> nueva());
        editar.addActionListener(e -> editar());
        atras.addActionListener(e -> mover(false));
        adelante.addActionListener(e -> mover(true));
        eliminar.addActionListener(e -> eliminar());

        JPanel tableroVisual = new JPanel(new GridLayout(1, Estado.values().length, 8, 0));
        for (Estado estado : Estado.values()) {
            ColumnaTareas columna = new ColumnaTareas(estado, hoy);
            columna.lista.addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting() && columna.lista.getSelectedIndex() >= 0) {
                    // Solo una tarea seleccionada en todo el tablero.
                    columnas.values().stream().filter(c -> c != columna).forEach(c -> c.lista.clearSelection());
                }
                actualizarBotones();
            });
            columna.lista.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        editar();
                    }
                }
            });
            columnas.put(estado, columna);
            tableroVisual.add(columna);
        }

        progreso.setStringPainted(true);
        JPanel pie = new JPanel(new BorderLayout(8, 0));
        pie.add(progreso, BorderLayout.LINE_START);
        pie.add(resumen, BorderLayout.CENTER);

        add(acciones, BorderLayout.PAGE_START);
        add(tableroVisual, BorderLayout.CENTER);
        add(pie, BorderLayout.PAGE_END);
        refrescar();
    }

    void usar(Tablero otro, Path otroArchivo) {
        tablero = otro;
        archivo = otroArchivo;
        refrescar();
    }

    Tablero tablero() {
        return tablero;
    }

    Path archivo() {
        return archivo;
    }

    Tarea seleccionada() {
        return columnas.values().stream().map(ColumnaTareas::seleccionada)
                .filter(t -> t != null).findFirst().orElse(null);
    }

    void seleccionar(Tarea tarea) {
        ColumnaTareas columna = columnas.get(tarea.getEstado());
        for (int i = 0; i < columna.modelo.size(); i++) {
            if (columna.modelo.get(i).getId() == tarea.getId()) {
                columna.lista.setSelectedIndex(i);
                return;
            }
        }
    }

    private void nueva() {
        FormularioTarea formulario = new FormularioTarea();
        while (dialogos.pedirTarea("Nueva tarea", formulario)) {
            try {
                Tarea t = tablero.crear(formulario.leerTitulo(), formulario.leerDescripcion(),
                        formulario.leerPrioridad(), formulario.leerFechaLimite(), hoy.get());
                guardarYMostrar(t);
                return;
            } catch (IllegalArgumentException ex) {
                dialogos.error(ex.getMessage());
            }
        }
    }

    private void editar() {
        Tarea t = seleccionada();
        if (t == null) {
            return;
        }
        FormularioTarea formulario = new FormularioTarea();
        formulario.cargar(t);
        while (dialogos.pedirTarea("Editar tarea", formulario)) {
            try {
                tablero.editar(t.getId(), formulario.leerTitulo(), formulario.leerDescripcion(),
                        formulario.leerPrioridad(), formulario.leerFechaLimite());
                guardarYMostrar(t);
                return;
            } catch (IllegalArgumentException ex) {
                dialogos.error(ex.getMessage());
            }
        }
    }

    private void mover(boolean haciaAdelante) {
        Tarea t = seleccionada();
        if (t == null) {
            return;
        }
        Estado destino = haciaAdelante ? t.getEstado().siguiente() : t.getEstado().anterior();
        tablero.mover(t.getId(), destino, hoy.get());
        guardarYMostrar(t);
    }

    private void eliminar() {
        Tarea t = seleccionada();
        if (t != null && dialogos.confirmar("¿Eliminar la tarea \"" + t.getTitulo() + "\"?")) {
            tablero.eliminar(t.getId());
            guardarYMostrar(null);
        }
    }

    private void guardarYMostrar(Tarea seleccionar) {
        try {
            ArchivoTablero.guardar(tablero, archivo);
        } catch (IOException ex) {
            dialogos.error("No se pudo guardar en " + archivo + ": " + ex.getMessage());
        }
        refrescar();
        if (seleccionar != null) {
            seleccionar(seleccionar);
        }
    }

    void refrescar() {
        columnas.forEach((estado, columna) -> columna.mostrar(tablero.columna(estado)));
        progreso.setValue(tablero.progreso());
        progreso.setString(tablero.progreso() + " % hecho");
        long vencidas = tablero.vencidas(hoy.get());
        resumen.setText(tablero.tareas().size() + " tareas"
                + (vencidas > 0 ? " · " + vencidas + " vencida(s)" : "") + " · " + archivo);
        actualizarBotones();
    }

    private void actualizarBotones() {
        Tarea t = seleccionada();
        editar.setEnabled(t != null);
        eliminar.setEnabled(t != null);
        atras.setEnabled(t != null && t.getEstado().tieneAnterior());
        adelante.setEnabled(t != null && t.getEstado().tieneSiguiente());
    }
}
