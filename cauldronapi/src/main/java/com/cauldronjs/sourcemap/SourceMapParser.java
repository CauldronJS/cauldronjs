package com.cauldronjs.sourcemap;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import java.io.*;

public class SourceMapParser {
    private final Context context;

    public SourceMapParser(Context context) {
        this.context = context;
    }

    public SourceMap parsesourcemap(InputStream stream) throws IOException {
        var streamReader = new InputStreamReader(stream);
        var bufferedReader = new BufferedReader(streamReader);
        String line;
        var result = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null) {
            result.append(line);
        }
        var json = this.context.eval("js", "JSON.parse").execute(result.toString());
        return parseSourceMap(json);
    }

    public SourceMap parseSourceMap(Value value) {
        var version = value.getMember("version").asInt();
        var sources = value.getMember("sources").as(String[].class);
        var names = value.getMember("names").as(String[].class);
        var mappings = value.getMember("mappings").asString();
        var sourcesContent = value.getMember("sourcesContent").as(String[].class);
        var file = value.getMember("file").asString();
        var entries = MappingListParser.parseMappings(mappings, names, sources);
        return new SourceMap(version, sources, names, mappings, sourcesContent, file, entries);
    }
}
