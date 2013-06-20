package pt.ist.fenix.plugin;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
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
public class FenixPostCompileMojo extends AbstractMojo {
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

    @Override
    public void execute() throws MojoExecutionException {
        File actionAnnotations = new File(mavenProject.getBasedir(), ".actionAnnotationLog");
        if (actionAnnotations.exists()) {
            actionAnnotations.renameTo(new File(classesDirectory.getAbsolutePath() + File.separatorChar
                    + mavenProject.getArtifactId() + File.separatorChar + ".actionAnnotationLog"));
        }

    }

}
