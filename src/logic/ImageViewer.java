package logic;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by ahmedatef on 12/10/15.
 */
public class ImageViewer {

  private ImagePane imagePane;

  public ImageViewer(String title, String stringPath) throws IOException {
    Path path = Paths.get("/home/ahmedatef/img.jpg");
    new ImageViewer(title, Files.readAllBytes(path));
  }

  public ImageViewer(final String title, byte[] data) throws IOException {
    InputStream in = new ByteArrayInputStream(data);
    final BufferedImage bi = ImageIO.read(in);

    java.awt.EventQueue.invokeLater(new Runnable() {
      public void run() {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(500, 500));       //Sets the dimensions of panel to appear when run
        frame.setResizable(false);

        imagePane = new ImagePane(bi);
        frame.add(imagePane);

        frame.setVisible(true);
      }
    });
  }

  public void setImageData(byte[] newData) throws IOException {
    imagePane.l.setIcon(new ImageIcon(ImageIO.read(new ByteArrayInputStream(newData))));
  }

  private class ImagePane extends JPanel {
    public JLabel l;

    public ImagePane(BufferedImage bufferedImage) {
      setLayout(new BorderLayout());
      ImageIcon icon = null;
      try {
        icon = new ImageIcon(bufferedImage);
      } catch (Exception e) {
        e.printStackTrace();
      }
      l = new JLabel(icon);
      add(l);
    }

  }
}
