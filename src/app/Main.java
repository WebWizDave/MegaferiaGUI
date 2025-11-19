
package app;

import core.view.MegaferiaFrame; // Importa la clase de la vista
import core.controller.MegaferiaController;

public class Main {
    public static void main(String[] args) {
        // Inicializar el tema gráfico de la interfaz (FlatDarkLaf), si lo usas
        try {
            javax.swing.UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            System.err.println("Failed to initialize LaF: " + ex.getMessage());
        }
        
        // PASO 1: Instanciar el Controlador.
        // El Controlador es la única clase que debe saber sobre el Modelo (MegaferiaStorage).
        MegaferiaController controller = new MegaferiaController(); 

        // PASO 2: Iniciar la interfaz gráfica (Vista)
        java.awt.EventQueue.invokeLater(() -> {
            // Se crea la Vista, y se le pasa el Controlador. 
            // Esto se llama 'Inyección de Dependencias'.
            new MegaferiaFrame(controller).setVisible(true); 
        });
    }
}
