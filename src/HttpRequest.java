import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;

public class HttpRequest
{
    public String Method;
    public String Path;
    public String Version;
    public HashMap<String, String> Headers;
    public String Body;

    public HttpRequest(String request)
    {
        String firstLine = request.split("\n")[0];
        String[] firstLineSplitted = firstLine.split(" ");
        Method = firstLineSplitted[0];
        Path = firstLineSplitted[1];
        Version = firstLineSplitted[2];

        Headers = new HashMap<String, String>();

    }
}
