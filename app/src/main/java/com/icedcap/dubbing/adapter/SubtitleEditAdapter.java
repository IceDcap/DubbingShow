package com.icedcap.dubbing.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.icedcap.dubbing.R;
import com.icedcap.dubbing.entity.SRTEntity;

import java.util.ArrayList;

/**
 * Created by dsq on 2017/5/4.
 */

public class SubtitleEditAdapter extends RecyclerView.Adapter<SubtitleEditAdapter.SubtitleEditVH> {
    private Context mContext;
    private ArrayList<SRTEntity> mList;

    public SubtitleEditAdapter(ArrayList<SRTEntity> list) {
        mList = list;
    }

    public ArrayList<SRTEntity> getList() {
        return mList;
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

        // fixed edittext content error when scroll recyclerview
        if (holder.mEditText.getTag() instanceof TextWatcher ) {
            holder.mEditText.removeTextChangedListener((TextWatcher) holder.mEditText.getTag());
        }
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
