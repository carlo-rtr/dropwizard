package io.dropwizard.cli;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import io.dropwizard.Configuration;
import io.dropwizard.util.Duration;
import io.dropwizard.validation.MinDuration;
import net.sourceforge.argparse4j.inf.Namespace;
import org.hibernate.validator.constraints.NotEmpty;
import org.junit.Test;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultsCommandTest {
    public static class SomeBean {
        @NotNull
        private String driverClass = null;

        @Min(0)
        @Max(100)
        private int abandonWhenPercentageFull = 0;

        @NotNull
        @MinDuration(value = 1, unit = TimeUnit.SECONDS)
        private Duration maxWaitForConnection = Duration.seconds(30);
    }
    
    public static class MyConfiguration extends Configuration {
        @NotEmpty
        private String template;

        @NotEmpty
        private String defaultName = "Stranger";

        @Valid
        @NotNull
        private SomeBean bean = new SomeBean();

        @NotNull
        private Map<String, Map<String, String>> viewRendererConfiguration = Collections.emptyMap();

        @JsonProperty
        public String getTemplate() {
            return template;
        }

        @JsonProperty
        public void setTemplate(String template) {
            this.template = template;
        }

        @JsonProperty
        public String getDefaultName() {
            return defaultName;
        }

        @JsonProperty
        public void setDefaultName(String defaultName) {
            this.defaultName = defaultName;
        }

        @JsonProperty("bean")
        public SomeBean getBean() {
            return bean;
        }

        @JsonProperty("bean")
        public void setBean(SomeBean bean) {
            this.bean = bean;
        }

        @JsonProperty("viewRendererConfiguration")
        public Map<String, Map<String, String>> getViewRendererConfiguration() {
            return viewRendererConfiguration;
        }
    }
    
    @Test
    public void testOutput() throws Exception {
        Namespace namespace = mock(Namespace.class);
        MyConfiguration configuration = new MyConfiguration();
        when(namespace.getString("ConfigType")).thenReturn("io.dropwizard.cli.DefaultsCommandTest$MyConfiguration");
        
        DefaultsCommand<MyConfiguration> defaultsCommand = new DefaultsCommand<>();
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        defaultsCommand.setOutputStream(new PrintStream(bao));
        defaultsCommand.run(null, namespace, configuration);
        
        URL resource = ClassLoader.getSystemClassLoader().getResource("defaults_output.txt");
        try(InputStreamReader inputStreamReader = new InputStreamReader(resource.openStream(), Charsets.UTF_8)) {
            assertEquals(CharStreams.toString(inputStreamReader), new String(bao.toByteArray()));
        }
    }
}
