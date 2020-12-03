package com.example.tjarnav.ar.arcore;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.AmapNaviPage;
import com.amap.api.navi.enums.NaviType;
import com.amap.api.navi.model.AMapCalcRouteResult;
import com.amap.api.navi.model.AMapCarInfo;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapModelCross;
import com.amap.api.navi.model.AMapNaviCameraInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviLink;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviRouteGuideGroup;
import com.amap.api.navi.model.AMapNaviRouteNotifyData;
import com.amap.api.navi.model.AMapNaviStep;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AMapServiceAreaInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.example.tjarnav.R;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.ArrayList;
import java.util.List;

public class ArActivity extends AppCompatActivity implements AMapNaviListener, AMapNaviViewListener {

    private static final String TAG = "ar test";
    private static final double MIN_OPENGL_VERSION = 3.0;
    private static final int SHOW_OBJECT = 0x1101;

    private CleanArFragment arFragment;
    private AnchorNode tempNode;
    private ModelRenderable placeRenderable;
    private ModelRenderable leftRenderable;
    private ModelRenderable rightRenderable;
    private ModelRenderable straightRenderable;
    private AnchorNode lineNode;

    boolean isHide = false;
    protected AMapNaviView mAMapNaviView;
    protected AMapNavi mAMapNavi;
    protected NaviLatLng mEndLatlng = new NaviLatLng(40.084894, 116.603039);
    protected NaviLatLng mStartLatlng = new NaviLatLng(39.825934, 116.342972);
    protected final List<NaviLatLng> sList = new ArrayList<NaviLatLng>();
    protected final List<NaviLatLng> eList = new ArrayList<NaviLatLng>();

    protected List<NaviLatLng> mWayPointList;
    protected AMapNaviPath aMapNaviPath;
    private List<AMapNaviStep> steps;
    private List<AMapNaviLink> links;
    private List<AMapNaviRouteGuideGroup> guides;
    private List<NaviLatLng> pathPoints;

//    private  final RotatingSettings rotatingSettings =new RotatingSettings();

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == SHOW_OBJECT) {
//                 提供 3D 模型显示的位置（世界坐标系，相机面向的方向 2m 处）
//                 World Arc Point
//                Vector3 point = new Vector3(0, 0, -2);
//                showObj(point);
            }
            super.handleMessage(msg);
        }
    };


    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//强制为横屏
        setContentView(R.layout.activity_ar);
        arFragment = (CleanArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        ArFragment arFragment;

        Intent intent = getIntent();
        mAMapNaviView = (AMapNaviView) findViewById(R.id.navi_view_2);
        mAMapNavi = AMapNavi.getInstance(getApplicationContext());
        mAMapNaviView.onCreate(savedInstanceState);
        mAMapNaviView.setAMapNaviViewListener(this);
        mAMapNavi.addAMapNaviListener(this);
        mStartLatlng = new NaviLatLng(intent.getDoubleExtra("startLat", 0.0), intent.getDoubleExtra("startLon", 0.0));
        mEndLatlng = new NaviLatLng(intent.getDoubleExtra("endLat", 0.0), intent.getDoubleExtra("endLon", 0.0));

        //构造 3D 模型资源
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ModelRenderable.builder()
                    .setSource(this, R.raw.placemark)
                    .build()
                    .thenAccept(renderable -> placeRenderable = renderable)
                    .exceptionally(
                            throwable -> {
                                Toast toast =
                                        Toast.makeText(this, "Unable to load placemark renderable", Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                                return null;
                            });
            ModelRenderable.builder()
                    .setSource(this, R.raw.left)
                    .build()
                    .thenAccept(renderable -> leftRenderable = renderable)
                    .exceptionally(
                            throwable -> {
                                Toast toast =
                                        Toast.makeText(this, "Unable to load left renderable", Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                                return null;
                            });
            ModelRenderable.builder()
                    .setSource(this, R.raw.right)
                    .build()
                    .thenAccept(renderable -> rightRenderable = renderable)
                    .exceptionally(
                            throwable -> {
                                Toast toast =
                                        Toast.makeText(this, "Unable to load right renderable", Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                                return null;
                            });
            ModelRenderable.builder()
                    .setSource(this, R.raw.straight)
                    .build()
                    .thenAccept(renderable -> straightRenderable = renderable)
                    .exceptionally(
                            throwable -> {
                                Toast toast =
                                        Toast.makeText(this, "Unable to load straight renderable", Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                                return null;
                            });
        }

        // 线程通知延迟绘制
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Message msg = handler.obtainMessage();
                msg.what = SHOW_OBJECT;
                handler.sendMessage(msg);
            }
        }) {
        }.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //设置模拟导航的行车速度
        mAMapNavi.setEmulatorNaviSpeed(75);
        sList.add(mStartLatlng);
        eList.add(mEndLatlng);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAMapNaviView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mAMapNaviView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAMapNaviView.onDestroy();
        //since 1.6.0 不再在naviview destroy的时候自动执行AMapNavi.stopNavi();请自行执行
        mAMapNavi.stopNavi();
        mAMapNavi.destroy();
    }

    //    private void showObj(Vector3 worldSet) {
//        AnchorNode anchorNode = new AnchorNode();
//        //设置锚点在世界坐标系的位置
//        anchorNode.setWorldPosition(worldSet);
//        anchorNode.setParent(arFragment.getArSceneView().getScene());
//
//        TransformableNode andy = new TransformableNode(arFragment.getTransformationSystem());
//        andy.setParent(anchorNode);
//        andy.setRenderable(andyRenderable);
//
//        andy.select();
//        andy.setWorldScale(new Vector3(0.1f, 0.1f, 0.1f));
//        // 禁止缩放，没禁止缩放，设置的倍数会失效，自动加载默认的大小
//        andy.getScaleController().setEnabled(false);
//
//
////        Node markAnimation=createAnimation();
////        anchorNode.addChild(markAnimation);
//    }
//    private Node createAnimation(){
//        Node base = new Node();
//        base.setLocalPosition(new Vector3(0.0f, 0.0f, 0.0f));
//        createMark("turnLeft",base,1f,50f,andyRenderable,0.3f,0.0f);
//        return base;
//    }
//
//
//    private Node createMark(
//            String name,
//            Node parent,
//            float fromParent,
//            float degreesPerSecond,
//            ModelRenderable modelRenderable,
//            float markScale,
//            float axisTilt){
//        // Orbit is a rotating node with no renderable positioned at the sun.
//        // The planet is positioned relative to the orbit so that it appears to rotate around the sun.
//        // This is done instead of making the sun rotate so each planet can orbit at its own speed.
//        RotatingNode orbit =new RotatingNode(rotatingSettings,true,false,0f);
//        orbit.setDegreesPerSecond(degreesPerSecond);
//        orbit.setParent(parent);
//        Planet mark=new Planet(this,name,markScale,degreesPerSecond,axisTilt,modelRenderable, rotatingSettings);
//        mark.setParent(orbit);
//        mark.setLocalPosition((new Vector3(fromParent,0.0f,0.0f)));
//
//        return mark;
//    }
    private void showObj(Vector3 worldSet, int type) {
        if (tempNode != null) {
            arFragment.getArSceneView().getScene().removeChild(tempNode);
        }
        AnchorNode anchorNode = new AnchorNode();
        //设置锚点在世界坐标系的位置
        anchorNode.setWorldPosition(worldSet);
        anchorNode.setParent(arFragment.getArSceneView().getScene());
        tempNode = anchorNode;
        TransformableNode mark = new TransformableNode(arFragment.getTransformationSystem());
        mark.setParent(anchorNode);
        if (type == 0) {
            mark.setRenderable(placeRenderable);
        } else if (type == 2) {
            mark.setRenderable(leftRenderable);
            Quaternion quaternion = new Quaternion(0f, 0.989f, 0.15f, 0f);
            mark.setLocalRotation(quaternion);
        } else if (type == 3) {
            mark.setRenderable(rightRenderable);
            Quaternion quaternion = new Quaternion(0f, 0.989f, 0.15f, 0f);
            mark.setLocalRotation(quaternion);
        } else if (type == 9) {
            mark.setRenderable(straightRenderable);
            Quaternion quaternion = new Quaternion(0.062f, 0.704f, 0.062f, 0.704f);
            mark.setLocalRotation(quaternion);
        }

        mark.select();
        mark.setWorldScale(new Vector3(0.2f, 0.2f, 0.2f));
        // 禁止缩放，没禁止缩放，设置的倍数会失效，自动加载默认的大小
        mark.getScaleController().setEnabled(false);
    }

    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later");
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        }
        String openGlVersionString =
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }

    public void turnLeft() {
        Toast.makeText(ArActivity.this, "左转", Toast.LENGTH_LONG).show();
        Vector3 point = new Vector3(-1f, -2f, -4f);
        showObj(point, 2);
    }

    public void turnLeft(View view) {
        Toast.makeText(ArActivity.this, "左转", Toast.LENGTH_LONG).show();
        Vector3 point = new Vector3(-1f, -2f, -4f);
        showObj(point, 2);
    }

    public void turnRight() {
        Toast.makeText(ArActivity.this, "右转", Toast.LENGTH_LONG).show();
        Vector3 point = new Vector3(1f, -2f, -4f);
        showObj(point, 3);
    }

    public void turnRight(View view) {
        Toast.makeText(ArActivity.this, "右转", Toast.LENGTH_LONG).show();
        Vector3 point = new Vector3(1f, -2f, -4f);
        showObj(point, 3);
    }

    public void goStraight() {
        Toast.makeText(ArActivity.this, "直行", Toast.LENGTH_LONG).show();
        Vector3 point = new Vector3(0, -2f, -4f);
        showObj(point, 9);
    }

    public void goStraight(View view) {
        Toast.makeText(ArActivity.this, "直行", Toast.LENGTH_LONG).show();
        Vector3 point = new Vector3(0, -2f, -4f);
        showObj(point, 9);
    }

    public void hideNav(View view) {
        if (isHide) {
            mAMapNaviView.setVisibility(View.VISIBLE);
            isHide = false;
        } else {
            mAMapNaviView.setVisibility(View.INVISIBLE);
            isHide = true;
        }
    }

    public double getDistance(LatLng start, LatLng end) {

        double lon1 = (Math.PI / 180) * start.longitude;
        double lon2 = (Math.PI / 180) * end.longitude;
        double lat1 = (Math.PI / 180) * start.latitude;
        double lat2 = (Math.PI / 180) * end.latitude;

        // 地球半径
        double R = 6371;

        // 两点间距离 km，如果想要米的话，结果*1000就可以了
        double d = Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1))
                * R;

        return d * 1000;
    }
    public static float getMin(float[] array) {
        float Max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (Max > array[i]) {
                Max = array[i];
            }
        }
        return Max;
    }

    /**
     * 绘制路径线
     * @param point1
     * @param point2
     * @param worldSet
     */

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void lineBetweenPoints(Vector3 point1, Vector3 point2, Vector3 worldSet){
        AnchorNode anchorNode=new AnchorNode();
        anchorNode.setWorldPosition(worldSet);
        anchorNode.setParent(arFragment.getArSceneView().getScene());
//        TransformableNode lineNode=new TransformableNode(arFragment.getTransformationSystem());
        lineNode=new AnchorNode();
        lineNode.setParent(anchorNode);
        final Vector3 difference=Vector3.subtract(point1,point2);
        final Vector3 directionFromTopToBottom =difference.normalized();
        final Quaternion rotationFromAtoB=Quaternion.lookRotation(directionFromTopToBottom,Vector3.up());
        float len=difference.length();
        System.out.println("lenth is ----"+len);
        MaterialFactory.makeOpaqueWithColor(this,new Color(android.graphics.Color.YELLOW))
                .thenAccept(
                        material -> {
                            ModelRenderable modelRenderable= ShapeFactory.makeCube(new Vector3(0.1f,0.01f,len),
                                    Vector3.zero(),material);
                            lineNode.setRenderable(modelRenderable);
//                            placeModel(lineNode,modelRenderable);
                        }
                );
//        lineNode.setParent(anchorNode);
        lineNode.setLocalPosition(point1);
    }

    private void placeModel(AnchorNode anchorNode,ModelRenderable modelRenderable){
        anchorNode.setRenderable(modelRenderable);
        arFragment.getArSceneView().getScene().addChild(anchorNode);
    }


//    高德

    @Override
    public void onInitNaviFailure() {
        Toast.makeText(this, "init navi Failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInitNaviSuccess() {
        /**
         * 方法: int strategy=mAMapNavi.strategyConvert(congestion, avoidhightspeed, cost, hightspeed, multipleroute); 参数:
         *
         * @congestion 躲避拥堵
         * @avoidhightspeed 不走高速
         * @cost 避免收费
         * @hightspeed 高速优先
         * @multipleroute 多路径
         *
         *  说明: 以上参数都是boolean类型，其中multipleroute参数表示是否多条路线，如果为true则此策略会算出多条路线。
         *  注意: 不走高速与高速优先不能同时为true 高速优先与避免收费不能同时为true
         */
        int strategy = 0;
        try {
            //再次强调，最后一个参数为true时代表多路径，否则代表单路径
            strategy = mAMapNavi.strategyConvert(true, false, false, false, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        AMapCarInfo aMapCarInfo = new AMapCarInfo();
        aMapCarInfo.setCarNumber("京DFZ588");
        mAMapNavi.setCarInfo(aMapCarInfo);
        mAMapNavi.calculateDriveRoute(sList, eList, mWayPointList, strategy);
    }

    @Override
    public void onStartNavi(int i) {
        // 开始导航回调
//        List<AMapNaviRouteGuideGroup> guides = aMapNaviPath.getNaviGuideList();
//        int length=aMapNaviPath.getAllLength();
//        System.out.println("guides----"+length);
    }

    @Override
    public void onTrafficStatusUpdate() {

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onLocationChange(AMapNaviLocation aMapNaviLocation) {
        // 当前位置回调
        int len = 5;
        System.out.println("path points size:   "+pathPoints.size());
        LatLng cur = new LatLng(aMapNaviLocation.getCoord().getLatitude(), aMapNaviLocation.getCoord().getLongitude());
        LatLng[] next = new LatLng[len];
        float[] dist = new float[len];
        for (int i = 0; i < len; i++) {
            NaviLatLng point=pathPoints.get(i);
            next[i]=new LatLng(point.getLatitude(), point.getLongitude());
            dist[i]=(float) Math.abs(getDistance(cur, next[i]));
            System.out.println("distance " + i + " is now-----" + dist[i]);
        }
        if(dist[0]<5f && dist[0]==getMin(dist)){
            pathPoints.remove(0);
            System.out.println("path points size:   "+pathPoints.size());
        }
        System.out.println("1th dist:   "+-dist[1]);
        Vector3 point1=new Vector3(0f,0f,0f);
        Vector3 point2=new Vector3(0f,0f,-dist[1]);
        Vector3 worldSet=new Vector3(0f,-2f,0f);
        lineBetweenPoints(point1,point2,worldSet);
    }
    @Override
    public void onGetNavigationText(int i, String s) {
        // 播报类型和播报文字回调
//        Toast.makeText(ArActivity.this, s, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGetNavigationText(String s) {

    }

    @Override
    public void onEndEmulatorNavi() {
        //结束模拟导航
    }

    @Override
    public void onArriveDestination() {
        //到达目的地
    }


    @Override
    public void onCalculateRouteFailure(int i) {
        //路线计算失败
    }

    @Override
    public void onReCalculateRouteForYaw() {
        //偏航后重新计算路线回调
    }

    @Override
    public void onReCalculateRouteForTrafficJam() {
        //拥堵后重新计算路线回调
    }

    @Override
    public void onArrivedWayPoint(int i) {
        //到达途径点
    }

    @Override
    public void onGpsOpenStatus(boolean b) {
        //GPS开关状态回调
    }

    @Override
    public void onNaviInfoUpdate(NaviInfo naviInfo) {
        int dist = naviInfo.getCurStepRetainDistance();
        int currentStep = naviInfo.getCurStep();
        int currentLink = naviInfo.getCurLink();
        System.out.println("当前Step index:" + currentStep + "当前Link index:" + currentLink);
//        System.out.println(dist);
        int type = naviInfo.getIconType();
        if (type == 2) {
            turnLeft();
        } else if (type == 3) {
            turnRight();
        } else if (type == 9) {
            goStraight();
        }
    }

    @Override
    public void updateCameraInfo(AMapNaviCameraInfo[] aMapNaviCameraInfos) {

    }

    @Override
    public void updateIntervalCameraInfo(AMapNaviCameraInfo aMapNaviCameraInfo, AMapNaviCameraInfo aMapNaviCameraInfo1, int i) {

    }

    @Override
    public void onServiceAreaUpdate(AMapServiceAreaInfo[] aMapServiceAreaInfos) {

    }

    @Override
    public void showCross(AMapNaviCross aMapNaviCross) {
        //显示转弯回调
    }

    @Override
    public void hideCross() {
        //隐藏转弯回调
    }

    @Override
    public void showModeCross(AMapModelCross aMapModelCross) {

    }

    @Override
    public void hideModeCross() {

    }

    @Override
    public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes1) {
        //显示车道信息
    }

    @Override
    public void showLaneInfo(AMapLaneInfo aMapLaneInfo) {

    }

    @Override
    public void hideLaneInfo() {
        //隐藏车道信息
    }


    @Override
    public void onCalculateRouteSuccess(int[] ints) {
        mAMapNavi.startNavi(NaviType.GPS);
        if (ints != null && ints.length > 0) {
            aMapNaviPath = mAMapNavi.getNaviPath();//导航返回的路径
            guides = aMapNaviPath.getNaviGuideList();
            steps = aMapNaviPath.getSteps();
            if (guides.size() > 0 && steps.size() > 0) {
                if (aMapNaviPath.getCoordList() != null) {
                    pathPoints = aMapNaviPath.getCoordList();
                    int size = pathPoints.size();//路径上所有的点
                    for (int i = 0; i < size; i++) {
                        NaviLatLng naviLatLng = pathPoints.get(i);
                        System.out.println("路径上的点: " + naviLatLng);
                    }

                }
                for (int i = 0; i < steps.size() - 1; i++) {
                    //guide step相生相惜，指的是大导航段
                    AMapNaviRouteGuideGroup guide = guides.get(i);
                    System.out.println("AMapNaviGuide 路线起点经纬度:" + guide.getGroupEnterCoord() + "");
                    System.out.println("AMapNaviGuide 路线名:" + guide.getGroupName() + "");
                    System.out.println("AMapNaviGuide 路线长:" + guide.getGroupLen() + "m");
                    System.out.println("AMapNaviGuide 路线耗时:" + guide.getGroupTime() + "s");
                    System.out.println("AMapNaviGuide 路线IconType" + guide.getGroupIconType() + "");
                    AMapNaviStep step = steps.get(i);
                    System.out.println("AMapNaviStep 距离:" + step.getLength() + "m" + " " + "耗时:" + step.getTime() + "s");
                    System.out.println("AMapNaviStep 红绿灯个数:" + step.getTrafficLightNumber());


                    //link指的是大导航段中的小导航段
                    links = step.getLinks();
                    for (AMapNaviLink link : links) {
                        // 请看com.amap.api.navi.enums.RoadClass，以及帮助文档
                        System.out.println("AMapNaviLink 道路名:" + link.getRoadName() + " " + "道路等级: " + link.getRoadClass());
                        // 请看com.amap.api.navi.enums.RoadType，以及帮助文档
                        System.out.println("AMapNaviLink 道路类型:" + link.getRoadType());
                    }
                }

            } else {
                Toast.makeText(this, "BUG！请联系我们", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void notifyParallelRoad(int i) {
//        if (i == 0) {
//            Toast.makeText(this, "当前在主辅路过渡", Toast.LENGTH_SHORT).show();
//            Log.d("wlx", "当前在主辅路过渡");
//            return;
//        }
//        if (i == 1) {
//            Toast.makeText(this, "当前在主路", Toast.LENGTH_SHORT).show();
//            Log.d("wlx", "当前在主路");
//            return;
//        }
//        if (i == 2) {
//            Toast.makeText(this, "当前在辅路", Toast.LENGTH_SHORT).show();
//            Log.d("wlx", "当前在辅路");
//        }

    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {
        //更新交通设施信息
    }

    @Override
    public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {

    }

    @Override
    public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {
        //更新巡航模式的统计信息
    }

    @Override
    public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {
        //更新巡航模式的拥堵信息
    }

    @Override
    public void onPlayRing(int i) {
        //锁地图状态发生变化时回调
    }

    @Override
    public void onCalculateRouteSuccess(AMapCalcRouteResult aMapCalcRouteResult) {

    }

    @Override
    public void onCalculateRouteFailure(AMapCalcRouteResult aMapCalcRouteResult) {

    }

    @Override
    public void onNaviRouteNotify(AMapNaviRouteNotifyData aMapNaviRouteNotifyData) {

    }

    @Override
    public void onGpsSignalWeak(boolean b) {

    }

    @Override
    public void onNaviSetting() {

    }

    @Override
    public void onNaviCancel() {

    }

    @Override
    public boolean onNaviBackClick() {
        return false;
    }

    @Override
    public void onNaviMapMode(int i) {

    }

    @Override
    public void onNaviTurnClick() {

    }

    @Override
    public void onNextRoadClick() {

    }

    @Override
    public void onScanViewButtonClick() {

    }

    @Override
    public void onLockMap(boolean b) {

    }

    @Override
    public void onNaviViewLoaded() {

    }

    @Override
    public void onMapTypeChanged(int i) {

    }

    @Override
    public void onNaviViewShowMode(int i) {

    }
}
