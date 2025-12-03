// client/src/net/TcpClient.java
package net;

import java.io.*;
import java.net.Socket;

public class TcpClient 
{
    private final String host;
    private final int port;
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    public TcpClient(String host,int port) {
        this.host=host;
        this.port=port;
    }

    public void connect() throws IOException {
        socket=new Socket(host,port);
        reader=new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));
        writer=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"));
    }

    public String sendAndReceive(String jsonLine) throws IOException {
        if(socket==null||socket.isClosed())
            throw new IllegalStateException("Not connected");

        // 发送一行 JSON + 换行
        writer.write(jsonLine);
        if(!jsonLine.endsWith("\n")) writer.write("\n");
        writer.flush();

        // 读取一行响应
        String resp=reader.readLine();
        return resp;
    }

    public void close() {
        try {
            if(reader!=null) reader.close();
        } catch(IOException ignored) {}
        try {
            if(writer!=null) writer.close();
        } catch(IOException ignored) {}
        try {
            if(socket!=null) socket.close();
        } catch(IOException ignored) {}
    }
}
