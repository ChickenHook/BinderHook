#include <jni.h>
#include <android/log.h>
#include <malloc.h>
#include <iostream>
#include <fstream>
#include <sstream>
#include <unistd.h>

///////////////////////// HELPERS

void printHexBuffer2(const uint8_t *buf, int len) {
    char *str_buf = (char *) malloc(
            3 * len + 1);   // X is the number of bytes to be converted

    const uint8_t *pin = buf;
    const char *hex = "0123456789ABCDEF";
    char *pout = str_buf;
    int i = 0;
    for (; i < len - 1; ++i) {
        *pout++ = hex[(*pin >> 4) & 0xF];
        *pout++ = hex[(*pin++) & 0xF];
        *pout++ = ':';
    }
    *pout++ = hex[(*pin >> 4) & 0xF];
    *pout++ = hex[(*pin) & 0xF];
    *pout = 0;

    __android_log_print(ANDROID_LOG_DEBUG, "parcel", "%s", str_buf);
    free(str_buf);
}

// Log a large byte array by logging it incrementally in smaller chunks to overcome printf buffering issues
void printHexBuffer(const char *title, const uint8_t *buf, unsigned int len) {
    __android_log_print(ANDROID_LOG_DEBUG, "parcel", "%s (%d bytes):", title, len);
    const int linewidth = 50;
    int loop = 0;
    int remainingSize = len;

    while (len != 0) {
        if (remainingSize < linewidth) {
            printHexBuffer2(&buf[loop * linewidth], remainingSize);
            break;
        } else {
            printHexBuffer2(&buf[loop * linewidth], linewidth);
            remainingSize -= linewidth;
        }
        loop++;
    }
}

void printHexBuffer(const uint8_t *buf, unsigned int len) {
    printHexBuffer("HEX", buf, len);
}

///////////////////////// PARCEL


uint8_t *GetData(jlong parcel_addr) {
    void **parcel = (void **) parcel_addr;

    /*for (int i = 0; i < 5; i++) {
    __android_log_print(ANDROID_LOG_DEBUG, "chickenbinder", "Got parcel content <%p>",
                        parcel[i]);
    }*/
    return static_cast<uint8_t *>(parcel[1]);
}

void JNICALL writeParcel(JNIEnv *env,
                         jclass interface,
                         jlong parcelAddr,
                         int offset,
                         jbyteArray toInsert) {
    __android_log_print(ANDROID_LOG_DEBUG, "chickenbinder", "write parcel <%p>", parcelAddr);
    uint8_t *data = GetData(parcelAddr);
    env->GetByteArrayRegion(toInsert, 0, env->GetArrayLength(toInsert),
                            reinterpret_cast<jbyte *>(data + offset));
}


jobject JNICALL readParcel(JNIEnv *env,
                           jclass interface,
                           jlong parcelAddr,
                           int offset,
                           int size) {
    __android_log_print(ANDROID_LOG_DEBUG, "chickenbinder", "read parcel <%p>", parcelAddr);
    uint8_t *data = GetData(parcelAddr);
    jbyteArray jarr = env->NewByteArray(size);
    env->SetByteArrayRegion(jarr, 0, size, reinterpret_cast<const jbyte *>(data));
    return jarr;
}

static JNICALL void dumpParcel(
        JNIEnv *env,
        jclass interface,
        jlong parcelAddr,
        int size) {
    __android_log_print(ANDROID_LOG_DEBUG, "chickenbinder", "dump parcel <%p>", parcelAddr);
    __android_log_print(ANDROID_LOG_DEBUG, "chickenbinder",
                        "---------------------------------------------------------------------------------------------------------------------------------------------");


    uint8_t *data = GetData(parcelAddr);
    printHexBuffer(data, size);
    __android_log_print(ANDROID_LOG_DEBUG, "chickenbinder",
                        "---------------------------------------------------------------------------------------------------------------------------------------------");
}



////////// JNI STUFF


static const JNINativeMethod gMethods[] = {
        /*{"manipulateParcel", "(JII)J",               (void *) manipulateParcel},
        {"readAddr",         "()Ljava/lang/String;", (void *) Java_readAddr},*/
        {"dump", "(JI)V",   (void *) &dumpParcel},
        {"read", "(JII)[B", (void *) &readParcel},
        {"write", "(JI[B)V", (void *) &writeParcel},
};
static const char *classPathName = "org/chickenhook/binderhooks/ParcelTools";

static int registerNativeMethods(JNIEnv *env, const char *className,
                                 JNINativeMethod *gMethods, int numMethods) {
    jclass clazz;
    clazz = env->FindClass(className);
    if (clazz == nullptr) {
        __android_log_print(ANDROID_LOG_DEBUG, "registerNativeMethods",
                            "Native registration unable to find class '%s'", className);
        return JNI_FALSE;
    }
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        __android_log_print(ANDROID_LOG_DEBUG, "registerNativeMethods",
                            "Native registration unable to register natives...");
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

jint JNI_OnLoad(JavaVM *vm, void * /*reserved*/) {
    JNIEnv *env = nullptr;

    if (vm->GetEnv((void **) (&env), JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }


    if (!registerNativeMethods(env, classPathName,
                               (JNINativeMethod *) gMethods,
                               sizeof(gMethods) / sizeof(gMethods[0]))) {
        return -1;
    }
    return JNI_VERSION_1_4;
}