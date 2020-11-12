package com.example.tjarnav;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.directions.v5.models.LegStep;
import com.mapbox.api.directions.v5.models.RouteLeg;
import com.mapbox.core.constants.Constants;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.utils.PolylineUtils;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigationOptions;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.services.android.navigation.v5.offroute.OffRouteListener;
import com.mapbox.services.android.navigation.v5.route.RouteFetcher;
import com.mapbox.services.android.navigation.v5.route.RouteListener;
import com.mapbox.services.android.navigation.v5.routeprogress.ProgressChangeListener;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;
import com.mapbox.vision.VisionManager;
import com.mapbox.vision.ar.VisionArManager;
import com.mapbox.vision.ar.core.models.ManeuverType;
import com.mapbox.vision.ar.core.models.Route;
import com.mapbox.vision.ar.core.models.RoutePoint;
import com.mapbox.vision.ar.view.gl.VisionArView;
import com.mapbox.vision.mobile.core.interfaces.VisionEventsListener;
import com.mapbox.vision.mobile.core.models.AuthorizationStatus;
import com.mapbox.vision.mobile.core.models.Camera;
import com.mapbox.vision.mobile.core.models.Country;
import com.mapbox.vision.mobile.core.models.FrameSegmentation;
import com.mapbox.vision.mobile.core.models.classification.FrameSignClassifications;
import com.mapbox.vision.mobile.core.models.detection.FrameDetections;
import com.mapbox.vision.mobile.core.models.position.GeoCoordinate;
import com.mapbox.vision.mobile.core.models.position.VehicleState;
import com.mapbox.vision.mobile.core.models.road.RoadDescription;
import com.mapbox.vision.mobile.core.models.world.WorldDescription;
import com.mapbox.vision.performance.ModelPerformance;
import com.mapbox.vision.performance.ModelPerformanceMode;
import com.mapbox.vision.performance.ModelPerformanceRate;
import com.mapbox.vision.utils.VisionLogger;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Example shows how Vision and VisionAR SDKs are used to draw AR lane over the video stream from camera.
 * Also, Mapbox navigation services are used to build route and  navigation session.
 */
public class NaviActivity extends BaseActivity implements RouteListener, ProgressChangeListener, OffRouteListener {

    private static final String TAG = NaviActivity.class.getSimpleName();

    // Handles navigation.
    private MapboxNavigation mapboxNavigation;
    // Fetches route from points.
    private RouteFetcher routeFetcher;
    private RouteProgress lastRouteProgress;
    private LocationEngine locationEngine;
    private LocationEngineCallback<LocationEngineResult> locationCallback;

    private boolean visionManagerWasInit = false;
    private boolean navigationWasStarted = false;

    // This dummy points will be used to build route. For real world test this needs to be changed to real values for
// source and target locations.
    private final Point ROUTE_ORIGIN = Point.fromLngLat(121.212058, 31.287271);
    private final Point ROUTE_DESTINATION = Point.fromLngLat(121.498555, 31.285400);

    @Override
    protected void initViews() {
        setContentView(R.layout.activity_navi);
        Configuration mConfiguration = this.getResources().getConfiguration(); //获取设置的配置信息
        int ori = mConfiguration.orientation; //获取屏幕方向
         if (ori == mConfiguration.ORIENTATION_PORTRAIT) {
            //竖屏
             setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//强制为横屏
//             隐藏状态栏和导航栏
         }
        setStatusBarFullTransparent();
    }
    private void setStatusBarFullTransparent() {
    //View.SYSTEM_UI_FLAG_LAYOUT_STABLE：全屏显示时保证尺寸不变。
    //View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN：Activity全屏显示，状态栏显示在Activity页面上面。
    //View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION：
    //   效果同View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    //        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION：隐藏导航栏
    //        View.SYSTEM_UI_FLAG_FULLSCREEN：Activity全屏显示，且状态栏被隐藏覆盖掉。
    //        View.SYSTEM_UI_FLAG_VISIBLE：Activity非全屏显示，显示状态栏和导航栏。
    //        View.INVISIBLE：Activity伸展全屏显示，隐藏状态栏。
    //        View.SYSTEM_UI_LAYOUT_FLAGS：效果同View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    //        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY：必须配合//View.SYSTEM_UI_FLAG_FULLSCREEN和View.SYSTEM_UI_FLAG_HIDE_NAVIGATION组合使用，达到的效//果是拉出状态栏和导航栏后显示一会儿消失。
        //21表示5.0;19表示4.4
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            |View.SYSTEM_UI_FLAG_LAYOUT_STABLE

                            |View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else  {
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //虚拟键盘也透明
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    protected void setArRenderOptions(@NotNull final VisionArView visionArView) {
        visionArView.setFenceVisible(true);
    }

    @Override
    protected void onPermissionsGranted() {
        startVisionManager();
        startNavigation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        startVisionManager();
        startNavigation();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopVisionManager();
        stopNavigation();
    }

    private void startVisionManager() {
        if (allPermissionsGranted() && !visionManagerWasInit) {
// Create and start VisionManager.
            VisionManager.create();
            VisionManager.setModelPerformance(
                    new ModelPerformance.On(ModelPerformanceMode.DYNAMIC, ModelPerformanceRate.LOW.INSTANCE)
            );
//            VisionManager.setModelPerformanceConfig(new Merged(new On(ModelPerformanceMode.DYNAMIC, ModelPerformanceRate.LOW)));
            VisionManager.start();
            VisionManager.setVisionEventsListener(new VisionEventsListener() {
                @Override
                public void onAuthorizationStatusUpdated(@NotNull AuthorizationStatus authorizationStatus) {
                }

                @Override
                public void onFrameSegmentationUpdated(@NotNull FrameSegmentation frameSegmentation) {
                }

                @Override
                public void onFrameDetectionsUpdated(@NotNull FrameDetections frameDetections) {
                }

                @Override
                public void onFrameSignClassificationsUpdated(@NotNull FrameSignClassifications frameSignClassifications) {
                }

                @Override
                public void onRoadDescriptionUpdated(@NotNull RoadDescription roadDescription) {
                }

                @Override
                public void onWorldDescriptionUpdated(@NotNull WorldDescription worldDescription) {
                }

                @Override
                public void onVehicleStateUpdated(@NotNull VehicleState vehicleState) {
                }

                @Override
                public void onCameraUpdated(@NotNull Camera camera) {
                }

                @Override
                public void onCountryUpdated(@NotNull Country country) {
                }

                @Override
                public void onUpdateCompleted() {
                }
            });

            VisionArView visionArView = findViewById(R.id.mapbox_ar_view);

// Create VisionArManager.
            VisionArManager.create(VisionManager.INSTANCE);
            visionArView.setArManager(VisionArManager.INSTANCE);
            setArRenderOptions(visionArView);

            visionManagerWasInit = true;
        }
    }

    private void stopVisionManager() {
        if (visionManagerWasInit) {
            VisionArManager.destroy();
            VisionManager.stop();
            VisionManager.destroy();

            visionManagerWasInit = false;
        }
    }

    private void startNavigation() {
        if (allPermissionsGranted() && !navigationWasStarted) {
// Initialize navigation with your Mapbox access token.
            mapboxNavigation = new MapboxNavigation(
                    this,
                    getString(R.string.mapbox_access_token),
                    MapboxNavigationOptions.builder().build()
            );

// Initialize route fetcher with your Mapbox access token.
            routeFetcher = new RouteFetcher(this, getString(R.string.mapbox_access_token));
            routeFetcher.addRouteListener(this);

            locationEngine = LocationEngineProvider.getBestLocationEngine(this);

            LocationEngineRequest arLocationEngineRequest = new LocationEngineRequest.Builder(0)
                    .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                    .setFastestInterval(1000)
                    .build();

            locationCallback = new LocationEngineCallback<LocationEngineResult>() {
                @Override
                public void onSuccess(LocationEngineResult result) {

                }

                @Override
                public void onFailure(@NonNull Exception exception) {

                }
            };

            try {
                locationEngine.requestLocationUpdates(arLocationEngineRequest, locationCallback, Looper.getMainLooper());
            } catch (SecurityException se) {
                VisionLogger.Companion.e(TAG, se.toString());
            }

            initDirectionsRoute();

// Route need to be reestablished if off route happens.
            mapboxNavigation.addOffRouteListener(this);
            mapboxNavigation.addProgressChangeListener(this);

            navigationWasStarted = true;
        }
    }

    private void stopNavigation() {
        if (navigationWasStarted) {
            locationEngine.removeLocationUpdates(locationCallback);

            mapboxNavigation.removeProgressChangeListener(this);
            mapboxNavigation.removeOffRouteListener(this);
            mapboxNavigation.stopNavigation();

            navigationWasStarted = false;
        }
    }

    private void initDirectionsRoute() {
// Get route from predefined points.
        NavigationRoute.builder(this)
                .accessToken(getString(R.string.mapbox_access_token))
                .origin(ROUTE_ORIGIN)
                .destination(ROUTE_DESTINATION)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        if (response.body() == null || response.body().routes().isEmpty()) {
                            return;
                        }

// Start navigation session with retrieved route.
                        DirectionsRoute route = response.body().routes().get(0);
                        mapboxNavigation.startNavigation(route);
                        Log.i("Route --------",""+route);
                        Log.i("Route points ---",""+getRoutePoints(route));
                        Log.i("Route duration --------",""+getRoutePoints(route));

// Set route progress.
                        VisionArManager.setRoute(new Route(
                                getRoutePoints(route),
                                route.duration().floatValue(),
                                "",
                                ""
                        ));
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                    }
                });
    }

    @Override
    public void onErrorReceived(Throwable throwable) {
        if (throwable != null) {
            throwable.printStackTrace();
        }

        mapboxNavigation.stopNavigation();
        Toast.makeText(this, "Can not calculate the route requested", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResponseReceived(@NotNull DirectionsResponse response, RouteProgress routeProgress) {
        mapboxNavigation.stopNavigation();
        if (response.routes().isEmpty()) {
            Toast.makeText(this, "Can not calculate the route requested", Toast.LENGTH_SHORT).show();
        } else {
            DirectionsRoute route = response.routes().get(0);

            mapboxNavigation.startNavigation(route);

// Set route progress.
            Log.i("Route --------",""+route);
            Log.i("Route points ---",""+getRoutePoints(route));
            VisionArManager.setRoute(new Route(
                    getRoutePoints(route),
                    (float) routeProgress.durationRemaining(),
                    "",
                    ""
            ));
        }
    }

    @Override
    public void onProgressChange(Location location, RouteProgress routeProgress) {
        lastRouteProgress = routeProgress;
    }

    @Override
    public void userOffRoute(Location location) {
        routeFetcher.findRouteFromRouteProgress(location, lastRouteProgress);
    }

    private RoutePoint[] getRoutePoints(@NotNull DirectionsRoute route) {
        ArrayList<RoutePoint> routePoints = new ArrayList<>();

        List<RouteLeg> legs = route.legs();
        if (legs != null) {
            for (RouteLeg leg : legs) {

                List<LegStep> steps = leg.steps();
                if (steps != null) {
                    for (LegStep step : steps) {
                        RoutePoint point = new RoutePoint((new GeoCoordinate(
                                step.maneuver().location().latitude(),
                                step.maneuver().location().longitude()
                        )), mapToManeuverType(step.maneuver().type()));

                        routePoints.add(point);

                        List<Point> geometryPoints = buildStepPointsFromGeometry(step.geometry());
                        for (Point geometryPoint : geometryPoints) {
                            point = new RoutePoint((new GeoCoordinate(
                                    geometryPoint.latitude(),
                                    geometryPoint.longitude()
                            )), ManeuverType.None);

                            routePoints.add(point);
                        }
                    }
                }
            }
        }

        return routePoints.toArray(new RoutePoint[0]);
    }

    private List<Point> buildStepPointsFromGeometry(String geometry) {
        return PolylineUtils.decode(geometry, Constants.PRECISION_6);
    }

    private ManeuverType mapToManeuverType(@Nullable String maneuver) {
        if (maneuver == null) {
            return ManeuverType.None;
        }
        switch (maneuver) {
            case "turn":
                return ManeuverType.Turn;
            case "depart":
                return ManeuverType.Depart;
            case "arrive":
                return ManeuverType.Arrive;
            case "merge":
                return ManeuverType.Merge;
            case "on ramp":
                return ManeuverType.OnRamp;
            case "off ramp":
                return ManeuverType.OffRamp;
            case "fork":
                return ManeuverType.Fork;
            case "roundabout":
                return ManeuverType.Roundabout;
            case "exit roundabout":
                return ManeuverType.RoundaboutExit;
            case "end of road":
                return ManeuverType.EndOfRoad;
            case "new name":
                return ManeuverType.NewName;
            case "continue":
                return ManeuverType.Continue;
            case "rotary":
                return ManeuverType.Rotary;
            case "roundabout turn":
                return ManeuverType.RoundaboutTurn;
            case "notification":
                return ManeuverType.Notification;
            case "exit rotary":
                return ManeuverType.RotaryExit;
            default:
                return ManeuverType.None;
        }
    }
}