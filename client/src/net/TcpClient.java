package net;

import java.io.*;
import java.net.Socket;

/**
 * 负责与后端 TCP 服务器通信的底层客户端。
 * 协议：一行一条 JSON，以 '\n' 结尾。
 */
public class TcpClient {
    private final String host;
    private final int port;

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    public TcpClient(String host,int port){
        this.host=host;
        this.port=port;
    }

    /**
     * 建立与服务器的 TCP 连接。
     * @throws IOException 连接失败时抛出
     */
    public void connect() throws IOException{
        if(socket!=null && socket.isConnected() && !socket.isClosed()) return;

        socket=new Socket(host,port);
        reader=new BufferedReader(new InputStreamReader(socket.getInputStream(),"UTF-8"));
        writer=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"UTF-8"));
    }

    /**
     * 发送一行文本并同步读取一行响应。
     * @param line 不包含换行符的请求字符串（即 JSON 文本）
     * @return 响应行（不包含结尾的 '\n'）
     * @throws IOException 网络错误或对端关闭连接时抛出
     */
    public String sendAndReceive(String line) throws IOException{
        if(socket==null || socket.isClosed()){
            throw new IllegalStateException("TcpClient not connected");
        }
        if(line==null) line="";

        // 发送一行
        writer.write(line);
        writer.write('\n');
        writer.flush();

        // 等待一行响应
        String resp=reader.readLine();
        if(resp==null){
            throw new IOException("Server closed connection");
        }
        return resp;
    }

    /**
     * 关闭连接并释放资源。
     */
    public void close(){
        try{
            if(reader!=null) reader.close();
        }catch(IOException ignored){}
        try{
            if(writer!=null) writer.close();
        }catch(IOException ignored){}
        try{
            if(socket!=null) socket.close();
        }catch(IOException ignored){}
    }
}
