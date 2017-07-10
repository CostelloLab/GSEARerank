import edu.mit.broad.genome.Conf;
import xtools.api.AbstractTool;
import xtools.gsea.GseaPreranked;

public class Main {

	public static void main(String[] args) {
		System.out.println("Hello world!");
		GseaPreranked tool = new GseaPreranked(args);
        tool_main(tool);
	}
	protected static void tool_main(final AbstractTool tool) {

        if (tool == null) {
            throw new IllegalArgumentException("Param tool cannot be null");
        }

        boolean was_error = false;
        try {

            tool.execute();

        } catch (Throwable t) {
            // if the rpt dir was made try to rename it so that easily identifiable
            was_error = true;
            t.printStackTrace();
        }

        if (was_error && tool.getReport() != null) {
            tool.getReport().setErroredOut();
        }

        if (tool.getParamSet().getGuiParam().isFalse()) {
            Conf.exitSystem(was_error);
        } else {
            // dont exit!!
        }
    }

}
