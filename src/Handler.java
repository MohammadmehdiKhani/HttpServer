import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

public class Handler extends Thread
{
    Socket HandlerSocket;
    String HomeDirectory = "./Store";

    public Handler(Socket handlerSocket)
    {
        HandlerSocket = handlerSocket;
    }

    @Override
    public void run()
    {
        try
        {
            String httpRequestInString = readSocketAsChars(HandlerSocket);

            if (httpRequestInString.equals(""))
            {
                HandlerSocket.close();
                return;
            }

            HttpRequest httpRequest = new HttpRequest(httpRequestInString);

            if (httpRequest.Method.equals("GET"))
                handleGET(httpRequest);


            HandlerSocket.shutdownInput();
            HandlerSocket.close();
        } catch (Exception e)
        {
            System.out.println("close handler sockets - exception occurred");
        }
    }

    private void handleGET(HttpRequest httpRequest) throws IOException
    {
        String fileName = (httpRequest.Path.substring(1)).toLowerCase();
        if (fileName.contains("."))
            fileName = fileName.split("\\.")[0];

        File homeDirectory = new File(HomeDirectory);
        String[] allFiles = homeDirectory.list();
        ArrayList<String> allFilesAF = new ArrayList<String>();

        for (int i = 0; i < allFiles.length; i++)
        {
            allFilesAF.add(allFiles[i].split("\\.")[0].toLowerCase());
        }

        boolean isFileExists = allFilesAF.contains(fileName);
        int index = allFilesAF.indexOf(fileName);
        String fullFileName;
        byte[] resHeaders;
        byte[] resBody;

        if (isFileExists)
        {
            fullFileName = allFiles[index];
            resBody = readFileAsByte(fullFileName);
            resHeaders = createHeaders(200, "OK", resBody.length);
        } else
        {
            fullFileName = "NotFounded.html";
            resBody = readFileAsByte(fullFileName);
            resHeaders = createHeaders(404, "NOT FOUND", resBody.length);
        }

        writeSocketAsByte(resHeaders, HandlerSocket);
        writeSocketAsByte(resBody, HandlerSocket);
    }

    private byte[] createHeaders(int statusCode, String statusString, int contentLength)
    {
        String response = "";
        response += "HTTP/1.1 " + statusCode + " " + statusString + "\n";
        response += "Date: " + getServerTime() + "\n";
        response += "Content-Length: " + contentLength + "\n";
        response += "Content-Type: */*" + "\n";
        response += "Connection: Closed\n";
        response += "\r\n";

        return response.getBytes();
    }

    private String readSocketAsBytes(Socket socket) throws IOException
    {
        InputStream inputStream = socket.getInputStream();
        byte[] bytes = inputStream.readNBytes(inputStream.available());
        ByteBuffer bytesBuffer = ByteBuffer.wrap(bytes);
        CharBuffer charBuffer = StandardCharsets.UTF_8.decode(bytesBuffer);
        return charBuffer.toString();
    }

    private String readSocketAsChars(Socket socket) throws IOException
    {
        InputStream inputStream = socket.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
        char[] chars = new char[inputStream.available()];
        inputStreamReader.read(chars, 0, chars.length);
        return new String(chars);
    }

    private void writeSocketAsByte(byte[] content, Socket socket) throws IOException
    {
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(content);
    }

    private String readFileAsString(String fileName) throws IOException
    {
        String content = "";
        Path path = Paths.get(HomeDirectory + "/" + fileName + ".html");
        List<String> lines = Files.readAllLines(path);

        for (int i = 0; i < lines.size(); i++)
        {
            content += lines.get(i);
        }
        return content;
    }

    private byte[] readFileAsByte(String fileName) throws IOException
    {
        InputStream inputStream = new FileInputStream("./Store/" + fileName);
        byte[] bytes = inputStream.readAllBytes();
        return bytes;
    }

    private byte[] readFileAsBase64(String fileName) throws IOException
    {
        InputStream inputStream = new FileInputStream("./Store/" + fileName);
        byte[] bytes = Base64.getEncoder().encode(inputStream.readAllBytes());
        return bytes;
    }

    private String getServerTime()
    {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }
}