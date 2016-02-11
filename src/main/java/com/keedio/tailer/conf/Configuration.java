package com.keedio.tailer.conf;

import com.sun.javaws.exceptions.InvalidArgumentException;
import org.apache.commons.io.filefilter.*;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.util.StringUtils;

import java.io.File;

/**
 * Created by luca on 10/2/16.
 */
@org.springframework.context.annotation.Configuration
@PropertySource(value = "classpath:application.properties", ignoreResourceNotFound = true)
public class Configuration {

    @Value("${root.scan.dir}")
    private File directory;

    @Value("${file.alteration.scan.interval:1000}")
    private long fileAlterationInterval;

    @Autowired
    private FileAlterationListener listener;

    @Value("${whitelist.regexp:}")
    private String whiteistRegexp;

    @Value("${blacklist.regexp:}")
    private String blacklistRegexp;

    @Bean
    IOFileFilter fileFilter() {

        IOFileFilter filter = DirectoryFileFilter.DIRECTORY;

        if (!StringUtils.isEmpty(whiteistRegexp)) {

            RegexFileFilter regexFileFilter = new RegexFileFilter(whiteistRegexp);

            filter = FileFilterUtils.or(filter,
                    FileFilterUtils.and(
                            HiddenFileFilter.VISIBLE, regexFileFilter));
        } else if (!StringUtils.isEmpty(blacklistRegexp)){

            RegexFileFilter regexFileFilter = new RegexFileFilter(blacklistRegexp);

            filter = FileFilterUtils.or(filter,
                    FileFilterUtils.and(
                            HiddenFileFilter.VISIBLE, FileFilterUtils.notFileFilter(regexFileFilter)));

        } else {
            throw new IllegalArgumentException("Either whitelist.regexp or blacklist.regexp must be provided");
        }

        return filter;
    }

    @Bean
    public FileAlterationObserver fileAlterationObserver() {
        FileAlterationObserver observer = new FileAlterationObserver(directory, fileFilter());
        observer.addListener(listener);
        return observer;
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public FileAlterationMonitor fileAlterationMonitor() {
        return new FileAlterationMonitor(fileAlterationInterval, fileAlterationObserver());
    }
}
