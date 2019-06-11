package org.dreamwork.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by game on 2017/4/19
 */
public class SchemaFileExecutor {
    private static final Logger logger = LoggerFactory.getLogger (SchemaFileExecutor.class);

    public static void importSchema (IDatabase db, Path schemaFile) throws SQLException, IOException {
        try (BufferedReader reader = Files.newBufferedReader (schemaFile, Charset.forName ("utf-8"))) {
            StringBuilder builder = new StringBuilder ();
            String line;
            try (Connection conn = db.getConnection ()) {
                while ((line = reader.readLine ()) != null) {
                    line = line.trim ();
                    if (line.length () == 0) {
                        continue;
                    }
                    builder.append (line);
                    if (line.charAt (line.length () - 1) == ';') {
                        // statement is terminated
                        String sql = builder.deleteCharAt (builder.length () - 1).toString ();

                        Statement stmt = conn.createStatement ();
                        if (logger.isTraceEnabled ())
                            logger.trace ("executing sql: " + sql);
                        stmt.execute (sql);
                        stmt.close ();
                        builder.setLength (0);
                    }
                }
            }
        }
    }
}