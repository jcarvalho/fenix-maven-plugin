package pt.ist.bennu.plugin;

/**
 * Prepare Full Run File
 * 
 * @goal full-run-file
 * 
 * @phase test
 * 
 * @requiresDependencyResolution runtime
 */
public class PrepareFullRunFileMojo extends RunnableMojo {

    /**
     * @parameter expression="${full-run-file.from}"
     * 
     */

    private String from;

    @Override
    public String getMainClassName() {
        return "pt.utl.ist.fenix.tools.scripts.JoinSQLScripts";
    }

    @Override
    public String[] getArguments() {
        final String baseDir = getMavenProject().getBasedir().getAbsolutePath();
        final String dbPath = baseDir + "/etc/database_operations";
        final String outputFile = baseDir + "/etc/run";
        return new String[] { dbPath, from, outputFile };
    }

}