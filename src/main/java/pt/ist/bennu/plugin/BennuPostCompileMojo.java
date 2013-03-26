package pt.ist.bennu.plugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

/**
 * Post process compiled classes
 * 
 * @goal post-compile
 * 
 * @phase process-classes
 * 
 * @requiresDependencyResolution runtime
 */
public class BennuPostCompileMojo extends AbstractMojo {
    /**
     * Maven Project
     * 
     * @parameter default-value="${project}"
     */
    private MavenProject mavenProject;

    /**
     * Classes Directory
     * 
     * @parameter expression="${post-compile.classesDirectory}"
     *            default-value="${project.build.outputDirectory}"
     */
    private File classesDirectory;

    /**
     * @parameter expression="${post-compile.injectors}"
     * 
     */

    private String[] injectors;

    private static final String[] DEFAULT_INJECTORS = new String[] {
            "pt.ist.fenixWebFramework.security.accessControl.CheckedAnnotationInjector",
            "pt.ist.fenixWebFramework.services.ServiceAnnotationInjector" };

    @Override
    public void execute() throws MojoExecutionException {
        if (ArrayUtils.isEmpty(injectors)) {
            injectors = DEFAULT_INJECTORS;
        }
        getLog().info("Injectors : " + StringUtils.join(injectors, ", "));
        try (URLClassLoader loader = augmentClassLoader(getLog(), mavenProject)) {
            for (String injector : injectors) {
                invokeInjector(injector, loader);
            }
        } catch (ClassNotFoundException e) {
            getLog().info("No @Service injector found in classpath, not processing.");
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new MojoExecutionException(null, e);
        } catch (IOException e) {
            throw new MojoExecutionException(null, e);
        }

        // TODO: checked
        // CheckedAnnotationInjector.inject(classesDirectory, loader);

    }

    private void invokeInjector(String clazz, URLClassLoader loader) throws ClassNotFoundException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        Class<?> serviceInjector = loader.loadClass(clazz);
        Method injector = serviceInjector.getMethod("inject", new Class<?>[] { File.class, ClassLoader.class });
        injector.invoke(null, classesDirectory, loader);
    }

    public static URLClassLoader augmentClassLoader(Log log, MavenProject project) {
        List<String> classpathElements = null;
        try {
            classpathElements = project.getCompileClasspathElements();
        } catch (DependencyResolutionRequiredException e) {
            log.error(e);
        }

        URL[] classesURL = new URL[classpathElements.size()];
        int i = 0;

        for (String path : classpathElements) {
            try {
                classesURL[i++] = new File(path).toURI().toURL();
            } catch (MalformedURLException e) {
                log.error(e);
            }
        }

        URLClassLoader loader = new URLClassLoader(classesURL, Thread.currentThread().getContextClassLoader());
        Thread.currentThread().setContextClassLoader(loader);
        return loader;
    }
}
