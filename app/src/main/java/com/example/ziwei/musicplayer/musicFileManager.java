package com.example.ziwei.musicplayer;

import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class musicFileManager {
  private static String FILE_PATH = "/Download";
  private static String MEDIA_PATH = Environment.
      getExternalStorageDirectory().getPath() + FILE_PATH;


  public static ArrayList<SingleSong> getSongList() {
    Log.e("fileManager","getSongList" + MEDIA_PATH);
    ArrayList<SingleSong> result = new ArrayList<SingleSong>();
    File[] fileList = new File(MEDIA_PATH).listFiles(new MP3FileFilter());
    if (fileList != null && fileList.length > 0) {
      for (File file: fileList) {
        result.add(getSingleSong(file));
      }
    }
    return result;
  }

  private static class MP3FileFilter implements FilenameFilter{
    @Override public boolean accept(File dir, String name) {
      return audioFilter(name) || videoFilter(name);
    }
  }

  public static SingleSong getSingleSong(File file) {
    String[] names = file.getName().split("_");
    String titleName = (names.length >0)? names[0]:"";
    String authorName = (names.length > 1)? names[1]:"";
    return new SingleSong("",titleName,
        authorName, file.getAbsolutePath());
  }

  private static boolean audioFilter(String name) {
    return name.endsWith(".mp3") || name.endsWith(".MP3");
  }

  private static boolean videoFilter(String name) {
    return name.endsWith(".mp4") || name.endsWith(".MP4");
  }

}
