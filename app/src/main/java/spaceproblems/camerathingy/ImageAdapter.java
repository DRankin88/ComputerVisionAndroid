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

import java.util.HashMap;

import spaceproblems.camerathingy.R;


/**
 * Created by spaceproblems on 19/08/14.
 */
public class ImageAdapter extends ArrayAdapter
{

    Context context;
    int layoutResourceId;
    BitmapDrawable data[] = null;
    HashMap<Integer, String> thumbNailsToPaths;
    String[] imagePaths;

    public ImageAdapter(Context context, int layoutResourceId,
                        BitmapDrawable[] data, String[] paths)
    {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
        thumbNailsToPaths = createMap(data, paths);


    }

    private HashMap createMap(BitmapDrawable[] thumbnails, String[] paths)
    {
        HashMap<Integer, String> mapping = new HashMap<Integer, String>();

        for (int x = 0; x < thumbnails.length; x++)
        {
            mapping.put(thumbnails[x].hashCode(), paths[x]);
        }

        return mapping;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View row = convertView;
        ImageHolder holder = null;

        if (row == null)
        {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ImageHolder();
            holder.imgIcon = (ImageView) row.findViewById(R.id.imgIcon);

            row.setTag(holder);
        } else
        {
            holder = (ImageHolder) row.getTag();
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
