package tareas.vista;

import tareas.modelo.Tablero;
import tareas.persistencia.ArchivoTablero;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Ventana del tablero con el menú de archivo.
 */
public class VentanaTareas extends JFrame implements Dialogos {

    static final Path ARCHIVO_PREDETERMINADO = Path.of(System.getProperty("user.home"), ".tablero-tareas", "tablero.ser");

    private final PanelTablero panel;

    public VentanaTareas(Tablero tablero, Path archivo) {
        super("Tablero de tareas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        panel = new PanelTablero(tablero, archivo, LocalDate::now, this);

        JMenuItem abrir = new JMenuItem("Abrir…");
        abrir.addActionListener(e -> abrir());
        JMenuItem guardarComo = new JMenuItem("Guardar como…");
        guardarComo.addActionListener(e -> guardarComo());
        JMenu menu = new JMenu("Archivo");
        menu.add(abrir);
        menu.add(guardarComo);
        JMenuBar barra = new JMenuBar();
        barra.add(menu);
        setJMenuBar(barra);

        setContentPane(panel);
        setSize(960, 600);
        setLocationRelativeTo(null);
    }

    private JFileChooser selector() {
        JFileChooser selector = new JFileChooser(panel.archivo().toAbsolutePath().getParent().toFile());
        selector.setFileFilter(new FileNameExtensionFilter("Tablero de tareas (*.ser)", "ser"));
        return selector;
    }

    private void abrir() {
        JFileChooser selector = selector();
        if (selector.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            Path elegido = selector.getSelectedFile().toPath();
            try {
                panel.usar(ArchivoTablero.cargar(elegido, "Tablero"), elegido);
            } catch (IOException ex) {
                error("No se pudo abrir: " + ex.getMessage());
            }
        }
    }

    private void guardarComo() {
        JFileChooser selector = selector();
        if (selector.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            Path destino = selector.getSelectedFile().toPath();
            if (!destino.getFileName().toString().endsWith(".ser")) {
                destino = destino.resolveSibling(destino.getFileName() + ".ser");
            }
            try {
                ArchivoTablero.guardar(panel.tablero(), destino);
                panel.usar(panel.tablero(), destino);
            } catch (IOException ex) {
                error("No se pudo guardar: " + ex.getMessage());
            }
        }
    }

    @Override
    public boolean pedirTarea(String titulo, FormularioTarea formulario) {
        return JOptionPane.showConfirmDialog(this, formulario, titulo, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION;
    }

    @Override
    public boolean confirmar(String pregunta) {
        return JOptionPane.showConfirmDialog(this, pregunta, "Confirmar",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    @Override
    public void error(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "No se pudo completar", JOptionPane.WARNING_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ignorada) {
                // Se usa el look and feel de Java.
            }
            Path archivo = args.length > 0 ? Path.of(args[0]) : ARCHIVO_PREDETERMINADO;
            try {
                new VentanaTareas(ArchivoTablero.cargar(archivo, "Mis tareas"), archivo).setVisible(true);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "No se pudo abrir " + archivo + ": " + ex.getMessage(),
                        "Tablero de tareas", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
