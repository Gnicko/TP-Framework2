package gomez.frameworks;

import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public class Consola {
    private Properties properties = new Properties();
    private ArrayList<Actions> actions;
    private String clases;

    public Consola() {
        actions = new ArrayList<>();
        ejecutar();
    }

    private void ejecutar() {

        try (InputStream archivo = getClass().getResourceAsStream("/config.properties")) {
            properties.load(archivo);
            clases = properties.getProperty("clase");
            String[] cadena = clases.split(",");
            for (String s : cadena) {
                Class c = Class.forName(s);
                actions.add((Actions) c.getDeclaredConstructor().newInstance());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al cargar las clases", e);
        }
        cargarConsola();
    }

    private void cargarConsola() {
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        Screen screen = null;
        try {

            screen = terminalFactory.createScreen();
            screen.startScreen();
            BasicWindow window = new BasicWindow();
            window.setHints(Arrays.asList(Window.Hint.CENTERED));

            final WindowBasedTextGUI textGUI = new MultiWindowTextGUI(screen);


            Panel contentPanel = new Panel(new GridLayout(2));


            GridLayout gridLayout = (GridLayout) contentPanel.getLayoutManager();
            gridLayout.setHorizontalSpacing(3);

            Label title = new Label("Seleccione una opcion");
            title.setLayoutData(GridLayout.createLayoutData(
                    GridLayout.Alignment.BEGINNING, // Horizontal alignment in the grid cell if the cell is larger than the component's preferred size
                    GridLayout.Alignment.BEGINNING, // Vertical alignment in the grid cell if the cell is larger than the component's preferred size
                    true,       // Give the component extra horizontal space if available
                    false,        // Give the component extra vertical space if available
                    2,                  // Horizontal span
                    1));                  // Vertical span
            contentPanel.addComponent(title);


            ArrayList<String> cadena = new ArrayList<>();
            for (Actions a : actions) {
                cadena.add(a.nombreItemMenu() + " (" + a.descripcionItemMenu() + ")");
            }
            ComboBox<String> c = new ComboBox<String>(cadena);

            contentPanel.addComponent(
                    c.setReadOnly(false)
                            .setLayoutData(GridLayout.createHorizontallyFilledLayoutData(1)));

            contentPanel.addComponent(new Button("Button", () -> {
                MessageDialog.showMessageDialog(textGUI, "Aviso", "Se ejecutara la Opcion seleccionada.", MessageDialogButton.Continue);
                actions.get(c.getSelectedIndex()).ejecutar();
            })
                    .setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.CENTER, GridLayout.Alignment.CENTER)));

            contentPanel.addComponent(
                    new EmptySpace()
                            .setLayoutData(
                                    GridLayout.createHorizontallyFilledLayoutData(2)));
            contentPanel.addComponent(
                    new Separator(Direction.HORIZONTAL)
                            .setLayoutData(
                                    GridLayout.createHorizontallyFilledLayoutData(2)));
            contentPanel.addComponent(
                    new Button("Close", window::close).setLayoutData(
                            GridLayout.createHorizontallyEndAlignedLayoutData(2)));


            window.setComponent(contentPanel);

            textGUI.addWindowAndWait(window);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (screen != null) {
                try {
                    screen.stopScreen();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
