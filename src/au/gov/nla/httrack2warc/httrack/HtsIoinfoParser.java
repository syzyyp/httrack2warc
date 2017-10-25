package au.gov.nla.httrack2warc.httrack;

import au.gov.nla.httrack2warc.ParsingException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class HtsIoinfoParser implements Closeable {
    private static final Pattern HEADER_RE = Pattern.compile("(request|response) for (.*):");
    private final BufferedReader reader;
    boolean request;
    String filename;
    String header;
    int code;

    private HtsIoinfoParser(BufferedReader reader) {
        this.reader = reader;
    }

    HtsIoinfoParser(InputStream ioinfoStream) {
        this(new BufferedReader(new InputStreamReader(ioinfoStream, StandardCharsets.ISO_8859_1)));
    }

    public boolean parseRecord() throws IOException {
        String header = reader.readLine();
        if (header == null) {
            return false;
        }
        Matcher matcher = HEADER_RE.matcher(header);
        if (!matcher.matches()) {
            throw new ParsingException("invalid header line: " + header);
        }
        request = matcher.group(1).equals("request");
        filename = matcher.group(2);
        code = 0;

        String prefix = request ? "<<< " : ">>> ";
        StringBuilder buffer = new StringBuilder();
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                throw new EOFException("missing trailer");
            } else if (line.isEmpty()) {
                String trailer = reader.readLine();
                if (trailer == null && !trailer.isEmpty()) {
                    throw new EOFException("expected second trailer line but got: " + trailer);
                }
                break;
            } else if (line.startsWith(prefix)) {
                buffer.append(line.substring(prefix.length()));
                buffer.append("\r\n");
            } else if (!request && line.startsWith("code=")) {
                code = Integer.parseInt(line.substring("code=".length()));
            } else {
                throw new ParsingException("invalid hts-ioinfo.txt header line: " + line);
            }
        }

        // include the trailing \r\n so we don't have to add it later
        buffer.append("\r\n");

        this.header = buffer.toString();

        return true;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}