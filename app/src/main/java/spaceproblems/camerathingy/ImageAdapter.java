package spaceproblems.camerathingy;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import spaceproblems.camerathingy.R;

/**
 * Created by spaceproblems on 19/08/14.
 */
public class ImageAdapter extends ArrayAdapter {

    Context context;
    int layoutResourceId;
    BitmapDrawable data[] = null;

    public ImageAdapter(Context context, int layoutResourceId, BitmapDrawable[] data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ImageHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ImageHolder();
            holder.imgIcon = (ImageView)row.findViewById(R.id.imgIcon);

            row.setTag(holder);
        }
        else
        {
            holder = (ImageHolder)row.getTag();
        }

        BitmapDrawable image = data[position];
        holder.imgIcon.setImageDrawable(image);

        return row;
    }

    static class ImageHolder
    {
        ImageView imgIcon;
    }

}
