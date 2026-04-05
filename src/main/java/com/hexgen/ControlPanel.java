package com.hexgen;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.print.Book;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
    boolean advanced = true;
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    setMaximumSize(new Dimension(300, Integer.MAX_VALUE));
    setPreferredSize(new Dimension(260, 600)); // máximo 1/3 aprox
    setAlignmentY(Component.TOP_ALIGNMENT);

    JLabel orientationLabel = new JLabel("Orientación:");
    String[] orientations = {"Lado arriba", "Punta arriba"};
    JComboBox<String> orientationCombo = new JComboBox<>(orientations);
    orientationCombo.setSelectedIndex(0);
    orientationCombo
        .addActionListener(e -> hexPanel.setFlatTop(orientationCombo.getSelectedIndex() == 0));
    hexPanel.setFlatTop(orientationCombo.getSelectedIndex() == 0);

    JLabel layersLabel = new JLabel("Número de anillos:");
    JSpinner layerSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 5, 2));
    // Solo valores impares (1, 3, 5): con valores pares el borde exterior pasa
    // por los centros de las celdas del perímetro, solapando las celdas de losetas adyacentes.
    layerSpinner.addChangeListener(e -> hexPanel.setLayers((int) layerSpinner.getValue()));
    hexPanel.setLayers((int) layerSpinner.getValue());

    JLabel radiusLabel = new JLabel("Tamaño del hexágono:");
    JSpinner radiusSpinner = new JSpinner(new SpinnerNumberModel(30, 10, 100, 1));
    radiusSpinner.setValue(27);
    radiusSpinner.addChangeListener(e -> hexPanel.setRadius((int) radiusSpinner.getValue()));
    hexPanel.setRadius( (int) radiusSpinner.getValue());

    JLabel tipoLabel = new JLabel("Tipo de loseta:");
    String[] tipos = {"Pasillos angostos", "Pasillos", "Salas pequeñas", "Salas grandes", "Salas muy grandes"};
    JComboBox<String> tipoCombo = new JComboBox<>(tipos);
    tipoCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, tipoCombo.getPreferredSize().height));
    tipoCombo.setSelectedIndex(1);
    tipoCombo.addActionListener(e -> hexPanel.setTileType(tipos[tipoCombo.getSelectedIndex()]));
    hexPanel.setTileType(tipos[tipoCombo.getSelectedIndex()]);

    JLabel cantidadLabel = new JLabel("Cantidad:");
    JSpinner cantidadSpinner = new JSpinner(new SpinnerNumberModel(6, 1, 200, 1));
    cantidadSpinner.setMaximumSize(
        new Dimension(Integer.MAX_VALUE, cantidadSpinner.getPreferredSize().height));
    cantidadSpinner.addChangeListener(e -> hexPanel.setTileCount((int) cantidadSpinner.getValue()));
    hexPanel.setTileCount((int) cantidadSpinner.getValue());

    JCheckBox esquematicoCheck = new JCheckBox("Mostrar esquemático", true);
    esquematicoCheck.setAlignmentX(Component.LEFT_ALIGNMENT);

    JPanel leyendaPanel = buildLeyendaPanel();
    leyendaPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    esquematicoCheck.addActionListener(e -> {
      hexPanel.setEsquematico(esquematicoCheck.isSelected());
      leyendaPanel.setVisible(esquematicoCheck.isSelected());
    });
    hexPanel.setEsquematico(true);

    // El calculo de intersecciones es muy delicado, no dejemos configurar esto.
    if( advanced ) {
      add(orientationLabel);
      add(orientationCombo);
      add(layersLabel);
      add(layerSpinner);
      add(radiusLabel);
      add(radiusSpinner);
    }
    add(tipoLabel);
    add(tipoCombo);
    add(cantidadLabel);
    add(cantidadSpinner);
    add(esquematicoCheck);
    add(leyendaPanel);

    orientationCombo.setMaximumSize(
        new Dimension(Integer.MAX_VALUE, orientationCombo.getPreferredSize().height));
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

  private JPanel buildLeyendaPanel() {
    record Entrada(Color color, String etiqueta) {}
    List<Entrada> entradas = List.of(
        new Entrada(Color.GREEN,                "Entrada"),
        new Entrada(Color.BLUE,                 "Paso abierto"),
        new Entrada(Color.RED,                  "Puerta"),
        new Entrada(Color.GRAY,                 "Muro"),
        new Entrada(new Color(0, 200, 0, 80),   "Zona de entrada"),
        new Entrada(new Color(0, 0, 200, 80),   "Zona de paso"),
        new Entrada(new Color(200, 0, 0, 80),   "Zona con puerta"),
        new Entrada(new Color(10, 10, 10, 100), "Zona de muro")
    );

    int swatch = 12, rowH = 18, padding = 6, separatorRow = 4;
    int panelH = padding * 2 + 14 + entradas.size() * rowH + 6 + 4; // +4 separator gap

    JPanel panel = new JPanel() {
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        g2.setColor(new Color(245, 245, 245));
        g2.fillRoundRect(0, 0, w - 1, getHeight() - 1, 8, 8);
        g2.setColor(new Color(180, 180, 180));
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(0, 0, w - 1, getHeight() - 1, 8, 8);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 9f));
        g2.setColor(Color.DARK_GRAY);
        g2.drawString("Leyenda (esquemático)", padding, padding + 9);
        g2.drawLine(padding, padding + 13, w - padding, padding + 13);

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 8.5f));
        FontMetrics fm = g2.getFontMetrics();

        int y0 = padding + 20;
        for (int i = 0; i < entradas.size(); i++) {
          if (i == separatorRow) {
            g2.setColor(new Color(200, 200, 200));
            g2.drawLine(padding, y0 + i * rowH - 3, w - padding, y0 + i * rowH - 3);
          }
          Entrada e = entradas.get(i);
          int ey = y0 + i * rowH;
          g2.setColor(Color.WHITE);
          g2.fillRect(padding, ey, swatch, swatch);
          g2.setColor(e.color());
          g2.fillRect(padding, ey, swatch, swatch);
          g2.setColor(Color.DARK_GRAY);
          g2.drawRect(padding, ey, swatch, swatch);
          g2.drawString(e.etiqueta(), padding + swatch + 5, ey + swatch / 2 + fm.getAscent() / 2 - 1);
        }
        g2.dispose();
      }
    };
    panel.setOpaque(false);
    panel.setPreferredSize(new Dimension(240, panelH));
    panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panelH));
    return panel;
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
