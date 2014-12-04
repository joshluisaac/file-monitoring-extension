package com.appdynamics.monitors.FileWatcher;

import com.appdynamics.extensions.fileWatcher.config.Configuration;
import com.appdynamics.extensions.yml.YmlReader;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileNotFoundException;

public class ConfigUtilTest {

    @Test
    public void loadConfigSuccessfully() throws FileNotFoundException {
        Configuration configuration = YmlReader.readFromFile(this.getClass().getResource("/conf/config.yml").getFile(), Configuration.class);
        Assert.assertTrue(configuration != null);
    }
}
