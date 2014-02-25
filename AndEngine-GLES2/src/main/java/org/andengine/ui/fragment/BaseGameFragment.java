package org.andengine.ui.fragment;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;

import org.andengine.BuildConfig;
import org.andengine.audio.music.MusicManager;
import org.andengine.audio.sound.SoundManager;
import org.andengine.engine.Engine;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.WakeLockOptions;
import org.andengine.entity.scene.Scene;
import org.andengine.input.sensor.acceleration.AccelerationSensorOptions;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.input.sensor.location.ILocationListener;
import org.andengine.input.sensor.location.LocationSensorOptions;
import org.andengine.input.sensor.orientation.IOrientationListener;
import org.andengine.input.sensor.orientation.OrientationSensorOptions;
import org.andengine.opengl.font.FontManager;
import org.andengine.opengl.shader.ShaderProgramManager;
import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.util.GLState;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.opengl.view.IRendererListener;
import org.andengine.opengl.view.RenderSurfaceView;
import org.andengine.ui.IGameInterface;
import org.andengine.util.ActivityUtils;
import org.andengine.util.Constants;
import org.andengine.util.debug.Debug;
import org.andengine.util.system.SystemUtils;

import java.io.IOException;

/**
 * (c) 2010 Nicolas Gramlich
 * (c) 2011 Zynga Inc.
 *
 * @author Nicolas Gramlich
 * @since 11:27:06 - 08.03.2010
 */
public abstract class BaseGameFragment extends BaseFragment implements IGameInterface, IRendererListener {
    // ===========================================================
    // Constants
    // ===========================================================

    private static final String TAG = BaseGameFragment.class.getSimpleName();

    // ===========================================================
    // Fields
    // ===========================================================

    protected Engine mEngine;

    private WakeLock mWakeLock;

    protected RenderSurfaceView mRenderSurfaceView;

    private boolean mGamePaused;
    private boolean mGameCreated;
    private boolean mCreateGameCalled;
    private boolean mOnReloadResourcesScheduled;

    // ===========================================================
    // Constructors
    // ===========================================================

    @Override
    public void onCreate(final Bundle pSavedInstanceState) {
        if (BuildConfig.DEBUG) {
            Debug.d(TAG + ".onCreate" + " @(Thread: '" + Thread.currentThread().getName() + "')");
        }

        super.onCreate(pSavedInstanceState);

        this.mGamePaused = true;

        this.mEngine = this.onCreateEngine(this.onCreateEngineOptions());
        this.mEngine.startUpdateThread();

        this.applyEngineOptions();

    }

    @Override
    public Engine onCreateEngine(EngineOptions pEngineOptions) {
        pEngineOptions = new EngineOptions(pEngineOptions.isFullscreen(),ScreenOrientation.LANDSCAPE_FIXED,pEngineOptions.getResolutionPolicy(),pEngineOptions.getCamera());
        return new Engine(pEngineOptions);
    }

    @Override
    public synchronized void onSurfaceCreated(final GLState pGLState) {
        if (BuildConfig.DEBUG) {
            Debug.d(TAG + ".onSurfaceCreated" + " @(Thread: '" + Thread.currentThread().getName() + "')");
        }

        if (this.mGameCreated) {
            this.onReloadResources();

            if (this.mGamePaused && this.mGameCreated) {
                this.onResumeGame();
            }
        } else {
            if (this.mCreateGameCalled) {
                this.mOnReloadResourcesScheduled = true;
            } else {
                this.mCreateGameCalled = true;
                this.onCreateGame();
            }
        }
    }

    @Override
    public synchronized void onSurfaceChanged(final GLState pGLState, final int pWidth, final int pHeight) {
        if (BuildConfig.DEBUG) {
            Debug.d(TAG + ".onSurfaceChanged(Width=" + pWidth + ", Height=" + pHeight + ")" + " @(Thread: '" + Thread.currentThread().getName() + "')");
        }
    }

    protected synchronized void onCreateGame() {
        if (BuildConfig.DEBUG) {
            Debug.d(TAG + ".onCreateGame" + " @(Thread: '" + Thread.currentThread().getName() + "')");
        }

        final OnPopulateSceneCallback onPopulateSceneCallback = new OnPopulateSceneCallback() {
            @Override
            public void onPopulateSceneFinished() {
                try {
                    if (BuildConfig.DEBUG) {
                        Debug.d(BaseGameFragment.TAG + ".onGameCreated" + " @(Thread: '" + Thread.currentThread().getName() + "')");
                    }

                    BaseGameFragment.this.onGameCreated();
                } catch(final Throwable pThrowable) {
                    Debug.e(BaseGameFragment.TAG + ".onGameCreated failed." + " @(Thread: '" + Thread.currentThread().getName() + "')", pThrowable);
                }

                BaseGameFragment.this.callGameResumedOnUIThread();
            }
        };

        final OnCreateSceneCallback onCreateSceneCallback = new OnCreateSceneCallback() {
            @Override
            public void onCreateSceneFinished(final Scene pScene) {
                BaseGameFragment.this.mEngine.setScene(pScene);

                try {
                    if (BuildConfig.DEBUG) {
                        Debug.d(BaseGameFragment.TAG + ".onPopulateScene" + " @(Thread: '" + Thread.currentThread().getName() + "')");
                    }

                    BaseGameFragment.this.onPopulateScene(pScene, onPopulateSceneCallback);
                } catch(final Throwable pThrowable) {
                    Debug.e(BaseGameFragment.TAG + ".onPopulateScene failed." + " @(Thread: '" + Thread.currentThread().getName() + "')", pThrowable);
                }
            }
        };

        final OnCreateResourcesCallback onCreateResourcesCallback = new OnCreateResourcesCallback() {
            @Override
            public void onCreateResourcesFinished() {
                try {
                    if (BuildConfig.DEBUG) {
                        Debug.d(BaseGameFragment.TAG + ".onCreateScene" + " @(Thread: '" + Thread.currentThread().getName() + "')");
                    }

                    BaseGameFragment.this.onCreateScene(onCreateSceneCallback);
                } catch(final Throwable pThrowable) {
                    Debug.e(BaseGameFragment.TAG + ".onCreateScene failed." + " @(Thread: '" + Thread.currentThread().getName() + "')", pThrowable);
                }
            }
        };

        try {
            if (BuildConfig.DEBUG) {
                Debug.d(TAG + ".onCreateResources" + " @(Thread: '" + Thread.currentThread().getName() + "')");
            }

            this.onCreateResources(onCreateResourcesCallback);
        } catch(final Throwable pThrowable) {
            Debug.e(TAG + ".onCreateGame failed." + " @(Thread: '" + Thread.currentThread().getName() + "')", pThrowable);
        }
    }

    @Override
    public synchronized void onGameCreated() {
        this.mGameCreated = true;

		/* Since the potential asynchronous resource creation,
		 * the surface might already be invalid
		 * and a resource reloading might be necessary. */
        if (this.mOnReloadResourcesScheduled) {
            this.mOnReloadResourcesScheduled = false;
            try {
                this.onReloadResources();
            } catch(final Throwable pThrowable) {
                Debug.e(TAG + ".onReloadResources failed." + " @(Thread: '" + Thread.currentThread().getName() + "')", pThrowable);
            }
        }
    }

    @Override
    public synchronized void onResume() {
        if (BuildConfig.DEBUG) {
            Debug.d(TAG + ".onResume" + " @(Thread: '" + Thread.currentThread().getName() + "')");
        }

        super.onResume();

        this.acquireWakeLock();
        this.mRenderSurfaceView.onResume();
    }

    @Override
    public synchronized void onResumeGame() {
        if (BuildConfig.DEBUG) {
            Debug.d(TAG + ".onResumeGame" + " @(Thread: '" + Thread.currentThread().getName() + "')");
        }

        this.mEngine.start();

        this.mGamePaused = false;
    }

    /*
    @Override
    public synchronized void onWindowFocusChanged(final boolean pHasWindowFocus) {
        super.onWindowFocusChanged(pHasWindowFocus);

        if (pHasWindowFocus && this.mGamePaused && this.mGameCreated) {
            this.onResumeGame();
        }
    }
    */

    @Override
    public void onReloadResources() {
        if (BuildConfig.DEBUG) {
            Debug.d(TAG + ".onReloadResources" + " @(Thread: '" + Thread.currentThread().getName() + "')");
        }

        this.mEngine.onReloadResources();
    }

    @Override
    public void onPause() {
        if (BuildConfig.DEBUG) {
            Debug.d(TAG + ".onPause" + " @(Thread: '" + Thread.currentThread().getName() + "')");
        }

        super.onPause();

        this.mRenderSurfaceView.onPause();
        this.releaseWakeLock();

        if (!this.mGamePaused) {
            this.onPauseGame();
        }
    }

    @Override
    public synchronized void onPauseGame() {
        if (BuildConfig.DEBUG) {
            Debug.d(TAG + ".onPauseGame" + " @(Thread: '" + Thread.currentThread().getName() + "')");
        }

        this.mGamePaused = true;

        this.mEngine.stop();
    }

    @Override
    public void onDestroy() {
        if (BuildConfig.DEBUG) {
            Debug.d(TAG + ".onDestroy" + " @(Thread: '" + Thread.currentThread().getName() + "')");
        }

        super.onDestroy();

        this.mEngine.onDestroy();

        try {
            this.onDestroyResources();
        } catch (final Throwable pThrowable) {
            Debug.e(TAG + ".onDestroyResources failed." + " @(Thread: '" + Thread.currentThread().getName() + "')", pThrowable);
        }

        this.onGameDestroyed();

        this.mEngine = null;
    }

    @Override
    public void onDestroyResources() throws IOException {
        if (BuildConfig.DEBUG) {
            Debug.d(TAG + ".onDestroyResources" + " @(Thread: '" + Thread.currentThread().getName() + "')");
        }

        if (this.mEngine.getEngineOptions().getAudioOptions().needsMusic()) {
            this.getMusicManager().releaseAll();
        }

        if (this.mEngine.getEngineOptions().getAudioOptions().needsSound()) {
            this.getSoundManager().releaseAll();
        }
    }

    @Override
    public synchronized void onGameDestroyed() {
        if (BuildConfig.DEBUG) {
            Debug.d(TAG + ".onGameDestroyed" + " @(Thread: '" + Thread.currentThread().getName() + "')");
        }

        this.mGameCreated = false;
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    public Engine getEngine() {
        return this.mEngine;
    }

    public boolean isGamePaused() {
        return this.mGamePaused;
    }

    public boolean isGameRunning() {
        return !this.mGamePaused;
    }

    public boolean isGameLoaded() {
        return this.mGameCreated;
    }

    public VertexBufferObjectManager getVertexBufferObjectManager() {
        return this.mEngine.getVertexBufferObjectManager();
    }

    public TextureManager getTextureManager() {
        return this.mEngine.getTextureManager();
    }

    public FontManager getFontManager() {
        return this.mEngine.getFontManager();
    }

    public ShaderProgramManager getShaderProgramManager() {
        return this.mEngine.getShaderProgramManager();
    }

    public SoundManager getSoundManager() {
        return this.mEngine.getSoundManager();
    }

    public MusicManager getMusicManager() {
        return this.mEngine.getMusicManager();
    }

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================

    private void callGameResumedOnUIThread() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!getActivity().isFinishing()) {
                    BaseGameFragment.this.onResumeGame();
                }
            }
        });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mRenderSurfaceView = new RenderSurfaceView(getActivity());
        this.mRenderSurfaceView.setRenderer(this.mEngine, this);
        this.mRenderSurfaceView.setLayoutParams(BaseGameFragment.createSurfaceViewLayoutParams());

        return mRenderSurfaceView;
    }

    /**
     * @see Engine#runOnUpdateThread(Runnable)
     */
    public void runOnUpdateThread(final Runnable pRunnable) {
        this.mEngine.runOnUpdateThread(pRunnable);
    }

    /**
     * @see Engine#runOnUpdateThread(Runnable, boolean)
     */
    public void runOnUpdateThread(final Runnable pRunnable, final boolean pOnlyWhenEngineRunning) {
        this.mEngine.runOnUpdateThread(pRunnable, pOnlyWhenEngineRunning);
    }

    private void acquireWakeLock() {
        this.acquireWakeLock(this.mEngine.getEngineOptions().getWakeLockOptions());
    }

    private void acquireWakeLock(final WakeLockOptions pWakeLockOptions) {
        if (pWakeLockOptions == WakeLockOptions.SCREEN_ON) {
            ActivityUtils.keepScreenOn(getActivity());
        } else {
            final PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
            this.mWakeLock = pm.newWakeLock(pWakeLockOptions.getFlag() | PowerManager.ON_AFTER_RELEASE, Constants.DEBUGTAG);
            try {
                this.mWakeLock.acquire();
            } catch (final SecurityException pSecurityException) {
                Debug.e("You have to add\n\t<uses-permission android:name=\"android.permission.WAKE_LOCK\"/>\nto your AndroidManifest.xml !", pSecurityException);
            }
        }
    }

    private void releaseWakeLock() {
        if (this.mWakeLock != null && this.mWakeLock.isHeld()) {
            this.mWakeLock.release();
        }
    }

    private void applyEngineOptions() {
        final EngineOptions engineOptions = this.mEngine.getEngineOptions();

        if (engineOptions.isFullscreen()) {
            //ActivityUtils.requestFullscreen(getActivity());
        }

        if (engineOptions.getAudioOptions().needsMusic() || engineOptions.getAudioOptions().needsSound()) {
            getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
        }

        switch (engineOptions.getScreenOrientation()) {
            case LANDSCAPE_FIXED:
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case LANDSCAPE_SENSOR:
                if (SystemUtils.SDK_VERSION_GINGERBREAD_OR_LATER) {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                } else {
                    Debug.w(ScreenOrientation.class.getSimpleName() + "." + ScreenOrientation.LANDSCAPE_SENSOR + " is not supported on this device. Falling back to " + ScreenOrientation.class.getSimpleName() + "." + ScreenOrientation.LANDSCAPE_FIXED);
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
                break;
            case PORTRAIT_FIXED:
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case PORTRAIT_SENSOR:
                if (SystemUtils.SDK_VERSION_GINGERBREAD_OR_LATER) {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                } else {
                    Debug.w(ScreenOrientation.class.getSimpleName() + "." + ScreenOrientation.PORTRAIT_SENSOR + " is not supported on this device. Falling back to " + ScreenOrientation.class.getSimpleName() + "." + ScreenOrientation.PORTRAIT_FIXED);
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
                break;
        }
    }

    protected static LayoutParams createSurfaceViewLayoutParams() {
        final LayoutParams layoutParams = new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;
        return layoutParams;
    }

    protected void enableVibrator() {
        this.mEngine.enableVibrator(getActivity());
    }

    /**
     * @see {@link Engine#enableLocationSensor(Context, ILocationListener, LocationSensorOptions)}
     */
    protected void enableLocationSensor(final ILocationListener pLocationListener, final LocationSensorOptions pLocationSensorOptions) {
        this.mEngine.enableLocationSensor(getActivity(), pLocationListener, pLocationSensorOptions);
    }

    /**
     * @see {@link Engine#disableLocationSensor(Context)}
     */
    protected void disableLocationSensor() {
        this.mEngine.disableLocationSensor(getActivity());
    }

    /**
     * @see {@link Engine#enableAccelerationSensor(Context, IAccelerationListener)}
     */
    protected boolean enableAccelerationSensor(final IAccelerationListener pAccelerationListener) {
        return this.mEngine.enableAccelerationSensor(getActivity(), pAccelerationListener);
    }

    /**
     * @see {@link Engine#enableAccelerationSensor(Context, IAccelerationListener, AccelerationSensorOptions)}
     */
    protected boolean enableAccelerationSensor(final IAccelerationListener pAccelerationListener, final AccelerationSensorOptions pAccelerationSensorOptions) {
        return this.mEngine.enableAccelerationSensor(getActivity(), pAccelerationListener, pAccelerationSensorOptions);
    }

    /**
     * @see {@link Engine#disableAccelerationSensor(Context)}
     */
    protected boolean disableAccelerationSensor() {
        return this.mEngine.disableAccelerationSensor(getActivity());
    }

    /**
     * @see {@link Engine#enableOrientationSensor(Context, IOrientationListener)}
     */
    protected boolean enableOrientationSensor(final IOrientationListener pOrientationListener) {
        return this.mEngine.enableOrientationSensor(getActivity(), pOrientationListener);
    }

    /**
     * @see {@link Engine#enableOrientationSensor(Context, IOrientationListener, OrientationSensorOptions)}
     */
    protected boolean enableOrientationSensor(final IOrientationListener pOrientationListener, final OrientationSensorOptions pLocationSensorOptions) {
        return this.mEngine.enableOrientationSensor(getActivity(), pOrientationListener, pLocationSensorOptions);
    }

    /**
     * @see {@link Engine#disableOrientationSensor(Context)}
     */
    protected boolean disableOrientationSensor() {
        return this.mEngine.disableOrientationSensor(getActivity());
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
