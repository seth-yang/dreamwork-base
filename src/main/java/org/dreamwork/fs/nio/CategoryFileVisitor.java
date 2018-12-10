package org.dreamwork.fs.nio;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by game on 2017/4/18
 */
public class CategoryFileVisitor<T extends FileIndex> extends SimpleFileVisitor<Path> {
    private String category;
    private IFileIndexAdapter<T> adapter;

    private static final Logger logger = Logger.getLogger (CategoryFileVisitor.class.getName ());

    public CategoryFileVisitor (String category, IFileIndexAdapter<T> adapter) {
        this.category = category;
        this.adapter = adapter;
    }

    @Override
    public FileVisitResult visitFile (Path file, BasicFileAttributes attrs) throws IOException {
        if (adapter != null && adapter.accept (file, attrs)) {
            adapter.save (category, file);
        }
        return super.visitFile (file, attrs);
    }

    @Override
    public FileVisitResult visitFileFailed (Path file, IOException exc) throws IOException {
        logger.log (Level.WARNING, exc.getMessage (), exc);
        return super.visitFileFailed (file, exc);
    }
}