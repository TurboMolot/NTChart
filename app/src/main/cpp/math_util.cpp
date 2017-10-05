#include <jni.h>
#include <cmath>
#include <float.h>
#include <vector>

extern "C" {
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
    jclass ptCls = env->FindClass("ru/turbomolot/ntchart/data/IPointLine");
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
