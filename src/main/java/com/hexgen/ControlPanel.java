package com.hexgen;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.util.List;
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

//    JLabel orientationLabel = new JLabel("Orientación:");
//    String[] orientations = {"Lado arriba", "Punta arriba"};
//    JComboBox<String> orientationCombo = new JComboBox<>(orientations);
//    orientationCombo.setSelectedIndex(0);
//    orientationCombo
//        .addActionListener(e -> hexPanel.setFlatTop(orientationCombo.getSelectedIndex() == 0));
    hexPanel.setFlatTop(true);//orientationCombo.getSelectedIndex() == 0);


    JLabel layersLabel = new JLabel("Número de anillos:");
    JSpinner layerSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 5, 1));
    layerSpinner.setValue(3);
    layerSpinner.addChangeListener(e -> hexPanel.setLayers((int) layerSpinner.getValue()));
    hexPanel.setLayers((int) layerSpinner.getValue());

    JLabel radiusLabel = new JLabel("Tamaño del hexágono:");
    JSpinner radiusSpinner = new JSpinner(new SpinnerNumberModel(30, 10, 100, 1));
    radiusSpinner.setValue(27);
    radiusSpinner.addChangeListener(e -> hexPanel.setRadius((int) radiusSpinner.getValue()));
    hexPanel.setRadius( (int) radiusSpinner.getValue());

    JLabel tipoLabel = new JLabel("Tipo de loseta:");
    String[] tipos = {"Pasillo", "Sala"};
    JComboBox<String> tipoCombo = new JComboBox<>(tipos);
    tipoCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, tipoCombo.getPreferredSize().height));
    tipoCombo.setSelectedIndex(0);
    tipoCombo.addActionListener(e -> hexPanel.setTileType(tipos[tipoCombo.getSelectedIndex()]));
    hexPanel.setTileType(tipos[tipoCombo.getSelectedIndex()]);

    JLabel cantidadLabel = new JLabel("Cantidad:");
    JSpinner cantidadSpinner = new JSpinner(new SpinnerNumberModel(12, 1, 200, 1));
    cantidadSpinner.setMaximumSize(
        new Dimension(Integer.MAX_VALUE, cantidadSpinner.getPreferredSize().height));
    cantidadSpinner.addChangeListener(e -> hexPanel.setTileCount((int) cantidadSpinner.getValue()));
    hexPanel.setTileCount((int) cantidadSpinner.getValue());

//    add(orientationLabel);
//    add(orientationCombo);
    add(layersLabel);
    add(layerSpinner);
    add(radiusLabel);
    add(radiusSpinner);
    add(tipoLabel);
    add(tipoCombo);
    add(cantidadLabel);
    add(cantidadSpinner);

//    orientationCombo.setMaximumSize(
//        new Dimension(Integer.MAX_VALUE, orientationCombo.getPreferredSize().height));
    layerSpinner
        .setMaximumSize(new Dimension(Integer.MAX_VALUE, layerSpinner.getPreferredSize().height));
    radiusSpinner
        .setMaximumSize(new Dimension(Integer.MAX_VALUE, radiusSpinner.getPreferredSize().height));
    tipoCombo
        .setMaximumSize(new Dimension(Integer.MAX_VALUE, radiusSpinner.getPreferredSize().height));
    cantidadSpinner
        .setMaximumSize(new Dimension(Integer.MAX_VALUE, radiusSpinner.getPreferredSize().height));

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
    List<BufferedImage> pages = hexPanel.paintFolioOnly(72);

    PrinterJob job = PrinterJob.getPrinterJob();
    job.setJobName("Imprimir losetas");
    
    Book book = new Book();

    for (BufferedImage img : pages) {
        Printable page = (graphics, pageFormat, pageIndex) -> {
            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            double scaleX = pageFormat.getImageableWidth() / img.getWidth();
            double scaleY = pageFormat.getImageableHeight() / img.getHeight();
            double scale = Math.min(scaleX, scaleY);
            g2d.scale(scale, scale);

            g2d.drawImage(img, 0, 0, null);
            return Printable.PAGE_EXISTS;
        };

        book.append(page, job.defaultPage());
    }
    job.setPageable(book);
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
