package com.icedcap.dubbing;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import com.icedcap.dubbing.utils.MediaUtil;

import static android.media.MediaMetadataRetriever.METADATA_KEY_DURATION;

/**
 * Created by dsq on 2017/5/24.
 */

public class ChooseCoverFromVideoActivity extends Activity {
    private static final String EXTRA_VIDEO_PATH_KEY = "extra-video-path-key";
    private MediaMetadataRetriever mMediaMetadataRetriever;
    private long mVideoTotalTime;
    private String mCoverPath;


    public static void launch(Activity context, int reqCode, String videoPath) {
        Intent i = new Intent(context, ChooseCoverFromVideoActivity.class);
        i.putExtra(EXTRA_VIDEO_PATH_KEY, videoPath);
        context.startActivityForResult(i, reqCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_cover_from_video);
        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseRetriever();
    }

    private void init() {
        mCoverPath = getExternalCacheDir() + "/video_cover.png";
        final String video = getIntent().getStringExtra(EXTRA_VIDEO_PATH_KEY);
        if (TextUtils.isEmpty(video)) {
            Toast.makeText(this, "视频素材加载出现问题，o(╯□╰)o", Toast.LENGTH_SHORT).show();
            finish();
        }
        prepareMediaMeta(video);
        final ImageView imageView = (ImageView) findViewById(R.id.video_choose_thumb);
        imageView.setImageBitmap(MediaUtil.getThumbnail(this, 0, video));

        final ProgressBar pb = (ProgressBar) findViewById(R.id.progress_bar);
        pb.getIndeterminateDrawable().setColorFilter(0xFFCECECE, android.graphics.PorterDuff.Mode.MULTIPLY);

        SeekBar seekBar = (SeekBar) findViewById(R.id.video_choose_seek_bar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
                // no - op
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // no - op
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                final int p = seekBar.getProgress();
                new AsyncTask<Void, Void, Bitmap>() {
                    @Override
                    protected void onPreExecute() {
                        pb.setVisibility(View.VISIBLE);
                    }

                    @Override
                    protected Bitmap doInBackground(Void... params) {
                        long l = p * mVideoTotalTime;
                        return getBitmapFromVideo(l);
                    }

                    @Override
                    protected void onPostExecute(Bitmap b) {
                        if (b != null) {
                            imageView.setImageBitmap(b);
                            MediaUtil.writeCoverImgToLocal(b, mCoverPath);

                        }
                        pb.setVisibility(View.GONE);
                    }
                }.execute();
            }
        });

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResultAndExit();
            }
        });
    }

    private void prepareMediaMeta(String path) {
        try {
            if (mMediaMetadataRetriever == null) {
                mMediaMetadataRetriever = new MediaMetadataRetriever();
            }
            mMediaMetadataRetriever.setDataSource(path);
            String info = mMediaMetadataRetriever.extractMetadata(METADATA_KEY_DURATION);
            if (info != null) {
                mVideoTotalTime = Long.valueOf(info);
            }
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            releaseRetriever();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            releaseRetriever();
        } finally {
        }
    }

    private void releaseRetriever() {
        if (mMediaMetadataRetriever == null) return;
        try {
            mMediaMetadataRetriever.release();
        } catch (Exception e) {
            // no - op
        }
    }

    private Bitmap getBitmapFromVideo(long timeUs) {
        if (mMediaMetadataRetriever != null) {
            try {
                return mMediaMetadataRetriever.getFrameAtTime(timeUs);
            } catch (IllegalStateException e) {
            } catch (RuntimeException e) {
            } finally {
//                releaseRetriever();
            }
        }
        return null;
    }

    private void setResultAndExit() {
        Intent data = new Intent();
        data.putExtra("cover-from-video", mCoverPath);
        setResult(Activity.RESULT_OK, data);

        finish();
    }
}
