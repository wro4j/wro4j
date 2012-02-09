package n4u.wro4j.extensions;

/**
 * Created by IntelliJ IDEA.
 * User: Dmitry.Erman
 * Date: 1/4/12
 * Time: 8:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class Options {
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    protected String filePath = "";
}
