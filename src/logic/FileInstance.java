package logic;

import java.io.*;
import java.util.Arrays;

/**
 * Created by ahmedatef on 12/12/15.
 */
public class FileInstance {
  public static final int FILE_CHUNK_SIZE = 1024; // In bytes.

  private String path;
  private byte[] data;
  private long length;

  private int chunkCount;

  public FileInstance(String filePath, int windowSize) throws IOException {
    this.path = filePath;

    FileInputStream fis = new FileInputStream(filePath);

    length = fis.getChannel().size();
    data = new byte[(int) length];
    fis.read(data, 0, (int) length);
    chunkCount = (int) Math.ceil((double) data.length / FILE_CHUNK_SIZE);
    System.out.println(String.format("Numbers of packets to be sent: %d / %d = %d",
            data.length, FILE_CHUNK_SIZE,
            chunkCount));
  }

  public boolean hasNextAfter(int chunkId) {
    return (chunkId + 1) * FILE_CHUNK_SIZE < length;
  }

  public boolean chunkExists(int chunkId) {
    return chunkId < chunkCount;
  }

  public byte[] getChunk(int chunkId) {
    return Arrays.copyOfRange(data, FILE_CHUNK_SIZE * chunkId,
            Math.min(data.length, FILE_CHUNK_SIZE * (chunkId + 1)));
  }

  public int getChunkCount() {
    return chunkCount;
  }
}
