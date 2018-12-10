package org.dreamwork.fs;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

/**
 * 文件名过滤器
 *
 * <p>文件名过滤器类允许用户指定 FolderWalker 感兴趣的文件名规则，使用诸如 *.gif 或 .gif 之类的格式；若需要同时过滤多种格式，
 * 各个格式之间使用竖线'|'分割，如：*.gif|*.jpg
 *
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 2010-4-12
 * Time: 14:41:39
 */
public class WalkerFileFilter implements FileFilter {
    private String filter;
    private Pattern pattern;

    /**
     * 获取过滤器规则表达式
     * @return 过滤器规则表达式
     */
    public String getFilter () {
        return filter;
    }

    /**
     * 设置过滤器规则表达式
     * @param fileFilter 过滤器规则表达式
     */
    public void setFilter (String fileFilter) {
        this.filter = fileFilter;
        if (fileFilter == null) return;
        String[] parts = fileFilter.split ("\\|");
        StringBuilder builder = new StringBuilder ();
        for (String p : parts) {
            String a = p.trim ();
            if (p.startsWith ("*.")) a = p.substring (2);
            else if (p.startsWith (".")) a = p.substring (1);
            if (builder.length () > 0) builder.append ("|");
            builder.append (a);
        }
        fileFilter = ".*\\.(" + builder + ")$";
        pattern = Pattern.compile (fileFilter, Pattern.CASE_INSENSITIVE);
    }

    public boolean accept (File pathname) {
        return pattern == null || pattern.matcher (pathname.getName ()).matches ();
    }
}