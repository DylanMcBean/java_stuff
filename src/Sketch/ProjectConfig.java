package Sketch;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ProjectConfig {
    public static String getDataPath(String relativePath) {
        try {
            String callingClassName = new Exception().getStackTrace()[1].getClassName();
            Class<?> callingClass = Class.forName(callingClassName);
            String packageName = callingClass.getPackage().getName();
            String projectName = packageName.substring(packageName.lastIndexOf('.') + 1);

            Path sketchPath = Paths.get("").toAbsolutePath();
            return sketchPath.resolve("src/Sketch/" + projectName + "/data/" + relativePath).toString();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}