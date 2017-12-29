#include <jni.h>
#include <string>
#include "inc/ntchart.h"
#include "jni_log.h"
#include <memory>

#include <iostream>
#include <iomanip>
#include <sstream>

#include "inc/render.h"
#include <android/native_window.h> // requires ndk r5 or newer
#include <android/native_window_jni.h> // requires ndk r5 or newer

//#include <SkCanvas.h>

//#include "GraphicsJNI.h"
//#include "SkBlurMaskFilter.h"
//#include "SkCanvas.h"
//#include "SkCornerPathEffect.h"
//#include "SkDumpCanvas.h"
//#include "SkPath.h"
//#include "SkPicture.h"
//#include "SkPixelXorXfermode.h"
//#include "SkPaint.h"
//#include "SkRect.h"
//#include "SkTime.h"

//template<typename TPoint, typename std::enable_if<std::is_base_of<Point, TPoint>::value>::type * = nullptr>
//Series<TPoint> *getSeries(jlong ptr) {
//    return ptr == 0 ? nullptr : (Series<TPoint> *) ptr;
////    return (Series *) env->GetLongField(
////            jInstance,
////            getPtrFieldId(env, jInstance));
//}

SeriesLine *getSeriesLine(jlong ptr) {
    return ptr == 0 ? nullptr : (SeriesLine *) ptr;
}

std::string f2str(const float &val) {
    std::ostringstream ss;
    ss.precision(2);
    ss << val;
    return ss.str();
}

extern "C" {

jfieldID getPtrFieldId(JNIEnv *env, jobject obj) {
    static jfieldID ptrFieldId = 0;
    if (!ptrFieldId) {
        jclass c = env->GetObjectClass(obj);
        ptrFieldId = env->GetFieldID(c, "npSeries", "J");
        env->DeleteLocalRef(c);
    }

    return ptrFieldId;
}



//std::shared_ptr<Series> sharedPtrSeries;
//std::shared_ptr<Series> getSeries(JNIEnv *env, jobject jInstance) {
////    std::shared_ptr<Series> *sharedPtrSeries = (std::shared_ptr<Series> *) env->GetLongField(
////            jInstance,
////            getPtrFieldId(env, jInstance));
//    return sharedPtrSeries;
//}


JNIEXPORT void JNICALL
Java_ru_turbomolot_ntchart_series_SeriesLine_nInitSeries(JNIEnv *env, jobject jInstance) {
    jlong ptrLong = (jlong) new SeriesLine();
    env->SetLongField(jInstance, getPtrFieldId(env, jInstance), ptrLong);
}

JNIEXPORT void JNICALL
Java_ru_turbomolot_ntchart_series_SeriesLine_nDeinitSeries(JNIEnv *env, jobject jInstance,
                                                           jlong ptr) {
    SeriesLine *s = getSeriesLine(ptr);
    if (s != nullptr)
        delete s;
}

JNIEXPORT void JNICALL
Java_ru_turbomolot_ntchart_series_SeriesLine_nAddPoint(JNIEnv *env, jobject jInstance, jlong ptr,
                                                       jfloat x, jfloat y) {
    SeriesLine *series = getSeriesLine(ptr);
    if (series != nullptr) {
        series->addPoint(Point2D(x, y));
    }
}

JNIEXPORT void JNICALL
Java_ru_turbomolot_ntchart_series_SeriesLine_nAddPointList(JNIEnv *env, jobject jInstance,
                                                           jlong ptr,
                                                           jobject ptsInput) {
    jclass alCls = env->FindClass("java/util/List");
    jclass ptCls = env->FindClass("ru/turbomolot/ntchart/data/Point2D");
    if (alCls == nullptr || ptCls == nullptr) {
        return;
    }
    jmethodID alGetId = env->GetMethodID(alCls, "get", "(I)Ljava/lang/Object;");
    jmethodID alSizeId = env->GetMethodID(alCls, "size", "()I");
    jmethodID ptGetXId = env->GetMethodID(ptCls, "getX", "()F");
    jmethodID ptGetYId = env->GetMethodID(ptCls, "getY", "()F");

    jsize pointCount = static_cast<jsize>(env->CallIntMethod(ptsInput, alSizeId));
    if (pointCount < 1) {
        return;
    }

    SeriesLine *series = getSeriesLine(ptr);
    if (series == nullptr) {
        return;
    }

    Point2D points[pointCount];
    for (int i = 0; i < pointCount; ++i) {
        jobject point = env->CallObjectMethod(ptsInput, alGetId, i);
        points[i].setXY(static_cast<float>(env->CallFloatMethod(point, ptGetXId)),
                        static_cast<float>(env->CallFloatMethod(point, ptGetYId)));
//        Point2D pt = Point2D(static_cast<jfloat>(env->CallFloatMethod(point, ptGetXId)),
//                             static_cast<jfloat>(env->CallFloatMethod(point, ptGetYId)));
//        series->addPoint(pt);
        env->DeleteLocalRef(point);
    }
    series->addPoints(points, (std::size_t) pointCount);

//    const std::vector<Point2D> data = series->getData();
//    for (auto value: data) {
//        std::string val = f2str(value.getX()) + " " + f2str(value.getY());
//        log_d_str("[POINTS]", val.c_str());
//    }
}

JNIEXPORT jobject JNICALL
Java_ru_turbomolot_ntchart_series_SeriesLine_nGetPointList(JNIEnv *env, jobject jInstance,
                                                           jlong ptr) {
    SeriesLine *series = getSeriesLine(ptr);
    if (series == nullptr || series->getData().size() == 0) {
        return nullptr;
    }

    jclass alCls = env->FindClass("java/util/ArrayList");
    jclass ptCls = env->FindClass("ru/turbomolot/ntchart/data/Point2D");
    if (alCls == nullptr || ptCls == nullptr) {
        return nullptr;
    }
    jmethodID alAddId = env->GetMethodID(alCls, "add", "(Ljava/lang/Object;)Z");
    jmethodID alConstructor = env->GetMethodID(alCls, "<init>", "(I)V");
    jmethodID ptConstructor = env->GetMethodID(ptCls, "<init>", "(FF)V");

    const std::vector<Point2D> data = series->getData();
    jobject ptsLstRes = env->NewObject(alCls, alConstructor, data.size());
    for (auto value: data) {
        jobject p2d = env->NewObject(ptCls, ptConstructor,
                                     static_cast<jfloat>(value.getX()),
                                     static_cast<jfloat>(value.getY()));
        env->CallBooleanMethod(ptsLstRes, alAddId, p2d);
        env->DeleteLocalRef(p2d);
    }
    return ptsLstRes;
}

JNIEXPORT void JNICALL
Java_ru_turbomolot_ntchart_series_SeriesLine_nClearPoints(JNIEnv *env, jobject jInstance,
                                                          jlong ptr) {
    SeriesLine *series = getSeriesLine(ptr);
    if (series == nullptr || series->getData().size() == 0) {
        return;
    }
    series->clearData();
}

JNIEXPORT void JNICALL
Java_ru_turbomolot_ntchart_series_SeriesLine_nCalculateRender(JNIEnv *env, jobject jInstance,
                                                              jlong ptr) {
    SeriesLine *series = getSeriesLine(ptr);
    if (series == nullptr) {
        return;
    }
    series->calculateRender();
}

JNIEXPORT jobject JNICALL
Java_ru_turbomolot_ntchart_series_SeriesLine_nGetPointListRender(JNIEnv *env, jobject jInstance,
                                                                 jlong ptr) {
    SeriesLine *series = getSeriesLine(ptr);
    if (series == nullptr || series->getDataRender().size() == 0) {
        return nullptr;
    }

    jclass alCls = env->FindClass("java/util/ArrayList");
    jclass ptCls = env->FindClass("ru/turbomolot/ntchart/data/Point2D");
    if (alCls == nullptr || ptCls == nullptr) {
        return nullptr;
    }
    jmethodID alAddId = env->GetMethodID(alCls, "add", "(Ljava/lang/Object;)Z");
    jmethodID alConstructor = env->GetMethodID(alCls, "<init>", "(I)V");
    jmethodID ptConstructor = env->GetMethodID(ptCls, "<init>", "(FF)V");

    const std::vector<Point2D> data = series->getDataRender();
    jobject ptsLstRes = env->NewObject(alCls, alConstructor, data.size());
    for (auto value: data) {
        jobject p2d = env->NewObject(ptCls, ptConstructor,
                                     static_cast<jfloat>(value.getX()),
                                     static_cast<jfloat>(value.getY()));
        env->CallBooleanMethod(ptsLstRes, alAddId, p2d);
        env->DeleteLocalRef(p2d);
    }
    return ptsLstRes;
}

JNIEXPORT jfloatArray JNICALL
Java_ru_turbomolot_ntchart_series_SeriesLine_nGetPointsRender(JNIEnv *env, jobject jInstance,
                                                              jlong ptr) {
    SeriesLine *series = getSeriesLine(ptr);
    if (series == nullptr || series->getDataRender().size() == 0) {
        return nullptr;
    }
    jfloatArray result;
    jsize sizeDR = (jsize) series->getDataRender().size();
    jsize size = ((sizeDR << 1) - 2) << 1;
    if (size == 0)
        return nullptr;
    result = env->NewFloatArray(size);
    if (result == nullptr) {
        return nullptr; /* out of memory error thrown */
    }
    jfloat pts[size];
    jsize idx = 0;
    jsize idxR = 0;
    for (auto &itm : series->getDataRender()) {
        pts[idx++] = itm.getX();
        pts[idx++] = itm.getY();
        if (idxR > 0 && idxR < (sizeDR - 1)) {
            pts[idx++] = itm.getX();
            pts[idx++] = itm.getY();
        }
        idxR++;
    }
    env->SetFloatArrayRegion(result, 0, size, pts);
    return result;
}

JNIEXPORT void JNICALL
Java_ru_turbomolot_ntchart_series_SeriesLine_nSetRenderSize(JNIEnv *env, jobject jInstance,
                                                            jlong ptr, jfloat width,
                                                            jfloat height) {
    SeriesLine *series = getSeriesLine(ptr);
    if (series == nullptr) {
        return;
    }
    series->setRenderSize(width, height);
}


/**************************************************************************************************/
static ANativeWindow *window = 0;
static Renderer *renderer = 0;

JNIEXPORT void JNICALL
Java_ru_turbomolot_ntchart_holder_NTChartSV_nOnStart(JNIEnv *jenv, jobject obj) {
    renderer = new Renderer();
    return;
}

JNIEXPORT void JNICALL
Java_ru_turbomolot_ntchart_holder_NTChartSV_nOnResume(JNIEnv *jenv, jobject obj) {
    renderer->start();
    return;
}

JNIEXPORT void JNICALL
Java_ru_turbomolot_ntchart_holder_NTChartSV_nOnPause(JNIEnv *jenv, jobject obj) {
    renderer->stop();
    return;
}

JNIEXPORT void JNICALL
Java_ru_turbomolot_ntchart_holder_NTChartSV_nOnStop(JNIEnv *jenv, jobject obj) {
    delete renderer;
    renderer = 0;
    return;
}

JNIEXPORT void JNICALL
Java_ru_turbomolot_ntchart_holder_NTChartSV_nSetSurface(JNIEnv *jenv, jobject obj,
                                                        jobject surface) {
    if (surface != 0) {
        window = ANativeWindow_fromSurface(jenv, surface);
        renderer->setWindow(window);
    } else {
        ANativeWindow_release(window);
    }
    return;
}
/**************************************************************************************************/
typedef struct Point2Df {
    jfloat x;
    jfloat y;
    jboolean skip;

    bool operator==(const Point2Df &left) {
        return left.x == x && left.y == y;
    }

    bool operator!=(const Point2Df &left) {
        return !(left.x == x && left.y == y);
    }
} Point2D_f;

void reducePointsFOrderedX(Point2Df *pts, jsize &sizePts, jfloat tolerance) {
    // Сохраним начало и конец
    pts[0].skip = 0;
    pts[sizePts - 1].skip = 0;
    // Переменные цикла
    jsize anchor = 0;
    jsize floater = sizePts - 1;


    jfloat anchorX, anchorY, segLen;
    Point2Df a, f, ptsTmp;
    std::vector <jsize> stack((unsigned) sizePts);
    stack.clear();
    sizePts = 2;

    stack.push_back(anchor);
    stack.push_back(floater);

    while (!stack.empty()) {
        // Инициализация отрезка
        floater = stack.back();
        stack.pop_back();
        anchor = stack.back();
        stack.pop_back();
        f = pts[floater];
        a = pts[anchor];
        if (f != a) {
            anchorX = f.x - a.x;
            anchorY = f.y - a.y;
            segLen = sqrtf((anchorX * anchorX) + (anchorY * anchorY));
            anchorX /= segLen;
            anchorY /= segLen;
        } else {
            anchorX = anchorY = 0;
//            segLen = 0;
        }
        // Внутренний цикл:
        jfloat maxDist = 0;
        jsize farthest = anchor + 1;
        jsize i = farthest - 1;
        while (++i < floater) {
            ptsTmp = pts[i];
            jfloat distToSeg = 0;
            jfloat vecX = ptsTmp.x - a.x;
            jfloat vecY = ptsTmp.y - a.y;
//            segLen = sqrt(vecX * vecX + vecY * vecY);
            jfloat proj = vecX * anchorX + vecY * anchorY;
            if (proj >= 0) {
                vecX = ptsTmp.x - f.x;
                vecY = ptsTmp.y - f.y;
                segLen = sqrtf((vecX * vecX) + (vecY * vecY));
                proj = vecX * (-anchorX) + vecY * (-anchorY);
                if (proj < 0)
                    distToSeg = segLen;
                else // расстояние от до прямой по теореме Пифагора:
                    distToSeg = sqrtf(fabsf(segLen * segLen - proj * proj));
                if (maxDist < distToSeg) {
                    maxDist = distToSeg;
                    farthest = i;
                }
            }
        }
        if (maxDist <= tolerance) {
            if (pts[anchor].skip) {
                pts[anchor].skip = 0;
                sizePts++;
            }
            if (pts[floater].skip) {
                pts[floater].skip = 0;
                sizePts++;
            }
        } else {
            stack.push_back(anchor);
            stack.push_back(farthest);

            stack.push_back(farthest);
            stack.push_back(floater);
        }
    }
}

JNIEXPORT jfloatArray JNICALL
Java_ru_turbomolot_ntchart_utils_MathHelper_nReduceListOrderedX(JNIEnv *env, jobject jInstance,
                                                                jobject ptsInput,
                                                                jfloat tolerance) {
    jclass alCls = env->FindClass("java/util/List");
    jclass ptCls = env->FindClass("ru/turbomolot/ntchart/data/IPoint2D");
    if (alCls == nullptr || ptCls == nullptr) {
        return nullptr;
    }
    jmethodID alGetId = env->GetMethodID(alCls, "get", "(I)Ljava/lang/Object;");
    jmethodID alSizeId = env->GetMethodID(alCls, "size", "()I");
    jmethodID ptGetXId = env->GetMethodID(ptCls, "getX", "()F");
    jmethodID ptGetYId = env->GetMethodID(ptCls, "getY", "()F");

    if (alGetId == nullptr || alSizeId == nullptr || ptGetXId == nullptr || ptGetYId == nullptr) {
        return nullptr;
    }
    jsize pointCount = static_cast<jsize>(env->CallIntMethod(ptsInput, alSizeId));
    if (pointCount < 1) {
        return nullptr;
    }
    Point2Df points[pointCount];
    for (int i = 0; i < pointCount; ++i) {
        jobject point = env->CallObjectMethod(ptsInput, alGetId, i);
        points[i].x = static_cast<jfloat>(env->CallFloatMethod(point, ptGetXId));
        points[i].y = static_cast<jfloat>(env->CallFloatMethod(point, ptGetYId));
        points[i].skip = 1;
        env->DeleteLocalRef(point);
    }
    env->DeleteLocalRef(alCls);
    env->DeleteLocalRef(ptCls);
    env->DeleteLocalRef(ptsInput);

    jsize retArrSize = pointCount;
    reducePointsFOrderedX(points, retArrSize, tolerance);
    retArrSize = retArrSize << 1;
    jfloatArray result = env->NewFloatArray(retArrSize);
    jfloat retArray[retArrSize];
    int idx = pointCount;
    jsize idxRet = retArrSize;
    while ((--idx) >= 0) {
        if (points[idx].skip)
            continue;
        retArray[--idxRet] = points[idx].y;
        retArray[--idxRet] = points[idx].x;
    }

    env->SetFloatArrayRegion(result, 0,
                             retArrSize,
                             retArray);
    return result;
}
}