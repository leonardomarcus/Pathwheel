package leonardomarcus.com.br.pathwheel.api.io;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class Logger {

    public static void debug(Context context, String data) {
        Log.d("pathwheel",  data);
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "Pathwheel");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("App", "failed to create directory");
            }
            // initiate media scan and put the new things into the path array to
            // make the scanner aware of the location and the files you want to see
            MediaScannerConnection.scanFile(context, new String[] {mediaStorageDir.toString()}, null, null);
        }

        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date();

        String fileName = dateFormat.format(date)+"-pathwheel.txt";

        try {

            FileWriter fw = new FileWriter(mediaStorageDir.toString()+"/"+fileName,true);
            fw.write(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date)+" => "+data+"\r\n");
            fw.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
}
