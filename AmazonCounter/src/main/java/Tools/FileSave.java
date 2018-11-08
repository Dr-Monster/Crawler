package Tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by 1 on 17-10-21.
 */
public class FileSave {
    public void writeToFile(String f_Name , String info) {
        String fileName = "F:\\Html\\" + f_Name + ".txt";
        File file = new File(fileName);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String filePath = file.getPath();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filePath);
            fos.write(info.getBytes());
            System.out.println("Success");
            fos.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}
