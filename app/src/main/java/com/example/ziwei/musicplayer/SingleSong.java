package com.example.ziwei.musicplayer;

import android.os.Parcel;
import android.os.Parcelable;

class SingleSong implements Parcelable{
private String titleImage;
private String authorName;
private String titleName;
private String songUrl;

  public int describeContents() {
    return 0;
  }

  public void writeToParcel(Parcel out, int flags) {
    out.writeString(titleImage);
    out.writeString(authorName);
    out.writeString(titleName);
    out.writeString(songUrl);

  }

  public static final Parcelable.Creator<SingleSong> CREATOR
      = new Parcelable.Creator<SingleSong>() {
    public SingleSong createFromParcel(Parcel in) {
      return new SingleSong(in);
    }

    public SingleSong[] newArray(int size) {
      return new SingleSong[size];
    }
  };

  private SingleSong(Parcel in) {
    titleImage = in.readString();
    authorName = in.readString();
    titleName = in.readString();
    songUrl = in.readString();

  }

  SingleSong(String titleImage, String authorName,
    String titleName, String songUrl) {
      this.titleImage = titleImage;
      this.authorName = authorName;
      this.titleName = titleName;
      this.songUrl = songUrl;
    }

    void setSongUrl(String songUrl) {this.songUrl = songUrl;}

    void setTitleImage(String titleImage) {
    this.titleImage = titleImage;
    }

    void setAuthorName(String authorName) {
    this.authorName = authorName;
    }

    void setTitleName(String titleName) {
    this.titleName = titleName;
    }

    String getTitleImage(){ return titleImage;}

    String getAuthorName(){ return authorName;}

    String  getTitleName(){return titleName;}

    String getSongUrl() {return songUrl;}

    }