package logic;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Created by ahmedatef on 12/10/15.
 */
public class ImageViewer {

  private ImagePane imagePane;
  private final String title;
  private final JFrame frame;

  public ImageViewer(final String title, byte[] data) throws IOException {
    this.title = title;
    frame = new JFrame(title);
    //    InputStream in = new ByteArrayInputStream(data);
    //    final BufferedImage bi = ImageIO.read(in);

    java.awt.EventQueue.invokeLater(new Runnable() {
      public void run() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(500, 500));       //Sets the dimensions of panel to appear when run
        frame.setResizable(false);

        imagePane = new ImagePane();
        frame.add(imagePane);

        frame.setVisible(true);
      }
    });
  }

  public void setImageData(byte[] newData) throws IOException {
    imagePane.l.setIcon(new ImageIcon(ImageIO.read(new ByteArrayInputStream(newData))));
//    java.awt.EventQueue.invokeLater(new Runnable() {
//      public void run() {
//        frame.setTitle(title + " " + titleInfo);
//      }
//    });

  }

  private class ImagePane extends JPanel {
    public JLabel l;

    public ImagePane() {
      setLayout(new BorderLayout());
      ImageIcon icon = null;
      try {
        icon = new ImageIcon();
      } catch (Exception e) {
        e.printStackTrace();
      }
      l = new JLabel(icon);
      add(l);
    }

  }
}
