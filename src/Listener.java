import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Listener
{
    ServerSocket ListenerSocket;

    public Listener() throws IOException
    {
        ListenerSocket = new ServerSocket(80);
    }

    public void listen() throws IOException
    {
        while (!ListenerSocket.isClosed())
        {
            Socket handlerSocket = ListenerSocket.accept();
            Handler handler = new Handler(handlerSocket);
            handler.start();
        }

        ListenerSocket.close();
    }
}