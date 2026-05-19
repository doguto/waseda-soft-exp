package src.client;

import src.client.ui.MainFrame;

import javax.swing.*;

public class GUIClient {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }
}
