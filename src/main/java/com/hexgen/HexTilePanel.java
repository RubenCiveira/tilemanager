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
import java.awt.Shape;
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
    this.radius = (int) Math.floor( 1.65 * (double)radius );
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
    List<BufferedImage> images = new ArrayList<>();
    Object[] dim = dimensions(dpi, false);
    int pageWidth = (int) dim[0];
    int pageHeight = (int) dim[1];
    int foliosNecesarios = (int)dim[2];
    int printed = 0;
    for (int i = 0; i < foliosNecesarios && printed < totalToPrint; i++) {
      BufferedImage image = new BufferedImage(pageWidth, pageHeight, BufferedImage.TYPE_INT_RGB);
      Graphics2D ig2 = image.createGraphics();
      ig2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      ig2.setColor(Color.WHITE);
      ig2.fillRect(0, 0, pageWidth, pageHeight);
      printed = paintLosetas(20, ig2, 0, 0, pageWidth, pageHeight, printed);
      ig2.dispose();
      images.add( image );
    }
    return images;
  }


  public void exportFolioToPDF(File outputFile) {
    try {
      Document doc = new Document(PageSize.A4);
      PdfWriter.getInstance(doc, new FileOutputStream(outputFile));
      doc.open();
      List<BufferedImage> images = paintFolioOnly(72);
      for(BufferedImage buff: images) {
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
    int foliosNecesarios = (int)dim[2];
    Insets contentMargin = (Insets) dim[3];
    Graphics2D g2 = (Graphics2D) g.create();

    g2.setColor(new Color(220, 220, 220));
    g2.fillRect(0, 0, getWidth(), getHeight());

    for (int i = 0; i < foliosNecesarios; i++) {
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
    
    int altura = contentMargin.top + contentMargin.bottom + foliosNecesarios * (pageHeight + contentMargin.bottom);
    setPreferredSize(new Dimension(pageWidth + contentMargin.left + contentMargin.right, altura));
  }
  
  
  private int paintLosetas(int margin, Graphics2D g2, int x0, int y0, int folioWidth,
      int folioHeight, int startIndex) {
    int spacing = 10;

    int usableWidth = folioWidth - 2 * margin;
    int usableHeight = folioHeight - 2 * margin;

    int losetaWidth = getLosetaAncho();
    int losetaHeight = getLosetaAlto();

    int cols = Math.max(1, (usableWidth + spacing) / (losetaWidth + spacing));
    int rows = Math.max(1, (usableHeight + spacing)/ (losetaHeight + spacing));

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
    return flatTop ? (int) (radius * 3.0 * layers) + radius
        : (int) (radius * Math.sqrt(3) * (2 * layers + 1));
  }

  private int getLosetaAlto() {
    return flatTop ? (int) (radius * Math.sqrt(3) * (2 * layers + 1))
        : (int) (radius * 3.0 * layers) + radius;
  }


  private Object[] dimensions(boolean withMargin) {
    return dimensions(72, withMargin);
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

    int margin = withMargin ? 20 : 0;

    int marginX = Math.max((availableWidth - pageWidth) / 2, margin);
    int marginY = Math.max((availableHeight - pageHeight) / 2, margin);

    Insets contentMargin = new Insets(marginY, marginX, marginY, marginX);

    int losetaWidth = getLosetaAncho();
    int losetaHeight = getLosetaAlto();
    int spacing = 10;

    int usableWidth = pageWidth - 2 * margin;
    int usableHeight = pageHeight - 2 * margin;

    int cols = Math.max(1, (usableWidth + spacing) / (losetaWidth + spacing));
    int rows = Math.max(1, (usableHeight + spacing) / (losetaHeight + spacing));
    
    if( losetaWidth == 0 ) {
      cols = 2;
    }
    if( losetaHeight == 0 ) {
      rows = 2;
    }
    
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
    Shape clipHex = buildOuterBoundaryShape(cx, cy, radius, layers);
    g2.setClip(clipHex); // cualquier dibujo fuera se recorta automáticamente

    for (int q = -layers; q <= layers; q++) {
      int r1 = Math.max(-layers, -q - layers);
      int r2 = Math.min(layers, -q + layers);
      for (int r = r1; r <= r2; r++) {
        Point p = HexUtils.hexToPixel(q, r, radius, flatTop);
        drawHex(g2, cx + p.x, cy + p.y, radius);
      }
    }
    drawOuterBoundary(g2, cx, cy, radius, layers);
    PasilloRenderer.drawPasilloDecoracion(g2, cx, cy, radius, layers, flatTop);
  }

  private void drawOuterBoundary(Graphics2D g2, int cx, int cy, int radius, int layers) {
    int[][] directions = flatTop ? new int[][] {{1, 0}, {1, -1}, {0, -1}, {-1, 0}, {-1, 1}, {0, 1}}
        : new int[][] {{1, 0}, {0, -1}, {-1, -1}, {-1, 0}, {0, 1}, {1, 1}};

    Path2D hex = new Path2D.Double();

    for (int i = 0; i < 6; i++) {
      int dq = directions[i][0];
      int dr = directions[i][1];
      int q = dq * layers;
      int r = dr * layers;
      Point p = HexUtils.hexToPixel(q, r, radius, flatTop);
      double x = cx + p.x;
      double y = cy + p.y;
      if (i == 0)
        hex.moveTo(x, y);
      else
        hex.lineTo(x, y);
    }

    hex.closePath();
    g2.setColor(Color.BLACK);
    g2.setStroke(new BasicStroke(2));
    g2.draw(hex);
  }

  private Shape buildOuterBoundaryShape(int cx, int cy, int radius, int layers) {
    int[][] directions = flatTop ? new int[][] {{1, 0}, {1, -1}, {0, -1}, {-1, 0}, {-1, 1}, {0, 1}}
        : new int[][] {{1, 0}, {0, -1}, {-1, -1}, {-1, 0}, {0, 1}, {1, 1}};

    Path2D hex = new Path2D.Double();

    for (int i = 0; i < 6; i++) {
      int q = directions[i][0] * layers;
      int r = directions[i][1] * layers;
      Point p = HexUtils.hexToPixel(q, r, radius, flatTop);
      double x = cx + p.x;
      double y = cy + p.y;
      if (i == 0)
        hex.moveTo(x, y);
      else
        hex.lineTo(x, y);
    }

    hex.closePath();
    return hex;
  }


  private void drawHex(Graphics2D g2, int x, int y, int radius) {
    Path2D hex = new Path2D.Double();
    for (int i = 0; i < 6; i++) {
      double angle = Math.toRadians(flatTop ? (60 * i) : (60 * i - 30));
      int dx = (int) (x + radius * Math.cos(angle));
      int dy = (int) (y + radius * Math.sin(angle));
      if (i == 0)
        hex.moveTo(dx, dy);
      else
        hex.lineTo(dx, dy);
    }
    hex.closePath();
    g2.draw(hex);
  }
  
  private void refresh() {
    renderedPages.clear();
    renderedPages.addAll(this.paintFolioOnly(72)); // tu método actual de renderizado
    int totalHeight = renderedPages.size() * renderedPages.get(0).getHeight();
    setPreferredSize(new Dimension(renderedPages.get(0).getWidth(), totalHeight));
    revalidate();
    repaint();
//    
//    resize();
//    revalidate();
//    repaint();
//    Container parent = this.getParent();
//    if( null != parent ) {
//      parent.repaint(); // fuerza redibujo en scroll
//    }
  }
  
  private void resize() {
    Object[] dim = dimensions(true);
    int pageWidth = (int) dim[0];
    int pageHeight = (int) dim[1];
    int foliosNecesarios = (int)dim[2];
    Insets contentMargin = (Insets) dim[3];
    int totalHeight = foliosNecesarios * (pageHeight + contentMargin.top + contentMargin.bottom);
    setPreferredSize(
        new Dimension(pageWidth + contentMargin.left + contentMargin.right, totalHeight));
  }
}
