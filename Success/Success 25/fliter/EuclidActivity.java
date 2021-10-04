package com.deffe.macros.fliter;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.CycleInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nhaarman.listviewanimations.appearance.ViewAnimator;
import com.nhaarman.listviewanimations.appearance.simple.SwingLeftInAnimationAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

/**
 * Created by Oleksii Shliama on 1/27/15.
 */
public abstract class EuclidActivity extends Activity {

    private static final int REVEAL_ANIMATION_DURATION = 1000;
    private static final int MAX_DELAY_SHOW_DETAILS_ANIMATION = 500;
    private static final int ANIMATION_DURATION_SHOW_PROFILE_DETAILS = 500;
    private static final int STEP_DELAY_HIDE_DETAILS_ANIMATION = 80;
    private static final int ANIMATION_DURATION_CLOSE_PROFILE_DETAILS = 500;
    private static final int ANIMATION_DURATION_SHOW_PROFILE_BUTTON = 300;
    private static final int CIRCLE_RADIUS_DP = 50;

    protected RelativeLayout mWrapper;
    protected ListView mListView;
    protected FrameLayout mToolbar;
    protected RelativeLayout mToolbarProfile;
    protected LinearLayout mProfileDetails;
    protected TextView mTextViewProfileName;
    protected TextView mTextViewProfileDescription;
    protected View mButtonProfile;

    public static ShapeDrawable sOverlayShape;
    static int sScreenWidth;
    static int sProfileImageHeight;

    private SwingLeftInAnimationAdapter mListViewAnimationAdapter;
    private ViewAnimator mListViewAnimator;

    private View mOverlayListItemView;
    private EuclidState mState = EuclidState.Closed;

    private float mInitialProfileButtonX;

    private AnimatorSet mOpenProfileAnimatorSet;
    private AnimatorSet mCloseProfileAnimatorSet;
    private Animation mProfileButtonShowAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mWrapper = (RelativeLayout) findViewById(R.id.wrapper);
        mListView = (ListView) findViewById(R.id.list_view);
        mToolbar = (FrameLayout) findViewById(R.id.toolbar_list);
        mToolbarProfile = (RelativeLayout) findViewById(R.id.toolbar_profile);
        mProfileDetails = (LinearLayout) findViewById(R.id.wrapper_profile_details);
        mTextViewProfileName = (TextView) findViewById(R.id.text_view_profile_name);
        mTextViewProfileDescription = (TextView) findViewById(R.id.text_view_profile_description);
        mButtonProfile = findViewById(R.id.button_profile);
        mButtonProfile.post(new Runnable() {
            @Override
            public void run() {
                mInitialProfileButtonX = mButtonProfile.getX();
            }
        });
        findViewById(R.id.toolbar_profile_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateCloseProfileDetails();
            }
        });

        sScreenWidth = getResources().getDisplayMetrics().widthPixels;
        sProfileImageHeight = getResources().getDimensionPixelSize(R.dimen.height_profile_image);
        sOverlayShape = buildAvatarCircleOverlay();

        initList();
    }

    private void initList() {
        mListViewAnimationAdapter = new SwingLeftInAnimationAdapter(getAdapter());
        mListViewAnimationAdapter.setAbsListView(mListView);
        mListViewAnimator = mListViewAnimationAdapter.getViewAnimator();
        if (mListViewAnimator != null) {
            mListViewAnimator.setAnimationDurationMillis(getAnimationDurationCloseProfileDetails());
            mListViewAnimator.disableAnimations();
        }
        mListView.setAdapter(mListViewAnimationAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mState = EuclidState.Opening;
                showProfileDetails((Map<String, Object>) parent.getItemAtPosition(position), view);
            }
        });
    }


    private void showProfileDetails(Map<String, Object> item, final View view) {
        mListView.setEnabled(false);

        int profileDetailsAnimationDelay = getMaxDelayShowDetailsAnimation() * Math.abs(view.getTop())
                / sScreenWidth;

        addOverlayListItem(item, view);
        startRevealAnimation(profileDetailsAnimationDelay);
        animateOpenProfileDetails(profileDetailsAnimationDelay);
    }


    private void addOverlayListItem(Map<String, Object> item, View view) {
        if (mOverlayListItemView == null) {
            mOverlayListItemView = getLayoutInflater().inflate(R.layout.overlay_list_item, mWrapper, false);
        } else {
            mWrapper.removeView(mOverlayListItemView);
        }

        mOverlayListItemView.findViewById(R.id.view_avatar_overlay).setBackground(sOverlayShape);

        Picasso.with(EuclidActivity.this).load((Integer) item.get(EuclidListAdapter.KEY_AVATAR))
                .resize(sScreenWidth, sProfileImageHeight).centerCrop()
                .placeholder(R.color.blue)
                .into((ImageView) mOverlayListItemView.findViewById(R.id.image_view_reveal_avatar));
        Picasso.with(EuclidActivity.this).load((Integer) item.get(EuclidListAdapter.KEY_AVATAR))
                .resize(sScreenWidth, sProfileImageHeight).centerCrop()
                .placeholder(R.color.blue)
                .into((ImageView) mOverlayListItemView.findViewById(R.id.image_view_avatar));

        ((TextView) mOverlayListItemView.findViewById(R.id.text_view_name)).setText((String) item.get(EuclidListAdapter.KEY_NAME));
        ((TextView) mOverlayListItemView.findViewById(R.id.text_view_description)).setText((String) item.get(EuclidListAdapter.KEY_DESCRIPTION_SHORT));
        setProfileDetailsInfo(item);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = view.getTop() + mToolbar.getHeight();
        params.bottomMargin = -(view.getBottom() - mListView.getHeight());
        mWrapper.addView(mOverlayListItemView, params);
        mToolbar.bringToFront();
    }

    private void setProfileDetailsInfo(Map<String, Object> item) {
        mTextViewProfileName.setText((String) item.get(EuclidListAdapter.KEY_NAME));
        mTextViewProfileDescription.setText((String) item.get(EuclidListAdapter.KEY_DESCRIPTION_FULL));
    }


    private void startRevealAnimation(final int profileDetailsAnimationDelay) {
        mOverlayListItemView.post(new Runnable() {
            @Override
            public void run() {
                getAvatarRevealAnimator().start();
                getAvatarShowAnimator(profileDetailsAnimationDelay).start();
            }
        });
    }
    private SupportAnimator getAvatarRevealAnimator() {
        final LinearLayout mWrapperListItemReveal = (LinearLayout) mOverlayListItemView.findViewById(R.id.wrapper_list_item_reveal);

        int finalRadius = Math.max(mOverlayListItemView.getWidth(), mOverlayListItemView.getHeight());

        final SupportAnimator mRevealAnimator = ViewAnimationUtils.createCircularReveal(
                mWrapperListItemReveal,
                sScreenWidth / 2,
                sProfileImageHeight / 2,
                dpToPx(getCircleRadiusDp() * 2),
                finalRadius);
        mRevealAnimator.setDuration(getRevealAnimationDuration());
        mRevealAnimator.addListener(new SupportAnimator.AnimatorListener() {
            @Override
            public void onAnimationStart() {
                mWrapperListItemReveal.setVisibility(View.VISIBLE);
                mOverlayListItemView.setX(0);
            }

            @Override
            public void onAnimationEnd() {

            }

            @Override
            public void onAnimationCancel() {

            }

            @Override
            public void onAnimationRepeat() {

            }
        });
        return mRevealAnimator;
    }

    private Animator getAvatarShowAnimator(int profileDetailsAnimationDelay) {
        final Animator mAvatarShowAnimator = ObjectAnimator.ofFloat(mOverlayListItemView, View.Y, mOverlayListItemView.getTop(), mToolbarProfile.getBottom());
        mAvatarShowAnimator.setDuration(profileDetailsAnimationDelay + getAnimationDurationShowProfileDetails());
        mAvatarShowAnimator.setInterpolator(new DecelerateInterpolator());
        return mAvatarShowAnimator;
    }


    private void animateOpenProfileDetails(int profileDetailsAnimationDelay) {
        createOpenProfileButtonAnimation();
        getOpenProfileAnimatorSet(profileDetailsAnimationDelay).start();
    }

    private AnimatorSet getOpenProfileAnimatorSet(int profileDetailsAnimationDelay) {
        if (mOpenProfileAnimatorSet == null) {
            List<Animator> profileAnimators = new ArrayList<>();
            profileAnimators.add(getOpenProfileToolbarAnimator());
            profileAnimators.add(getOpenProfileDetailsAnimator());

            mOpenProfileAnimatorSet = new AnimatorSet();
            mOpenProfileAnimatorSet.playTogether(profileAnimators);
            mOpenProfileAnimatorSet.setDuration(getAnimationDurationShowProfileDetails());
        }
        mOpenProfileAnimatorSet.setStartDelay(profileDetailsAnimationDelay);
        mOpenProfileAnimatorSet.setInterpolator(new DecelerateInterpolator());
        return mOpenProfileAnimatorSet;
    }


    private void createOpenProfileButtonAnimation() {
        if (mProfileButtonShowAnimation == null)
        {
            mProfileButtonShowAnimation = AnimationUtils.loadAnimation(this, R.anim.profile_button_scale);
            mProfileButtonShowAnimation.setDuration(getAnimationDurationShowProfileButton());
            mProfileButtonShowAnimation.setInterpolator(new DecelerateInterpolator());
            mProfileButtonShowAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    mButtonProfile.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
    }

    private Animator getOpenProfileToolbarAnimator() {
        Animator mOpenProfileToolbarAnimator = ObjectAnimator.ofFloat(mToolbarProfile, View.Y, -mToolbarProfile.getHeight(), 0);
        mOpenProfileToolbarAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mToolbarProfile.setX(0);
                mToolbarProfile.bringToFront();
                mToolbarProfile.setVisibility(View.VISIBLE);
                mProfileDetails.setX(0);
                mProfileDetails.bringToFront();
                mProfileDetails.setVisibility(View.VISIBLE);

                mButtonProfile.setX(mInitialProfileButtonX);
                mButtonProfile.bringToFront();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mButtonProfile.startAnimation(mProfileButtonShowAnimation);

                mState = EuclidState.Opened;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        return mOpenProfileToolbarAnimator;
    }


    private Animator getOpenProfileDetailsAnimator() {
        Animator mOpenProfileDetailsAnimator = ObjectAnimator.ofFloat(mProfileDetails, View.Y,
                getResources().getDisplayMetrics().heightPixels,
                getResources().getDimensionPixelSize(R.dimen.height_profile_picture_with_toolbar));
        return mOpenProfileDetailsAnimator;
    }


    private void animateCloseProfileDetails() {
        mState = EuclidState.Closing;
        getCloseProfileAnimatorSet().start();
    }

    private AnimatorSet getCloseProfileAnimatorSet() {
        if (mCloseProfileAnimatorSet == null) {
            Animator profileToolbarAnimator = ObjectAnimator.ofFloat(mToolbarProfile, View.X,
                    0, mToolbarProfile.getWidth());

            Animator profilePhotoAnimator = ObjectAnimator.ofFloat(mOverlayListItemView, View.X,
                    0, mOverlayListItemView.getWidth());
            profilePhotoAnimator.setStartDelay(getStepDelayHideDetailsAnimation());

            Animator profileButtonAnimator = ObjectAnimator.ofFloat(mButtonProfile, View.X,
                    mInitialProfileButtonX, mOverlayListItemView.getWidth() + mInitialProfileButtonX);
            profileButtonAnimator.setStartDelay(getStepDelayHideDetailsAnimation() * 2);

            Animator profileDetailsAnimator = ObjectAnimator.ofFloat(mProfileDetails, View.X,
                    0, mToolbarProfile.getWidth());
            profileDetailsAnimator.setStartDelay(getStepDelayHideDetailsAnimation() * 2);

            List<Animator> profileAnimators = new ArrayList<>();
            profileAnimators.add(profileToolbarAnimator);
            profileAnimators.add(profilePhotoAnimator);
            profileAnimators.add(profileButtonAnimator);
            profileAnimators.add(profileDetailsAnimator);

            mCloseProfileAnimatorSet = new AnimatorSet();
            mCloseProfileAnimatorSet.playTogether(profileAnimators);
            mCloseProfileAnimatorSet.setDuration(getAnimationDurationCloseProfileDetails());
            mCloseProfileAnimatorSet.setInterpolator(new AccelerateInterpolator());
            mCloseProfileAnimatorSet.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (mListViewAnimator != null) {
                        mListViewAnimator.reset();
                        mListViewAnimationAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mToolbarProfile.setVisibility(View.INVISIBLE);
                    mButtonProfile.setVisibility(View.INVISIBLE);
                    mProfileDetails.setVisibility(View.INVISIBLE);

                    mListView.setEnabled(true);
                    mListViewAnimator.disableAnimations();

                    mState = EuclidState.Closed;
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }
        return mCloseProfileAnimatorSet;
    }


    private ShapeDrawable buildAvatarCircleOverlay() {
        int radius = 666;
        ShapeDrawable overlay = new ShapeDrawable(new RoundRectShape(null,
                new RectF(
                        sScreenWidth / 2 - dpToPx(getCircleRadiusDp() * 2),
                        sProfileImageHeight / 2 - dpToPx(getCircleRadiusDp() * 2),
                        sScreenWidth / 2 - dpToPx(getCircleRadiusDp() * 2),
                        sProfileImageHeight / 2 - dpToPx(getCircleRadiusDp() * 2)),
                new float[]{radius, radius, radius, radius, radius, radius, radius, radius}));
        overlay.getPaint().setColor(getResources().getColor(R.color.gray));

        return overlay;
    }

    public int dpToPx(int dp) {
        return Math.round((float) dp * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onBackPressed() {
        if (getState() == EuclidState.Opened) {
            animateCloseProfileDetails();
        } else if (getState() == EuclidState.Closed) {
            super.onBackPressed();
        }
    }


    protected abstract BaseAdapter getAdapter();


    public EuclidState getState() {
        return mState;
    }


    protected int getRevealAnimationDuration() {
        return REVEAL_ANIMATION_DURATION;
    }


    protected int getMaxDelayShowDetailsAnimation() {
        return MAX_DELAY_SHOW_DETAILS_ANIMATION;
    }


    protected int getAnimationDurationShowProfileDetails() {
        return ANIMATION_DURATION_SHOW_PROFILE_DETAILS;
    }


    protected int getStepDelayHideDetailsAnimation() {
        return STEP_DELAY_HIDE_DETAILS_ANIMATION;
    }


    protected int getAnimationDurationCloseProfileDetails() {
        return ANIMATION_DURATION_CLOSE_PROFILE_DETAILS;
    }

    protected int getAnimationDurationShowProfileButton() {
        return ANIMATION_DURATION_SHOW_PROFILE_BUTTON;
    }

    protected int getCircleRadiusDp() {
        return CIRCLE_RADIUS_DP;
    }

}
