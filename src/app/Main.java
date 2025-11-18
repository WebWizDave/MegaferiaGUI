/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package app;

import core.view.MegaferiaFrame; // Importa la clase de la vista
/**
 *
 * @author david
 */
public class Main {
    public static void main(String[] args) {
        // inicializar el tema gráfico de la interfaz (FlatDarkLaf)
        try {
            javax.swing.UIManager.setLookAndFeel(new com.formdev.flatlaf.FlatDarkLaf());
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            System.err.println("Failed to initialize LaF");
        }
        
        // Aquí es donde arranca la aplicación (La Vista)
        java.awt.EventQueue.invokeLater(() -> {
            new MegaferiaFrame().setVisible(true);
        });
    }
}
