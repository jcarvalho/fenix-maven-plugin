package pt.ist.fenix.plugin;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

public abstract class RunnableMojo extends AbstractMojo {

    /**
     * Maven Project
     * 
     * @parameter default-value="${project}"
     */
    private MavenProject mavenProject;

    public abstract String getMainClassName();

    public String[] getArguments() {
        return new String[0];
    }

    /**
     * a ThreadGroup to isolate execution and collect exceptions.
     */
    class IsolatedThreadGroup extends ThreadGroup {
        private Throwable uncaughtException; // synchronize access to this

        public IsolatedThreadGroup(String name) {
            super(name);
        }

        @Override
        public void uncaughtException(Thread thread, Throwable throwable) {
            if (throwable instanceof ThreadDeath) {
                return; //harmless
            }
            synchronized (this) {
                if (uncaughtException == null) // only remember the first one
                {
                    uncaughtException = throwable; // will be reported eventually
                }
            }
            getLog().warn(throwable);
        }
    }

    private URLClassLoader getClassLoader() {
        Set<String> classpathElements = new HashSet<String>();
        try {
            classpathElements.addAll(mavenProject.getCompileClasspathElements());
            classpathElements.addAll(mavenProject.getRuntimeClasspathElements());
        } catch (DependencyResolutionRequiredException e) {
            getLog().error(e);
        }

        classpathElements.add(new File(mavenProject.getBuild().getOutputDirectory()).getAbsolutePath());

        URL[] classesURL = new URL[classpathElements.size()];
        int i = 0;

        for (String path : classpathElements) {
            try {
                classesURL[i++] = new File(path).toURI().toURL();
            } catch (MalformedURLException e) {
                getLog().error(e);
            }
        }
        getLog().debug("Classpath : " + StringUtils.join(classpathElements, "\n"));
        return new URLClassLoader(classesURL);
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final URLClassLoader classLoader = getClassLoader();
        IsolatedThreadGroup threadGroup = new IsolatedThreadGroup(getMainClassName() /*name*/);
        Thread bootstrapThread = new Thread(threadGroup, new Runnable() {
            @Override
            public void run() {
                try {
                    Method main =
                            Thread.currentThread().getContextClassLoader().loadClass(getMainClassName())
                                    .getMethod("main", new Class[] { String[].class });
                    if (!main.isAccessible()) {
                        getLog().debug("Setting accessibility to true in order to invoke main().");
                        main.setAccessible(true);
                    }
                    if (!Modifier.isStatic(main.getModifiers())) {
                        throw new MojoExecutionException("Can't call main(String[])-method because it is not static.");
                    }
                    main.invoke(null, new Object[] { getArguments() });
                } catch (NoSuchMethodException e) {   // just pass it on
                    Thread.currentThread()
                            .getThreadGroup()
                            .uncaughtException(
                                    Thread.currentThread(),
                                    new Exception(
                                            "The specified mainClass doesn't contain a main method with appropriate signature.",
                                            e));
                } catch (Exception e) {   // just pass it on
                    Thread.currentThread().getThreadGroup().uncaughtException(Thread.currentThread(), e);
                }
            }
        }, getMainClassName() + ".main()");
        bootstrapThread.setContextClassLoader(classLoader);
        bootstrapThread.start();
        try {
            bootstrapThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public MavenProject getMavenProject() {
        return mavenProject;
    }

}
