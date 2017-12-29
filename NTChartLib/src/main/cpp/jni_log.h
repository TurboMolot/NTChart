//
// Created by denis on 02.12.17.
//

#ifndef NTCHART_JNI_LOG_H
#define NTCHART_JNI_LOG_H

#include <android/log.h>

void log_e_str(const char *tag, const char *msg) {
    __android_log_print(ANDROID_LOG_ERROR, tag, "%s", msg);
}

void log_d_str(const char *tag, const char *msg) {
    __android_log_print(ANDROID_LOG_DEBUG, tag, "%s", msg);
}

void log_w_str(const char *tag, const char *msg) {
    __android_log_print(ANDROID_LOG_WARN, tag, "%s", msg);
}

#endif //NTCHART_JNI_LOG_H
