package io.dropwizard.cli;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.util.Duration;
import io.dropwizard.validation.MaxDuration;
import io.dropwizard.validation.MinDuration;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.hibernate.validator.constraints.NotEmpty;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * Prints the default values of the configuration
 */
public class DefaultsCommand<T extends Configuration> extends ConfiguredCommand<T> {
    PrintStream outputStream = System.out;

    /**
     * Create a new command with the given name and description.
     */
    public DefaultsCommand() {
        super("defaults", "Prints configuration default values as YAML");
    }

    public PrintStream getOutputStream() {
        return outputStream;
    }

    void setOutputStream(PrintStream outputStream) {
        this.outputStream = outputStream;
    }

    @Override
    protected void run(Bootstrap<T> bootstrap, Namespace namespace, T configuration) throws Exception {
        String configType = namespace.getString("ConfigType");
        Class type = Class.forName(configType);
        sub(type, type.newInstance(), 0);
    }

    @Override
    public void configure(Subparser subparser) {
        subparser.addArgument("ConfigType").nargs("?").help("Fully qualified class name of configuration file");
    }
    
    private void sub(Class<?> clazz, Object instance, int indent) throws IllegalAccessException {
        for (Field f : clazz.getDeclaredFields()) {
            if (isPrimitive(f)) {
                f.setAccessible(true);
                Object value = f.get(instance);
                printAnnotatedValue(f, value, indent);
            } else {
                printValue(f, "", indent);

                Class<?> type = f.getType();
                try {
                    Object newInstance = type.newInstance();
                    sub(type, newInstance, indent + 1);
                } catch (InstantiationException e) {
                    // Sub class without default constructor.
                }
            }
        }
    }

    private void printAnnotatedValue(Field f, Object value, int indent) {
        StringBuilder sb = new StringBuilder("# ");
        sb.append(f.getType().getSimpleName());
        int count = 0;
        
        for (Annotation annotation : f.getAnnotations()) {
            Class<? extends Annotation> aClass = annotation.annotationType();
            if (aClass.isAssignableFrom(JsonProperty.class)) {
                continue;
            }
            
            count++;
            if(count == 1){
                sb.append("( ");
            }
            
            if(count > 1){
                sb.append("; ");
            }
            
            if (aClass.isAssignableFrom(NotEmpty.class)) {
                sb.append("Not Empty");
            } else if (aClass.isAssignableFrom(Min.class)) {
                Min min = Min.class.cast(annotation);
                sb.append("Min: ").append(min.value());
            } else if (aClass.isAssignableFrom(Max.class)) {
                Max max = Max.class.cast(annotation);
                sb.append("Max: ").append(max.value());
            } else if (aClass.isAssignableFrom(MinDuration.class)) {
                MinDuration duration = MinDuration.class.cast(annotation);
                sb.append("DurationMin: ").append(duration.unit()).append(" ").append(duration.value());
            } else if (aClass.isAssignableFrom(MaxDuration.class)) {
                MaxDuration duration = MaxDuration.class.cast(annotation);
                sb.append("DurationMax: ").append(duration.unit()).append(" ").append(duration.value());
            } else {
                sb.append(aClass.getSimpleName());
            }
        }
        
        if(count >= 1){
            sb.append(" )");
        }
        
        printIndented(indent, sb.toString());

        printValue(f, value, indent);
    }

    private void printValue(Field f, Object value, int indent) {
        String name = f.getName();
        printIndented(indent, String.format("%s: %s\n", name, value));
    }

    private boolean isPrimitive(Field field) {
        Class<?> type = field.getType();
        return type.isAssignableFrom(String.class) 
                || type.isAssignableFrom(Boolean.class)
                || type.isAssignableFrom(boolean.class)
                || type.isAssignableFrom(Integer.class)
                || type.isAssignableFrom(int.class)
                || type.isAssignableFrom(Long.class)
                || type.isAssignableFrom(long.class)
                || type.isAssignableFrom(Double.class)
                || type.isAssignableFrom(double.class)
                || type.isAssignableFrom(Map.class)
                || type.isAssignableFrom(Duration.class)
                || type.isEnum();
    }

    private void printIndented(int indent, String value) {
        getOutputStream().print(Strings.repeat("  ", indent));
        getOutputStream().println(value);
    }

}