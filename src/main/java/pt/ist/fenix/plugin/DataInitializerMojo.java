package pt.ist.fenix.plugin;

/**
 * Data Initializer
 * 
 * @goal data-init
 * 
 * @phase test
 * 
 * @requiresDependencyResolution runtime
 */
public class DataInitializerMojo extends RunnableMojo {

    @Override
    public String getMainClassName() {
        return "pt.utl.ist.codeGenerator.database.DataInitializer";
    }

}