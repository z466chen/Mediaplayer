package com.example.ziwei.musicplayer;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ListofMusicAdapter extends RecyclerView.Adapter<ListofMusicAdapter.ViewHolder>{

  private Context mContext;
  private ArrayList<SingleSong> songs;
  private static boolean isCheckPlayerListMode;
  public static String CHANGE_LAYOUT = "changelayout";

  ListofMusicAdapter(Context mContext, ArrayList<SingleSong> songs) {
    this.mContext = mContext;
    this.songs = songs;
    this.isCheckPlayerListMode = false;
  }

  class ViewHolder extends RecyclerView.ViewHolder {
    View songHolder;
    TextView authorNameView;
    TextView titleNameView;
    ImageView titleImageView;
    CheckBox checkBox;



    ViewHolder(View view) {
      super(view);
      songHolder = view;
      authorNameView = view.findViewById(R.id.author_name);
      titleNameView = view.findViewById(R.id.title_name);
      titleImageView = view.findViewById(R.id.title_image);
      checkBox = view.findViewById(R.id.song_list_checkbox);
    }


  }

  @NotNull @Override public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
    LayoutInflater LI = LayoutInflater.from(parent.getContext());
    return new ViewHolder(LI.inflate(R.layout.single_song_layout, parent, false));
  }

  @Override public void onBindViewHolder(@NotNull final ViewHolder holder, final int position) {
    holder.titleNameView.setText(songs.get(position).getTitleName());
    holder.authorNameView.setText(songs.get(position).getAuthorName());
    final int pos = position;
    holder.songHolder.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        Log.e("activity", " " + pos);
        ((MainActivity) mContext).startSongActivity(pos);
      }
    });

  }


  public static void setIsCheckPlayerListMode(boolean mode) {
    isCheckPlayerListMode = mode;
  }

  public static boolean getIsCheckPlayerListMode() {
    return isCheckPlayerListMode;
  }

  public ArrayList<SingleSong> getSongs () { return songs; }

  @Override public int getItemCount() {
    return songs.size();
  }
}

