package com.android.incallui.customizebkav.incall;

import android.Manifest;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.telecom.CallAudioState;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.dialer.common.FragmentUtils;
import com.android.dialer.common.LogUtil;
import com.android.incallui.ContactInfoCache;
import com.android.incallui.InCallActivity;
import com.android.incallui.InCallPresenter;
import com.android.incallui.call.DialerCall;
import com.android.incallui.contactgrid.ContactGridManager;
import com.android.incallui.customizebkav.Funtions;
import com.android.incallui.customizebkav.incall.shortcuts.DeepShortcutsContainer;
import com.android.incallui.customizebkav.incall.shortcuts.MenuItemInfo;
import com.android.incallui.customizebkav.utils.BkavBitmapUtils;
import com.android.incallui.customizebkav.widget.AnimationInCall;
import com.android.incallui.customizebkav.widget.Setting;
import com.android.incallui.incall.impl.InCallFragment;
import com.android.incallui.R;
import com.android.incallui.incall.protocol.InCallButtonIds;
import com.android.incallui.incall.protocol.InCallButtonUi;
import com.android.incallui.incall.protocol.InCallButtonUiDelegateFactory;
import com.android.incallui.incall.protocol.InCallScreenDelegateFactory;
import com.android.incallui.incall.protocol.PrimaryCallState;
import com.android.incallui.incall.protocol.SecondaryInfo;
import com.android.incallui.speakerbuttonlogic.SpeakerButtonInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by duclq on 04/05/2018.
 */

public class BkavIncallFragment  extends InCallFragment implements View.OnClickListener, ContactGridManager.CallbackUpdateInfo,
        CallbackCheckPermisstionIncall{

    private Button mBkavDialpadButton;

    private Button mBkavVideoButton;

    private Button mBkavHoldButton;

    private Button mBkavMuteButton;

    private Button mBkavBluetoothButton;

    private Button mBkavSpeakerButton;

    private Button mBkavEndButton;

    private Button mBkavRecordAndAdd;
    private Button mBkavRecordAndMerge;
    private Button mBkavMuteAndHoldCall;
    private boolean mOpenDeepShortcutMuteAndHold;
    private boolean mOpenDeepShortcut;
    private boolean mOpenDeepShortcutMerge;
    private ViewGroup mViewBottomFragment;
    private DeepShortcutsContainer mDeepShortcutsContainer;
    private BkavRecordCallControl mRecordControl;
    private String mTextViewRecord;
    private String mTextViewHold;
    private String mTextViewMute;
    private CallButtonOnClick mCallButtonOnClick;
    private View mViewIncall;
    private View mSecondaryView;
    private TextView mSecondaryName;
    private SecondaryInfo mSecondaryInfo;
    private LinearLayout mBkavSecondaryCallWrapper;
    private boolean mShouldShowManageConference = false;
    private RelativeLayout mBkavAvatarContainer;
    private static final int ID_SHORTCUT_CLOSED = -1;
    private static final int ID_SHORTCUT_OPENED = 1;
    private int mIdShortCutOpen = ID_SHORTCUT_CLOSED;
    private static final int STATE_MUTE = 1 << 1;// dang mu
    private static final int STATE_HOLD = 1 << 2;// dang hold
    private int mStateMuteAndHold = 0;
    private boolean canAddCall = false;

    private LinearLayout mThreeRowInfoView;

    private LinearLayout mTwoRowInfoView;
    public static final String[] STORAGE_PERMISSION = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public static final int EXTERNAL_STORAGE_PERMISSIONS_REQUEST = 1;


    public interface CallButtonOnClick{
        void addCallOnClick();
        void mergerCallOnClick();
        void recordCallOnClick();
        void holdCallOnclick();
        void muteCallOnclick();
    }

    private AnimationInCall mPhoto;
    private Drawable mPhotoCache;

    @Override
    protected int getLayoutFragIncall() {
        return R.layout.frag_incall_voice_bkav;
    }

    @Override
    public void enableButton(int buttonId, boolean enable) {
        super.enableButton(buttonId, enable);
        if(buttonId == InCallButtonIds.BUTTON_ADD_CALL) {
            canAddCall = enable;
        }
    }

    @Override
    public void canShowModifyVideoCall(boolean show) {
        super.canShowModifyVideoCall(show);
        //Bkav DucLQ hien thi button video theo logic android goc
        updateViewUpgradeToVideo(show);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View view =  super.onCreateView(layoutInflater, viewGroup, bundle);
        inCallButtonUiDelegate =
                FragmentUtils.getParent(this, InCallButtonUiDelegateFactory.class)
                        .newInCallButtonUiDelegate();
        mViewIncall = (View)view.findViewById(R.id.incall_view_bkav);
        mBkavDialpadButton = (Button) view.findViewById(R.id.dialpadButtonBkav);
        mBkavDialpadButton.setOnClickListener(this);
        mBkavMuteButton = (Button) view.findViewById(R.id.muteButtonBkav);
        mBkavMuteButton.setOnClickListener(this);
        mBkavBluetoothButton = (Button) view.findViewById(R.id.bluetoothButtonBkav);
        mBkavBluetoothButton.setOnClickListener(this);
        mBkavSpeakerButton = (Button) view.findViewById(R.id.speakerButtonBkav);
        mBkavSpeakerButton.setOnClickListener(this);
        mBkavHoldButton = (Button) view.findViewById(R.id.holdButtonBkav);
        mBkavHoldButton.setOnClickListener(this);
        mBkavVideoButton = (Button) view.findViewById(R.id.videoButtonBkav);
        mBkavVideoButton.setOnClickListener(this);
        mBkavEndButton = (Button) view.findViewById(R.id.endButtonBkav);
        mBkavEndButton.setOnClickListener(this);
        mBkavRecordAndAdd = (Button) view.findViewById(R.id.record_and_add_button);
        mBkavRecordAndAdd.setOnClickListener(this);
        mBkavRecordAndMerge = (Button) view.findViewById(R.id.record_and_merge_button);
        mBkavRecordAndMerge.setOnClickListener(this);
        mBkavMuteAndHoldCall = (Button) view.findViewById(R.id.muteAndHoldBkav);
        mBkavMuteAndHoldCall.setOnClickListener(this);
        mSecondaryView = (View) view.findViewById(R.id.bkav_secondary_call_info);
        mSecondaryName = (TextView) view.findViewById(R.id.bkav_secondary_call_name);
        mBkavSecondaryCallWrapper = (LinearLayout) view
                .findViewById(R.id.bkav_secondary_call_wrapper);
        mBkavSecondaryCallWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSecondaryClick();
            }
        });
        mBkavAvatarContainer = (RelativeLayout) view.findViewById(R.id.bkav_avatar_container);
        mBkavAvatarContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mShouldShowManageConference) {
                    onManageConferenceClicked();
                }
            }
        });
        mOpenDeepShortcut = true;
        mOpenDeepShortcutMerge = true;
        mOpenDeepShortcutMuteAndHold = true;
        mViewBottomFragment = (ViewGroup) view.findViewById(R.id.button_incall_bkav);
        mRecordControl = InCallPresenter.getInstance().getRecordCallControl();
        final InCallActivity incall = (InCallActivity)getActivity();
        if(incall != null) {
            incall.setCallbackCheckPermisstionIncall(this);
        }
        mCallButtonOnClick = new CallButtonOnClick() {
            @Override
            public void recordCallOnClick() {
                closePopupDeepshortcut();
                if (mRecordControl == null) {
                    return;
                }
                if (mRecordControl.isStartRecord()) {
                    updateStateRecord(false);
                    mRecordControl.stopRecordCall();
                } else {
                    updateStateRecord(true);
                    mRecordControl.startRecordCall(false);


                    if(incall != null) {
                        incall.checkPermissionStorage();
                    }
                }
            }

            @Override
            public void holdCallOnclick() {
                closePopupDeepshortcut();
                boolean isHold = (mStateMuteAndHold & STATE_HOLD) != 0;
                if(isHold){
                    mStateMuteAndHold &= ~STATE_HOLD;
                }else{
                    mStateMuteAndHold |= STATE_HOLD;
                }
                inCallButtonUiDelegate.holdClicked(!isHold);
                updateStateMuteAndHold();
            }

            @Override
            public void muteCallOnclick() {
                closePopupDeepshortcut();
                boolean isMute = (mStateMuteAndHold & STATE_MUTE) != 0;
                if(isMute){
                    mStateMuteAndHold &= ~STATE_MUTE;
                }else{
                    mStateMuteAndHold |= STATE_MUTE;
                }
                inCallButtonUiDelegate.muteClicked(!isMute, true);
                updateStateMuteAndHold();
            }

            @Override
            public void mergerCallOnClick() {
                closePopupDeepshortcut();
                inCallButtonUiDelegate.mergeClicked();
            }

            @Override
            public void addCallOnClick() {
                closePopupDeepshortcut();
                if(canAddCall) {
                    inCallButtonUiDelegate.addCallClicked();
                }
            }
        };
        contactGridManager.setCallbackUpdateInfo(this);
        //Bkav DucLQ
        mThreeRowInfoView = (LinearLayout) view.findViewById(R.id.primary_call_banner);
        mTwoRowInfoView = (LinearLayout) view.findViewById(R.id.primary_call_banner_bkav);
        if (Settings.System.getInt(getActivity().getContentResolver(),
                Setting.ENABLE_FULL_INFO_CALL, 0) == 1) {
            mTwoRowInfoView.setVisibility(View.GONE);
            mThreeRowInfoView.setVisibility(View.VISIBLE);
        } else {
            mThreeRowInfoView.setVisibility(View.GONE);
            mTwoRowInfoView.setVisibility(View.VISIBLE);
        }

        if (isBphone2()) {
            int hackMargin = getActivity().getResources().getDimensionPixelSize(R.dimen.hack_incall_layout_margin);
            RelativeLayout.LayoutParams layoutParam = (RelativeLayout.LayoutParams) mBkavAvatarContainer
                    .getLayoutParams();
            layoutParam.setMargins(0, hackMargin, 0, 0);

            mBkavAvatarContainer.setLayoutParams(layoutParam);
            LinearLayout.LayoutParams linearParam = (LinearLayout.LayoutParams) mTwoRowInfoView.getLayoutParams();
            linearParam.setMargins(0, hackMargin, 0, 0);
            mTwoRowInfoView.setLayoutParams(linearParam);

            LinearLayout ll = (LinearLayout) view.findViewById(R.id.callButtonFragment);
            int hackMargin2 = getActivity().getResources().getDimensionPixelSize(R.dimen.hack_padding_top_button);
            LinearLayout.LayoutParams linearParam2 = (LinearLayout.LayoutParams) ll.getLayoutParams();
            linearParam2.setMargins(0, hackMargin2, 0, 0);
            ll.setLayoutParams(linearParam2);

        }
        return view;
    }

    private void showMergeButton(boolean show){
        if(show){
            mBkavRecordAndAdd.setVisibility(View.GONE);
            mBkavRecordAndMerge.setVisibility(View.VISIBLE);
        }else{
            mBkavRecordAndMerge.setVisibility(View.GONE);
            mBkavRecordAndAdd.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setSecondary(@NonNull SecondaryInfo secondaryInfo) {
        if(mSecondaryInfo == secondaryInfo){
            return;
        }
        mSecondaryInfo = null;
        if(secondaryInfo != null && secondaryInfo.shouldShow()){
            mSecondaryView.setVisibility(View.VISIBLE);
            mSecondaryName.setText(secondaryInfo.name());
            showMergeButton(true);
        }else {
            mSecondaryView.setVisibility(View.GONE);
            showMergeButton(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateStateBluetooth();
        if (mRecordControl != null) {
            if (mRecordControl.isStartRecord()) {
                //Bkav DucLQ truong hop auto ghi am
                InCallActivity incall = (InCallActivity)getActivity();
                if(incall != null) {
                    incall.checkPermissionStorage();
                }
                updateStateRecord(true);
            } else {
                updateStateRecord(false);
            }
        }else {
            updateStateRecord(false);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.record_and_add_button:
                clickRecordAndAddCall();
                break;
            case R.id.record_and_merge_button:
                clickRecordAndMergeCall();
                break;
            case R.id.dialpadButtonBkav:
                final Button buttonDialpad = (Button) view;
                inCallButtonUiDelegate.showDialpadClicked(!buttonDialpad.isSelected());
                break;
            case R.id.videoButtonBkav:
                inCallButtonUiDelegate.changeToVideoClicked();
                break;
            case R.id.holdButtonBkav:
                final Button buttonHold = (Button) view;
                inCallButtonUiDelegate.holdClicked(!buttonHold.isSelected());
                break;
            case R.id.muteButtonBkav:
                final Button button = (Button) view;
                inCallButtonUiDelegate.muteClicked(!button.isSelected(), true);
                setMute(!button.isSelected());
                break;
            case R.id.speakerButtonBkav:
                onSpeakerButtonClicked();
                break;
            case R.id.bluetoothButtonBkav:
                onBluetoothOnclick();
                break;
            case R.id.endButtonBkav:
                onClickEndCall();
                break;
            case R.id.muteAndHoldBkav:
                clickMuteAndHoldCall();
                break;
            default:
                return;

        }
    }



    /**
     * Bkav DucLQ
     */
    private void onBluetoothOnclick(){
        if(inCallButtonUiDelegate == null){
            return;
        }
        CallAudioState audioState = inCallButtonUiDelegate.getCurrentAudioState();
        int currentMode = audioState.getRoute();
        int newMode = 0;
        if (currentMode == CallAudioState.ROUTE_BLUETOOTH) {
            newMode = CallAudioState.ROUTE_WIRED_OR_EARPIECE;
        } else {
            if (0 != (CallAudioState.ROUTE_BLUETOOTH & audioState.getSupportedRouteMask())) {
                newMode = CallAudioState.ROUTE_BLUETOOTH;
            }
        }
        inCallButtonUiDelegate.setAudioRoute(newMode);
    }

    private void updateStateBluetooth(){
        if(inCallButtonUiDelegate == null){
            return;
        }
        CallAudioState audioState = inCallButtonUiDelegate.getCurrentAudioState();
        if(audioState != null) {
            if ((audioState.getSupportedRouteMask() & CallAudioState.ROUTE_BLUETOOTH)
                    == CallAudioState.ROUTE_BLUETOOTH) {
                boolean isBlueTooth = ((audioState.getRoute() & CallAudioState.ROUTE_BLUETOOTH)
                        == CallAudioState.ROUTE_BLUETOOTH);
                setBackgroundColorView(mBkavBluetoothButton, isBlueTooth);
                int resId = isBlueTooth ? R.drawable.bkav_ic_blutouch_call_control_orange : R.drawable.bkav_ic_blutouch_call_control;
                mBkavBluetoothButton.setCompoundDrawablesWithIntrinsicBounds(0, resId, 0, 0);
            }
        }
    }


    /**
     * Bkav DucLQ
     * thuc hien khi click vao nut bat tat loa ngoai
     */
    private void onSpeakerButtonClicked() {
        inCallButtonUiDelegate.toggleSpeakerphone();
    }


    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        mPhoto = (AnimationInCall) view.findViewById(R.id.bkav_photo);
        mPhoto.setShouldDraw(true);
        inCallButtonUiDelegate.onInCallButtonUiReady(getInCallButtonUi());
    }

    @Override
    public void setHold(boolean value) {
        if (mBkavHoldButton.isSelected() != value) {
            setBackgroundColorView(mBkavHoldButton, value);
            mBkavHoldButton.setText(value ? R.string.incall_content_description_unhold
                    : R.string.incall_content_description_hold);
            int resId = value ?
                    R.drawable.bkav_ic_hold_call_control_orange :
                    R.drawable.bkav_ic_hold_call_control;
            mBkavHoldButton.setCompoundDrawablesWithIntrinsicBounds(0, resId, 0, 0);
        }
        updateStateMuteAndHold();//Bkav DucLQ
    }

    public void setMute(boolean value) {
        if (mBkavMuteButton.isSelected() != value) {
            setBackgroundColorView(mBkavMuteButton, value);
            mBkavMuteButton.setText(value
                    ?
                    R.string.incall_content_description_muted :
                    R.string.incall_content_description_unmuted);
            int resId = value ?
                    R.drawable.bkav_ic_mute_call_control_orange :
                    R.drawable.bkav_ic_mute_call_control;
            mBkavMuteButton.setCompoundDrawablesWithIntrinsicBounds(0, resId, 0, 0);
        }
    }


    /**
     * Bkav QuyetDV: Set mau nen cua view tuy theo trang thai selected
     *
     * @param view
     * @param value
     */
    private void setBackgroundColorView(View view, boolean value) {
        view.setSelected(value);
        view.setBackgroundResource(
                value ? R.drawable.bkav_btn_bg_select : R.drawable.bkav_btn_bg_unselect);
    }

    /**
     * Bkav DucLQ an hien giao dien button
     */
    /*@Override
    public void showCallButtonUi(boolean show) {
        getView().setVisibility(show ? View.VISIBLE : View.GONE);
        super.showCallButtonUi(show);
    }*/

    @Override
    public void setAudioState(CallAudioState audioState) {
        super.setAudioState(audioState);
        SpeakerButtonInfo info = new SpeakerButtonInfo(audioState, SpeakerButtonInfo.IconSize.SIZE_36_DP);
        updateStateSpeakerButton(info.isChecked);
        updateStateBluetooth();
    }

    private void updateStateRecord(boolean recording) {
        mBkavRecordAndAdd.setCompoundDrawablesWithIntrinsicBounds(0,
                recording ? R.drawable.ic_recording_and_add : R.drawable.icon_record_addcall, 0, 0);
        mBkavRecordAndMerge.setCompoundDrawablesWithIntrinsicBounds(0,
                recording ? R.drawable.ic_recording_and_merge : R.drawable.icon_record_merge, 0, 0);
        mTextViewRecord = recording ? getResources().getString(R.string.stop_record) : getResources().getString(R.string.start_record);
    }

    protected void updateStateSpeakerButton(boolean select) {
        setBackgroundColorView(mBkavSpeakerButton, select);
        int resId = select ? R.drawable.bkav_ic_speaker_call_control_orange : R.drawable.bkav_ic_speaker_call_control;
        mBkavSpeakerButton.setCompoundDrawablesWithIntrinsicBounds(0, resId, 0, 0);
    }

    private void clickRecordAndAddCall() {
        //Bkav DucLQ neu ma dang co 1 popup mo va (mOpenDeepShortcut = true)
        // ==> khong phai popup nay dang mo thi khong lam gi
        if (mIdShortCutOpen == ID_SHORTCUT_OPENED && mOpenDeepShortcut) {
            return;
        }
        if (mOpenDeepShortcut) {
            List<MenuItemInfo> items = new ArrayList<MenuItemInfo>();
            items.add(new MenuItemInfo(
                    mTextViewRecord == null ? getResources().getString(R.string.recording_call)
                            : mTextViewRecord,
                    R.drawable.ic_record_shortcut, mCallButtonOnClick));
            items.add(new MenuItemInfo(getResources().getString(R.string.incall_label_add_call),
                    R.drawable.ic_addcall_shortcut, mCallButtonOnClick));
            mDeepShortcutsContainer = DeepShortcutsContainer.showForIcon(mBkavRecordAndAdd,
                    mViewBottomFragment, items, false);
            mIdShortCutOpen = ID_SHORTCUT_OPENED;
        } else {
            mIdShortCutOpen = ID_SHORTCUT_CLOSED;
            mDeepShortcutsContainer.animateClose();
        }
        mOpenDeepShortcut = !mOpenDeepShortcut;
    }

    private void closePopupDeepshortcut() {
        mOpenDeepShortcut = true;
        mOpenDeepShortcutMerge = true;
        mOpenDeepShortcutMuteAndHold = true;
        mIdShortCutOpen = ID_SHORTCUT_CLOSED;
        if(mDeepShortcutsContainer != null) {
            mDeepShortcutsContainer.animateClose();
        }
    }

    @Override
    public void onButtonGridDestroyed() {
        inCallButtonUiDelegate.onInCallButtonUiUnready();
    }

    private void clickRecordAndMergeCall() {
        //Bkav DucLQ neu ma dang co 1 popup mo va (mOpenDeepShortcutMerge = true)
        // ==> khong phai popup nay dang mo thi khong lam gi
        if (mIdShortCutOpen == ID_SHORTCUT_OPENED && mOpenDeepShortcutMerge) {
            return;
        }
        if (mOpenDeepShortcutMerge) {
            List<MenuItemInfo> items = new ArrayList<MenuItemInfo>();
            items.add(new MenuItemInfo(
                    mTextViewRecord == null ? getResources().getString(R.string.recording_call)
                            : mTextViewRecord,
                    R.drawable.ic_record_shortcut, mCallButtonOnClick));
            items.add(new MenuItemInfo(getResources().getString(R.string.incall_label_merge),
                    R.drawable.ic_merge_shortcut, mCallButtonOnClick));
            mDeepShortcutsContainer = DeepShortcutsContainer.showForIcon(mBkavRecordAndMerge,
                    mViewBottomFragment, items, false);
            mIdShortCutOpen = ID_SHORTCUT_OPENED;
        } else {
            mIdShortCutOpen = ID_SHORTCUT_CLOSED;
            mDeepShortcutsContainer.animateClose();
        }
        mOpenDeepShortcutMerge = !mOpenDeepShortcutMerge;
    }

    private void clickMuteAndHoldCall() {
        //Bkav DucLQ neu ma dang co 1 popup mo va (mOpenDeepShortcutMuteAndHold = true)
        // ==> khong phai popup nay dang mo thi khong lam gi
        if (mIdShortCutOpen == ID_SHORTCUT_OPENED && mOpenDeepShortcutMuteAndHold) {
            return;
        }

        if (mOpenDeepShortcutMuteAndHold) {
            List<MenuItemInfo> items = new ArrayList<MenuItemInfo>();
            items.add(new MenuItemInfo(
                    mTextViewHold == null ?
                            getResources().getString(R.string.incall_content_description_hold)
                            :
                            mTextViewHold,
                    R.drawable.hold_ic_popup, mCallButtonOnClick));
            items.add(new MenuItemInfo(mTextViewMute == null ?
                    getResources().getString(R.string.incall_content_description_unmuted) :
                    mTextViewMute,
                    R.drawable.mute_ic_popup, mCallButtonOnClick));
            mDeepShortcutsContainer = DeepShortcutsContainer.showForIcon(mBkavMuteAndHoldCall,
                    mViewBottomFragment, items, true);
            mIdShortCutOpen = ID_SHORTCUT_OPENED;
        } else {
            mIdShortCutOpen = ID_SHORTCUT_CLOSED;
            mDeepShortcutsContainer.animateClose();
        }
        mOpenDeepShortcutMuteAndHold = !mOpenDeepShortcutMuteAndHold;
    }

    public void callbackCheckPermisstio(boolean startRecord) {
        if(mRecordControl == null) {
            return;
        }
        if(startRecord) {
            mRecordControl.startRecordCall(false);
        }else {
            mRecordControl.stopRecordCall();
        }
        updateStateRecord(startRecord);
    }

    @Override
    public void displayAvatarWhenCallStateChanged(boolean isStateChanged) {
        if(mPhoto != null){
            mPhoto.setIsCallStateChanged(isStateChanged);
        }
    }

    @Override
    public void updateAvatar(Drawable avatar) {
        if (mPhoto != null) {
            Context context = getContext();
            if (context != null) {
                if (avatar != null) {
                    mPhoto.loadAvatarBitmap(BkavBitmapUtils.drawableToBitmap(avatar), true);
                    if ((avatar != BkavBitmapUtils.getDefaultContactPhotoDrawable(getContext()))
                            && (mPhotoCache != avatar)) {
                        if (BkavBitmapUtils
                                .setBackgroundCallCard(mViewIncall, avatar, context)) {
                            mPhotoCache = avatar;
                        }
                    }
                } else {
                    mPhoto.loadAvatarBitmap(BkavBitmapUtils.drawableToBitmap(
                            BkavBitmapUtils.getDefaultContactPhotoDrawable(getContext())), true);
                }
            }
        }
    }

    @Override
    public void updateAvatarConference() {
        if(mPhoto != null){
            if(mViewIncall != null){
                mViewIncall.setBackgroundColor(R.color.transparent);
            }
            mPhoto.loadAvatarBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.avatar_group_incall_bkav), true);
        }
    }

    @Override
    public void callbackCheckPermisstion(boolean startRecord) {
        if(mRecordControl == null) {
            return;
        }
        if(startRecord) {
            mRecordControl.startRecordCall(false);
        }else {
            mRecordControl.stopRecordCall();
        }
        updateStateRecord(startRecord);
    }

    @Override
    public void showManageConferenceCallButton(boolean visible) {
        mShouldShowManageConference = visible;
        super.showManageConferenceCallButton(visible);
    }

    private boolean isBphone2() {
        String model = Funtions.getSystemProperty("ro.product.device", "unknown");
        return "B2017".equals(model);
    }

    private void updateStateMuteAndHold() {
        boolean isHold = (mStateMuteAndHold & STATE_HOLD) != 0;
        boolean isMute = (mStateMuteAndHold & STATE_MUTE) != 0;
        if (isHold && isMute) {
            // dang hold va dang mute
            mBkavMuteAndHoldCall
                    .setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.resume_mute, 0, 0);
        } else if (isHold && !isMute) {
            //dang hold va khong mute
            mBkavMuteAndHoldCall
                    .setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.resume_unmute, 0, 0);
        } else if (!isHold && isMute) {
            //khong hold va dang mute
            mBkavMuteAndHoldCall
                    .setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.hold_mute, 0, 0);
        } else {
            // khong trang thai nao
            mBkavMuteAndHoldCall
                    .setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.hold_unmute, 0, 0);
        }
        Context context = getContext();
        if(context != null) {
            mTextViewHold = getResources()
                    .getString(isHold ? R.string.incall_content_description_unhold
                            : R.string.incall_content_description_hold);
            mTextViewMute = getResources().getString(isMute
                    ?
                    R.string.incall_content_description_muted :
                    R.string.incall_content_description_unmuted);
        }
    }

    //Bkav DucLQ neu khong ho tro video thi khong hien thi nut video ra nua
    private void updateViewUpgradeToVideo(boolean isUpgrade) {
        if (mBkavMuteButton == null || mBkavHoldButton == null || mBkavMuteAndHoldCall == null ||
                mBkavVideoButton == null) {
            return;
        }
        if (isUpgrade) {
            mBkavMuteButton.setVisibility(View.GONE);
            mBkavHoldButton.setVisibility(View.GONE);
            mBkavMuteAndHoldCall.setVisibility(View.VISIBLE);
            mBkavVideoButton.setVisibility(View.VISIBLE);
        } else {
            mBkavMuteAndHoldCall.setVisibility(View.GONE);
            mBkavVideoButton.setVisibility(View.GONE);
            mBkavMuteButton.setVisibility(View.VISIBLE);
            mBkavHoldButton.setVisibility(View.VISIBLE);
        }
    }
}
package com.android.incallui.customizebkav.incall;

        import android.Manifest;
        import android.content.Context;
        import android.graphics.BitmapFactory;
        import android.graphics.drawable.Drawable;
        import android.os.Bundle;
        import android.provider.Settings;
        import android.support.annotation.NonNull;
        import android.telecom.CallAudioState;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.Button;

        import android.widget.LinearLayout;
        import android.widget.RelativeLayout;
        import android.widget.TextView;
        import com.android.dialer.common.FragmentUtils;
        import com.android.dialer.common.LogUtil;
        import com.android.incallui.ContactInfoCache;
        import com.android.incallui.InCallActivity;
        import com.android.incallui.InCallPresenter;
        import com.android.incallui.call.DialerCall;
        import com.android.incallui.contactgrid.ContactGridManager;
        import com.android.incallui.customizebkav.Funtions;
        import com.android.incallui.customizebkav.incall.shortcuts.DeepShortcutsContainer;
        import com.android.incallui.customizebkav.incall.shortcuts.MenuItemInfo;
        import com.android.incallui.customizebkav.utils.BkavBitmapUtils;
        import com.android.incallui.customizebkav.widget.AnimationInCall;
        import com.android.incallui.customizebkav.widget.Setting;
        import com.android.incallui.incall.impl.InCallFragment;
        import com.android.incallui.R;
        import com.android.incallui.incall.protocol.InCallButtonIds;
        import com.android.incallui.incall.protocol.InCallButtonUi;
        import com.android.incallui.incall.protocol.InCallButtonUiDelegateFactory;
        import com.android.incallui.incall.protocol.InCallScreenDelegateFactory;
        import com.android.incallui.incall.protocol.PrimaryCallState;
        import com.android.incallui.incall.protocol.SecondaryInfo;
        import com.android.incallui.speakerbuttonlogic.SpeakerButtonInfo;

        import java.util.ArrayList;
        import java.util.List;

/**
 * Created by duclq on 04/05/2018.
 */

public class BkavIncallFragment  extends InCallFragment implements View.OnClickListener, ContactGridManager.CallbackUpdateInfo,
        CallbackCheckPermisstionIncall{

    private Button mBkavDialpadButton;

    private Button mBkavVideoButton;

    private Button mBkavHoldButton;

    private Button mBkavMuteButton;

    private Button mBkavBluetoothButton;

    private Button mBkavSpeakerButton;

    private Button mBkavEndButton;

    private Button mBkavRecordAndAdd;
    private Button mBkavRecordAndMerge;
    private Button mBkavMuteAndHoldCall;
    private boolean mOpenDeepShortcutMuteAndHold;
    private boolean mOpenDeepShortcut;
    private boolean mOpenDeepShortcutMerge;
    private ViewGroup mViewBottomFragment;
    private DeepShortcutsContainer mDeepShortcutsContainer;
    private BkavRecordCallControl mRecordControl;
    private String mTextViewRecord;
    private String mTextViewHold;
    private String mTextViewMute;
    private CallButtonOnClick mCallButtonOnClick;
    private View mViewIncall;
    private View mSecondaryView;
    private TextView mSecondaryName;
    private SecondaryInfo mSecondaryInfo;
    private LinearLayout mBkavSecondaryCallWrapper;
    private boolean mShouldShowManageConference = false;
    private RelativeLayout mBkavAvatarContainer;
    private static final int ID_SHORTCUT_CLOSED = -1;
    private static final int ID_SHORTCUT_OPENED = 1;
    private int mIdShortCutOpen = ID_SHORTCUT_CLOSED;
    private static final int STATE_MUTE = 1 << 1;// dang mu
    private static final int STATE_HOLD = 1 << 2;// dang hold
    private int mStateMuteAndHold = 0;
    private boolean canAddCall = false;

    private LinearLayout mThreeRowInfoView;

    private LinearLayout mTwoRowInfoView;
    public static final String[] STORAGE_PERMISSION = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public static final int EXTERNAL_STORAGE_PERMISSIONS_REQUEST = 1;


    public interface CallButtonOnClick{
        void addCallOnClick();
        void mergerCallOnClick();
        void recordCallOnClick();
        void holdCallOnclick();
        void muteCallOnclick();
    }

    private AnimationInCall mPhoto;
    private Drawable mPhotoCache;

    @Override
    protected int getLayoutFragIncall() {
        return R.layout.frag_incall_voice_bkav;
    }

    @Override
    public void enableButton(int buttonId, boolean enable) {
        super.enableButton(buttonId, enable);
        if(buttonId == InCallButtonIds.BUTTON_ADD_CALL) {
            canAddCall = enable;
        }
    }

    @Override
    public void canShowModifyVideoCall(boolean show) {
        super.canShowModifyVideoCall(show);
        //Bkav DucLQ hien thi button video theo logic android goc
        updateViewUpgradeToVideo(show);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View view =  super.onCreateView(layoutInflater, viewGroup, bundle);
        inCallButtonUiDelegate =
                FragmentUtils.getParent(this, InCallButtonUiDelegateFactory.class)
                        .newInCallButtonUiDelegate();
        mViewIncall = (View)view.findViewById(R.id.incall_view_bkav);
        mBkavDialpadButton = (Button) view.findViewById(R.id.dialpadButtonBkav);
        mBkavDialpadButton.setOnClickListener(this);
        mBkavMuteButton = (Button) view.findViewById(R.id.muteButtonBkav);
        mBkavMuteButton.setOnClickListener(this);
        mBkavBluetoothButton = (Button) view.findViewById(R.id.bluetoothButtonBkav);
        mBkavBluetoothButton.setOnClickListener(this);
        mBkavSpeakerButton = (Button) view.findViewById(R.id.speakerButtonBkav);
        mBkavSpeakerButton.setOnClickListener(this);
        mBkavHoldButton = (Button) view.findViewById(R.id.holdButtonBkav);
        mBkavHoldButton.setOnClickListener(this);
        mBkavVideoButton = (Button) view.findViewById(R.id.videoButtonBkav);
        mBkavVideoButton.setOnClickListener(this);
        mBkavEndButton = (Button) view.findViewById(R.id.endButtonBkav);
        mBkavEndButton.setOnClickListener(this);
        mBkavRecordAndAdd = (Button) view.findViewById(R.id.record_and_add_button);
        mBkavRecordAndAdd.setOnClickListener(this);
        mBkavRecordAndMerge = (Button) view.findViewById(R.id.record_and_merge_button);
        mBkavRecordAndMerge.setOnClickListener(this);
        mBkavMuteAndHoldCall = (Button) view.findViewById(R.id.muteAndHoldBkav);
        mBkavMuteAndHoldCall.setOnClickListener(this);
        mSecondaryView = (View) view.findViewById(R.id.bkav_secondary_call_info);
        mSecondaryName = (TextView) view.findViewById(R.id.bkav_secondary_call_name);
        mBkavSecondaryCallWrapper = (LinearLayout) view
                .findViewById(R.id.bkav_secondary_call_wrapper);
        mBkavSecondaryCallWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSecondaryClick();
            }
        });
        mBkavAvatarContainer = (RelativeLayout) view.findViewById(R.id.bkav_avatar_container);
        mBkavAvatarContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mShouldShowManageConference) {
                    onManageConferenceClicked();
                }
            }
        });
        mOpenDeepShortcut = true;
        mOpenDeepShortcutMerge = true;
        mOpenDeepShortcutMuteAndHold = true;
        mViewBottomFragment = (ViewGroup) view.findViewById(R.id.button_incall_bkav);
        mRecordControl = InCallPresenter.getInstance().getRecordCallControl();
        final InCallActivity incall = (InCallActivity)getActivity();
        if(incall != null) {
            incall.setCallbackCheckPermisstionIncall(this);
        }
        mCallButtonOnClick = new CallButtonOnClick() {
            @Override
            public void recordCallOnClick() {
                closePopupDeepshortcut();
                if (mRecordControl == null) {
                    return;
                }
                if (mRecordControl.isStartRecord()) {
                    updateStateRecord(false);
                    mRecordControl.stopRecordCall();
                } else {
                    updateStateRecord(true);
                    mRecordControl.startRecordCall(false);


                    if(incall != null) {
                        incall.checkPermissionStorage();
                    }
                }
            }

            @Override
            public void holdCallOnclick() {
                closePopupDeepshortcut();
                boolean isHold = (mStateMuteAndHold & STATE_HOLD) != 0;
                if(isHold){
                    mStateMuteAndHold &= ~STATE_HOLD;
                }else{
                    mStateMuteAndHold |= STATE_HOLD;
                }
                inCallButtonUiDelegate.holdClicked(!isHold);
                updateStateMuteAndHold();
            }

            @Override
            public void muteCallOnclick() {
                closePopupDeepshortcut();
                boolean isMute = (mStateMuteAndHold & STATE_MUTE) != 0;
                if(isMute){
                    mStateMuteAndHold &= ~STATE_MUTE;
                }else{
                    mStateMuteAndHold |= STATE_MUTE;
                }
                inCallButtonUiDelegate.muteClicked(!isMute, true);
                updateStateMuteAndHold();
            }

            @Override
            public void mergerCallOnClick() {
                closePopupDeepshortcut();
                inCallButtonUiDelegate.mergeClicked();
            }

            @Override
            public void addCallOnClick() {
                closePopupDeepshortcut();
                if(canAddCall) {
                    inCallButtonUiDelegate.addCallClicked();
                }
            }
        };
        contactGridManager.setCallbackUpdateInfo(this);
        //Bkav DucLQ
        mThreeRowInfoView = (LinearLayout) view.findViewById(R.id.primary_call_banner);
        mTwoRowInfoView = (LinearLayout) view.findViewById(R.id.primary_call_banner_bkav);
        if (Settings.System.getInt(getActivity().getContentResolver(),
                Setting.ENABLE_FULL_INFO_CALL, 0) == 1) {
            mTwoRowInfoView.setVisibility(View.GONE);
            mThreeRowInfoView.setVisibility(View.VISIBLE);
        } else {
            mThreeRowInfoView.setVisibility(View.GONE);
            mTwoRowInfoView.setVisibility(View.VISIBLE);
        }

        if (isBphone2()) {
            int hackMargin = getActivity().getResources().getDimensionPixelSize(R.dimen.hack_incall_layout_margin);
            RelativeLayout.LayoutParams layoutParam = (RelativeLayout.LayoutParams) mBkavAvatarContainer
                    .getLayoutParams();
            layoutParam.setMargins(0, hackMargin, 0, 0);

            mBkavAvatarContainer.setLayoutParams(layoutParam);
            LinearLayout.LayoutParams linearParam = (LinearLayout.LayoutParams) mTwoRowInfoView.getLayoutParams();
            linearParam.setMargins(0, hackMargin, 0, 0);
            mTwoRowInfoView.setLayoutParams(linearParam);

            LinearLayout ll = (LinearLayout) view.findViewById(R.id.callButtonFragment);
            int hackMargin2 = getActivity().getResources().getDimensionPixelSize(R.dimen.hack_padding_top_button);
            LinearLayout.LayoutParams linearParam2 = (LinearLayout.LayoutParams) ll.getLayoutParams();
            linearParam2.setMargins(0, hackMargin2, 0, 0);
            ll.setLayoutParams(linearParam2);

        }
        return view;
    }

    private void showMergeButton(boolean show){
        if(show){
            mBkavRecordAndAdd.setVisibility(View.GONE);
            mBkavRecordAndMerge.setVisibility(View.VISIBLE);
        }else{
            mBkavRecordAndMerge.setVisibility(View.GONE);
            mBkavRecordAndAdd.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setSecondary(@NonNull SecondaryInfo secondaryInfo) {
        if(mSecondaryInfo == secondaryInfo){
            return;
        }
        mSecondaryInfo = null;
        if(secondaryInfo != null && secondaryInfo.shouldShow()){
            mSecondaryView.setVisibility(View.VISIBLE);
            mSecondaryName.setText(secondaryInfo.name());
            showMergeButton(true);
        }else {
            mSecondaryView.setVisibility(View.GONE);
            showMergeButton(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateStateBluetooth();
        if (mRecordControl != null) {
            if (mRecordControl.isStartRecord()) {
                //Bkav DucLQ truong hop auto ghi am
                InCallActivity incall = (InCallActivity)getActivity();
                if(incall != null) {
                    incall.checkPermissionStorage();
                }
                updateStateRecord(true);
            } else {
                updateStateRecord(false);
            }
        }else {
            updateStateRecord(false);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.record_and_add_button:
                clickRecordAndAddCall();
                break;
            case R.id.record_and_merge_button:
                clickRecordAndMergeCall();
                break;
            case R.id.dialpadButtonBkav:
                final Button buttonDialpad = (Button) view;
                inCallButtonUiDelegate.showDialpadClicked(!buttonDialpad.isSelected());
                break;
            case R.id.videoButtonBkav:
                inCallButtonUiDelegate.changeToVideoClicked();
                break;
            case R.id.holdButtonBkav:
                final Button buttonHold = (Button) view;
                inCallButtonUiDelegate.holdClicked(!buttonHold.isSelected());
                break;
            case R.id.muteButtonBkav:
                final Button button = (Button) view;
                inCallButtonUiDelegate.muteClicked(!button.isSelected(), true);
                setMute(!button.isSelected());
                break;
            case R.id.speakerButtonBkav:
                onSpeakerButtonClicked();
                break;
            case R.id.bluetoothButtonBkav:
                onBluetoothOnclick();
                break;
            case R.id.endButtonBkav:
                onClickEndCall();
                break;
            case R.id.muteAndHoldBkav:
                clickMuteAndHoldCall();
                break;
            default:
                return;

        }
    }



    /**
     * Bkav DucLQ
     */
    private void onBluetoothOnclick(){
        if(inCallButtonUiDelegate == null){
            return;
        }
        CallAudioState audioState = inCallButtonUiDelegate.getCurrentAudioState();
        int currentMode = audioState.getRoute();
        int newMode = 0;
        if (currentMode == CallAudioState.ROUTE_BLUETOOTH) {
            newMode = CallAudioState.ROUTE_WIRED_OR_EARPIECE;
        } else {
            if (0 != (CallAudioState.ROUTE_BLUETOOTH & audioState.getSupportedRouteMask())) {
                newMode = CallAudioState.ROUTE_BLUETOOTH;
            }
        }
        inCallButtonUiDelegate.setAudioRoute(newMode);
    }

    private void updateStateBluetooth(){
        if(inCallButtonUiDelegate == null){
            return;
        }
        CallAudioState audioState = inCallButtonUiDelegate.getCurrentAudioState();
        if(audioState != null) {
            if ((audioState.getSupportedRouteMask() & CallAudioState.ROUTE_BLUETOOTH)
                    == CallAudioState.ROUTE_BLUETOOTH) {
                boolean isBlueTooth = ((audioState.getRoute() & CallAudioState.ROUTE_BLUETOOTH)
                        == CallAudioState.ROUTE_BLUETOOTH);
                setBackgroundColorView(mBkavBluetoothButton, isBlueTooth);
                int resId = isBlueTooth ? R.drawable.bkav_ic_blutouch_call_control_orange : R.drawable.bkav_ic_blutouch_call_control;
                mBkavBluetoothButton.setCompoundDrawablesWithIntrinsicBounds(0, resId, 0, 0);
            }
        }
    }


    /**
     * Bkav DucLQ
     * thuc hien khi click vao nut bat tat loa ngoai
     */
    private void onSpeakerButtonClicked() {
        inCallButtonUiDelegate.toggleSpeakerphone();
    }


    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        mPhoto = (AnimationInCall) view.findViewById(R.id.bkav_photo);
        mPhoto.setShouldDraw(true);
        inCallButtonUiDelegate.onInCallButtonUiReady(getInCallButtonUi());
    }

    @Override
    public void setHold(boolean value) {
        if (mBkavHoldButton.isSelected() != value) {
            setBackgroundColorView(mBkavHoldButton, value);
            mBkavHoldButton.setText(value ? R.string.incall_content_description_unhold
                    : R.string.incall_content_description_hold);
            int resId = value ?
                    R.drawable.bkav_ic_hold_call_control_orange :
                    R.drawable.bkav_ic_hold_call_control;
            mBkavHoldButton.setCompoundDrawablesWithIntrinsicBounds(0, resId, 0, 0);
        }
        updateStateMuteAndHold();//Bkav DucLQ
    }

    public void setMute(boolean value) {
        if (mBkavMuteButton.isSelected() != value) {
            setBackgroundColorView(mBkavMuteButton, value);
            mBkavMuteButton.setText(value
                    ?
                    R.string.incall_content_description_muted :
                    R.string.incall_content_description_unmuted);
            int resId = value ?
                    R.drawable.bkav_ic_mute_call_control_orange :
                    R.drawable.bkav_ic_mute_call_control;
            mBkavMuteButton.setCompoundDrawablesWithIntrinsicBounds(0, resId, 0, 0);
        }
    }


    /**
     * Bkav QuyetDV: Set mau nen cua view tuy theo trang thai selected
     *
     * @param view
     * @param value
     */
    private void setBackgroundColorView(View view, boolean value) {
        view.setSelected(value);
        view.setBackgroundResource(
                value ? R.drawable.bkav_btn_bg_select : R.drawable.bkav_btn_bg_unselect);
    }

    /**
     * Bkav DucLQ an hien giao dien button
     */
    /*@Override
    public void showCallButtonUi(boolean show) {
        getView().setVisibility(show ? View.VISIBLE : View.GONE);
        super.showCallButtonUi(show);
    }*/

    @Override
    public void setAudioState(CallAudioState audioState) {
        super.setAudioState(audioState);
        SpeakerButtonInfo info = new SpeakerButtonInfo(audioState, SpeakerButtonInfo.IconSize.SIZE_36_DP);
        updateStateSpeakerButton(info.isChecked);
        updateStateBluetooth();
    }

    private void updateStateRecord(boolean recording) {
        mBkavRecordAndAdd.setCompoundDrawablesWithIntrinsicBounds(0,
                recording ? R.drawable.ic_recording_and_add : R.drawable.icon_record_addcall, 0, 0);
        mBkavRecordAndMerge.setCompoundDrawablesWithIntrinsicBounds(0,
                recording ? R.drawable.ic_recording_and_merge : R.drawable.icon_record_merge, 0, 0);
        mTextViewRecord = recording ? getResources().getString(R.string.stop_record) : getResources().getString(R.string.start_record);
    }

    protected void updateStateSpeakerButton(boolean select) {
        setBackgroundColorView(mBkavSpeakerButton, select);
        int resId = select ? R.drawable.bkav_ic_speaker_call_control_orange : R.drawable.bkav_ic_speaker_call_control;
        mBkavSpeakerButton.setCompoundDrawablesWithIntrinsicBounds(0, resId, 0, 0);
    }

    private void clickRecordAndAddCall() {
        //Bkav DucLQ neu ma dang co 1 popup mo va (mOpenDeepShortcut = true)
        // ==> khong phai popup nay dang mo thi khong lam gi
        if (mIdShortCutOpen == ID_SHORTCUT_OPENED && mOpenDeepShortcut) {
            return;
        }
        if (mOpenDeepShortcut) {
            List<MenuItemInfo> items = new ArrayList<MenuItemInfo>();
            items.add(new MenuItemInfo(
                    mTextViewRecord == null ? getResources().getString(R.string.recording_call)
                            : mTextViewRecord,
                    R.drawable.ic_record_shortcut, mCallButtonOnClick));
            items.add(new MenuItemInfo(getResources().getString(R.string.incall_label_add_call),
                    R.drawable.ic_addcall_shortcut, mCallButtonOnClick));
            mDeepShortcutsContainer = DeepShortcutsContainer.showForIcon(mBkavRecordAndAdd,
                    mViewBottomFragment, items, false);
            mIdShortCutOpen = ID_SHORTCUT_OPENED;
        } else {
            mIdShortCutOpen = ID_SHORTCUT_CLOSED;
            mDeepShortcutsContainer.animateClose();
        }
        mOpenDeepShortcut = !mOpenDeepShortcut;
    }

    private void closePopupDeepshortcut() {
        mOpenDeepShortcut = true;
        mOpenDeepShortcutMerge = true;
        mOpenDeepShortcutMuteAndHold = true;
        mIdShortCutOpen = ID_SHORTCUT_CLOSED;
        if(mDeepShortcutsContainer != null) {
            mDeepShortcutsContainer.animateClose();
        }
    }

    @Override
    public void onButtonGridDestroyed() {
        inCallButtonUiDelegate.onInCallButtonUiUnready();
    }

    private void clickRecordAndMergeCall() {
        //Bkav DucLQ neu ma dang co 1 popup mo va (mOpenDeepShortcutMerge = true)
        // ==> khong phai popup nay dang mo thi khong lam gi
        if (mIdShortCutOpen == ID_SHORTCUT_OPENED && mOpenDeepShortcutMerge) {
            return;
        }
        if (mOpenDeepShortcutMerge) {
            List<MenuItemInfo> items = new ArrayList<MenuItemInfo>();
            items.add(new MenuItemInfo(
                    mTextViewRecord == null ? getResources().getString(R.string.recording_call)
                            : mTextViewRecord,
                    R.drawable.ic_record_shortcut, mCallButtonOnClick));
            items.add(new MenuItemInfo(getResources().getString(R.string.incall_label_merge),
                    R.drawable.ic_merge_shortcut, mCallButtonOnClick));
            mDeepShortcutsContainer = DeepShortcutsContainer.showForIcon(mBkavRecordAndMerge,
                    mViewBottomFragment, items, false);
            mIdShortCutOpen = ID_SHORTCUT_OPENED;
        } else {
            mIdShortCutOpen = ID_SHORTCUT_CLOSED;
            mDeepShortcutsContainer.animateClose();
        }
        mOpenDeepShortcutMerge = !mOpenDeepShortcutMerge;
    }

    private void clickMuteAndHoldCall() {
        //Bkav DucLQ neu ma dang co 1 popup mo va (mOpenDeepShortcutMuteAndHold = true)
        // ==> khong phai popup nay dang mo thi khong lam gi
        if (mIdShortCutOpen == ID_SHORTCUT_OPENED && mOpenDeepShortcutMuteAndHold) {
            return;
        }

        if (mOpenDeepShortcutMuteAndHold) {
            List<MenuItemInfo> items = new ArrayList<MenuItemInfo>();
            items.add(new MenuItemInfo(
                    mTextViewHold == null ?
                            getResources().getString(R.string.incall_content_description_hold)
                            :
                            mTextViewHold,
                    R.drawable.hold_ic_popup, mCallButtonOnClick));
            items.add(new MenuItemInfo(mTextViewMute == null ?
                    getResources().getString(R.string.incall_content_description_unmuted) :
                    mTextViewMute,
                    R.drawable.mute_ic_popup, mCallButtonOnClick));
            mDeepShortcutsContainer = DeepShortcutsContainer.showForIcon(mBkavMuteAndHoldCall,
                    mViewBottomFragment, items, true);
            mIdShortCutOpen = ID_SHORTCUT_OPENED;
        } else {
            mIdShortCutOpen = ID_SHORTCUT_CLOSED;
            mDeepShortcutsContainer.animateClose();
        }
        mOpenDeepShortcutMuteAndHold = !mOpenDeepShortcutMuteAndHold;
    }

    public void callbackCheckPermisstio(boolean startRecord) {
        if(mRecordControl == null) {
            return;
        }
        if(startRecord) {
            mRecordControl.startRecordCall(false);
        }else {
            mRecordControl.stopRecordCall();
        }
        updateStateRecord(startRecord);
    }

    @Override
    public void displayAvatarWhenCallStateChanged(boolean isStateChanged) {
        if(mPhoto != null){
            mPhoto.setIsCallStateChanged(isStateChanged);
        }
    }

    @Override
    public void updateAvatar(Drawable avatar) {
        if (mPhoto != null) {
            Context context = getContext();
            if (context != null) {
                if (avatar != null) {
                    mPhoto.loadAvatarBitmap(BkavBitmapUtils.drawableToBitmap(avatar), true);
                    if ((avatar != BkavBitmapUtils.getDefaultContactPhotoDrawable(getContext()))
                            && (mPhotoCache != avatar)) {
                        if (BkavBitmapUtils
                                .setBackgroundCallCard(mViewIncall, avatar, context)) {
                            mPhotoCache = avatar;
                        }
                    }
                } else {
                    mPhoto.loadAvatarBitmap(BkavBitmapUtils.drawableToBitmap(
                            BkavBitmapUtils.getDefaultContactPhotoDrawable(getContext())), true);
                }
            }
        }
    }

    @Override
    public void updateAvatarConference() {
        if(mPhoto != null){
            if(mViewIncall != null){
                mViewIncall.setBackgroundColor(R.color.transparent);
            }
            mPhoto.loadAvatarBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.avatar_group_incall_bkav), true);
        }
    }

    @Override
    public void callbackCheckPermisstion(boolean startRecord) {
        if(mRecordControl == null) {
            return;
        }
        if(startRecord) {
            mRecordControl.startRecordCall(false);
        }else {
            mRecordControl.stopRecordCall();
        }
        updateStateRecord(startRecord);
    }

    @Override
    public void showManageConferenceCallButton(boolean visible) {
        mShouldShowManageConference = visible;
        super.showManageConferenceCallButton(visible);
    }

    private boolean isBphone2() {
        String model = Funtions.getSystemProperty("ro.product.device", "unknown");
        return "B2017".equals(model);
    }

    private void updateStateMuteAndHold() {
        boolean isHold = (mStateMuteAndHold & STATE_HOLD) != 0;
        boolean isMute = (mStateMuteAndHold & STATE_MUTE) != 0;
        if (isHold && isMute) {
            // dang hold va dang mute
            mBkavMuteAndHoldCall
                    .setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.resume_mute, 0, 0);
        } else if (isHold && !isMute) {
            //dang hold va khong mute
            mBkavMuteAndHoldCall
                    .setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.resume_unmute, 0, 0);
        } else if (!isHold && isMute) {
            //khong hold va dang mute
            mBkavMuteAndHoldCall
                    .setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.hold_mute, 0, 0);
        } else {
            // khong trang thai nao
            mBkavMuteAndHoldCall
                    .setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.hold_unmute, 0, 0);
        }
        Context context = getContext();
        if(context != null) {
            mTextViewHold = getResources()
                    .getString(isHold ? R.string.incall_content_description_unhold
                            : R.string.incall_content_description_hold);
            mTextViewMute = getResources().getString(isMute
                    ?
                    R.string.incall_content_description_muted :
                    R.string.incall_content_description_unmuted);
        }
    }

    //Bkav DucLQ neu khong ho tro video thi khong hien thi nut video ra nua
    private void updateViewUpgradeToVideo(boolean isUpgrade) {
        if (mBkavMuteButton == null || mBkavHoldButton == null || mBkavMuteAndHoldCall == null ||
                mBkavVideoButton == null) {
            return;
        }
        if (isUpgrade) {
            mBkavMuteButton.setVisibility(View.GONE);
            mBkavHoldButton.setVisibility(View.GONE);
            mBkavMuteAndHoldCall.setVisibility(View.VISIBLE);
            mBkavVideoButton.setVisibility(View.VISIBLE);
        } else {
            mBkavMuteAndHoldCall.setVisibility(View.GONE);
            mBkavVideoButton.setVisibility(View.GONE);
            mBkavMuteButton.setVisibility(View.VISIBLE);
            mBkavHoldButton.setVisibility(View.VISIBLE);
        }
    }
}
