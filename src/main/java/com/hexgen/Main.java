package com.hexgen;

import java.awt.BorderLayout;
import javax.swing.*;

public class Main {
  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      JFrame frame = new JFrame("Generador de Losetas Hexagonales");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setSize(1200, 700);


      HexTilePanel hexPanel = new HexTilePanel();
      ControlPanel controlPanel = new ControlPanel(hexPanel);

      frame.setLayout(new BorderLayout());

      frame.add(controlPanel, BorderLayout.WEST);
      JScrollPane scrollPane = new JScrollPane(hexPanel);
      scrollPane.getVerticalScrollBar().setUnitIncrement(16); // desplazamiento suave
      frame.add(scrollPane, BorderLayout.CENTER);

      frame.setVisible(true);
    });
  }
}
