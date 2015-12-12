package logic;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by ahmedatef on 12/12/15.
 */
public class FileInstance {
  public static final int FILE_CHUNK_SIZE = 20; // In bytes.

  private String path;
  private byte[] data;
  private long length;

  public FileInstance(String filePath, int windowSize) throws IOException {
    this.path = filePath;

    FileInputStream fis = new FileInputStream(filePath);

    length = fis.getChannel().size();
    data = new byte[(int) length];
    fis.read(data, 0, (int) length);
    System.out.println(String.format("Numbers of packets to be sent: %d / %d = %d",
            data.length, FILE_CHUNK_SIZE,
            (int) Math.ceil((double) data.length / FILE_CHUNK_SIZE)));
  }

  public boolean hasNextAfter(int chunkId) {
    return chunkId * FILE_CHUNK_SIZE < length;
  }

  public byte[] getChunk(int chunkId) {
    return Arrays.copyOfRange(data, FILE_CHUNK_SIZE * chunkId,
            Math.min(data.length, FILE_CHUNK_SIZE * (chunkId + 1)));
  }
}
