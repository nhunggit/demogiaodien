package com.android.incallui.customizebkav.anwser;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.dialer.common.Assert;
import com.android.dialer.common.FragmentUtils;
import com.android.incallui.InCallActivity;
import com.android.incallui.R;
import com.android.incallui.answer.impl.AnswerFragment;
import com.android.incallui.answer.protocol.AnswerScreenDelegateFactory;
import com.android.incallui.contactgrid.ContactGridManager;
import com.android.incallui.customizebkav.utils.BkavBitmapUtils;
import com.android.incallui.customizebkav.widget.AnimationInCall;
import com.android.incallui.customizebkav.widget.BkavResolverDrawerLayout;
import com.android.incallui.customizebkav.widget.Setting;

import java.util.List;

/**
 * Created by duclq.
 */

public class BkavAnwserFragment extends AnswerFragment implements BkavSlideBarCall.OnAnwserListener, ContactGridManager.CallbackUpdateInfo {

    private static final String TAG = BkavAnwserFragment.class.getName();

    private BkavSlideBarCall mBkavSlideBarCall;
    private AnimationInCall mPhoto;
    private BkavResolverDrawerLayout mResolverDrawerLayout;
    private ListView mListMessage;
    private LinearLayout mSlidingView;
    private TextView mRextRejectCallWithMessage;
    private BkavResolverDrawerLayout.ScrollCallbackListener mScrollCallbackListener;
    private LinearLayout mBackgroundTransparent;
    private LinearLayout mSlideButton;
    private String[] mListMessageRejectAnswer ;
    private MessageListAdapter mMessageListAdapter;
    private Drawable mPhotoCache;
    private View mViewIncall;
    private View mThreeRowInfoView;
    private View mTwoRowInfoView;

    @Override
    protected int getViewInflate() {
        return R.layout.frag_incoming_call_bkav;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        answerScreenDelegate =
                FragmentUtils.getParentUnsafe(this, AnswerScreenDelegateFactory.class)
                        .newAnswerScreenDelegate(this);
        mBkavSlideBarCall = (BkavSlideBarCall) view.findViewById(R.id.background);
        mBkavSlideBarCall.setOnAnwserListener(this);
        contactGridManager.setCallbackUpdateInfo(this);
        mViewIncall = (View)view.findViewById(R.id.view_answer);
        //Bkav DucLQ
        mThreeRowInfoView = (View) view.findViewById(R.id.primary_call_banner);
        mTwoRowInfoView = (View) view.findViewById(R.id.primary_call_banner_bkav);
        if(Settings.System.getInt(getActivity().getContentResolver(), Setting.ENABLE_FULL_INFO_CALL, 0) == 1){
            mTwoRowInfoView.setVisibility(View.GONE);
            mThreeRowInfoView.setVisibility(View.VISIBLE);
        }else{
            mThreeRowInfoView.setVisibility(View.GONE);
            mTwoRowInfoView.setVisibility(View.VISIBLE);
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPhoto = (AnimationInCall) view.findViewById(R.id.bkav_photo);
        mPhoto.setShouldDraw(true);
        initViewResolverDrawerLayout(view);
    }

    public static BkavAnwserFragment newInstance(
            String callId,
            boolean isRttCall,
            boolean isVideoCall,
            boolean isVideoUpgradeRequest,
            boolean isSelfManagedCamera,
            boolean allowAnswerAndRelease,
            boolean hasCallOnHold) {
        Bundle bundle = new Bundle();
        bundle.putString(ARG_CALL_ID, Assert.isNotNull(callId));
        bundle.putBoolean(ARG_IS_RTT_CALL, isRttCall);
        bundle.putBoolean(ARG_IS_VIDEO_CALL, isVideoCall);
        bundle.putBoolean(ARG_IS_VIDEO_UPGRADE_REQUEST, isVideoUpgradeRequest);
        bundle.putBoolean(ARG_IS_SELF_MANAGED_CAMERA, isSelfManagedCamera);
        bundle.putBoolean(ARG_ALLOW_ANSWER_AND_RELEASE, allowAnswerAndRelease);
        bundle.putBoolean(ARG_HAS_CALL_ON_HOLD, hasCallOnHold);

        BkavAnwserFragment instance = new BkavAnwserFragment();
        instance.setArguments(bundle);
        return instance;
    }

    @Override
    public void onAnwserBkav() {
        if(answerScreenDelegate != null){
            //Anhhn
            mBkavSlideBarCall.setVisibility(View.GONE);
            answerScreenDelegate.onAnswer(true);//Bkav DucLQ
        }

    }

    @Override
    public void onDeclineBkav() {
        if(answerScreenDelegate != null){
            mBkavSlideBarCall.setVisibility(View.GONE);
            answerScreenDelegate.onReject();
        }
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if(mBkavSlideBarCall.getVisibility() == View.GONE){
            mBkavSlideBarCall.setVisibility(View.VISIBLE);
        }
        mBkavSlideBarCall.resetState();
    }

    @Override
    public Fragment getAnswerScreenFragment() {
        return this;
    }

    @Override
    public void updateAvatar(Drawable avatar) {
        if (mPhoto != null) {
            if (avatar != null) {
                mPhoto.loadAvatarBitmap(BkavBitmapUtils.drawableToBitmap(avatar), true);
                if((avatar != BkavBitmapUtils.getDefaultContactPhotoDrawable(getContext()))
                        && (mPhotoCache != avatar)){
                    if(BkavBitmapUtils.setBackgroundCallCard(mViewIncall, avatar, getContext())){
                        mPhotoCache = avatar;
                    }
                }
            } else {
                mPhoto.loadAvatarBitmap(BkavBitmapUtils.drawableToBitmap(BkavBitmapUtils.getDefaultContactPhotoDrawable(getContext())), true);
            }
        }
    }

    @Override
    public void updateAvatarConference() {

    }

    private class MessageListAdapter extends ArrayAdapter<String> {

        private String[] mListMessage;

        public MessageListAdapter(Context context, int textViewResourceId, String[] listmessage) {
            super(context, textViewResourceId);
            mListMessage = listmessage;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MessageHolder holder;

            if (convertView == null) {
                holder = new MessageHolder();
                LayoutInflater inflate = LayoutInflater.from(getActivity());
                convertView = inflate.inflate(R.layout.bkav_message_list_item_incomming, null);
                holder.messageText = (TextView) convertView
                        .findViewById(R.id.bkav_text_list_icomming);
                convertView.setTag(holder);
            } else {
                holder = (MessageHolder) convertView.getTag();
            }

            holder.messageText.setText(mListMessage[position]);

            return convertView;
        }

        @Override
        public int getCount() {
            return mListMessage.length;
        }

    }

    private class MessageHolder {
        public TextView messageText;

        public ImageView messageIcon;

    }

    private void initViewResolverDrawerLayout(View view){
        mResolverDrawerLayout = (BkavResolverDrawerLayout)view.findViewById(R.id.sliding_drawer_bkav);
        final InCallActivity inCallActivity = (InCallActivity) getActivity();
        mBackgroundTransparent = (LinearLayout)view.findViewById(R.id.background_blur);
        Drawable backgroundTransparent = mBackgroundTransparent.getBackground();
        backgroundTransparent.setAlpha(0);
        mRextRejectCallWithMessage = (TextView)view.findViewById(R.id.text_reject_call_with_message);
        mSlideButton = (LinearLayout)view.findViewById(R.id.slide_button);
        mScrollCallbackListener = new BkavResolverDrawerLayout.ScrollCallbackListener() {

            @Override
            public void updateView(int topOffsetIsClose, int topOffset, int heightUser) {
                float a = (((float)topOffsetIsClose - (float)topOffset)/((float)heightUser/5));
                int alpha = 0;
                if(a >= 0 && a <=1){
                    alpha = Math.round(a*255);
                }else{
                    if(a < 0){
                        alpha = 0;
                    }else{
                        alpha = 255;
                    }
                }
                Drawable backgroundSlideButton = mSlideButton.getBackground();
                backgroundSlideButton.setAlpha(alpha);
                Drawable backgroundTransparent = mBackgroundTransparent.getBackground();
                backgroundTransparent.setAlpha(Math.round(alpha));
                if(alpha >215){
                    mRextRejectCallWithMessage.setTextColor(inCallActivity.getResources()
                            .getColor(R.color.bkav_text_list_icomming));
                }else{
                    mRextRejectCallWithMessage.setTextColor(inCallActivity.getResources()
                            .getColor(R.color.bkav_message_text_icomming));
                }

                if (!mResolverDrawerLayout.isClose()) {
                    mResolverDrawerLayout.setOnTouchEvent(true);
                } else {
                    mResolverDrawerLayout.setOnTouchEvent(false);
                }
            }
        };
        mResolverDrawerLayout.setScrollCallbackListener(mScrollCallbackListener);
        if(mListMessageRejectAnswer == null) {
            mListMessageRejectAnswer = new String[] {
                    getResources().getString(R.string.message_call_1),
                    getResources().getString(R.string.message_call_2),
                    getResources().getString(R.string.message_call_3),
                    getResources().getString(R.string.message_call_4)
            };
        }

        Button btnNewMessageIncoming;
        mSlidingView = (LinearLayout) view.findViewById(R.id.slideButtonBkav);
        mSlidingView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()){
                    case MotionEvent.ACTION_MOVE:
                        mResolverDrawerLayout.setOnTouchEvent(true);
                        break;
                    case MotionEvent.ACTION_DOWN:
                        mResolverDrawerLayout.setOnTouchEvent(true);
                        break;
                    default:
                        mResolverDrawerLayout.setOnTouchEvent(false);
                        break;
                }
                // TODO Auto-generated method stub
                return false;
            }
        });
        mResolverDrawerLayout.setViewAlwaysShow(mSlidingView);
        mListMessage = (ListView) view.findViewById(R.id.list_message_anwser);

        btnNewMessageIncoming = (Button) view.findViewById(R.id.btnNewMessageIncomingBkav);
        mMessageListAdapter = new MessageListAdapter(getActivity(),
                R.layout.bkav_message_list_item_incomming, mListMessageRejectAnswer);
        mListMessage.setAdapter(mMessageListAdapter);
        mListMessage.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String mes = mListMessageRejectAnswer[position];
                smsSelected(mes);
            }
        });

        mSlidingView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Bkav DucLQ bo su kien onclick vao nut hien thi tin nhan tra loi nhanh, chi dung keo len
                // bo di vi khi click vao thi de nham nen chu tich chot la chi cho keo len
                //mResolverDrawerLayout.scrollOnClick();
            }
        });

        btnNewMessageIncoming.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO: Ra giao dien Compose cua Btalk, tam thoi de show Custom
                // Message Dialog nhu cua phan mem goc
                smsSelected(null);

            }
        });
    }

    @Override
    public void updateListMessageAnswer(List<String> listMessagerAnwser) {
        if (listMessagerAnwser == null || listMessagerAnwser.size() <= 0
                || listMessagerAnwser.size() > 4) {
            return;
        }

        if(mListMessageRejectAnswer == null){
            mListMessageRejectAnswer = new String[4];
        }

        for (int i = 0; i < listMessagerAnwser.size(); i++) {
            mListMessageRejectAnswer[i] = listMessagerAnwser.get(i);
        }
        if (mMessageListAdapter != null) {
            mMessageListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void setTextResponses(List<String> textResponses) {
        //Bkav DucLQ set text nhan tin
        updateListMessageAnswer(textResponses);
    }
}
