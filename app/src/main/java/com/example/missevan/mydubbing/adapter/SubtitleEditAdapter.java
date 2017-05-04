package com.example.missevan.mydubbing.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.missevan.mydubbing.R;
import com.example.missevan.mydubbing.entity.SRTEntity;
import com.example.missevan.mydubbing.entity.SRTSubtitleEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by missevan on 2017/5/4.
 */

public class SubtitleEditAdapter extends RecyclerView.Adapter<SubtitleEditAdapter.SubtitleEditVH> {
    private Context mContext;
    private ArrayList<SRTEntity> mList;
    private Map<Integer, SubtitleEditVH> mVHMap;

    public SubtitleEditAdapter(ArrayList<SRTEntity> list) {
        mList = list;
        mVHMap = new HashMap<>();
    }

    @Override
    public SubtitleEditVH onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_subtitle_edit, parent, false);
        return new SubtitleEditVH(view);
    }

    @Override
    public void onBindViewHolder(SubtitleEditVH holder, int position) {
        final SRTEntity entity = mList.get(position);
        if (entity == null) return;

        holder.mEditText.setText(entity.getContent());
        TextWatcher textWatcher =new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // no - op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // no - op
            }

            @Override
            public void afterTextChanged(Editable s) {
                entity.setContent(s.toString());
            }
        };
        holder.mEditText.addTextChangedListener(textWatcher);
        holder.mEditText.setTag(textWatcher);
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    class SubtitleEditVH extends RecyclerView.ViewHolder {
        EditText mEditText;

        public SubtitleEditVH(View itemView) {
            super(itemView);
            mEditText = (EditText) itemView.findViewById(R.id.editText);
        }
    }
}
