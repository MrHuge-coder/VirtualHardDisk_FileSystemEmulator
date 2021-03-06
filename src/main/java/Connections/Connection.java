/*
 * Copyright (c) 2022. Arkady Dymkov townhospis<townhospis@gmail.com>
 */

package Connections;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Connection implements Serializable {

    // Buffer for files
    private transient ByteBuffer buffer;

    // connector to "disk" file
    private transient final FileChannel channel;

    // Gets us access to the file
    private transient final RandomAccessFile fileAccess;

    /**
     * Constructs a new instance of the Connections.Connection
     *
     * @param accessFile Access file to connect to
     */
    private Connection(RandomAccessFile accessFile) {
        this.fileAccess = accessFile;
        this.channel = this.fileAccess.getChannel();
        this.buffer = ByteBuffer.allocate(0);
    }


    /**
     * Create a new connection to the file by file Object
     *
     * @param file File to connect to
     * @return new connection to the file
     * @throws IOException if an error occurs while creating the access
     */
    public static Connection connectByFile(File file) throws IOException {
        RandomAccessFile accessFile = new RandomAccessFile(file, "rw");
        return new Connection(accessFile);
    }

    /**
     * Create a new Connections.Connection by path (fabric method)
     *
     * @param path Path to file
     * @return new Connections.Connection instance
     * @throws IOException if an I/O error occurs during creating the connection to file
     */
    public static Connection connectByPath(String path) throws IOException {
        RandomAccessFile accessFile = new RandomAccessFile(path, "rw");
        return new Connection(accessFile);
    }

    /**
     * Removes all information from the buffer
     *
     * @return last data in the buffer
     */
    private byte[] clearBuffer() {
        byte[] result = buffer.array();
        buffer = ByteBuffer.allocate(0);
        return result;
    }

    /**
     * Removes the specified number of bytes from the file
     *
     * @param startByte startByte to remove from
     * @param length    length to remove
     * @throws IOException if an I/O error occurs during the removal process
     */
    public void remove(long startByte, int length) throws IOException {
        long endStart = startByte + length;
        int lengthTillTheEnd = (int) (this.getSize() - startByte - length);
        if (lengthTillTheEnd != 0) {
            byte[] ending = this.read(endStart, lengthTillTheEnd);
            this.overwriteFrom(startByte, ending);
        }
        this.channel.truncate(this.getSize() - length);
    }

    /**
     * Reads the specified number of bytes from the file
     *
     * @param startByte start byte to read
     * @param length    count of bytes to read
     * @return Bytes reads
     * @throws IOException if an I/O error occurs during read operation
     */
    public byte[] read(long startByte, int length) throws IOException {
        buffer = ByteBuffer.allocate(length);
        channel.read(buffer, startByte);
        return clearBuffer();
    }

    /**
     * Writes the specified bytes from the specified position
     * This method overrides the previous bytes in the file.
     *
     * @param startByte start byte to write
     * @param data      data to write
     * @return Start byte
     * @throws IOException if an I/O error occurs during writing
     */
    public long overwriteFrom(long startByte, byte[] data) throws IOException {
        buffer = ByteBuffer.wrap(data);
        channel.write(buffer, startByte);
        return startByte;
    }

    /**
     * Writes the specified bytes to the end of the file
     *
     * @param data data to write
     * @return Start byte
     * @throws IOException if an I/O error occurs during writing
     */
    public long writeToTheEnd(byte[] data) throws IOException {
        return overwriteFrom(channel.size(), data);
    }

    public long getSize() throws IOException {
        return channel.size();
    }

    /**
     * Close the channel
     *
     * @throws IOException if an I/O error occurs during closing
     */
    public void close() throws IOException {
        //this.channel.force(true);
        this.channel.close();
        this.fileAccess.close();
    }
}
