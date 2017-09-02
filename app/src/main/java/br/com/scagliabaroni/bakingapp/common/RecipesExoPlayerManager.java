/*
 * Copyright (c) 2017 Igor Scaglia.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.scagliabaroni.bakingapp.common;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import br.com.scagliabaroni.bakingapp.R;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Responsible to manage video player ExoPlayer library
 */
public class RecipesExoPlayerManager implements ExoPlayer.EventListener {
    private static final String TAG = "BakingAppExoPlayerManager";
    private Context mContext;
    private static MediaSessionCompat sMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;
    private SimpleExoPlayer mSimpleExoPlayer;
    private SimpleExoPlayerView mSimpleExoPlayerView;
    private NotificationManager mNotificationManager;
    private String mNotificationTitle;
    private String mNotificationText;
    private TargetContentIntent mTargetContentIntent;

    public RecipesExoPlayerManager(Context context, SimpleExoPlayerView simpleExoPlayerView) {
        this.mContext = context;
        this.mSimpleExoPlayerView = simpleExoPlayerView;
        // Initialize Media Session
        this.initializeMediaSession();
    }

    /**
     * Initializes the Media Session to be enabled with media buttons, transport controls, callbacks
     * and media controller.
     */
    private void initializeMediaSession() {
        // Create a MediaSessionCompat.
        sMediaSession = new MediaSessionCompat(this.mContext, TAG);
        // Enable callbacks from MediaButtons and TransportControls.
        sMediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        // Do not let MediaButtons restart the player when the app is not visible.
        sMediaSession.setMediaButtonReceiver(null);
        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player.
        mStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY |
                        PlaybackStateCompat.ACTION_PAUSE |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackStateCompat.ACTION_PLAY_PAUSE);
        sMediaSession.setPlaybackState(mStateBuilder.build());
        // MySessionCallback has methods that handle callbacks from a media controller.
        sMediaSession.setCallback(new MySessionCallback());
        // Start the Media Session since the activity is active.
        sMediaSession.setActive(true);
    }

    public void initializePlayer(Uri mediaUri,
                                 String notificationTitle,
                                 String notificationText,
                                 TargetContentIntent targetContentIntent) {

        if (this.mSimpleExoPlayer != null) {
            this.destroy();
            this.initializeMediaSession();
        }
        this.mTargetContentIntent = targetContentIntent;
        this.mNotificationTitle = notificationTitle;
        this.mNotificationText = notificationText;
        // Create an instance of the ExoPlayer.
        TrackSelector trackSelector = new DefaultTrackSelector();
        // LoadControl is not supported anymore
        this.mSimpleExoPlayer = ExoPlayerFactory.newSimpleInstance(this.mContext, trackSelector);
        this.mSimpleExoPlayerView.setPlayer(this.mSimpleExoPlayer);
        // Set the ExoPlayer.EventListener to this activity.
        this.mSimpleExoPlayer.addListener(this);
        // Prepare the MediaSource.
        String userAgent = Util.getUserAgent(this.mContext, this.mContext.getResources()
                .getString(R.string.app_name));
        MediaSource mediaSource = new ExtractorMediaSource(mediaUri,
                new DefaultDataSourceFactory(this.mContext, userAgent),
                new DefaultExtractorsFactory(), null, null);
        this.mSimpleExoPlayer.prepare(mediaSource);
        this.mSimpleExoPlayer.setPlayWhenReady(true);
    }

    /**
     * Shows Media Style notification, with actions that depend on the current MediaSession
     * PlaybackState.
     *
     * @param state The PlaybackState of the MediaSession.
     */
    private void showNotification(PlaybackStateCompat state) {
        int icon;
        String play_pause;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.mContext);

        if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
            icon = R.drawable.exo_controls_pause;
            play_pause = this.mContext.getResources().getString(R.string.pause);
        } else {
            icon = R.drawable.exo_controls_play;
            play_pause = this.mContext.getResources().getString(R.string.play);
        }
        // Create a play/pause action onto notification
        NotificationCompat.Action playPauseAction = new NotificationCompat.Action(icon,
                play_pause,
                MediaButtonReceiver.buildMediaButtonPendingIntent(this.mContext,
                        PlaybackStateCompat.ACTION_PLAY_PAUSE));
        // Create a restart action onto notification
        NotificationCompat.Action restartAction =
                new android.support.v4.app.NotificationCompat.Action(
                        R.drawable.exo_controls_previous,
                        this.mContext.getResources().getString(R.string.restart),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(this.mContext,
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));
        // Create the intent that will start the activity to be opened. This task is delegated
        // by implementations of TargetContentIntent interface
        Intent recipeDetailStepIntent =
                new Intent(this.mContext, this.mTargetContentIntent.getActivityClass());
        recipeDetailStepIntent.putExtras(this.mTargetContentIntent.getExtras());
        // The PendingIntent that holds the true intent
        PendingIntent contentPendingIntent = PendingIntent.getActivity
                (this.mContext, 0, recipeDetailStepIntent, 0);
        // Set the notification by its builder
        builder.setContentTitle(this.mNotificationTitle)
                .setContentText(this.mNotificationText)
                .setContentIntent(contentPendingIntent)
                .setSmallIcon(R.drawable.ic_launcher)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(restartAction)
                .addAction(playPauseAction)
                .setStyle(new NotificationCompat.MediaStyle()
                        .setMediaSession(sMediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1));
        this.mNotificationManager = (NotificationManager) this.mContext
                .getSystemService(NOTIFICATION_SERVICE);
        // Show the notification
        this.mNotificationManager.notify(0, builder.build());
    }

    public void destroy() {
        this.mNotificationManager.cancelAll();
        this.mSimpleExoPlayer.stop();
        this.mSimpleExoPlayer.release();
        this.mSimpleExoPlayer = null;
        sMediaSession.setActive(false);
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        if ((playbackState == ExoPlayer.STATE_READY) && playWhenReady) {
            mStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                    this.mSimpleExoPlayer.getCurrentPosition(), 1f);
        } else if ((playbackState == ExoPlayer.STATE_READY)) {
            mStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                    this.mSimpleExoPlayer.getCurrentPosition(), 1f);
        }
        sMediaSession.setPlaybackState(mStateBuilder.build());
        this.showNotification(mStateBuilder.build());
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    /**
     * Media Session Callbacks, where all external clients control the player.
     */
    private class MySessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            mSimpleExoPlayer.setPlayWhenReady(true);
        }

        @Override
        public void onPause() {
            mSimpleExoPlayer.setPlayWhenReady(false);
        }

        @Override
        public void onSkipToPrevious() {
            mSimpleExoPlayer.seekTo(0);
        }
    }

    /**
     * Broadcast Receiver registered to receive the MEDIA_BUTTON intent coming from clients.
     */
    public static class MediaReceiver extends BroadcastReceiver {

        public MediaReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            MediaButtonReceiver.handleIntent(sMediaSession, intent);
        }
    }
}