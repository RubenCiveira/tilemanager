package com.hexgen;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JViewport;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfWriter;

public class HexTilePanel extends JPanel {
  private static final long serialVersionUID = -3614530822472919342L;
  private int radius = 30;
  private int layers = 2;
  private boolean flatTop = true;
  private String tileType = "Pasillo";
  private int totalToPrint = 12;
  private List<BufferedImage> renderedPages = new ArrayList<>();


  public void setTileType(String type) {
    this.tileType = type;
    refresh();
  }

  public void setTileCount(int count) {
    this.totalToPrint = count;
    refresh();
  }

  public void setRadius(int radius) {
    this.radius = (int) Math.floor(1.65 * (double) radius);
    refresh();
  }

  public void setLayers(int layers) {
    this.layers = layers;
    refresh();
  }

  public void setFlatTop(boolean flatTop) {
    this.flatTop = flatTop;
    refresh();
  }

  public List<BufferedImage> paintFolioOnly(int dpi) {
    return generateImages(dpi, false);
  }

  public void exportFolioToPDF(File outputFile) {
    try {
      Document doc = new Document(PageSize.A4);
      PdfWriter.getInstance(doc, new FileOutputStream(outputFile));
      doc.open();
      List<BufferedImage> images = paintFolioOnly(72);
      for (BufferedImage buff : images) {
        Image img = Image.getInstance(buff, null);
        img.scaleToFit(PageSize.A4.getWidth(), PageSize.A4.getHeight());
        img.setAbsolutePosition(0, 0);
        doc.newPage();
        doc.add(img);
      }
      doc.close();
    } catch (IOException | DocumentException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Object[] dim = dimensions(72, true);
    int pageWidth = (int) dim[0];
    int pageHeight = (int) dim[1];
    int foliosNecesarios = (int) dim[2];
    Insets contentMargin = (Insets) dim[3];
    Graphics2D g2 = (Graphics2D) g.create();

    g2.setColor(new Color(220, 220, 220));
    g2.fillRect(0, 0, getWidth(), getHeight());

    for (int i = 0; i < renderedPages.size(); i++) {
      BufferedImage img = renderedPages.get(i);
      int pageX = contentMargin.left;

      int pageY = contentMargin.top + i * (pageHeight + contentMargin.top + contentMargin.bottom);
      g2.setColor(Color.WHITE);
      g2.fillRect(pageX, pageY, pageWidth, pageHeight);

      g2.setColor(Color.BLACK);
      g2.drawRect(pageX, pageY, pageWidth, pageHeight);

      g2.drawImage(img, pageX, pageY, this);
    }
    g2.dispose();

    int altura = contentMargin.top + contentMargin.bottom
        + foliosNecesarios * (pageHeight + contentMargin.bottom);
    setPreferredSize(new Dimension(pageWidth + contentMargin.left + contentMargin.right, altura));
  }


  private int paintLosetas(int margin, Graphics2D g2, int x0, int y0, int folioWidth,
      int folioHeight, int startIndex) {
    int spacing = 0;

    int usableWidth = folioWidth - 2 * margin;
    int usableHeight = folioHeight - 2 * margin;

    int losetaWidth = getLosetaAncho();
    int losetaHeight = getLosetaAlto();

    int cols = Math.max(1, (usableWidth + spacing) / (losetaWidth + spacing));
    int rows = Math.max(1, (usableHeight + spacing) / (losetaHeight + spacing));

    // calcular el espacio real ocupado
    int totalWidth = cols * losetaWidth + (cols - 1) * spacing;
    int totalHeight = rows * losetaHeight + (rows - 1) * spacing;

    // calcular el offset para centrar dentro del folio
    int offsetX = x0 + (folioWidth - totalWidth) / 2;
    int offsetY = y0 + (folioHeight - totalHeight) / 2;

    int printed = startIndex;

    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < cols; col++) {
        // We fill the last page, so we dont return on printed >= totalToPrint

        int centerX = offsetX + col * (losetaWidth + spacing) + losetaWidth / 2;
        int centerY = offsetY + row * (losetaHeight + spacing) + losetaHeight / 2;
        drawLoseta(g2, centerX, centerY);
        printed++;
      }
    }
    return printed;
  }

  private int getLosetaAncho() {
    int sz = layers; // o layers, si aún usas ese nombre
    return flatTop ? (int) (radius * 1.2 * (2 * sz - 1))
        : (int) (radius * Math.sqrt(3) * (layers + 0.5));
  }

  private int getLosetaAlto() {
    int sz = layers;
    return flatTop ? (int) (radius * Math.sqrt(3) * (layers + 0.5))
        : (int) (radius * 1.2 * (2 * sz - 1));
  }

  private Object[] dimensions(int dpi, boolean withMargin) {
    int pageWidth = (int) (210 * dpi / 25.4); // A4 = 210mm
    int pageHeight = (int) (297 * dpi / 25.4); // A4 = 297mm

    int availableWidth = pageWidth;
    int availableHeight = pageHeight;

    Container parent = getParent();
    if (parent instanceof JViewport viewport) {
      Dimension viewSize = viewport.getExtentSize();
      availableWidth = Math.max(pageWidth, viewSize.width);
      availableHeight = Math.max(pageHeight, viewSize.height);
      // Usar para centrar el folio
    }

    int margin = withMargin ? 0 : 0;

    int marginX = Math.max((availableWidth - pageWidth) / 2, margin);
    int marginY = Math.max((availableHeight - pageHeight) / 2, margin);

    Insets contentMargin = new Insets(marginY, marginX, marginY, marginX);

    int losetaWidth = getLosetaAncho();
    int losetaHeight = getLosetaAlto();
    int spacing = 0;

    int usableWidth = pageWidth - 2 * margin;
    int usableHeight = pageHeight - 2 * margin;

    int cols = Math.max(1, (usableWidth + spacing) / (losetaWidth + spacing));
    int rows = Math.max(1, (usableHeight + spacing) / (losetaHeight + spacing));
    
    System.out.println("Calculando w a " + usableWidth + " entre " + losetaWidth + " = " + ( usableWidth / losetaWidth )  );
    System.out.println("Calculando h a " + usableHeight + " entre " + losetaHeight + " = " + ( usableHeight / losetaHeight )  );
    
    int losetasPorFolio = cols * rows;
    int foliosNecesarios = (int) Math.ceil((double) totalToPrint / losetasPorFolio);

    return new Object[] {pageWidth, pageHeight, foliosNecesarios, contentMargin};
  }


  private void drawLoseta(Graphics2D g2, int cx, int cy) {
    setBackground(Color.WHITE);

    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(Color.GRAY);
    g2.setStroke(new BasicStroke(1));

    // Creamos el contorno del hexágono exterior como Shape
    clipOuterBoundary(g2, cx, cy, radius, layers);
    for (int q = -layers; q <= layers; q++) {
      int r1 = Math.max(-layers, -q - layers);
      int r2 = Math.min(layers, -q + layers);
      for (int r = r1; r <= r2; r++) {
        Point p = HexUtils.hexToPixel(q, r, radius, flatTop, true);
        drawHex(g2, cx + p.x, cy + p.y, radius);
      }
    }
    drawOuterBoundary(g2, cx, cy, radius, layers);
    PasilloRenderer.drawPasilloDecoracion(g2, cx, cy, radius, layers, flatTop);
  }

  private Path2D generateHexPath(boolean inner, double cx, double cy, double radius,
      boolean flatTop) {
    if (inner && layers % 2 != 0) {
      cx -= radius;
    } else if (inner) {
      cx += radius;
    }

    Path2D hex = new Path2D.Double();
    for (int i = 0; i < 6; i++) {
      double angle = Math.toRadians(flatTop ? (60 * i) : (60 * i - 30));
      double dx = cx + radius * Math.cos(angle);
      double dy = cy + radius * Math.sin(angle);
      if (i == 0)
        hex.moveTo(dx, dy);
      else
        hex.lineTo(dx, dy);
    }
    hex.closePath();
    return hex;
  }

  private void drawOuterBoundary(Graphics2D g2, int cx, int cy, int radius, int layers) {
    g2.draw(generateHexPath(false, cx, cy, radius * layers, flatTop));
  }

  private void clipOuterBoundary(Graphics2D g2, int cx, int cy, int radius, int layers) {
    g2.setClip(generateHexPath(false, cx, cy, radius * layers, flatTop));
  }

  private void drawHex(Graphics2D g2, int x, int y, int radius) {
    g2.draw(generateHexPath(true, x, y, radius, flatTop));
  }

  private void refresh() {
    renderedPages.clear();
    renderedPages.addAll(this.generateImages(72, true)); // tu método actual de renderizado
    int totalHeight = renderedPages.size() * renderedPages.get(0).getHeight();
    setPreferredSize(new Dimension(renderedPages.get(0).getWidth(), totalHeight));
    revalidate();
    repaint();
  }
  
  private List<BufferedImage> generateImages(int dpi, boolean withMargin) {
    List<BufferedImage> images = new ArrayList<>();
    Object[] dim = dimensions(dpi, withMargin);
    int pageWidth = (int) dim[0];
    int pageHeight = (int) dim[1];
    int foliosNecesarios = (int) dim[2];
    int printed = 0;
    for (int i = 0; i < foliosNecesarios && printed < totalToPrint; i++) {
      BufferedImage image = new BufferedImage(pageWidth, pageHeight, BufferedImage.TYPE_INT_RGB);
      Graphics2D ig2 = image.createGraphics();
      ig2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      ig2.setColor(Color.WHITE);
      ig2.fillRect(0, 0, pageWidth, pageHeight);
      printed = paintLosetas(0, ig2, 0, 0, pageWidth, pageHeight, printed);
      ig2.dispose();
      images.add(image);
    }
    return images;
  }


}
