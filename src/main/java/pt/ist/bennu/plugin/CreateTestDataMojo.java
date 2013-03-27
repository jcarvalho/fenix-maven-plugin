package pt.ist.bennu.plugin;

/**
 * Create Test Data
 * 
 * @goal test-data
 * 
 * @phase test
 * 
 * @requiresDependencyResolution runtime
 */
public class CreateTestDataMojo extends RunnableMojo {

    @Override
    public String getMainClassName() {
        return "pt.utl.ist.codeGenerator.database.CreateTestData";
    }

}