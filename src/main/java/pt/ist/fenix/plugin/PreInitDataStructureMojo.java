package pt.ist.fenix.plugin;

/**
 * Pre Init Data Structure
 * 
 * @goal pre-init
 * 
 * @phase test
 * 
 * @requiresDependencyResolution runtime
 */
public class PreInitDataStructureMojo extends RunnableMojo {

    @Override
    public String getMainClassName() {
        return "pt.utl.ist.codeGenerator.database.PreInitDataStructure";
    }

}