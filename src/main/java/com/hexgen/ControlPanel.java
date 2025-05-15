package com.hexgen;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class ControlPanel extends JPanel {
  private static final long serialVersionUID = -7530321440889095447L;

  public ControlPanel(HexTilePanel hexPanel) {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    setMaximumSize(new Dimension(300, Integer.MAX_VALUE));
    setPreferredSize(new Dimension(260, 600)); // máximo 1/3 aprox
    setAlignmentY(Component.TOP_ALIGNMENT);

    JLabel orientationLabel = new JLabel("Orientación:");
    String[] orientations = {"Lado arriba", "Punta arriba"};
    JComboBox<String> orientationCombo = new JComboBox<>(orientations);
    orientationCombo.setSelectedIndex(0);
    orientationCombo.addActionListener(e -> {
      boolean flatTop = orientationCombo.getSelectedIndex() == 0;
      hexPanel.setFlatTop(flatTop);
    });

    JLabel layersLabel = new JLabel("Número de anillos:");
    JSpinner layerSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 5, 1));
    layerSpinner.addChangeListener(e -> {
      int layers = (int) layerSpinner.getValue();
      hexPanel.setLayers(layers);
    });

    JLabel radiusLabel = new JLabel("Tamaño del hexágono:");
    JSpinner radiusSpinner = new JSpinner(new SpinnerNumberModel(30, 10, 100, 1));
    radiusSpinner.addChangeListener(e -> {
      int radius = (int) radiusSpinner.getValue();
      hexPanel.setRadius(radius);
    });

    add(orientationLabel);
    add(orientationCombo);
    add(layersLabel);
    add(layerSpinner);
    add(radiusLabel);
    add(radiusSpinner);
    
    orientationCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, orientationCombo.getPreferredSize().height));
    layerSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, layerSpinner.getPreferredSize().height));
    radiusSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, radiusSpinner.getPreferredSize().height));
    
    add(Box.createVerticalGlue()); // empuja lo siguiente hacia abajo

    JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    buttonRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50)); // altura fija
    
    JButton printButton = new JButton("Imprimir");
    printButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    printButton.addActionListener(e -> {
      print(hexPanel);
    });
    
    JButton exportPdfButton = new JButton("Exportar PDF");
    exportPdfButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    exportPdfButton.addActionListener(e -> {
      JFileChooser chooser = new JFileChooser();
      chooser.setDialogTitle("Guardar como PDF");
      chooser.setSelectedFile(new File("losetas.pdf"));
      if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
          File output = chooser.getSelectedFile();
          hexPanel.exportFolioToPDF(output);
      }
    });

    buttonRow.add(printButton);
    buttonRow.add(exportPdfButton);

    add(buttonRow);
  }
  
  private void print(HexTilePanel hexPanel) {
    PrinterJob job = PrinterJob.getPrinterJob();
    job.setJobName("Imprimir losetas");

    job.setPrintable((graphics, pageFormat, pageIndex) -> {
        if (pageIndex > 0) return Printable.NO_SUCH_PAGE;

        Graphics2D g2d = (Graphics2D) graphics;
        int dpi = 72;
        int folioWidth = (int)(210 * dpi / 25.4);
        int folioHeight = (int)(297 * dpi / 25.4);
        int x = 0; // margen visual que usas en tu panel
        int y = 0;

        BufferedImage image = new BufferedImage(folioWidth, folioHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D gImage = image.createGraphics();
        hexPanel.paintFolioOnly(gImage, x, y); // un método que pinte solo el folio

        gImage.dispose();
        g2d.drawImage(image, 0, 0, null);
        return Printable.PAGE_EXISTS;
    });

    if (job.printDialog()) {
        try {
            job.print();
        } catch (PrinterException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al imprimir: " + ex.getMessage());
        }
    }
  }
  
  
}
